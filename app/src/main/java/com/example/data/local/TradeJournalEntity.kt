package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trade_journal")
data class TradeJournalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val dateTimeFormatted: String,
    val symbol: String,
    val direction: String, // "BUY" or "SELL"
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit1: Double,
    val takeProfit2: Double,
    val lotSize: Double,
    val riskPercentage: Double,
    val riskAmount: Double,
    val actualExitPrice: Double? = null,
    val realizedPnl: Double? = null,
    val outcome: String = "PENDING", // PENDING, WIN, LOSS, BREAKEVEN
    val strategyName: String = "LIT + MSNR TS Entry",
    val reason: String = "",
    val imageUri: String? = null,
    val psychologyNotes: String = "",
    val timeframeHtf: String = "M15",
    val timeframeLtf: String = "M1",
    val poiType: String = "SBR"
)
