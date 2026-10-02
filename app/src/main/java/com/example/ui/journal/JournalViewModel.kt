package com.example.ui.journal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.TradeJournalEntity
import com.example.data.repository.TradeJournalRepository
import com.example.model.PerformanceStats
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JournalViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TradeJournalRepository(
        AppDatabase.getInstance(application).tradeJournalDao()
    )

    val searchQuery = MutableStateFlow("")
    val filterOutcome = MutableStateFlow("ALL") // ALL, WIN, LOSS, BREAKEVEN, PENDING
    val filterDirection = MutableStateFlow("ALL") // ALL, BUY, SELL
    val filterSymbol = MutableStateFlow("ALL")

    val allTrades: StateFlow<List<TradeJournalEntity>> = repository.allTrades
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTrades: StateFlow<List<TradeJournalEntity>> = combine(
        allTrades,
        searchQuery,
        filterOutcome,
        filterDirection,
        filterSymbol
    ) { list, query, outcome, dir, sym ->
        list.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.symbol.contains(query, ignoreCase = true) ||
                    item.strategyName.contains(query, ignoreCase = true) ||
                    item.reason.contains(query, ignoreCase = true) ||
                    item.psychologyNotes.contains(query, ignoreCase = true)

            val matchesOutcome = outcome == "ALL" || item.outcome.equals(outcome, ignoreCase = true)
            val matchesDirection = dir == "ALL" || item.direction.equals(dir, ignoreCase = true)
            val matchesSymbol = sym == "ALL" || item.symbol.equals(sym, ignoreCase = true)

            matchesQuery && matchesOutcome && matchesDirection && matchesSymbol
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stats: StateFlow<PerformanceStats> = repository.statsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PerformanceStats())

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    fun updateTradeOutcome(
        trade: TradeJournalEntity,
        outcome: String,
        actualExitPrice: Double?,
        realizedPnl: Double?,
        psychologyNotes: String
    ) {
        viewModelScope.launch {
            val updated = trade.copy(
                outcome = outcome,
                actualExitPrice = actualExitPrice,
                realizedPnl = realizedPnl,
                psychologyNotes = psychologyNotes
            )
            repository.updateTrade(updated)
            _snackbarEvent.emit("Trade updated successfully!")
        }
    }

    fun deleteTrade(id: Long) {
        viewModelScope.launch {
            repository.deleteTrade(id)
            _snackbarEvent.emit("Trade entry deleted.")
        }
    }

    fun addManualTrade(
        symbol: String,
        direction: String,
        entryPrice: Double,
        stopLoss: Double,
        tp1: Double,
        tp2: Double,
        lotSize: Double,
        riskPct: Double,
        riskAmt: Double,
        outcome: String,
        notes: String
    ) {
        viewModelScope.launch {
            val dateFmt = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date())
            val entity = TradeJournalEntity(
                dateTimeFormatted = dateFmt,
                symbol = symbol.uppercase(),
                direction = direction.uppercase(),
                entryPrice = entryPrice,
                stopLoss = stopLoss,
                takeProfit1 = tp1,
                takeProfit2 = tp2,
                lotSize = lotSize,
                riskPercentage = riskPct,
                riskAmount = riskAmt,
                outcome = outcome,
                strategyName = "LIT + MSNR TS Strategy",
                reason = "Manual Trade Entry",
                psychologyNotes = notes
            )
            repository.insertTrade(entity)
            _snackbarEvent.emit("Trade added to journal!")
        }
    }

    fun exportJournalToJson(): String {
        val list = allTrades.value
        val arr = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("timestamp", item.timestamp)
            obj.put("dateTime", item.dateTimeFormatted)
            obj.put("symbol", item.symbol)
            obj.put("direction", item.direction)
            obj.put("entryPrice", item.entryPrice)
            obj.put("stopLoss", item.stopLoss)
            obj.put("takeProfit1", item.takeProfit1)
            obj.put("takeProfit2", item.takeProfit2)
            obj.put("lotSize", item.lotSize)
            obj.put("riskAmount", item.riskAmount)
            obj.put("riskPercentage", item.riskPercentage)
            obj.put("outcome", item.outcome)
            obj.put("actualExitPrice", item.actualExitPrice ?: JSONObject.NULL)
            obj.put("realizedPnl", item.realizedPnl ?: JSONObject.NULL)
            obj.put("strategyName", item.strategyName)
            obj.put("reason", item.reason)
            obj.put("psychologyNotes", item.psychologyNotes)
            arr.put(obj)
        }
        return arr.toString(2)
    }

    fun importJournalFromJson(jsonString: String) {
        viewModelScope.launch {
            try {
                val arr = JSONArray(jsonString)
                var count = 0
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val entity = TradeJournalEntity(
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        dateTimeFormatted = obj.optString("dateTime", "Imported Trade"),
                        symbol = obj.optString("symbol", "XAUUSD"),
                        direction = obj.optString("direction", "BUY"),
                        entryPrice = obj.optDouble("entryPrice", 0.0),
                        stopLoss = obj.optDouble("stopLoss", 0.0),
                        takeProfit1 = obj.optDouble("takeProfit1", 0.0),
                        takeProfit2 = obj.optDouble("takeProfit2", 0.0),
                        lotSize = obj.optDouble("lotSize", 0.1),
                        riskPercentage = obj.optDouble("riskPercentage", 1.0),
                        riskAmount = obj.optDouble("riskAmount", 100.0),
                        actualExitPrice = if (obj.isNull("actualExitPrice")) null else obj.optDouble("actualExitPrice"),
                        realizedPnl = if (obj.isNull("realizedPnl")) null else obj.optDouble("realizedPnl"),
                        outcome = obj.optString("outcome", "PENDING"),
                        strategyName = obj.optString("strategyName", "LIT + MSNR"),
                        reason = obj.optString("reason", ""),
                        psychologyNotes = obj.optString("psychologyNotes", "")
                    )
                    repository.insertTrade(entity)
                    count++
                }
                _snackbarEvent.emit("Successfully imported $count trades into Journal!")
            } catch (e: Exception) {
                _snackbarEvent.emit("Import failed: invalid JSON format.")
            }
        }
    }
}
