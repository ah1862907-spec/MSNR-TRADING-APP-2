package com.example.ui.journal

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.TradeJournalEntity
import com.example.model.PerformanceStats
import com.example.ui.components.DirectionBadge
import com.example.ui.components.MetricBox
import com.example.ui.theme.BearRed
import com.example.ui.theme.BearRedContainer
import com.example.ui.theme.BullGreen
import com.example.ui.theme.BullGreenContainer
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
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.roundToInt

@Composable
fun JournalScreen(
    viewModel: JournalViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val trades by viewModel.filteredTrades.collectAsState()
    val allTrades by viewModel.allTrades.collectAsState()
    val stats by viewModel.stats.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterOutcome by viewModel.filterOutcome.collectAsState()

    var selectedTradeForEdit by remember { mutableStateOf<TradeJournalEntity?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var exportedJsonText by remember { mutableStateOf("") }
    var importJsonInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(TerminalBg)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Trading Journal",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Track, review & audit trade setups",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CyanAccent
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = {
                                exportedJsonText = viewModel.exportJournalToJson()
                                showExportDialog = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = "Export JSON",
                                tint = CyanAccent
                            )
                        }
                        IconButton(
                            onClick = {
                                importJsonInput = ""
                                showImportDialog = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Import JSON",
                                tint = GoldAccent
                            )
                        }
                    }
                }
            }

            // Statistics Hero Banner
            item {
                PerformanceStatsCard(stats = stats)
            }

            // Search Bar & Filter Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchQuery.value = it },
                        placeholder = { Text("Search by symbol, notes, or strategy...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = TerminalBorder,
                            focusedContainerColor = TerminalSurface,
                            unfocusedContainerColor = TerminalSurface,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("journal_search_input")
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("ALL", "WIN", "LOSS", "BREAKEVEN", "PENDING").forEach { out ->
                            val isSel = filterOutcome.equals(out, ignoreCase = true)
                            FilterChip(
                                selected = isSel,
                                onClick = { viewModel.filterOutcome.value = out },
                                label = {
                                    Text(
                                        text = out,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when (out) {
                                        "WIN" -> BullGreenContainer
                                        "LOSS" -> BearRedContainer
                                        "BREAKEVEN" -> GoldContainer
                                        else -> CyanAccent.copy(alpha = 0.2f)
                                    },
                                    selectedLabelColor = when (out) {
                                        "WIN" -> BullGreen
                                        "LOSS" -> BearRed
                                        "BREAKEVEN" -> GoldAccent
                                        else -> CyanAccent
                                    },
                                    containerColor = TerminalCard,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSel,
                                    borderColor = when (out) {
                                        "WIN" -> BullGreen
                                        "LOSS" -> BearRed
                                        "BREAKEVEN" -> GoldAccent
                                        else -> CyanAccent
                                    }
                                )
                            )
                        }
                    }
                }
            }

            // Trades List
            if (trades.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, TerminalBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Empty",
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (allTrades.isEmpty()) "No trades logged yet" else "No matching trades found",
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Run a scan in Trade Analyzer and tap 'Save Setup to Journal' to track your trades here.",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(trades, key = { it.id }) { trade ->
                    JournalItemCard(
                        trade = trade,
                        onClick = { selectedTradeForEdit = trade },
                        onDelete = { viewModel.deleteTrade(trade.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }

    // Trade Edit / Outcome Dialog
    selectedTradeForEdit?.let { trade ->
        TradeEditDialog(
            trade = trade,
            onDismiss = { selectedTradeForEdit = null },
            onSave = { outcome, exitPrice, pnl, notes ->
                viewModel.updateTradeOutcome(trade, outcome, exitPrice, pnl, notes)
                selectedTradeForEdit = null
            }
        )
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Trading Journal (JSON)", color = TextPrimary) },
            text = {
                Column {
                    Text(
                        text = "Copy this JSON to backup your trade history or transfer it to another device:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = exportedJsonText,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = TerminalBg,
                            unfocusedContainerColor = TerminalBg
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Journal JSON", exportedJsonText)
                        clipboard.setPrimaryClip(clip)
                        showExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = TerminalBg)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy to Clipboard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close", color = TextSecondary)
                }
            },
            containerColor = TerminalSurface
        )
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Trading Journal (JSON)", color = TextPrimary) },
            text = {
                Column {
                    Text(
                        text = "Paste a previously exported JSON backup below:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importJsonInput,
                        onValueChange = { importJsonInput = it },
                        placeholder = { Text("[{\"symbol\": \"XAUUSD\", ...}]") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = TerminalBg,
                            unfocusedContainerColor = TerminalBg
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importJsonInput.isNotBlank()) {
                            viewModel.importJournalFromJson(importJsonInput)
                        }
                        showImportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = TerminalBg)
                ) {
                    Text("Import Trades")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = TerminalSurface
        )
    }
}

@Composable
fun PerformanceStatsCard(stats: PerformanceStats, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TerminalSurface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, TerminalBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PERFORMANCE STATISTICS",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Streak: ${stats.currentStreak}",
                    color = GoldAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            // Main stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricBox(
                    label = "Win Rate",
                    value = "${(stats.winRate * 10.0).roundToInt() / 10.0}%",
                    color = if (stats.winRate >= 50.0) BullGreen else WarningOrange,
                    modifier = Modifier.weight(1f)
                )
                MetricBox(
                    label = "Net PnL",
                    value = if (stats.totalPnl >= 0) "+$${(stats.totalPnl * 100.0).roundToInt() / 100.0}" else "-$${(-stats.totalPnl * 100.0).roundToInt() / 100.0}",
                    color = if (stats.totalPnl >= 0) BullGreen else BearRed,
                    modifier = Modifier.weight(1f)
                )
                MetricBox(
                    label = "Avg RR",
                    value = "1:${(stats.averageRr * 10.0).roundToInt() / 10.0}",
                    color = CyanAccent,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricBox(
                    label = "Total Trades",
                    value = stats.totalTrades.toString(),
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricBox(
                    label = "W / L / BE",
                    value = "${stats.winningTrades} / ${stats.losingTrades} / ${stats.breakevenTrades}",
                    color = TextSecondary,
                    modifier = Modifier.weight(1f)
                )
                MetricBox(
                    label = "Profit Factor",
                    value = String.format(java.util.Locale.US, "%.2f", stats.profitFactor),
                    color = GoldAccent,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun JournalItemCard(
    trade: TradeJournalEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val outcomeColor = when (trade.outcome.uppercase()) {
        "WIN" -> BullGreen
        "LOSS" -> BearRed
        "BREAKEVEN" -> GoldAccent
        else -> CyanAccent
    }
    val outcomeBg = when (trade.outcome.uppercase()) {
        "WIN" -> BullGreenContainer
        "LOSS" -> BearRedContainer
        "BREAKEVEN" -> GoldContainer
        else -> TerminalSurfaceVariant
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = TerminalSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, TerminalBorder),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Symbol, Direction, Outcome, Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DirectionBadge(direction = if (trade.direction == "SELL") com.example.model.TradeDirection.SELL else com.example.model.TradeDirection.BUY)
                    Text(
                        text = trade.symbol,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Surface(
                    color = outcomeBg,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, outcomeColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = trade.outcome,
                        color = outcomeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sub info
            Text(
                text = "${trade.dateTimeFormatted} • ${trade.strategyName}",
                color = TextMuted,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Levels Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Entry", color = TextMuted, fontSize = 10.sp)
                    Text(trade.entryPrice.toString(), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                }
                Column {
                    Text("SL", color = TextMuted, fontSize = 10.sp)
                    Text(trade.stopLoss.toString(), color = BearRed, fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                }
                Column {
                    Text("TP1", color = TextMuted, fontSize = 10.sp)
                    Text(trade.takeProfit1.toString(), color = BullGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                }
                Column {
                    Text("Lot Size", color = TextMuted, fontSize = 10.sp)
                    Text("${trade.lotSize} L", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("PnL", color = TextMuted, fontSize = 10.sp)
                    val pnlText = if (trade.realizedPnl != null) {
                        if (trade.realizedPnl >= 0) "+$${trade.realizedPnl}" else "-$${-trade.realizedPnl}"
                    } else if (trade.outcome == "WIN") {
                        "+$${trade.riskAmount * 2.0}"
                    } else if (trade.outcome == "LOSS") {
                        "-$${trade.riskAmount}"
                    } else {
                        "Pending"
                    }
                    Text(pnlText, color = outcomeColor, fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                }
            }

            if (trade.psychologyNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = TerminalCard,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Notes: ${trade.psychologyNotes}",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp), tint = CyanAccent)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Update Outcome", color = CyanAccent, fontSize = 12.sp)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun TradeEditDialog(
    trade: TradeJournalEntity,
    onDismiss: () -> Unit,
    onSave: (outcome: String, exitPrice: Double?, pnl: Double?, notes: String) -> Unit
) {
    var outcome by remember { mutableStateOf(trade.outcome) }
    var exitPriceStr by remember { mutableStateOf(trade.actualExitPrice?.toString() ?: "") }
    var pnlStr by remember { mutableStateOf(trade.realizedPnl?.toString() ?: "") }
    var notes by remember { mutableStateOf(trade.psychologyNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Update Trade Outcome: ${trade.symbol}",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select Outcome:", color = TextSecondary, fontSize = 12.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("WIN", "LOSS", "BREAKEVEN", "PENDING").forEach { o ->
                        val isSel = outcome.equals(o, ignoreCase = true)
                        Surface(
                            color = if (isSel) {
                                when (o) {
                                    "WIN" -> BullGreenContainer
                                    "LOSS" -> BearRedContainer
                                    "BREAKEVEN" -> GoldContainer
                                    else -> CyanAccent.copy(alpha = 0.2f)
                                }
                            } else TerminalCard,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isSel) {
                                    when (o) {
                                        "WIN" -> BullGreen
                                        "LOSS" -> BearRed
                                        "BREAKEVEN" -> GoldAccent
                                        else -> CyanAccent
                                    }
                                } else TerminalBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    outcome = o
                                    if (o == "WIN" && pnlStr.isBlank()) {
                                        pnlStr = (trade.riskAmount * 2.0).toString()
                                    } else if (o == "LOSS" && pnlStr.isBlank()) {
                                        pnlStr = (-trade.riskAmount).toString()
                                    } else if (o == "BREAKEVEN") {
                                        pnlStr = "0.0"
                                    }
                                }
                        ) {
                            Text(
                                text = o,
                                color = if (isSel) TextPrimary else TextSecondary,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = exitPriceStr,
                    onValueChange = { exitPriceStr = it },
                    label = { Text("Actual Exit Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = TerminalCard,
                        unfocusedContainerColor = TerminalCard
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pnlStr,
                    onValueChange = { pnlStr = it },
                    label = { Text("Realized PnL ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = TerminalCard,
                        unfocusedContainerColor = TerminalCard
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Psychology & Discipline Notes") },
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = TerminalCard,
                        unfocusedContainerColor = TerminalCard
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val exit = exitPriceStr.toDoubleOrNull()
                    val pnl = pnlStr.toDoubleOrNull()
                    onSave(outcome, exit, pnl, notes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = TerminalBg)
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = TerminalSurface
    )
}
