package com.example.model

object StrategyKnowledge {
    val RULES_LIST: List<StrategyRuleDoc> = listOf(
        StrategyRuleDoc(
            id = "msnr_7_levels",
            category = "MSNR",
            title = "7 MSNR Key Level Types",
            subtitle = "Market Structure, Support & Resistance Framework",
            explanation = "Price does not move randomly; it reacts to specific institutional price structures. Understanding these 7 key levels allows identifying high-probability Points of Interest (POI).",
            keyFeatures = listOf(
                "1. Resistance: Previous swing High where selling pressure overwhelmed buyers.",
                "2. Support: Previous swing Low where buying pressure defended price.",
                "3. RBS (Resistance Becomes Support): Price breaks above resistance, then pulls back to test the old ceiling as a new floor (BUY setup).",
                "4. SBR (Support Becomes Resistance): Price breaks below support, then pulls back to test the old floor as a new ceiling (SELL setup).",
                "5. OCL (Open-Close Level): Major candle open/close horizontal alignment representing pure institutional equilibrium and retest points.",
                "6. QM (Quasimodo Pattern): Left Shoulder -> Head (Higher High / Lower Low) -> Break of Structure -> Retest of Left Shoulder level for maximum reversal edge.",
                "7. Engulfing OB: Strong Order Block that executed a previous sweep before printing a massive engulfing momentum candle."
            ),
            highProbabilityTip = "Always mark your POI on the higher timeframe (M15 or H1) before dropping to M1 for the entry sequence."
        ),
        StrategyRuleDoc(
            id = "lit_inducement",
            category = "LIT",
            title = "LIT - Liquidity & Inducement Theory",
            subtitle = "The Trap Retail Traders Fall Into",
            explanation = "Inducements (IDM) are intentional 'bait' levels engineered by Market Makers to tempt early retail traders to enter prematurely. Retail stop losses cluster directly behind these bait levels.",
            keyFeatures = listOf(
                "Clearing Inducement is MANDATORY before the true institutional move can occur.",
                "If Inducement is NOT cleared, entering early has an extremely high failure rate (fakeout/stop-out).",
                "When Inducement is swept, retail stops are triggered, providing the smart money with liquidity to fill large institutional orders into the true POI."
            ),
            highProbabilityTip = "Never enter at the Inducement. Wait for the market to sweep the Inducement into your POI."
        ),
        StrategyRuleDoc(
            id = "ts_target_sweep",
            category = "TS",
            title = "TS (Target Sweep / Stop Hunt)",
            subtitle = "The Precise Liquidity Grabbing Mechanism",
            explanation = "TS is the sudden, sharp spike through an Old High, Old Low, or Inducement level to trigger breakout orders and stop losses before aggressively reversing.",
            keyFeatures = listOf(
                "Visual identification: Long candlestick wick piercing the key level or swing high/low.",
                "Price cannot sustain beyond the level and creates an immediate rejection.",
                "Indicates opposing institutional buyers or sellers have entered the market in full force.",
                "The extreme wick tip of the TS serves as the ultimate structural invalidation (Stop Loss location)."
            ),
            highProbabilityTip = "After TS occurs, immediately check for candle rejection and lower-timeframe MSS."
        ),
        StrategyRuleDoc(
            id = "rejection_mss",
            category = "CONFIRMATION",
            title = "Candle Rejection & MSS (Market Structure Shift)",
            subtitle = "Confirmation of Institutional Control",
            explanation = "Do not blindly trade the sweep. Wait for the candlestick rejection and Market Structure Shift (MSS) on the lower timeframe to prove smart money has taken control.",
            keyFeatures = listOf(
                "Reversal patterns: Pin Bar with long wick, Bullish/Bearish Engulfing candle.",
                "Candle Close: Candle body must close back inside or away from the swept zone, not expand through it.",
                "MSS (Market Structure Shift): On M1, a minor swing high (for BUY) or swing low (for SELL) must be broken with momentum.",
                "Enter on the retest or close of the rejection candle."
            ),
            highProbabilityTip = "No Rejection = NO TRADE. A candle that closes full-bodied beyond the level is a breakout, not a sweep."
        ),
        StrategyRuleDoc(
            id = "multi_timeframe_pipeline",
            category = "TIMEFRAME",
            title = "Multi-Timeframe Workflow: M15 (POI) > M1 (Entry)",
            subtitle = "SL 10 Pips Precision Framework",
            explanation = "Using higher timeframe context with lower timeframe execution gives the best risk-to-reward ratio in Forex and Gold (XAUUSD).",
            keyFeatures = listOf(
                "Step 1: Identify HTF Trend and mark POI on M15 (Support, Resistance, RBS, SBR, QM, or Engulfing OB).",
                "Step 2: Identify Inducement (IDM) bait level on M15/M5.",
                "Step 3: Drop to M1 as price enters the POI.",
                "Step 4: Watch for TS (Target Sweep) taking out the Inducement / Old High or Low.",
                "Step 5: Confirm Rejection Pin Bar + M1 MSS.",
                "Step 6: Place Stop Loss 1-2 pips beyond the TS wick (~10 pips default). Target recent liquidity for TP1 (1:2-1:3 RR) and HTF opposing pool for TP2 (1:5+ RR)."
            ),
            highProbabilityTip = "Strictly follow 'NO TRADE' if any rule is missing. Never force a trade."
        )
    )
}
