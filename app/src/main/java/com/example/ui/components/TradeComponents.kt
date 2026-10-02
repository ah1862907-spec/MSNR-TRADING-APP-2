package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChecklistStatus
import com.example.model.SetupStatus
import com.example.model.TradeDirection
import com.example.ui.theme.BearRed
import com.example.ui.theme.BearRedContainer
import com.example.ui.theme.BullGreen
import com.example.ui.theme.BullGreenContainer
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalCard
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningOrange

@Composable
fun DirectionBadge(
    direction: TradeDirection,
    modifier: Modifier = Modifier
) {
    val isBuy = direction == TradeDirection.BUY
    val bgColor = if (isBuy) BullGreenContainer else BearRedContainer
    val textColor = if (isBuy) BullGreen else BearRed
    val icon = if (isBuy) Icons.Default.TrendingUp else Icons.Default.TrendingDown

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = direction.name,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = direction.name,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun StatusBadge(status: SetupStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (status) {
        SetupStatus.VALID_SETUP -> Triple(BullGreenContainer, BullGreen, "VALID SETUP")
        SetupStatus.WAIT -> Triple(TerminalSurface, WarningOrange, "WAITING / DEVELOPING")
        SetupStatus.NO_TRADE -> Triple(BearRedContainer, BearRed, "NO TRADE")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.6f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Text(
                text = label,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun ChecklistItemRow(
    title: String,
    status: ChecklistStatus,
    note: String,
    modifier: Modifier = Modifier
) {
    val (icon, color) = when (status) {
        ChecklistStatus.PASS -> Pair(Icons.Default.CheckCircle, BullGreen)
        ChecklistStatus.FAIL -> Pair(Icons.Default.Warning, BearRed)
        ChecklistStatus.WAITING -> Pair(Icons.Default.HourglassTop, WarningOrange)
    }

    Surface(
        color = TerminalCard,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = status.name,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                if (note.isNotBlank()) {
                    Text(
                        text = note,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = status.name,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun TerminalPriceCard(
    label: String,
    value: String,
    accentColor: Color,
    subValue: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        color = TerminalCard,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = label.uppercase(),
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = accentColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            if (subValue != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subValue,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun MetricBox(
    label: String,
    value: String,
    color: Color = TextPrimary,
    modifier: Modifier = Modifier
) {
    Surface(
        color = TerminalCard,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = color,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
