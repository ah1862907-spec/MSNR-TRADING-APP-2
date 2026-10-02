package com.example.data.repository

import com.example.data.local.TradeJournalDao
import com.example.data.local.TradeJournalEntity
import com.example.model.PerformanceStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.max

class TradeJournalRepository(private val dao: TradeJournalDao) {

    val allTrades: Flow<List<TradeJournalEntity>> = dao.getAllTrades()

    fun getTradesByOutcome(outcome: String): Flow<List<TradeJournalEntity>> =
        dao.getTradesByOutcome(outcome)

    suspend fun insertTrade(trade: TradeJournalEntity): Long = dao.insertTrade(trade)

    suspend fun updateTrade(trade: TradeJournalEntity) = dao.updateTrade(trade)

    suspend fun deleteTrade(id: Long) = dao.deleteTradeById(id)

    suspend fun clearAll() = dao.clearAll()

    val statsFlow: Flow<PerformanceStats> = allTrades.map { list ->
        calculateStats(list)
    }

    companion object {
        fun calculateStats(trades: List<TradeJournalEntity>): PerformanceStats {
            val closedTrades = trades.filter { it.outcome != "PENDING" }
            val total = closedTrades.size
            if (total == 0) {
                return PerformanceStats()
            }

            var wins = 0
            var losses = 0
            var bes = 0
            var totalPnl = 0.0
            var grossProfit = 0.0
            var grossLoss = 0.0
            var currentStreakCount = 0
            var lastOutcome = ""

            // Calculate metrics
            val sortedAsc = closedTrades.sortedBy { it.timestamp }
            var peakBalance = 0.0
            var runningBalance = 0.0
            var maxDrawdown = 0.0

            for (trade in sortedAsc) {
                val pnl = trade.realizedPnl ?: when (trade.outcome) {
                    "WIN" -> {
                        // calculate default reward based on TP1 (or approx 2:1 RR)
                        trade.riskAmount * 2.0
                    }
                    "LOSS" -> -trade.riskAmount
                    else -> 0.0
                }

                totalPnl += pnl
                runningBalance += pnl
                if (runningBalance > peakBalance) {
                    peakBalance = runningBalance
                } else {
                    val dd = peakBalance - runningBalance
                    if (dd > maxDrawdown) {
                        maxDrawdown = dd
                    }
                }

                when (trade.outcome) {
                    "WIN" -> {
                        wins++
                        grossProfit += max(0.0, pnl)
                        if (lastOutcome == "WIN") currentStreakCount++ else {
                            lastOutcome = "WIN"
                            currentStreakCount = 1
                        }
                    }
                    "LOSS" -> {
                        losses++
                        grossLoss += max(0.0, -pnl)
                        if (lastOutcome == "LOSS") currentStreakCount++ else {
                            lastOutcome = "LOSS"
                            currentStreakCount = 1
                        }
                    }
                    "BREAKEVEN" -> {
                        bes++
                    }
                }
            }

            val winRate = if (total > 0) (wins.toDouble() / total) * 100.0 else 0.0
            val avgWin = if (wins > 0) grossProfit / wins else 0.0
            val avgLoss = if (losses > 0) grossLoss / losses else 0.0
            val profitFactor = if (grossLoss > 0.0) grossProfit / grossLoss else if (grossProfit > 0) 99.9 else 0.0
            val avgRr = if (avgLoss > 0.0) avgWin / avgLoss else if (wins > 0) 2.0 else 0.0

            val streakStr = if (lastOutcome.isNotBlank()) "$currentStreakCount ${lastOutcome}s" else "-"

            return PerformanceStats(
                totalTrades = total,
                winningTrades = wins,
                losingTrades = losses,
                breakevenTrades = bes,
                winRate = winRate,
                totalPnl = totalPnl,
                averageRr = avgRr,
                profitFactor = profitFactor,
                averageWin = avgWin,
                averageLoss = avgLoss,
                maxDrawdownPercent = if (peakBalance > 0) (maxDrawdown / peakBalance) * 100.0 else 0.0,
                currentStreak = streakStr
            )
        }
    }
}
