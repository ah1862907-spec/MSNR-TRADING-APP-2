package com.example.engine

import com.example.model.ChecklistStatus
import com.example.model.RiskSettings
import com.example.model.SetupStatus
import com.example.model.StrategyChecklistItem
import com.example.model.TradeDirection
import com.example.model.TradeSetup

object StrategyEngine {

    data class StrategyInput(
        val symbol: String,
        val direction: TradeDirection,
        val poiType: String, // Support, Resistance, RBS, SBR, OCL, QM, Engulfing OB
        val hasHtfPoi: Boolean,
        val hasHtfTrendAlignment: Boolean,
        val hasInducementFormed: Boolean,
        val isInducementClearedViaTs: Boolean,
        val hasCandleRejection: Boolean,
        val hasLtfMss: Boolean,
        val entryPrice: Double,
        val stopLossPrice: Double,
        val tp1Price: Double,
        val tp2Price: Double,
        val timeframeHtf: String = "M15",
        val timeframeLtf: String = "M1",
        val imageUri: String? = null
    )

    fun evaluateSetup(
        input: StrategyInput,
        riskSettings: RiskSettings
    ): TradeSetup {
        val checklist = mutableListOf<StrategyChecklistItem>()
        val missingConditions = mutableListOf<String>()

        // 1. HTF MSNR POI
        if (input.hasHtfPoi) {
            checklist.add(
                StrategyChecklistItem(
                    id = "poi",
                    name = "MSNR Point of Interest (${input.poiType})",
                    description = "Price is reacting to a valid MSNR key level (${input.poiType}) on ${input.timeframeHtf}",
                    status = ChecklistStatus.PASS,
                    note = "Valid structural zone identified on ${input.timeframeHtf}"
                )
            )
        } else {
            checklist.add(
                StrategyChecklistItem(
                    id = "poi",
                    name = "MSNR Point of Interest",
                    description = "No clear MSNR key level (S/R, RBS, SBR, QM, Engulfing OB) identified",
                    status = ChecklistStatus.FAIL,
                    note = "Must mark a valid MSNR zone before looking for entries"
                )
            )
            missingConditions.add("No valid MSNR POI level identified on ${input.timeframeHtf}")
        }

        // 2. Trend & Bias
        if (input.hasHtfTrendAlignment) {
            checklist.add(
                StrategyChecklistItem(
                    id = "trend",
                    name = "Higher Timeframe Alignment",
                    description = "Trade direction aligns with ${input.timeframeHtf} market flow or valid QM reversal",
                    status = ChecklistStatus.PASS,
                    note = "Trend direction confirmed"
                )
            )
        } else {
            checklist.add(
                StrategyChecklistItem(
                    id = "trend",
                    name = "Higher Timeframe Alignment",
                    description = "Setup opposes the primary HTF trend without QM structure",
                    status = ChecklistStatus.FAIL,
                    note = "Trading against HTF momentum without shift is low probability"
                )
            )
            missingConditions.add("Opposing HTF trend direction; lacks structural reversal basis")
        }

        // 3. Inducement Formation (LIT)
        if (input.hasInducementFormed) {
            checklist.add(
                StrategyChecklistItem(
                    id = "idm_formed",
                    name = "LIT - Inducement (IDM) Identified",
                    description = "Retail bait level created right in front of the POI zone",
                    status = ChecklistStatus.PASS,
                    note = "Clear retail trap / bait pool visible"
                )
            )
        } else {
            checklist.add(
                StrategyChecklistItem(
                    id = "idm_formed",
                    name = "LIT - Inducement (IDM) Identified",
                    description = "No inducement bait identified before the POI",
                    status = ChecklistStatus.WAITING,
                    note = "Without inducement, price may create a trap before continuing"
                )
            )
            missingConditions.add("Inducement (IDM) bait level not yet formed before the POI")
        }

        // 4. Target Sweep (TS) / Liquidity Sweep
        if (input.isInducementClearedViaTs) {
            checklist.add(
                StrategyChecklistItem(
                    id = "ts_sweep",
                    name = "TS - Target Sweep / Stop Hunt",
                    description = "Price spiked through Inducement / Old High or Low to clear stops",
                    status = ChecklistStatus.PASS,
                    note = "Retail liquidity pool successfully swept into POI"
                )
            )
        } else {
            if (input.hasInducementFormed) {
                checklist.add(
                    StrategyChecklistItem(
                        id = "ts_sweep",
                        name = "TS - Target Sweep / Stop Hunt",
                        description = "Inducement has NOT been swept yet. High risk of fakeout!",
                        status = ChecklistStatus.WAITING,
                        note = "Wait for price to sweep retail stops before looking to enter"
                    )
                )
                missingConditions.add("Inducement has NOT been cleared via TS (Target Sweep). DO NOT enter early!")
            } else {
                checklist.add(
                    StrategyChecklistItem(
                        id = "ts_sweep",
                        name = "TS - Target Sweep / Stop Hunt",
                        description = "No liquidity sweep observed",
                        status = ChecklistStatus.FAIL,
                        note = "Sweep is required to trigger institutional orders"
                    )
                )
                missingConditions.add("Liquidity sweep (TS) missing")
            }
        }

        // 5. Candlestick Rejection (Pin Bar / Engulfing)
        if (input.hasCandleRejection) {
            checklist.add(
                StrategyChecklistItem(
                    id = "rejection",
                    name = "Candle Rejection & Absorption",
                    description = "Clear reversal pattern (Pin Bar, long wick, or engulfing) rejecting the sweep level",
                    status = ChecklistStatus.PASS,
                    note = "Strong opposing institutional order flow demonstrated"
                )
            )
        } else {
            checklist.add(
                StrategyChecklistItem(
                    id = "rejection",
                    name = "Candle Rejection & Absorption",
                    description = "No strong rejection candlestick or long wick printed",
                    status = ChecklistStatus.WAITING,
                    note = "Wait for candle to close with rejection wick or engulfing pattern"
                )
            )
            missingConditions.add("Rejection candlestick confirmation (Pin Bar / long wick) not confirmed")
        }

        // 6. Market Structure Shift (MSS)
        if (input.hasLtfMss) {
            checklist.add(
                StrategyChecklistItem(
                    id = "mss",
                    name = "MSS (Market Structure Shift)",
                    description = "Shift of structure confirmed on ${input.timeframeLtf} following TS",
                    status = ChecklistStatus.PASS,
                    note = "Smart money shift confirmed on entry timeframe"
                )
            )
        } else {
            checklist.add(
                StrategyChecklistItem(
                    id = "mss",
                    name = "MSS (Market Structure Shift)",
                    description = "${input.timeframeLtf} has not shifted structure yet",
                    status = ChecklistStatus.WAITING,
                    note = "Wait for ${input.timeframeLtf} structural shift to confirm reversal"
                )
            )
            missingConditions.add("Market Structure Shift (MSS) on ${input.timeframeLtf} not yet formed")
        }

        // Calculate math values
        val calc = RiskCalculatorEngine.calculate(
            symbol = input.symbol,
            direction = input.direction,
            accountBalance = riskSettings.accountBalance,
            riskPercentage = riskSettings.riskPercentage,
            customRiskAmount = null,
            entryPrice = input.entryPrice,
            stopLossPrice = input.stopLossPrice,
            tp1Price = input.tp1Price,
            tp2Price = input.tp2Price
        )

        // 7. Risk-to-Reward Rule (minimum 1:1.5 for TP1, 1:3 for TP2)
        val hasAdequateRr = calc.rrTp1 >= 1.5 && calc.rrTp2 >= 2.5
        if (hasAdequateRr) {
            checklist.add(
                StrategyChecklistItem(
                    id = "rr_ratio",
                    name = "Risk-to-Reward Ratio",
                    description = "TP1 is 1:${calc.rrTp1} RR | TP2 is 1:${calc.rrTp2} RR",
                    status = ChecklistStatus.PASS,
                    note = "Risk-reward criteria satisfied"
                )
            )
        } else {
            checklist.add(
                StrategyChecklistItem(
                    id = "rr_ratio",
                    name = "Risk-to-Reward Ratio",
                    description = "TP1 is 1:${calc.rrTp1} RR | TP2 is 1:${calc.rrTp2} RR (Target >= 1:2.0)",
                    status = ChecklistStatus.FAIL,
                    note = "Sub-optimal risk-to-reward ratio for high-probability setup"
                )
            )
            if (calc.rrTp1 < 1.0) {
                missingConditions.add("Reward-to-risk ratio is below minimum standard (TP1 < 1:1)")
            }
        }

        // Determine Final Status strictly based on user rules
        val status: SetupStatus
        val reason: String

        val anyFail = checklist.any { it.status == ChecklistStatus.FAIL }
        val anyWaiting = checklist.any { it.status == ChecklistStatus.WAITING }

        if (anyFail) {
            status = SetupStatus.NO_TRADE
            reason = "NO TRADE: Strategy rules are violated. Missing or failed conditions: " +
                    missingConditions.joinToString("; ") +
                    ". Do NOT force a trade."
        } else if (anyWaiting) {
            status = SetupStatus.WAIT
            reason = "WAIT: Setup is developing but not fully confirmed. " +
                    missingConditions.joinToString("; ") +
                    ". Be patient and wait for confirmation."
        } else {
            status = SetupStatus.VALID_SETUP
            val directionStr = input.direction.name
            reason = "VALID HIGH-PROBABILITY SETUP: $directionStr on ${input.symbol}. Price approached ${input.timeframeHtf} ${input.poiType} POI, " +
                    "cleared Inducement via TS (Target Sweep), followed by aggressive candlestick rejection and ${input.timeframeLtf} MSS confirmation. " +
                    "Tight Stop Loss placed at ${input.stopLossPrice} with 1:${calc.rrTp1} RR to TP1 and 1:${calc.rrTp2} RR to TP2."
        }

        return TradeSetup(
            symbol = input.symbol,
            direction = input.direction,
            entryPrice = input.entryPrice,
            stopLoss = input.stopLossPrice,
            takeProfit1 = input.tp1Price,
            takeProfit2 = input.tp2Price,
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
            missingConditions = missingConditions,
            checklist = checklist,
            reason = reason,
            poiType = input.poiType,
            timeframeHtf = input.timeframeHtf,
            timeframeLtf = input.timeframeLtf,
            imageUri = input.imageUri
        )
    }

    val SYSTEM_PROMPT = """
You are an expert institutional trading engine implementing the mechanical "LIT + MSNR Strategy" (Liquidity Inducement Theory, Market Structure, Support & Resistance, Target Sweep, and 10-Pip tight SL model).
Your job is to analyze the provided chart image, symbol, timeframe, and price levels according to the STRICT strategy rules below.

MANDATORY RULES:
1. NEVER force a trade. If any required strategy condition is missing or invalid, output "status": "NO_TRADE" or "WAIT" and explain the exact missing rule. NEVER fabricate levels if conditions are not satisfied.
2. MSNR POI Types to recognize: Resistance, Support, RBS (Resistance Becomes Support), SBR (Support Becomes Resistance), OCL (Open-Close Level), QM (Quasimodo Pattern), Engulfing OB (Order block with prior sweep).
3. LIT (Inducement): An inducement (IDM) is a bait level created before the true POI. CLEARING INDUCEMENT IS MANDATORY. If inducement is not cleared, output "NO_TRADE".
4. TS (Target Sweep / Stop Hunt): Look for long wick piercing the inducement or old high/low to collect stop loss liquidity.
5. Rejection & Candlestick Confirmation: Look for pin bar, long rejection wick, or engulfing candle closing away from the sweep.
6. MSS (Market Structure Shift): Lower timeframe confirmation that market maker has seized control.
7. Risk Management: SL must be placed just beyond the TS sweep wick (~10 pips in Gold/Forex). TP1 at recent opposing structure, TP2 at major liquidity.

OUTPUT FORMAT:
You MUST respond with valid JSON ONLY matching this exact structure:
{
  "status": "VALID_SETUP" | "WAIT" | "NO_TRADE",
  "direction": "BUY" | "SELL",
  "entryPrice": 0.0,
  "stopLoss": 0.0,
  "takeProfit1": 0.0,
  "takeProfit2": 0.0,
  "poiType": "SBR" | "RBS" | "QM" | "Support" | "Resistance" | "Engulfing OB",
  "reason": "Detailed mechanical reason based ONLY on LIT, MSNR, TS rules",
  "missingConditions": ["Condition 1", "Condition 2"],
  "checklist": [
    {"name": "MSNR POI Identified", "status": "PASS" | "FAIL" | "WAITING", "note": "..."},
    {"name": "HTF Trend / Bias", "status": "PASS" | "FAIL" | "WAITING", "note": "..."},
    {"name": "Inducement (IDM) Formed", "status": "PASS" | "FAIL" | "WAITING", "note": "..."},
    {"name": "TS - Target Sweep Cleared", "status": "PASS" | "FAIL" | "WAITING", "note": "..."},
    {"name": "Candlestick Rejection", "status": "PASS" | "FAIL" | "WAITING", "note": "..."},
    {"name": "MSS (Market Structure Shift)", "status": "PASS" | "FAIL" | "WAITING", "note": "..."}
  ]
}
""".trimIndent()
}
