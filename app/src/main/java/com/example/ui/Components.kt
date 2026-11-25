package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.preferences.MapAppPreference
import com.example.navigation.InstalledMapApps
import com.example.ui.theme.DeepNavyCanvas
import com.example.ui.theme.DeepNavyOutline
import com.example.ui.theme.DeepNavySurface
import com.example.ui.theme.DeepNavySurfaceHigh
import com.example.ui.theme.DeepNavySurfaceHighest
import com.example.ui.theme.IceWhite
import com.example.ui.theme.OnSignalYellow
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SignalGreen
import com.example.ui.theme.SignalYellow

val QuickFloorPresets = listOf("지하 1층", "지하 2층", "지하 3층", "지하 4층", "1층", "2층", "옥상")
val QuickZonePresets = listOf("A구역", "B구역", "C구역", "D구역", "E구역", "엘리베이터 앞")

fun Modifier.tacticalNavyGridBackground(): Modifier = this.drawBehind {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0A1B36),
                DeepNavyCanvas,
                Color(0xFF050F1F)
            )
        )
    )
    // Subtle precision architectural parking grid for depth without reducing outdoor contrast
    val gridStep = 40.dp.toPx()
    val lineColor = Color(0x14FFFFFF)
    var x = 0f
    while (x <= size.width) {
        drawLine(
            color = lineColor,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1f
        )
        x += gridStep
    }
    var y = 0f
    while (y <= size.height) {
        drawLine(
            color = lineColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1f
        )
        y += gridStep
    }
}

@Composable
fun HighContrastYellowButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    testTag: String
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SignalYellow,
            contentColor = OnSignalYellow,
            disabledContainerColor = SignalYellow.copy(alpha = 0.35f),
            disabledContentColor = OnSignalYellow.copy(alpha = 0.6f)
        ),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(30.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = OnSignalYellow.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun HighContrastWhiteButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PureWhite,
            contentColor = DeepNavyCanvas,
            disabledContainerColor = PureWhite.copy(alpha = 0.4f),
            disabledContentColor = DeepNavyCanvas.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 1.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun HighContrastOutlinedButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color = PureWhite,
    contentColor: Color = PureWhite,
    testTag: String
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 56.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = DeepNavySurface,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun QuickPickChipRow(
    items: List<String>,
    currentText: String,
    onChipClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { label ->
            val selected = currentText.contains(label)
            Surface(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onChipClick(label) }
                    .testTag("quick_chip_$label"),
                shape = RoundedCornerShape(12.dp),
                color = if (selected) SignalYellow else DeepNavySurfaceHighest,
                border = BorderStroke(
                    width = 1.5.dp,
                    color = if (selected) SignalYellow else DeepNavyOutline
                )
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = if (selected) OnSignalYellow else PureWhite
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun MapAppSelectorRow(
    selected: MapAppPreference,
    installedApps: InstalledMapApps,
    onSelect: (MapAppPreference) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.label_map_app_selector),
            style = MaterialTheme.typography.labelMedium.copy(color = IceWhite),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val options = listOf(
                Triple(MapAppPreference.AUTO, stringResource(R.string.map_app_auto), null),
                Triple(MapAppPreference.KAKAO, stringResource(R.string.map_app_kakao), installedApps.kakaoInstalled),
                Triple(MapAppPreference.NAVER, stringResource(R.string.map_app_naver), installedApps.naverInstalled),
                Triple(MapAppPreference.GOOGLE, stringResource(R.string.map_app_google), installedApps.googleInstalled)
            )
            options.forEach { (pref, label, installed) ->
                val isSelected = pref == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) PureWhite else DeepNavySurfaceHigh)
                        .border(
                            width = if (isSelected) 2.dp else 1.5.dp,
                            color = if (isSelected) SignalYellow else DeepNavyOutline,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onSelect(pref) }
                        .testTag("map_app_option_${pref.key}")
                        .padding(horizontal = 6.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (installed == true) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color(0xFF0A8F5B) else SignalGreen)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                        }
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = if (isSelected) DeepNavyCanvas else PureWhite
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
