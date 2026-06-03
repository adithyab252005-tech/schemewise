package com.schemewise.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.schemewise.app.ui.theme.Muted

/** Generic empty-state widget used across Saved, Results, Updates screens */
@Composable
fun EmptyState(
    icon:     ImageVector,
    title:    String,
    subtitle: String    = "",
    modifier: Modifier  = Modifier,
    action:   @Composable (() -> Unit)? = null,
) {
    Column(
        modifier       = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = Muted.copy(alpha = 0.5f),
            modifier           = Modifier.size(56.dp),
        )
        Text(
            text       = title,
            style      = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color      = Muted,
            textAlign  = TextAlign.Center,
        )
        if (subtitle.isNotBlank()) {
            Text(
                text      = subtitle,
                style     = MaterialTheme.typography.bodySmall,
                color     = Muted.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
        action?.invoke()
    }
}
