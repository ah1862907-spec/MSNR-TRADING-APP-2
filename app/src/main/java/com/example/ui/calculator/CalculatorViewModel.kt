package com.example.ui.calculator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.local.UserPreferences
import com.example.engine.RiskCalculatorEngine
import com.example.model.TradeDirection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)

    val symbol = MutableStateFlow("XAUUSD")
    val direction = MutableStateFlow(TradeDirection.BUY)
    val balanceStr = MutableStateFlow("10000.00")
    val riskPercentStr = MutableStateFlow("1.0")
    val entryStr = MutableStateFlow("2650.00")
    val slStr = MutableStateFlow("2649.00")
    val tp1Str = MutableStateFlow("2653.00")
    val tp2Str = MutableStateFlow("2656.00")
    val leverageStr = MutableStateFlow("100")
    val currencyStr = MutableStateFlow("USD")

    private val _calculation = MutableStateFlow(calculateCurrent())
    val calculation: StateFlow<RiskCalculatorEngine.CalculationResult> = _calculation.asStateFlow()

    init {
        val defaultBalance = userPreferences.riskSettings.value.accountBalance
        val defaultRisk = userPreferences.riskSettings.value.riskPercentage
        val defaultCurrency = userPreferences.riskSettings.value.currency
        balanceStr.value = String.format(Locale.US, "%.2f", defaultBalance)
        riskPercentStr.value = String.format(Locale.US, "%.1f", defaultRisk)
        currencyStr.value = defaultCurrency
        recalculate()
    }

    fun setSymbol(sym: String) {
        symbol.value = sym
        when (sym.uppercase()) {
            "XAUUSD", "GOLD" -> {
                entryStr.value = "2650.00"
                slStr.value = "2649.00"
                tp1Str.value = "2653.00"
                tp2Str.value = "2656.00"
            }
            "EURUSD" -> {
                entryStr.value = "1.08500"
                slStr.value = "1.08400"
                tp1Str.value = "1.08750"
                tp2Str.value = "1.09000"
            }
            "GBPUSD" -> {
                entryStr.value = "1.29500"
                slStr.value = "1.29400"
                tp1Str.value = "1.29750"
                tp2Str.value = "1.30000"
            }
            "USDJPY" -> {
                entryStr.value = "152.50"
                slStr.value = "152.40"
                tp1Str.value = "152.75"
                tp2Str.value = "153.00"
            }
            "BTCUSD" -> {
                entryStr.value = "68000.00"
                slStr.value = "67800.00"
                tp1Str.value = "68500.00"
                tp2Str.value = "69000.00"
            }
        }
        recalculate()
    }

    fun setRiskPercentPreset(pct: Double) {
        riskPercentStr.value = String.format(Locale.US, "%.1f", pct)
        recalculate()
    }

    fun set10PipStop() {
        val entry = entryStr.value.toDoubleOrNull() ?: 2650.0
        val (sl, tp1, tp2) = RiskCalculatorEngine.calculateDefaultLevels(
            symbol = symbol.value,
            direction = direction.value,
            entryPrice = entry,
            slPips = 10.0,
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
        slStr.value = String.format(Locale.US, "%.${decimals}f", sl)
        tp1Str.value = String.format(Locale.US, "%.${decimals}f", tp1)
        tp2Str.value = String.format(Locale.US, "%.${decimals}f", tp2)
        recalculate()
    }

    fun recalculate() {
        _calculation.value = calculateCurrent()
    }

    private fun calculateCurrent(): RiskCalculatorEngine.CalculationResult {
        val bal = balanceStr.value.toDoubleOrNull() ?: 10000.0
        val riskPct = riskPercentStr.value.toDoubleOrNull() ?: 1.0
        val entry = entryStr.value.toDoubleOrNull() ?: 0.0
        val sl = slStr.value.toDoubleOrNull() ?: 0.0
        val tp1 = tp1Str.value.toDoubleOrNull() ?: 0.0
        val tp2 = tp2Str.value.toDoubleOrNull() ?: 0.0

        return RiskCalculatorEngine.calculate(
            symbol = symbol.value,
            direction = direction.value,
            accountBalance = bal,
            riskPercentage = riskPct,
            customRiskAmount = null,
            entryPrice = entry,
            stopLossPrice = sl,
            tp1Price = tp1,
            tp2Price = tp2
        )
    }
}
