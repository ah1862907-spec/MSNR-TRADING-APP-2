package com.example.ui.analyzer

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.ChecklistStatus
import com.example.model.SetupStatus
import com.example.model.TradeDirection
import com.example.model.TradeSetup
import com.example.ui.components.ChecklistItemRow
import com.example.ui.components.DirectionBadge
import com.example.ui.components.MetricBox
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.components.TerminalPriceCard
import com.example.ui.theme.BearRed
import com.example.ui.theme.BearRedContainer
import com.example.ui.theme.BullGreen
import com.example.ui.theme.BullGreenContainer
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkGray
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalCard
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningOrange
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnalyzerScreen(
    viewModel: AnalyzerViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    val symbol by viewModel.symbol.collectAsState()
    val direction by viewModel.direction.collectAsState()
    val poiType by viewModel.poiType.collectAsState()
    val htf by viewModel.timeframeHtf.collectAsState()
    val ltf by viewModel.timeframeLtf.collectAsState()
    val chartUri by viewModel.chartUri.collectAsState()

    val entryStr by viewModel.entryPriceStr.collectAsState()
    val slStr by viewModel.stopLossStr.collectAsState()
    val tp1Str by viewModel.tp1Str.collectAsState()
    val tp2Str by viewModel.tp2Str.collectAsState()

    val hasHtfPoi by viewModel.hasHtfPoi.collectAsState()
    val hasTrendAlign by viewModel.hasTrendAlign.collectAsState()
    val hasIdm by viewModel.hasIdm.collectAsState()
    val hasTsSweep by viewModel.hasTsSweep.collectAsState()
    val hasRejection by viewModel.hasRejection.collectAsState()
    val hasMss by viewModel.hasMss.collectAsState()

    val uiState by viewModel.uiState.collectAsState()
    val riskSettings by viewModel.riskSettings.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            viewModel.chartUri.value = uri
        }
    )

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(TerminalBg)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Trade Analyzer",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "LIT + MSNR Mechanical Strategy Engine",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CyanAccent
                    )
                }
                Surface(
                    color = TerminalSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, TerminalBorder)
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

            // Step 1: Instrument / Symbol Selector
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, TerminalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "1. SELECT MARKET / SYMBOL",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    val symbols = listOf("XAUUSD", "EURUSD", "GBPUSD", "USDJPY", "BTCUSD", "US30")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        symbols.forEach { sym ->
                            val isSelected = symbol.equals(sym, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setSymbol(sym) },
                                label = {
                                    Text(
                                        text = sym,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (sym == "XAUUSD") GoldAccent.copy(alpha = 0.2f) else CyanAccent.copy(alpha = 0.2f),
                                    selectedLabelColor = if (sym == "XAUUSD") GoldAccent else CyanAccent,
                                    containerColor = TerminalCard,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) (if (sym == "XAUUSD") GoldAccent else CyanAccent) else TerminalBorder
                                ),
                                modifier = Modifier.testTag("symbol_chip_$sym")
                            )
                        }
                    }
                }
            }

            // Step 2: Direction & Timeframes
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, TerminalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "2. DIRECTION & TIMEFRAME EXECUTION",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Direction toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = if (direction == TradeDirection.BUY) BullGreenContainer else TerminalCard,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                1.5.dp,
                                if (direction == TradeDirection.BUY) BullGreen else TerminalBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setDirection(TradeDirection.BUY) }
                                .testTag("direction_buy_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = "BUY",
                                    tint = if (direction == TradeDirection.BUY) BullGreen else TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "BUY / LONG",
                                    color = if (direction == TradeDirection.BUY) BullGreen else TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Surface(
                            color = if (direction == TradeDirection.SELL) BearRedContainer else TerminalCard,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                1.5.dp,
                                if (direction == TradeDirection.SELL) BearRed else TerminalBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setDirection(TradeDirection.SELL) }
                                .testTag("direction_sell_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingDown,
                                    contentDescription = "SELL",
                                    tint = if (direction == TradeDirection.SELL) BearRed else TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SELL / SHORT",
                                    color = if (direction == TradeDirection.SELL) BearRed else TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Timeframes: HTF (POI) > LTF (Entry)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "HTF Bias & POI",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("M15", "H1", "H4").forEach { tf ->
                                    val isSel = htf == tf
                                    Surface(
                                        color = if (isSel) CyanAccent.copy(alpha = 0.2f) else TerminalCard,
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, if (isSel) CyanAccent else TerminalBorder),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { viewModel.timeframeHtf.value = tf }
                                    ) {
                                        Text(
                                            text = tf,
                                            color = if (isSel) CyanAccent else TextSecondary,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(vertical = 6.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "To",
                            tint = TextMuted,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(top = 16.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "LTF Entry (TS + MSS)",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("M1", "M5").forEach { tf ->
                                    val isSel = ltf == tf
                                    Surface(
                                        color = if (isSel) GoldAccent.copy(alpha = 0.2f) else TerminalCard,
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, if (isSel) GoldAccent else TerminalBorder),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { viewModel.timeframeLtf.value = tf }
                                    ) {
                                        Text(
                                            text = tf,
                                            color = if (isSel) GoldAccent else TextSecondary,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(vertical = 6.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Step 3: MSNR POI Type
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, TerminalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "3. MSNR POINT OF INTEREST (POI)",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    val pois = listOf("RBS", "SBR", "QM", "Engulfing OB", "OCL", "Support", "Resistance")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pois.forEach { p ->
                            val isSel = poiType == p
                            FilterChip(
                                selected = isSel,
                                onClick = { viewModel.poiType.value = p },
                                label = {
                                    Text(
                                        text = p,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanAccent.copy(alpha = 0.15f),
                                    selectedLabelColor = CyanAccent,
                                    containerColor = TerminalCard,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSel,
                                    borderColor = if (isSel) CyanAccent else TerminalBorder
                                )
                            )
                        }
                    }
                }
            }

            // Step 4: Chart Screenshot Upload
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, TerminalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "4. MARKET CHART / SCREENSHOT",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        if (chartUri != null) {
                            Text(
                                text = "Attached",
                                color = BullGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    if (chartUri != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = chartUri,
                                contentDescription = "Attached Chart",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            IconButton(
                                onClick = { viewModel.chartUri.value = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .background(TerminalBg.copy(alpha = 0.8f), CircleShape)
                                    .size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove chart",
                                    tint = BearRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else {
                        Surface(
                            color = TerminalCard,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, TerminalBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .testTag("upload_chart_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Upload Chart",
                                    tint = CyanAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Upload TradingView / Chart Screenshot",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "AI vision models can analyze your chart for LIT & TS wicks",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Step 5: Price Levels & "SL 10 Pips" Preset
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, TerminalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "5. PRICE LEVELS",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        OutlinedButton(
                            onClick = { viewModel.apply10PipPreset() },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = GoldAccent
                            ),
                            border = BorderStroke(1.dp, GoldAccent),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(28.dp)
                                .testTag("sl_10_pips_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = "Auto",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Auto 10-Pip SL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = entryStr,
                            onValueChange = { viewModel.entryPriceStr.value = it },
                            label = { Text("Entry Price") },
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
                            modifier = Modifier
                                .weight(1f)
                                .testTag("entry_price_input")
                        )
                        OutlinedTextField(
                            value = slStr,
                            onValueChange = { viewModel.stopLossStr.value = it },
                            label = { Text("Stop Loss (TS Wick)") },
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
                            modifier = Modifier
                                .weight(1f)
                                .testTag("stop_loss_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = tp1Str,
                            onValueChange = { viewModel.tp1Str.value = it },
                            label = { Text("TP 1 (Opposing Struct)") },
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
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tp1_input")
                        )
                        OutlinedTextField(
                            value = tp2Str,
                            onValueChange = { viewModel.tp2Str.value = it },
                            label = { Text("TP 2 (Major Pool)") },
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
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tp2_input")
                        )
                    }
                }
            }

            // Step 6: Strategy Rule Checklist (From User Images)
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, TerminalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "6. LIT + MSNR STRATEGY CONDITIONS",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "All conditions must be satisfied for a VALID setup. Incomplete = NO TRADE.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ChecklistToggleRow(
                        title = "MSNR POI Identified",
                        subtitle = "Price reacting to $poiType on $htf",
                        checked = hasHtfPoi,
                        onCheckedChange = { viewModel.hasHtfPoi.value = it }
                    )
                    ChecklistToggleRow(
                        title = "HTF Trend / Bias Alignment",
                        subtitle = "Direction matches HTF market flow or QM reversal",
                        checked = hasTrendAlign,
                        onCheckedChange = { viewModel.hasTrendAlign.value = it }
                    )
                    ChecklistToggleRow(
                        title = "Inducement (IDM) Formed",
                        subtitle = "Retail bait level created before the POI",
                        checked = hasIdm,
                        onCheckedChange = { viewModel.hasIdm.value = it }
                    )
                    ChecklistToggleRow(
                        title = "TS (Target Sweep) Wick Completed",
                        subtitle = "Inducement / Old High or Low was swept into POI",
                        checked = hasTsSweep,
                        onCheckedChange = { viewModel.hasTsSweep.value = it }
                    )
                    ChecklistToggleRow(
                        title = "Candle Rejection Confirmed",
                        subtitle = "Pin bar, long rejection wick, or engulfing candle",
                        checked = hasRejection,
                        onCheckedChange = { viewModel.hasRejection.value = it }
                    )
                    ChecklistToggleRow(
                        title = "MSS (Market Structure Shift) on $ltf",
                        subtitle = "Lower timeframe confirmation of institutional control",
                        checked = hasMss,
                        onCheckedChange = { viewModel.hasMss.value = it }
                    )
                }
            }

            // Step 7: Run Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.runMechanicalEvaluation() },
                    colors = ButtonDefaults.buttonColors(containerColor = BullGreen, contentColor = TerminalBg),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("run_strategy_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Scan",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Run LIT Mechanical Strategy Scan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.runAiScan() },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CyanAccent
                    ),
                    border = BorderStroke(1.5.dp, CyanAccent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("run_ai_scan_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Scan with OpenRouter AI Vision",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Results UI State
            when (val state = uiState) {
                is AnalyzerViewModel.AnalyzerUiState.Analyzing -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CyanAccent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = CyanAccent,
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Evaluating strategy rules & calculating metrics...",
                                color = TextPrimary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                is AnalyzerViewModel.AnalyzerUiState.Result -> {
                    TradeOutputCard(
                        setup = state.setup,
                        isAi = state.isAi,
                        onSaveToJournal = { viewModel.saveSetupToJournal(state.setup) }
                    )
                }
                is AnalyzerViewModel.AnalyzerUiState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BearRedContainer),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BearRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = BearRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = state.message,
                                color = TextPrimary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
                AnalyzerViewModel.AnalyzerUiState.Idle -> {
                    // Ready state
                }
            }

            // Disclaimer Banner
            Surface(
                color = TerminalCard,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, TerminalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Disclaimer",
                        tint = TextMuted,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Decision Support Notice: LIT Trade Assistant is an analysis and decision-support tool, not an automated trading robot. Market conditions fluctuate; always practice strict risk management (1-2% max risk per trade).",
                        color = TextMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
fun ChecklistToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = BullGreen,
                checkmarkColor = TerminalBg,
                uncheckedColor = TextMuted
            )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (checked) TextPrimary else TextSecondary,
                fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 13.sp
            )
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 11.sp
            )
        }
        Text(
            text = if (checked) "PASS" else "FAIL",
            color = if (checked) BullGreen else BearRed,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(end = 4.dp)
        )
    }
}

@Composable
fun TradeOutputCard(
    setup: TradeSetup,
    isAi: Boolean,
    onSaveToJournal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when (setup.status) {
        SetupStatus.VALID_SETUP -> BullGreen
        SetupStatus.WAIT -> WarningOrange
        SetupStatus.NO_TRADE -> BearRed
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = TerminalSurface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, borderColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Status and Direction
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DirectionBadge(direction = setup.direction)
                    Text(
                        text = setup.symbol,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                }
                StatusBadge(status = setup.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Highlight message
            if (setup.status == SetupStatus.NO_TRADE) {
                Surface(
                    color = BearRedContainer,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BearRed.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "CRITICAL RULE: DO NOT FORCE TRADE",
                            color = BearRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = setup.reason,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else if (setup.status == SetupStatus.WAIT) {
                Surface(
                    color = TerminalSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, WarningOrange.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "SETUP IN PROGRESS - WAIT FOR CONFIRMATION",
                            color = WarningOrange,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = setup.reason,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                Surface(
                    color = BullGreenContainer,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BullGreen.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "HIGH PROBABILITY SETUP CONFIRMED",
                            color = BullGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = setup.reason,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Levels Grid
            Text(
                text = "EXECUTION LEVELS",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TerminalPriceCard(
                    label = "Entry",
                    value = setup.entryPrice.toString(),
                    accentColor = CyanAccent,
                    modifier = Modifier.weight(1f)
                )
                TerminalPriceCard(
                    label = "Stop Loss",
                    value = setup.stopLoss.toString(),
                    accentColor = BearRed,
                    subValue = "${setup.stopDistancePips} pips",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TerminalPriceCard(
                    label = "Take Profit 1",
                    value = setup.takeProfit1.toString(),
                    accentColor = BullGreen,
                    subValue = "1:${setup.rrTp1} RR (+${setup.tp1DistancePips}p)",
                    modifier = Modifier.weight(1f)
                )
                TerminalPriceCard(
                    label = "Take Profit 2",
                    value = setup.takeProfit2.toString(),
                    accentColor = BullGreen,
                    subValue = "1:${setup.rrTp2} RR (+${setup.tp2DistancePips}p)",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Risk & Position Sizing Metrics
            Text(
                text = "RISK & POSITION SIZING",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricBox(
                    label = "Position Size",
                    value = "${setup.suggestedLotSize} Lots",
                    color = GoldAccent,
                    modifier = Modifier.weight(1f)
                )
                MetricBox(
                    label = "Money at Risk",
                    value = "$${setup.riskAmount}",
                    color = BearRed,
                    modifier = Modifier.weight(1f)
                )
                MetricBox(
                    label = "Profit at TP1",
                    value = "+$${setup.potentialProfitTp1}",
                    color = BullGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Strategy Checklist Accordion / Items
            if (setup.checklist.isNotEmpty()) {
                Text(
                    text = "STRATEGY CHECKLIST AUDIT",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    setup.checklist.forEach { item ->
                        ChecklistItemRow(
                            title = item.name,
                            status = item.status,
                            note = item.description
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Button(
                onClick = onSaveToJournal,
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = TerminalBg),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("save_to_journal_button")
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkAdd,
                    contentDescription = "Save",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Setup to Trading Journal",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
