package com.example.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BearRed
import com.example.ui.theme.BullGreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalCard
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    val apiKey by viewModel.apiKey.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val balanceStr by viewModel.balanceStr.collectAsState()
    val riskPctStr by viewModel.riskPctStr.collectAsState()
    val currencyStr by viewModel.currencyStr.collectAsState()
    val defaultSlStr by viewModel.defaultSlStr.collectAsState()
    val isTesting by viewModel.isTestingConnection.collectAsState()

    var isKeyVisible by remember { mutableStateOf(false) }

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Column {
                Text(
                    text = "System Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Text(
                    text = "OpenRouter AI integration & risk profile",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CyanAccent
                )
            }

            // OpenRouter API Configuration Card
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, TerminalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "Key",
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "OPENROUTER API KEY",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Used exclusively for AI strategy analysis & vision scans. Never exposed or logged.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { viewModel.apiKey.value = it },
                        placeholder = { Text("sk-or-v1-...") },
                        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                Icon(
                                    imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle visibility",
                                    tint = TextSecondary
                                )
                            }
                        },
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
                            .fillMaxWidth()
                            .testTag("openrouter_api_key_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.saveApiKey() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = TerminalBg),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("save_api_key_button")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Key", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.testConnection() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent),
                            border = BorderStroke(1.dp, GoldAccent),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isTesting,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("test_connection_button")
                        ) {
                            if (isTesting) {
                                CircularProgressIndicator(color = GoldAccent, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.NetworkCheck, contentDescription = "Test", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Test Connection")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = TerminalCard,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "How to obtain an OpenRouter Key:",
                                color = GoldAccent,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "1. Visit openrouter.ai and sign in.\n2. Go to Keys -> 'Create Key'.\n3. Copy and paste it here or set OPENROUTER_API_KEY in the Secrets panel.",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // AI Model Selection Card
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, TerminalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AI MODEL SELECTION",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Select your preferred model on OpenRouter:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val models = listOf(
                        Pair("google/gemini-2.0-flash-001", "Gemini 2.0 Flash (Fast & Vision)"),
                        Pair("anthropic/claude-3.5-sonnet", "Claude 3.5 Sonnet (Advanced Reasoning)"),
                        Pair("deepseek/deepseek-chat", "DeepSeek Chat (Cost Effective)"),
                        Pair("meta-llama/llama-3.3-70b-instruct", "Llama 3.3 70B")
                    )

                    models.forEach { (modelId, displayName) ->
                        val isSelected = selectedModel == modelId
                        Surface(
                            color = if (isSelected) CyanAccent.copy(alpha = 0.15f) else TerminalCard,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (isSelected) CyanAccent else TerminalBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.saveModel(modelId) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = displayName,
                                        color = if (isSelected) CyanAccent else TextPrimary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = modelId,
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = CyanAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Default Risk Settings Card
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalSurface),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, TerminalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DEFAULT TRADING ACCOUNT PROFILE",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = balanceStr,
                            onValueChange = { viewModel.balanceStr.value = it },
                            label = { Text("Default Balance") },
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
                            value = riskPctStr,
                            onValueChange = { viewModel.riskPctStr.value = it },
                            label = { Text("Default Risk %") },
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
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = currencyStr,
                            onValueChange = { viewModel.currencyStr.value = it },
                            label = { Text("Currency") },
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
                            value = defaultSlStr,
                            onValueChange = { viewModel.defaultSlStr.value = it },
                            label = { Text("Target SL Pips") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldAccent,
                                unfocusedBorderColor = TerminalBorder,
                                focusedContainerColor = TerminalCard,
                                unfocusedContainerColor = TerminalCard,
                                focusedTextColor = GoldAccent,
                                unfocusedTextColor = GoldAccent
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.saveRiskParameters() },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = TerminalBg),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Account Profile", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Legal & Reliability Disclaimer
            Surface(
                color = TerminalCard,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, TerminalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Safety",
                        tint = BearRed,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Safety & Decision-Support Disclaimer",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "This application is strictly a decision-support and strategy validation tool. It does not place real trades on any brokerage or financial exchange. Market trading carries substantial risk of loss. Always trade with money you can afford to lose and strictly honor your stop loss.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
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
