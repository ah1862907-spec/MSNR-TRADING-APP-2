package com.example.ui.analyzer

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.TradeJournalEntity
import com.example.data.local.UserPreferences
import com.example.data.repository.TradeAnalysisRepository
import com.example.data.repository.TradeJournalRepository
import com.example.engine.RiskCalculatorEngine
import com.example.model.RiskSettings
import com.example.model.SetupStatus
import com.example.model.TradeDirection
import com.example.model.TradeSetup
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AnalyzerViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)
    private val analysisRepo = TradeAnalysisRepository(application, userPreferences)
    private val journalRepo = TradeJournalRepository(AppDatabase.getInstance(application).tradeJournalDao())

    val riskSettings: StateFlow<RiskSettings> = userPreferences.riskSettings

    // Form inputs
    val symbol = MutableStateFlow("XAUUSD")
    val direction = MutableStateFlow(TradeDirection.BUY)
    val poiType = MutableStateFlow("RBS")
    val timeframeHtf = MutableStateFlow("M15")
    val timeframeLtf = MutableStateFlow("M1")
    val chartUri = MutableStateFlow<Uri?>(null)

    val entryPriceStr = MutableStateFlow("2650.00")
    val stopLossStr = MutableStateFlow("2649.00")
    val tp1Str = MutableStateFlow("2653.00")
    val tp2Str = MutableStateFlow("2656.00")

    // Mechanical checklist toggles
    val hasHtfPoi = MutableStateFlow(true)
    val hasTrendAlign = MutableStateFlow(true)
    val hasIdm = MutableStateFlow(true)
    val hasTsSweep = MutableStateFlow(true)
    val hasRejection = MutableStateFlow(true)
    val hasMss = MutableStateFlow(true)

    // UI state
    sealed class AnalyzerUiState {
        object Idle : AnalyzerUiState()
        object Analyzing : AnalyzerUiState()
        data class Result(val setup: TradeSetup, val isAi: Boolean) : AnalyzerUiState()
        data class Error(val message: String) : AnalyzerUiState()
    }

    private val _uiState = MutableStateFlow<AnalyzerUiState>(AnalyzerUiState.Idle)
    val uiState: StateFlow<AnalyzerUiState> = _uiState.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    init {
        apply10PipPreset()
    }

    fun setSymbol(newSymbol: String) {
        symbol.value = newSymbol
        when (newSymbol.uppercase()) {
            "XAUUSD", "GOLD" -> {
                entryPriceStr.value = "2650.00"
                poiType.value = "RBS"
            }
            "EURUSD" -> {
                entryPriceStr.value = "1.0850"
                poiType.value = "SBR"
            }
            "GBPUSD" -> {
                entryPriceStr.value = "1.2950"
                poiType.value = "QM"
            }
            "USDJPY" -> {
                entryPriceStr.value = "152.50"
                poiType.value = "Support"
            }
            "BTCUSD" -> {
                entryPriceStr.value = "68000.00"
                poiType.value = "Engulfing OB"
            }
        }
        apply10PipPreset()
    }

    fun setDirection(newDirection: TradeDirection) {
        direction.value = newDirection
        apply10PipPreset()
    }

    fun apply10PipPreset() {
        val entry = entryPriceStr.value.toDoubleOrNull() ?: 2650.0
        val targetSlPips = riskSettings.value.defaultSlPips.takeIf { it > 0 } ?: 10.0
        val (sl, tp1, tp2) = RiskCalculatorEngine.calculateDefaultLevels(
            symbol = symbol.value,
            direction = direction.value,
            entryPrice = entry,
            slPips = targetSlPips,
            rrTp1Target = 2.0,
            rrTp2Target = 4.0
        )

        val pipUnit = RiskCalculatorEngine.getPipUnit(symbol.value)
        val decimals = when {
            pipUnit <= 0.0001 -> 5
            pipUnit <= 0.01 -> 3
            pipUnit <= 0.1 -> 2
            else -> 2
        }

        stopLossStr.value = String.format(Locale.US, "%.${decimals}f", sl)
        tp1Str.value = String.format(Locale.US, "%.${decimals}f", tp1)
        tp2Str.value = String.format(Locale.US, "%.${decimals}f", tp2)
    }

    fun runMechanicalEvaluation() {
        viewModelScope.launch {
            _uiState.value = AnalyzerUiState.Analyzing
            val entry = entryPriceStr.value.toDoubleOrNull() ?: 0.0
            val sl = stopLossStr.value.toDoubleOrNull() ?: 0.0
            val tp1 = tp1Str.value.toDoubleOrNull() ?: 0.0
            val tp2 = tp2Str.value.toDoubleOrNull() ?: 0.0

            val result = analysisRepo.analyzeTrade(
                symbol = symbol.value,
                direction = direction.value,
                poiType = poiType.value,
                entryPrice = entry,
                stopLoss = sl,
                tp1 = tp1,
                tp2 = tp2,
                timeframeHtf = timeframeHtf.value,
                timeframeLtf = timeframeLtf.value,
                chartUri = chartUri.value,
                hasHtfPoi = hasHtfPoi.value,
                hasTrendAlign = hasTrendAlign.value,
                hasIdm = hasIdm.value,
                hasTsSweep = hasTsSweep.value,
                hasRejection = hasRejection.value,
                hasMss = hasMss.value,
                riskSettings = riskSettings.value,
                forceAiScan = false
            )

            when (result) {
                is TradeAnalysisRepository.AnalysisResult.Success -> {
                    _uiState.value = AnalyzerUiState.Result(result.setup, isAi = false)
                }
                is TradeAnalysisRepository.AnalysisResult.Error -> {
                    _uiState.value = AnalyzerUiState.Error(result.message)
                }
            }
        }
    }

    fun runAiScan() {
        viewModelScope.launch {
            if (userPreferences.getApiKey().isBlank()) {
                _snackbarEvent.emit("OpenRouter API key is missing. Add it in Settings or run the mechanical scan.")
                return@launch
            }

            _uiState.value = AnalyzerUiState.Analyzing
            val entry = entryPriceStr.value.toDoubleOrNull() ?: 0.0
            val sl = stopLossStr.value.toDoubleOrNull() ?: 0.0
            val tp1 = tp1Str.value.toDoubleOrNull() ?: 0.0
            val tp2 = tp2Str.value.toDoubleOrNull() ?: 0.0

            val result = analysisRepo.analyzeTrade(
                symbol = symbol.value,
                direction = direction.value,
                poiType = poiType.value,
                entryPrice = entry,
                stopLoss = sl,
                tp1 = tp1,
                tp2 = tp2,
                timeframeHtf = timeframeHtf.value,
                timeframeLtf = timeframeLtf.value,
                chartUri = chartUri.value,
                hasHtfPoi = hasHtfPoi.value,
                hasTrendAlign = hasTrendAlign.value,
                hasIdm = hasIdm.value,
                hasTsSweep = hasTsSweep.value,
                hasRejection = hasRejection.value,
                hasMss = hasMss.value,
                riskSettings = riskSettings.value,
                forceAiScan = true
            )

            when (result) {
                is TradeAnalysisRepository.AnalysisResult.Success -> {
                    _uiState.value = AnalyzerUiState.Result(result.setup, isAi = true)
                }
                is TradeAnalysisRepository.AnalysisResult.Error -> {
                    _uiState.value = AnalyzerUiState.Error(result.message)
                    _snackbarEvent.emit(result.message)
                }
            }
        }
    }

    fun saveSetupToJournal(setup: TradeSetup) {
        viewModelScope.launch {
            val dateFmt = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date())
            val entity = TradeJournalEntity(
                timestamp = System.currentTimeMillis(),
                dateTimeFormatted = dateFmt,
                symbol = setup.symbol,
                direction = setup.direction.name,
                entryPrice = setup.entryPrice,
                stopLoss = setup.stopLoss,
                takeProfit1 = setup.takeProfit1,
                takeProfit2 = setup.takeProfit2,
                lotSize = setup.suggestedLotSize,
                riskPercentage = riskSettings.value.riskPercentage,
                riskAmount = setup.riskAmount,
                actualExitPrice = null,
                realizedPnl = null,
                outcome = "PENDING",
                strategyName = "LIT + MSNR TS Entry (${setup.poiType})",
                reason = setup.reason,
                imageUri = setup.imageUri,
                psychologyNotes = "Setup generated via LIT Strategy Assistant. Discipline: Followed 10-pip SL model and Inducement rule.",
                timeframeHtf = setup.timeframeHtf,
                timeframeLtf = setup.timeframeLtf,
                poiType = setup.poiType
            )

            journalRepo.insertTrade(entity)
            _snackbarEvent.emit("Setup saved to Trading Journal! Track outcome in Journal tab.")
        }
    }
}
