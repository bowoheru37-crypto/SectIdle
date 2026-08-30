package com.sect.idle.ui.components

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
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DomainDisabled
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CinnabarRed
import com.example.ui.theme.CultivationTextMuted
import com.example.ui.theme.CultivationTextPrimary
import com.example.ui.theme.CultivationTextSecondary
import com.example.ui.theme.GoldCelestial
import com.example.ui.theme.SectDarkSurface
import com.example.ui.theme.SectDarkSurfaceCard

/**
 * Types of critical actions requiring user confirmation.
 */
enum class CriticalActionType {
    DISMISS_DISCIPLE,
    DEMOLISH_BUILDING,
    EXPENSIVE_PURCHASE,
    GENERIC_WARNING
}

/**
 * Reusable AlertDialog component for critical operations such as:
 * - Dismissing disciples from the sect
 * - Demolishing/Dismantling pavilions and arrays
 * - High-risk irreversible sect decisions
 *
 * Adheres strictly to Material 3 accessibility, touch target constraints,
 * and high-contrast cultivation theme aesthetics.
 */
@Composable
fun CriticalActionDialog(
    visible: Boolean,
    title: String,
    description: String,
    actionType: CriticalActionType = CriticalActionType.GENERIC_WARNING,
    subtitle: String? = null,
    targetName: String? = null,
    targetDetail: String? = null,
    confirmButtonText: String = "Confirm",
    cancelButtonText: String = "Cancel",
    customIcon: ImageVector? = null,
    customIconTint: Color? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visible) return

    val defaultIcon: ImageVector = when (actionType) {
        CriticalActionType.DISMISS_DISCIPLE -> Icons.Default.DeleteForever
        CriticalActionType.DEMOLISH_BUILDING -> Icons.Default.DomainDisabled
        CriticalActionType.EXPENSIVE_PURCHASE -> Icons.Default.Warning
        CriticalActionType.GENERIC_WARNING -> Icons.Default.Warning
    }

    val icon = customIcon ?: defaultIcon
    val iconTint = customIconTint ?: when (actionType) {
        CriticalActionType.DISMISS_DISCIPLE -> CinnabarRed
        CriticalActionType.DEMOLISH_BUILDING -> Color(0xFFFF9800)
        CriticalActionType.EXPENSIVE_PURCHASE -> GoldCelestial
        CriticalActionType.GENERIC_WARNING -> CinnabarRed
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
            .testTag("critical_action_dialog")
            .border(1.5.dp, iconTint.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
        containerColor = SectDarkSurface,
        shape = RoundedCornerShape(20.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f))
                    .border(1.dp, iconTint.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Critical Action Warning",
                    tint = iconTint,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = CultivationTextPrimary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                )
                if (subtitle != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = iconTint,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = CultivationTextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                )

                // Optional target entity highlight card
                if (targetName != null) {
                    Spacer(Modifier.height(14.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SectDarkSurfaceCard),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, iconTint.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = targetName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = CultivationTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            if (targetDetail != null) {
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = targetDetail,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = CultivationTextMuted
                                    )
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = iconTint,
                    contentColor = if (iconTint == GoldCelestial) Color.Black else Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("confirm_dialog_confirm_button")
            ) {
                Text(
                    text = confirmButtonText,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = CultivationTextSecondary
                ),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("confirm_dialog_cancel_button")
            ) {
                Text(
                    text = cancelButtonText,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    )
}
