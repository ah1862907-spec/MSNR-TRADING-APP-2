package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.analyzer.AnalyzerScreen
import com.example.ui.analyzer.AnalyzerViewModel
import com.example.ui.calculator.CalculatorScreen
import com.example.ui.calculator.CalculatorViewModel
import com.example.ui.journal.JournalScreen
import com.example.ui.journal.JournalViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.strategy.StrategyKnowledgeScreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

sealed class ScreenTab(val title: String, val icon: ImageVector, val tag: String) {
    object Analyzer : ScreenTab("Analyzer", Icons.Default.QueryStats, "tab_analyzer")
    object Calculator : ScreenTab("Calculator", Icons.Default.Calculate, "tab_calculator")
    object Journal : ScreenTab("Journal", Icons.AutoMirrored.Filled.MenuBook, "tab_journal")
    object Strategy : ScreenTab("Strategy", Icons.Default.CandlestickChart, "tab_strategy")
    object Settings : ScreenTab("Settings", Icons.Default.Settings, "tab_settings")
}

@Composable
fun MainScreen() {
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }

    val tabs = listOf(
        ScreenTab.Analyzer,
        ScreenTab.Calculator,
        ScreenTab.Journal,
        ScreenTab.Strategy,
        ScreenTab.Settings
    )

    // ViewModels with ViewModelStore
    val analyzerViewModel: AnalyzerViewModel = viewModel()
    val calculatorViewModel: CalculatorViewModel = viewModel()
    val journalViewModel: JournalViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBg),
        containerColor = TerminalBg,
        bottomBar = {
            NavigationBar(
                containerColor = TerminalSurface,
                contentColor = TextPrimary,
                tonalElevation = androidx.compose.ui.unit.Dp(0f)
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedIndex = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 10.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TerminalBg,
                            selectedTextColor = CyanAccent,
                            indicatorColor = CyanAccent,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        val screenModifier = Modifier.padding(innerPadding)

        when (selectedIndex) {
            0 -> AnalyzerScreen(viewModel = analyzerViewModel, modifier = screenModifier)
            1 -> CalculatorScreen(viewModel = calculatorViewModel, modifier = screenModifier)
            2 -> JournalScreen(viewModel = journalViewModel, modifier = screenModifier)
            3 -> StrategyKnowledgeScreen(modifier = screenModifier)
            4 -> SettingsScreen(viewModel = settingsViewModel, modifier = screenModifier)
        }
    }
}
