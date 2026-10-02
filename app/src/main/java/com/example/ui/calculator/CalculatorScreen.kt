package com.example.ui.calculator

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TradeDirection
import com.example.ui.components.MetricBox
import com.example.ui.components.TerminalPriceCard
import com.example.ui.theme.BearRed
import com.example.ui.theme.BearRedContainer
import com.example.ui.theme.BullGreen
import com.example.ui.theme.BullGreenContainer
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalCard
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val symbol by viewModel.symbol.collectAsState()
    val direction by viewModel.direction.collectAsState()
    val balanceStr by viewModel.balanceStr.collectAsState()
    val riskPercentStr by viewModel.riskPercentStr.collectAsState()
    val entryStr by viewModel.entryStr.collectAsState()
    val slStr by viewModel.slStr.collectAsState()
    val tp1Str by viewModel.tp1Str.collectAsState()
    val tp2Str by viewModel.tp2Str.collectAsState()
    val currencyStr by viewModel.currencyStr.collectAsState()

    val calc by viewModel.calculation.collectAsState()

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
                    text = "Risk & Position Calculator",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Live dynamic position sizing & RR engine",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CyanAccent
                )
            }
            Icon(
                imageVector = Icons.Default.Calculate,
                contentDescription = "Calculator",
                tint = GoldAccent,
                modifier = Modifier.size(28.dp)
            )
        }

        // Live Calculated Results Hero Card
        Card(
            colors = CardDefaults.cardColors(containerColor = TerminalSurface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.5.dp, CyanAccent),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("calculator_results_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "RECOMMENDED POSITION SIZE",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "${calc.recommendedLotSize} LOTS",
                        color = GoldAccent,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Risk: $currencyStr ${calc.riskAmount}",
                        color = BearRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = TerminalBorder)
                Spacer(modifier = Modifier.height(14.dp))

                // Distance & RR Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricBox(
                        label = "Distance to SL",
                        value = "${calc.stopDistancePips} pips",
                        color = BearRed,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        label = "TP1 (1:${calc.rrTp1} RR)",
                        value = "+$${calc.potentialProfitTp1}",
                        color = BullGreen,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        label = "TP2 (1:${calc.rrTp2} RR)",
                        value = "+$${calc.potentialProfitTp2}",
                        color = BullGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Inputs Card
        Card(
            colors = CardDefaults.cardColors(containerColor = TerminalSurface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, TerminalBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ACCOUNT & RISK SETTINGS",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Balance & Currency
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = balanceStr,
                        onValueChange = {
                            viewModel.balanceStr.value = it
                            viewModel.recalculate()
                        },
                        label = { Text("Account Balance ($currencyStr)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = TerminalBorder,
                            focusedContainerColor = TerminalCard,
                            unfocusedContainerColor = TerminalCard,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1.4f)
                    )
                    OutlinedTextField(
                        value = riskPercentStr,
                        onValueChange = {
                            viewModel.riskPercentStr.value = it
                            viewModel.recalculate()
                        },
                        label = { Text("Risk %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BearRed,
                            unfocusedBorderColor = TerminalBorder,
                            focusedContainerColor = TerminalCard,
                            unfocusedContainerColor = TerminalCard,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Risk Percent Quick Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0.5, 1.0, 1.5, 2.0).forEach { pct ->
                        val isSel = riskPercentStr.toDoubleOrNull() == pct
                        Surface(
                            color = if (isSel) BearRedContainer else TerminalCard,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, if (isSel) BearRed else TerminalBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setRiskPercentPreset(pct) }
                        ) {
                            Text(
                                text = "$pct%",
                                color = if (isSel) BearRed else TextSecondary,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Symbol selection
                Text(
                    text = "INSTRUMENT / SYMBOL",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("XAUUSD", "EURUSD", "GBPUSD", "USDJPY", "BTCUSD").forEach { sym ->
                        val isSelected = symbol.equals(sym, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setSymbol(sym) },
                            label = {
                                Text(
                                    text = sym,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanAccent.copy(alpha = 0.2f),
                                selectedLabelColor = CyanAccent,
                                containerColor = TerminalCard,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) CyanAccent else TerminalBorder
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Direction selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = if (direction == TradeDirection.BUY) BullGreenContainer else TerminalCard,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (direction == TradeDirection.BUY) BullGreen else TerminalBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                viewModel.direction.value = TradeDirection.BUY
                                viewModel.recalculate()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = "BUY",
                                tint = if (direction == TradeDirection.BUY) BullGreen else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "BUY",
                                color = if (direction == TradeDirection.BUY) BullGreen else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Surface(
                        color = if (direction == TradeDirection.SELL) BearRedContainer else TerminalCard,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (direction == TradeDirection.SELL) BearRed else TerminalBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                viewModel.direction.value = TradeDirection.SELL
                                viewModel.recalculate()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingDown,
                                contentDescription = "SELL",
                                tint = if (direction == TradeDirection.SELL) BearRed else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SELL",
                                color = if (direction == TradeDirection.SELL) BearRed else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Price levels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TRADE LEVELS",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    OutlinedButton(
                        onClick = { viewModel.set10PipStop() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent),
                        border = BorderStroke(1.dp, GoldAccent),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "10 Pips",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Preset 10-Pip SL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = entryStr,
                        onValueChange = {
                            viewModel.entryStr.value = it
                            viewModel.recalculate()
                        },
                        label = { Text("Entry") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = TerminalBorder,
                            focusedContainerColor = TerminalCard,
                            unfocusedContainerColor = TerminalCard,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = slStr,
                        onValueChange = {
                            viewModel.slStr.value = it
                            viewModel.recalculate()
                        },
                        label = { Text("Stop Loss") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BearRed,
                            unfocusedBorderColor = TerminalBorder,
                            focusedContainerColor = TerminalCard,
                            unfocusedContainerColor = TerminalCard,
                            focusedTextColor = BearRed,
                            unfocusedTextColor = BearRed
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = tp1Str,
                        onValueChange = {
                            viewModel.tp1Str.value = it
                            viewModel.recalculate()
                        },
                        label = { Text("Take Profit 1") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BullGreen,
                            unfocusedBorderColor = TerminalBorder,
                            focusedContainerColor = TerminalCard,
                            unfocusedContainerColor = TerminalCard,
                            focusedTextColor = BullGreen,
                            unfocusedTextColor = BullGreen
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = tp2Str,
                        onValueChange = {
                            viewModel.tp2Str.value = it
                            viewModel.recalculate()
                        },
                        label = { Text("Take Profit 2") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BullGreen,
                            unfocusedBorderColor = TerminalBorder,
                            focusedContainerColor = TerminalCard,
                            unfocusedContainerColor = TerminalCard,
                            focusedTextColor = BullGreen,
                            unfocusedTextColor = BullGreen
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Instrument Specs Reference
        Card(
            colors = CardDefaults.cardColors(containerColor = TerminalSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, TerminalBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "SPECIFICATIONS & CONTRACT SPECS",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Gold (XAUUSD): 1 pip = 0.10 price movement ($1.00 move = 10 pips). Contract size = 100 oz. 1.00 lot @ 1 pip = $10.00.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Standard Forex (EURUSD, GBPUSD): 1 pip = 0.0001. Contract size = 100,000 units. 1.00 lot @ 1 pip = $10.00.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Tight 10-Pip SL Model: Designed for precise institutional entries with maximum lot efficiency and minimal drawdown.",
                    color = GoldAccent,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
