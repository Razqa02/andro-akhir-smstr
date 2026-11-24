package com.example.tiltball.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tiltball.ui.theme.Surface
import com.example.tiltball.ui.theme.SurfaceBorder
import com.example.tiltball.ui.theme.Warning

/** Kartu hasil level dengan label dan nilai yang rapi */
@Composable
fun ResultCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (highlight) Warning.copy(alpha = 0.12f) else Surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (highlight) Warning.copy(alpha = 0.4f) else SurfaceBorder
        ),
        shadowElevation = 2.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (highlight) Warning else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
