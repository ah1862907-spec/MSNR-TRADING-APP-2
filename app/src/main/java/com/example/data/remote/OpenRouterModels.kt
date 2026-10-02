package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenRouterRequest(
    val model: String,
    val messages: List<OpenRouterMessagePayload>,
    val temperature: Double = 0.2
)

@JsonClass(generateAdapter = true)
data class OpenRouterMessagePayload(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class OpenRouterMultimodalRequest(
    val model: String,
    val messages: List<OpenRouterMultimodalMessage>,
    val temperature: Double = 0.2
)

@JsonClass(generateAdapter = true)
data class OpenRouterMultimodalMessage(
    val role: String,
    val content: List<ContentPart>
)

@JsonClass(generateAdapter = true)
data class ContentPart(
    val type: String, // "text" or "image_url"
    val text: String? = null,
    val image_url: ImageUrlPart? = null
)

@JsonClass(generateAdapter = true)
data class ImageUrlPart(
    val url: String
)

@JsonClass(generateAdapter = true)
data class OpenRouterResponse(
    val id: String? = null,
    val choices: List<OpenRouterChoice>? = null,
    val error: OpenRouterError? = null
)

@JsonClass(generateAdapter = true)
data class OpenRouterChoice(
    val message: OpenRouterMessageResponse? = null,
    val finish_reason: String? = null
)

@JsonClass(generateAdapter = true)
data class OpenRouterMessageResponse(
    val role: String? = null,
    val content: String? = null
)

@JsonClass(generateAdapter = true)
data class OpenRouterError(
    val message: String? = null,
    val code: Any? = null
)
