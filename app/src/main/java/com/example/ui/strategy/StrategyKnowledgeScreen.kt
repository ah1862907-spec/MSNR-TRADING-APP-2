package com.example.ui.strategy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.StrategyKnowledge
import com.example.ui.theme.BearRed
import com.example.ui.theme.BullGreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkGray
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalCard
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningOrange

@Composable
fun StrategyKnowledgeScreen(
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var selectedTab by remember { mutableStateOf("ALL") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBg)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Strategy System",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Text(
                    text = "LIT + MSNR + Target Sweep (TS) Rules",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CyanAccent
                )
            }
            Surface(
                color = GoldContainer,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "SL 10 PIPS",
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Mechanical Philosophy Notice
        Surface(
            color = TerminalSurface,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, TerminalBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Philosophy",
                    tint = GoldAccent,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Mechanical Execution System",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Zero arbitrary indicators (no RSI, MACD, or Bollinger Bands). Market moves purely via liquidity, inducements, key structural MSNR levels, and stop hunts.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Interactive Diagrams: MSNR & TS Visualizer
        Card(
            colors = CardDefaults.cardColors(containerColor = TerminalSurface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "VISUAL ANATOMY: TS + INDUCEMENT SWEEP",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Custom Canvas drawing the Candlestick Target Sweep & Inducement pattern from images
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(TerminalCard)
                        .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Draw POI Zone (Light cyan box at the top)
                        val poiTop = h * 0.15f
                        val poiHeight = h * 0.22f
                        drawRect(
                            color = Color(0x3300E5FF),
                            topLeft = Offset(0f, poiTop),
                            size = Size(w, poiHeight)
                        )

                        // Draw Inducement dashed line
                        val idmY = h * 0.45f
                        drawLine(
                            color = Color(0xFFFFD54F),
                            start = Offset(w * 0.1f, idmY),
                            end = Offset(w * 0.9f, idmY),
                            strokeWidth = 2f,
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                        )

                        // Draw Candlesticks demonstrating the sweep
                        // Candle 1: Bullish into Inducement
                        val c1x = w * 0.25f
                        drawLine(color = BullGreen, start = Offset(c1x, h * 0.40f), end = Offset(c1x, h * 0.70f), strokeWidth = 3f)
                        drawRect(color = BullGreen, topLeft = Offset(c1x - 10f, h * 0.45f), size = Size(20f, h * 0.20f))

                        // Candle 2: Inducement bait candle
                        val c2x = w * 0.40f
                        drawLine(color = BearRed, start = Offset(c2x, h * 0.42f), end = Offset(c2x, h * 0.65f), strokeWidth = 3f)
                        drawRect(color = BearRed, topLeft = Offset(c2x - 10f, h * 0.45f), size = Size(20f, h * 0.15f))

                        // Candle 3: TS Sweep Spike piercing through IDM and tapping POI with long wick
                        val c3x = w * 0.60f
                        // Long upper wick reaching into POI
                        drawLine(color = Color.White, start = Offset(c3x, h * 0.18f), end = Offset(c3x, h * 0.55f), strokeWidth = 3f)
                        // Body closed down (Rejection)
                        drawRect(color = BearRed, topLeft = Offset(c3x - 12f, h * 0.42f), size = Size(24f, h * 0.18f))

                        // Candle 4: Massive Bearish Expansion / MSS
                        val c4x = w * 0.78f
                        drawLine(color = BearRed, start = Offset(c4x, h * 0.55f), end = Offset(c4x, h * 0.90f), strokeWidth = 3f)
                        drawRect(color = BearRed, topLeft = Offset(c4x - 12f, h * 0.58f), size = Size(24f, h * 0.28f))
                    }

                    // Annotations
                    Text(
                        text = "POI Zone (SBR / Resistance)",
                        color = CyanAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 8.dp, top = 4.dp)
                    )
                    Text(
                        text = "Inducement (IDM)",
                        color = GoldAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 8.dp)
                    )
                    Text(
                        text = "TS Sweep Wick",
                        color = BearRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 40.dp, top = 6.dp)
                    )
                }
            }
        }

        // Strategy Rule Cards (Iterating over user's extracted specification)
        Text(
            text = "STRATEGY PILLARS & MECHANICAL RULES",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )

        StrategyKnowledge.RULES_LIST.forEach { rule ->
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, TerminalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = rule.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = when (rule.category) {
                                "MSNR" -> CyanAccent.copy(alpha = 0.15f)
                                "LIT" -> GoldAccent.copy(alpha = 0.15f)
                                "TS" -> BearRed.copy(alpha = 0.15f)
                                else -> BullGreen.copy(alpha = 0.15f)
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = rule.category,
                                color = when (rule.category) {
                                    "MSNR" -> CyanAccent
                                    "LIT" -> GoldAccent
                                    "TS" -> BearRed
                                    else -> BullGreen
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = rule.subtitle,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = rule.explanation,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = TerminalBorder)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Key Mechanics:",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        rule.keyFeatures.forEach { feature ->
                            Row(verticalAlignment = Alignment.Top) {
                                Text("• ", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(feature, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = TerminalCard,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Tip",
                                tint = BullGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = rule.highProbabilityTip,
                                color = BullGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
