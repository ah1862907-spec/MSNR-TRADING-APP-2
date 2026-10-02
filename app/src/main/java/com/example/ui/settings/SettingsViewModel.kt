package com.example.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.UserPreferences
import com.example.data.remote.OpenRouterClient
import com.example.data.remote.OpenRouterMessagePayload
import com.example.data.remote.OpenRouterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)

    val apiKey = MutableStateFlow("")
    val selectedModel = MutableStateFlow("google/gemini-2.0-flash-001")
    val balanceStr = MutableStateFlow("10000.00")
    val riskPctStr = MutableStateFlow("1.0")
    val currencyStr = MutableStateFlow("USD")
    val defaultSlStr = MutableStateFlow("10.0")

    private val _isTestingConnection = MutableStateFlow(false)
    val isTestingConnection: StateFlow<Boolean> = _isTestingConnection.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    init {
        val current = userPreferences.riskSettings.value
        apiKey.value = current.customOpenRouterKey
        selectedModel.value = current.selectedModel
        balanceStr.value = String.format(Locale.US, "%.2f", current.accountBalance)
        riskPctStr.value = String.format(Locale.US, "%.1f", current.riskPercentage)
        currencyStr.value = current.currency
        defaultSlStr.value = String.format(Locale.US, "%.1f", current.defaultSlPips)
    }

    fun saveApiKey() {
        userPreferences.saveApiKey(apiKey.value.trim())
        viewModelScope.launch {
            _snackbarEvent.emit("OpenRouter API key saved securely!")
        }
    }

    fun saveModel(model: String) {
        selectedModel.value = model.trim()
        userPreferences.saveModel(model.trim())
        viewModelScope.launch {
            _snackbarEvent.emit("Model updated to '$model'!")
        }
    }

    fun saveRiskParameters() {
        val bal = balanceStr.value.toDoubleOrNull() ?: 10000.0
        val risk = riskPctStr.value.toDoubleOrNull() ?: 1.0
        val curr = currencyStr.value.trim().ifBlank { "USD" }
        val sl = defaultSlStr.value.toDoubleOrNull() ?: 10.0

        userPreferences.saveRiskParameters(bal, risk, curr, sl)
        viewModelScope.launch {
            _snackbarEvent.emit("Default trading risk settings updated!")
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            val key = apiKey.value.trim()
            if (key.isBlank()) {
                _snackbarEvent.emit("Please enter your OpenRouter API key first.")
                return@launch
            }

            _isTestingConnection.value = true
            val testResult = withContext(Dispatchers.IO) {
                try {
                    val req = OpenRouterRequest(
                        model = selectedModel.value,
                        messages = listOf(
                            OpenRouterMessagePayload(role = "user", content = "Respond with 'OK' if you receive this.")
                        ),
                        temperature = 0.1
                    )
                    val response = OpenRouterClient.service.createChatCompletion(
                        authorization = "Bearer $key",
                        request = req
                    )
                    if (response.isSuccessful) {
                        "Connection successful! Model ${selectedModel.value} is ready."
                    } else {
                        "OpenRouter Error (${response.code()}): ${response.message()}"
                    }
                } catch (e: Exception) {
                    "Connection failed: ${e.localizedMessage ?: "Network error"}"
                }
            }
            _isTestingConnection.value = false
            _snackbarEvent.emit(testResult)
        }
    }
}
