package com.example

import com.example.data.local.TradeJournalEntity
import com.example.data.repository.TradeJournalRepository
import com.example.engine.RiskCalculatorEngine
import com.example.engine.StrategyEngine
import com.example.model.ChecklistStatus
import com.example.model.RiskSettings
import com.example.model.SetupStatus
import com.example.model.TradeDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testGoldRiskCalculation() {
        // Balance: 10,000, 1% risk = $100
        // Gold: Entry 2650.00, SL 2649.00 -> 10 pips ($1.00 distance)
        // 10 pips * $10/pip at 1.00 lot = $100 -> exactly 1.00 lot
        val result = RiskCalculatorEngine.calculate(
            symbol = "XAUUSD",
            direction = TradeDirection.BUY,
            accountBalance = 10000.0,
            riskPercentage = 1.0,
            customRiskAmount = null,
            entryPrice = 2650.00,
            stopLossPrice = 2649.00,
            tp1Price = 2652.00, // 20 pips -> 1:2 RR
            tp2Price = 2654.00  // 40 pips -> 1:4 RR
        )

        assertEquals(10.0, result.stopDistancePips, 0.1)
        assertEquals(20.0, result.tp1DistancePips, 0.1)
        assertEquals(2.0, result.rrTp1, 0.1)
        assertEquals(4.0, result.rrTp2, 0.1)
        assertEquals(1.0, result.recommendedLotSize, 0.05)
        assertEquals(100.0, result.riskAmount, 0.1)
        assertEquals(200.0, result.potentialProfitTp1, 1.0)
        assertEquals(400.0, result.potentialProfitTp2, 1.0)
    }

    @Test
    fun testForexRiskCalculation() {
        // EURUSD: Entry 1.0850, SL 1.0840 -> 10 pips
        // $100 risk / (10 pips * $10/pip) = 1.00 lot
        val result = RiskCalculatorEngine.calculate(
            symbol = "EURUSD",
            direction = TradeDirection.BUY,
            accountBalance = 10000.0,
            riskPercentage = 1.0,
            customRiskAmount = null,
            entryPrice = 1.0850,
            stopLossPrice = 1.0840,
            tp1Price = 1.0875,
            tp2Price = 1.0900
        )

        assertEquals(10.0, result.stopDistancePips, 0.1)
        assertEquals(25.0, result.tp1DistancePips, 0.1)
        assertEquals(2.5, result.rrTp1, 0.1)
        assertEquals(5.0, result.rrTp2, 0.1)
        assertEquals(1.0, result.recommendedLotSize, 0.05)
    }

    @Test
    fun testStrategyEngineValidSetup() {
        val input = StrategyEngine.StrategyInput(
            symbol = "XAUUSD",
            direction = TradeDirection.BUY,
            poiType = "RBS",
            hasHtfPoi = true,
            hasHtfTrendAlignment = true,
            hasInducementFormed = true,
            isInducementClearedViaTs = true,
            hasCandleRejection = true,
            hasLtfMss = true,
            entryPrice = 2650.00,
            stopLossPrice = 2649.00,
            tp1Price = 2653.00,
            tp2Price = 2656.00
        )

        val setup = StrategyEngine.evaluateSetup(input, RiskSettings())
        assertEquals(SetupStatus.VALID_SETUP, setup.status)
        assertTrue(setup.missingConditions.isEmpty())
        assertTrue(setup.reason.contains("VALID HIGH-PROBABILITY SETUP"))
    }

    @Test
    fun testStrategyEngineInducementNotCleared() {
        val input = StrategyEngine.StrategyInput(
            symbol = "XAUUSD",
            direction = TradeDirection.BUY,
            poiType = "RBS",
            hasHtfPoi = true,
            hasHtfTrendAlignment = true,
            hasInducementFormed = true,
            isInducementClearedViaTs = false, // Critical failure: TS not done
            hasCandleRejection = false,
            hasLtfMss = false,
            entryPrice = 2650.00,
            stopLossPrice = 2649.00,
            tp1Price = 2653.00,
            tp2Price = 2656.00
        )

        val setup = StrategyEngine.evaluateSetup(input, RiskSettings())
        assertEquals(SetupStatus.WAIT, setup.status)
        assertTrue(setup.missingConditions.any { it.contains("Inducement has NOT been cleared") })
    }

    @Test
    fun testJournalPerformanceStats() {
        val trades = listOf(
            TradeJournalEntity(
                id = 1,
                dateTimeFormatted = "Oct 01",
                symbol = "XAUUSD",
                direction = "BUY",
                entryPrice = 2650.0,
                stopLoss = 2649.0,
                takeProfit1 = 2652.0,
                takeProfit2 = 2654.0,
                lotSize = 1.0,
                riskPercentage = 1.0,
                riskAmount = 100.0,
                outcome = "WIN",
                realizedPnl = 200.0
            ),
            TradeJournalEntity(
                id = 2,
                dateTimeFormatted = "Oct 01",
                symbol = "EURUSD",
                direction = "SELL",
                entryPrice = 1.0850,
                stopLoss = 1.0860,
                takeProfit1 = 1.0825,
                takeProfit2 = 1.0800,
                lotSize = 1.0,
                riskPercentage = 1.0,
                riskAmount = 100.0,
                outcome = "LOSS",
                realizedPnl = -100.0
            )
        )

        val stats = TradeJournalRepository.calculateStats(trades)
        assertEquals(2, stats.totalTrades)
        assertEquals(1, stats.winningTrades)
        assertEquals(1, stats.losingTrades)
        assertEquals(50.0, stats.winRate, 0.1)
        assertEquals(100.0, stats.totalPnl, 0.1)
    }
}
