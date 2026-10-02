package com.example.model

enum class TradeDirection {
    BUY,
    SELL
}

enum class SetupStatus {
    VALID_SETUP,
    WAIT,
    NO_TRADE
}

enum class ChecklistStatus {
    PASS,
    FAIL,
    WAITING
}

data class StrategyChecklistItem(
    val id: String,
    val name: String,
    val description: String,
    val status: ChecklistStatus,
    val note: String = ""
)

data class TradeSetup(
    val id: String = System.currentTimeMillis().toString(),
    val symbol: String = "XAUUSD",
    val direction: TradeDirection = TradeDirection.BUY,
    val entryPrice: Double = 0.0,
    val stopLoss: Double = 0.0,
    val takeProfit1: Double = 0.0,
    val takeProfit2: Double = 0.0,
    val stopDistancePips: Double = 0.0,
    val tp1DistancePips: Double = 0.0,
    val tp2DistancePips: Double = 0.0,
    val rrTp1: Double = 0.0,
    val rrTp2: Double = 0.0,
    val suggestedLotSize: Double = 0.0,
    val riskAmount: Double = 0.0,
    val potentialProfitTp1: Double = 0.0,
    val potentialProfitTp2: Double = 0.0,
    val status: SetupStatus = SetupStatus.WAIT,
    val missingConditions: List<String> = emptyList(),
    val checklist: List<StrategyChecklistItem> = emptyList(),
    val reason: String = "",
    val poiType: String = "SBR", // Support, Resistance, RBS, SBR, OCL, QM, Engulfing OB
    val timeframeHtf: String = "M15",
    val timeframeLtf: String = "M1",
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: String? = null
)

data class RiskSettings(
    val accountBalance: Double = 10000.0,
    val riskPercentage: Double = 1.0,
    val riskAmount: Double = 100.0,
    val currency: String = "USD",
    val defaultSlPips: Double = 10.0,
    val customOpenRouterKey: String = "",
    val selectedModel: String = "google/gemini-2.0-flash-001"
)

data class PerformanceStats(
    val totalTrades: Int = 0,
    val winningTrades: Int = 0,
    val losingTrades: Int = 0,
    val breakevenTrades: Int = 0,
    val winRate: Double = 0.0,
    val totalPnl: Double = 0.0,
    val averageRr: Double = 0.0,
    val profitFactor: Double = 0.0,
    val averageWin: Double = 0.0,
    val averageLoss: Double = 0.0,
    val maxDrawdownPercent: Double = 0.0,
    val currentStreak: String = "-"
)

data class StrategyRuleDoc(
    val id: String,
    val category: String, // "MSNR", "LIT", "TS", "TIMEFRAME", "CHECKLIST"
    val title: String,
    val subtitle: String,
    val explanation: String,
    val keyFeatures: List<String>,
    val highProbabilityTip: String
)
