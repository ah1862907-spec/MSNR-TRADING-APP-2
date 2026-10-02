package com.example.engine

import com.example.model.TradeDirection
import kotlin.math.abs
import kotlin.math.roundToInt

object RiskCalculatorEngine {

    fun getPipUnit(symbol: String): Double {
        val sym = symbol.uppercase().trim()
        return when {
            sym.contains("XAU") || sym.contains("GOLD") -> 0.10
            sym.contains("JPY") -> 0.01
            sym.contains("BTC") -> 1.0
            sym.contains("US30") || sym.contains("NAS") || sym.contains("SPX") -> 1.0
            else -> 0.0001 // Standard Forex (EURUSD, GBPUSD, etc.)
        }
    }

    fun getPipValuePerStandardLot(symbol: String): Double {
        val sym = symbol.uppercase().trim()
        return when {
            sym.contains("XAU") || sym.contains("GOLD") -> 10.0 // 100 oz contract: 0.10 move = $10
            sym.contains("JPY") -> 9.0 // Approx $9-$10 depending on USDJPY rate
            sym.contains("BTC") -> 1.0
            sym.contains("US30") || sym.contains("NAS") -> 1.0
            else -> 10.0 // Standard Forex: 100,000 units * 0.0001 = $10 per pip
        }
    }

    data class CalculationResult(
        val stopDistancePips: Double,
        val tp1DistancePips: Double,
        val tp2DistancePips: Double,
        val rrTp1: Double,
        val rrTp2: Double,
        val recommendedLotSize: Double,
        val riskAmount: Double,
        val potentialProfitTp1: Double,
        val potentialProfitTp2: Double
    )

    fun calculate(
        symbol: String,
        direction: TradeDirection,
        accountBalance: Double,
        riskPercentage: Double,
        customRiskAmount: Double? = null,
        entryPrice: Double,
        stopLossPrice: Double,
        tp1Price: Double,
        tp2Price: Double
    ): CalculationResult {
        val pipUnit = getPipUnit(symbol)
        val pipValPerLot = getPipValuePerStandardLot(symbol)

        val riskAmt = customRiskAmount ?: (accountBalance * (riskPercentage / 100.0))

        val stopDistance = abs(entryPrice - stopLossPrice)
        val stopDistancePips = if (pipUnit > 0) stopDistance / pipUnit else 0.0

        val tp1Distance = abs(tp1Price - entryPrice)
        val tp1DistancePips = if (pipUnit > 0) tp1Distance / pipUnit else 0.0

        val tp2Distance = abs(tp2Price - entryPrice)
        val tp2DistancePips = if (pipUnit > 0) tp2Distance / pipUnit else 0.0

        val rr1 = if (stopDistancePips > 0.0001) tp1DistancePips / stopDistancePips else 0.0
        val rr2 = if (stopDistancePips > 0.0001) tp2DistancePips / stopDistancePips else 0.0

        val rawLot = if (stopDistancePips > 0.0001 && pipValPerLot > 0) {
            riskAmt / (stopDistancePips * pipValPerLot)
        } else {
            0.0
        }

        // Round lot size to 2 decimal places with minimum 0.01 if valid
        val roundedLot = if (rawLot > 0.001) {
            (rawLot * 100.0).roundToInt() / 100.0
        } else {
            0.0
        }

        val profitTp1 = riskAmt * rr1
        val profitTp2 = riskAmt * rr2

        return CalculationResult(
            stopDistancePips = (stopDistancePips * 10.0).roundToInt() / 10.0,
            tp1DistancePips = (tp1DistancePips * 10.0).roundToInt() / 10.0,
            tp2DistancePips = (tp2DistancePips * 10.0).roundToInt() / 10.0,
            rrTp1 = (rr1 * 100.0).roundToInt() / 100.0,
            rrTp2 = (rr2 * 100.0).roundToInt() / 100.0,
            recommendedLotSize = roundedLot,
            riskAmount = (riskAmt * 100.0).roundToInt() / 100.0,
            potentialProfitTp1 = (profitTp1 * 100.0).roundToInt() / 100.0,
            potentialProfitTp2 = (profitTp2 * 100.0).roundToInt() / 100.0
        )
    }

    fun calculateDefaultLevels(
        symbol: String,
        direction: TradeDirection,
        entryPrice: Double,
        slPips: Double = 10.0,
        rrTp1Target: Double = 2.0,
        rrTp2Target: Double = 4.0
    ): Triple<Double, Double, Double> {
        val pipUnit = getPipUnit(symbol)
        val slDistance = slPips * pipUnit
        val tp1Distance = slPips * rrTp1Target * pipUnit
        val tp2Distance = slPips * rrTp2Target * pipUnit

        val sl = if (direction == TradeDirection.BUY) entryPrice - slDistance else entryPrice + slDistance
        val tp1 = if (direction == TradeDirection.BUY) entryPrice + tp1Distance else entryPrice - tp1Distance
        val tp2 = if (direction == TradeDirection.BUY) entryPrice + tp2Distance else entryPrice - tp2Distance

        return Triple(sl, tp1, tp2)
    }
}
