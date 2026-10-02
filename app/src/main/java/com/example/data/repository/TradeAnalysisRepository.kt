package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.data.local.UserPreferences
import com.example.data.remote.ContentPart
import com.example.data.remote.ImageUrlPart
import com.example.data.remote.OpenRouterClient
import com.example.data.remote.OpenRouterMessagePayload
import com.example.data.remote.OpenRouterMultimodalMessage
import com.example.data.remote.OpenRouterMultimodalRequest
import com.example.data.remote.OpenRouterRequest
import com.example.engine.RiskCalculatorEngine
import com.example.engine.StrategyEngine
import com.example.model.ChecklistStatus
import com.example.model.RiskSettings
import com.example.model.SetupStatus
import com.example.model.StrategyChecklistItem
import com.example.model.TradeDirection
import com.example.model.TradeSetup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream

class TradeAnalysisRepository(
    private val context: Context,
    private val userPreferences: UserPreferences
) {

    sealed class AnalysisResult {
        data class Success(val setup: TradeSetup, val isAiGenerated: Boolean) : AnalysisResult()
        data class Error(val message: String, val canFallbackToRuleEngine: Boolean = true) : AnalysisResult()
    }

    suspend fun analyzeTrade(
        symbol: String,
        direction: TradeDirection,
        poiType: String,
        entryPrice: Double,
        stopLoss: Double,
        tp1: Double,
        tp2: Double,
        timeframeHtf: String,
        timeframeLtf: String,
        chartUri: Uri?,
        hasHtfPoi: Boolean,
        hasTrendAlign: Boolean,
        hasIdm: Boolean,
        hasTsSweep: Boolean,
        hasRejection: Boolean,
        hasMss: Boolean,
        riskSettings: RiskSettings,
        forceAiScan: Boolean = false
    ): AnalysisResult = withContext(Dispatchers.IO) {
        val apiKey = userPreferences.getApiKey().trim()
        val model = userPreferences.getSelectedModel().ifBlank { "google/gemini-2.0-flash-001" }

        // If forceAiScan requested and API key is present, attempt OpenRouter AI call
        if (apiKey.isNotBlank() && forceAiScan) {
            try {
                val base64Image = chartUri?.let { convertUriToBase64(context, it) }

                val promptText = """
Analyze the following trade setup for $symbol based STRICTLY on LIT + MSNR strategy:
- Symbol: $symbol
- Proposed Direction: ${direction.name}
- HTF ($timeframeHtf) POI Type: $poiType
- Proposed Entry: $entryPrice
- Proposed SL: $stopLoss
- Proposed TP1: $tp1
- Proposed TP2: $tp2
- User notes/checklist:
  * POI marked: $hasHtfPoi
  * Trend aligned: $hasTrendAlign
  * Inducement formed: $hasIdm
  * Inducement swept via TS: $hasTsSweep
  * Candlestick rejection: $hasRejection
  * MSS on $timeframeLtf: $hasMss

Remember: DO NOT invent trades or force a setup. If Inducement is NOT cleared or Rejection/MSS is missing, output NO_TRADE or WAIT with clear reasons.
Provide your response in JSON format.
""".trimIndent()

                val response = if (base64Image != null) {
                    val messages = listOf(
                        OpenRouterMultimodalMessage(
                            role = "system",
                            content = listOf(ContentPart(type = "text", text = StrategyEngine.SYSTEM_PROMPT))
                        ),
                        OpenRouterMultimodalMessage(
                            role = "user",
                            content = listOf(
                                ContentPart(type = "text", text = promptText),
                                ContentPart(
                                    type = "image_url",
                                    image_url = ImageUrlPart(url = "data:image/jpeg;base64,$base64Image")
                                )
                            )
                        )
                    )
                    OpenRouterClient.service.createMultimodalCompletion(
                        authorization = "Bearer $apiKey",
                        request = OpenRouterMultimodalRequest(model = model, messages = messages, temperature = 0.2)
                    )
                } else {
                    val messages = listOf(
                        OpenRouterMessagePayload(role = "system", content = StrategyEngine.SYSTEM_PROMPT),
                        OpenRouterMessagePayload(role = "user", content = promptText)
                    )
                    OpenRouterClient.service.createChatCompletion(
                        authorization = "Bearer $apiKey",
                        request = OpenRouterRequest(model = model, messages = messages, temperature = 0.2)
                    )
                }

                if (response.isSuccessful && response.body() != null) {
                    val content = response.body()?.choices?.firstOrNull()?.message?.content
                    if (!content.isNullOrBlank()) {
                        val parsedSetup = parseAiResponseToSetup(
                            rawJson = content,
                            symbol = symbol,
                            fallbackDirection = direction,
                            fallbackEntry = entryPrice,
                            fallbackSl = stopLoss,
                            fallbackTp1 = tp1,
                            fallbackTp2 = tp2,
                            poiType = poiType,
                            timeframeHtf = timeframeHtf,
                            timeframeLtf = timeframeLtf,
                            imageUri = chartUri?.toString(),
                            riskSettings = riskSettings
                        )
                        return@withContext AnalysisResult.Success(parsedSetup, isAiGenerated = true)
                    }
                } else {
                    val errCode = response.code()
                    val errMsg = response.errorBody()?.string() ?: response.message()
                    val userFriendlyMsg = when (errCode) {
                        401 -> "Invalid OpenRouter API Key. Please verify your key in Settings."
                        429 -> "OpenRouter Rate limit or insufficient credits. Please check your account."
                        404 -> "Model '$model' not found or currently unavailable on OpenRouter."
                        else -> "OpenRouter API error ($errCode): $errMsg"
                    }
                    return@withContext AnalysisResult.Error(userFriendlyMsg)
                }
            } catch (e: Exception) {
                // If API call fails (network offline or timeout), report clear error with fallback
                return@withContext AnalysisResult.Error(
                    "Network error connecting to OpenRouter: ${e.localizedMessage ?: "Unknown error"}. You can run the local mechanical evaluation engine instead."
                )
            }
        }

        // Mechanical Strategy Engine evaluation (Works 100% offline & strictly adheres to LIT + MSNR rules)
        val input = StrategyEngine.StrategyInput(
            symbol = symbol,
            direction = direction,
            poiType = poiType,
            hasHtfPoi = hasHtfPoi,
            hasHtfTrendAlignment = hasTrendAlign,
            hasInducementFormed = hasIdm,
            isInducementClearedViaTs = hasTsSweep,
            hasCandleRejection = hasRejection,
            hasLtfMss = hasMss,
            entryPrice = entryPrice,
            stopLossPrice = stopLoss,
            tp1Price = tp1,
            tp2Price = tp2,
            timeframeHtf = timeframeHtf,
            timeframeLtf = timeframeLtf,
            imageUri = chartUri?.toString()
        )

        val setup = StrategyEngine.evaluateSetup(input, riskSettings)
        AnalysisResult.Success(setup, isAiGenerated = false)
    }

    private fun parseAiResponseToSetup(
        rawJson: String,
        symbol: String,
        fallbackDirection: TradeDirection,
        fallbackEntry: Double,
        fallbackSl: Double,
        fallbackTp1: Double,
        fallbackTp2: Double,
        poiType: String,
        timeframeHtf: String,
        timeframeLtf: String,
        imageUri: String?,
        riskSettings: RiskSettings
    ): TradeSetup {
        try {
            // Strip any markdown code fences if model returned ```json ... ```
            val cleaned = rawJson.replace("```json", "")
                .replace("```", "")
                .trim()

            val json = JSONObject(cleaned)
            val statusStr = json.optString("status", "WAIT").uppercase()
            val status = when (statusStr) {
                "VALID_SETUP" -> SetupStatus.VALID_SETUP
                "NO_TRADE" -> SetupStatus.NO_TRADE
                else -> SetupStatus.WAIT
            }

            val dirStr = json.optString("direction", fallbackDirection.name).uppercase()
            val direction = if (dirStr == "SELL") TradeDirection.SELL else TradeDirection.BUY

            val entry = json.optDouble("entryPrice", fallbackEntry).let { if (it.isNaN() || it <= 0.0) fallbackEntry else it }
            val sl = json.optDouble("stopLoss", fallbackSl).let { if (it.isNaN() || it <= 0.0) fallbackSl else it }
            val tp1 = json.optDouble("takeProfit1", fallbackTp1).let { if (it.isNaN() || it <= 0.0) fallbackTp1 else it }
            val tp2 = json.optDouble("takeProfit2", fallbackTp2).let { if (it.isNaN() || it <= 0.0) fallbackTp2 else it }
            val reason = json.optString("reason", "Analyzed according to LIT + MSNR strategy.")

            val missingList = mutableListOf<String>()
            val missingArr = json.optJSONArray("missingConditions")
            if (missingArr != null) {
                for (i in 0 until missingArr.length()) {
                    missingList.add(missingArr.getString(i))
                }
            }

            val checklistItems = mutableListOf<StrategyChecklistItem>()
            val checklistArr = json.optJSONArray("checklist")
            if (checklistArr != null) {
                for (i in 0 until checklistArr.length()) {
                    val itemObj = checklistArr.getJSONObject(i)
                    val name = itemObj.optString("name", "Condition $i")
                    val stStr = itemObj.optString("status", "PASS").uppercase()
                    val st = when (stStr) {
                        "PASS" -> ChecklistStatus.PASS
                        "FAIL" -> ChecklistStatus.FAIL
                        else -> ChecklistStatus.WAITING
                    }
                    val note = itemObj.optString("note", "")
                    checklistItems.add(
                        StrategyChecklistItem(
                            id = "ai_$i",
                            name = name,
                            description = note,
                            status = st,
                            note = note
                        )
                    )
                }
            }

            val calc = RiskCalculatorEngine.calculate(
                symbol = symbol,
                direction = direction,
                accountBalance = riskSettings.accountBalance,
                riskPercentage = riskSettings.riskPercentage,
                customRiskAmount = null,
                entryPrice = entry,
                stopLossPrice = sl,
                tp1Price = tp1,
                tp2Price = tp2
            )

            return TradeSetup(
                symbol = symbol,
                direction = direction,
                entryPrice = entry,
                stopLoss = sl,
                takeProfit1 = tp1,
                takeProfit2 = tp2,
                stopDistancePips = calc.stopDistancePips,
                tp1DistancePips = calc.tp1DistancePips,
                tp2DistancePips = calc.tp2DistancePips,
                rrTp1 = calc.rrTp1,
                rrTp2 = calc.rrTp2,
                suggestedLotSize = calc.recommendedLotSize,
                riskAmount = calc.riskAmount,
                potentialProfitTp1 = calc.potentialProfitTp1,
                potentialProfitTp2 = calc.potentialProfitTp2,
                status = status,
                missingConditions = missingList,
                checklist = checklistItems,
                reason = reason,
                poiType = json.optString("poiType", poiType),
                timeframeHtf = timeframeHtf,
                timeframeLtf = timeframeLtf,
                imageUri = imageUri
            )
        } catch (e: Exception) {
            // Fallback to mechanical calculation if parsing failed
            val calc = RiskCalculatorEngine.calculate(
                symbol = symbol,
                direction = fallbackDirection,
                accountBalance = riskSettings.accountBalance,
                riskPercentage = riskSettings.riskPercentage,
                entryPrice = fallbackEntry,
                stopLossPrice = fallbackSl,
                tp1Price = fallbackTp1,
                tp2Price = fallbackTp2
            )

            return TradeSetup(
                symbol = symbol,
                direction = fallbackDirection,
                entryPrice = fallbackEntry,
                stopLoss = fallbackSl,
                takeProfit1 = fallbackTp1,
                takeProfit2 = fallbackTp2,
                stopDistancePips = calc.stopDistancePips,
                tp1DistancePips = calc.tp1DistancePips,
                tp2DistancePips = calc.tp2DistancePips,
                rrTp1 = calc.rrTp1,
                rrTp2 = calc.rrTp2,
                suggestedLotSize = calc.recommendedLotSize,
                riskAmount = calc.riskAmount,
                potentialProfitTp1 = calc.potentialProfitTp1,
                potentialProfitTp2 = calc.potentialProfitTp2,
                status = SetupStatus.WAIT,
                reason = "AI returned response: $rawJson",
                poiType = poiType,
                timeframeHtf = timeframeHtf,
                timeframeLtf = timeframeLtf,
                imageUri = imageUri
            )
        }
    }

    private fun convertUriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) return null

            // Scale down to prevent oversized payloads while preserving sharp chart candlestick clarity
            val maxDimension = 1280
            val width = bitmap.width
            val height = bitmap.height
            val scaledBitmap = if (width > maxDimension || height > maxDimension) {
                val ratio = width.toFloat() / height.toFloat()
                val targetW = if (ratio > 1) maxDimension else (maxDimension * ratio).toInt()
                val targetH = if (ratio > 1) (maxDimension / ratio).toInt() else maxDimension
                Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
            } else {
                bitmap
            }

            val byteArrayOutputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }
}
