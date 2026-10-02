package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import com.example.model.RiskSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("lit_trader_prefs", Context.MODE_PRIVATE)

    private val _riskSettings = MutableStateFlow(loadSettings())
    val riskSettings: StateFlow<RiskSettings> = _riskSettings.asStateFlow()

    private fun loadSettings(): RiskSettings {
        val savedKey = prefs.getString("openrouter_api_key", "") ?: ""
        // Also check BuildConfig fallback if user has not set a custom key in app settings
        val effectiveKey = if (savedKey.isNotBlank()) {
            savedKey
        } else {
            try {
                // Read from BuildConfig if present
                val buildConfigKey = BuildConfig::class.java.getField("OPENROUTER_API_KEY").get(null) as? String
                buildConfigKey?.takeIf { it.isNotBlank() && it != "DEFAULT_KEY" } ?: ""
            } catch (e: Exception) {
                ""
            }
        }

        return RiskSettings(
            accountBalance = prefs.getFloat("account_balance", 10000.0f).toDouble(),
            riskPercentage = prefs.getFloat("risk_percentage", 1.0f).toDouble(),
            riskAmount = prefs.getFloat("risk_amount", 100.0f).toDouble(),
            currency = prefs.getString("currency", "USD") ?: "USD",
            defaultSlPips = prefs.getFloat("default_sl_pips", 10.0f).toDouble(),
            customOpenRouterKey = effectiveKey,
            selectedModel = prefs.getString("selected_model", "google/gemini-2.0-flash-001") ?: "google/gemini-2.0-flash-001"
        )
    }

    fun saveApiKey(apiKey: String) {
        prefs.edit().putString("openrouter_api_key", apiKey.trim()).apply()
        _riskSettings.value = _riskSettings.value.copy(customOpenRouterKey = apiKey.trim())
    }

    fun saveModel(model: String) {
        prefs.edit().putString("selected_model", model.trim()).apply()
        _riskSettings.value = _riskSettings.value.copy(selectedModel = model.trim())
    }

    fun saveRiskParameters(balance: Double, riskPercent: Double, currency: String, defaultSl: Double) {
        val riskAmt = balance * (riskPercent / 100.0)
        prefs.edit()
            .putFloat("account_balance", balance.toFloat())
            .putFloat("risk_percentage", riskPercent.toFloat())
            .putFloat("risk_amount", riskAmt.toFloat())
            .putString("currency", currency)
            .putFloat("default_sl_pips", defaultSl.toFloat())
            .apply()

        _riskSettings.value = _riskSettings.value.copy(
            accountBalance = balance,
            riskPercentage = riskPercent,
            riskAmount = riskAmt,
            currency = currency,
            defaultSlPips = defaultSl
        )
    }

    fun getApiKey(): String {
        return _riskSettings.value.customOpenRouterKey
    }

    fun getSelectedModel(): String {
        return _riskSettings.value.selectedModel
    }
}
