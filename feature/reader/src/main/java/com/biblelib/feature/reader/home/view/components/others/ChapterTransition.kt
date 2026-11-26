package com.biblelib.feature.reader.home.view.components.others

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// Hint shown at a chapter edge, naming the neighbouring chapter; keeps its space when hidden
@Composable
fun ChapterTransition(
    label: String,
    icon: ImageVector,
    iconAbove: Boolean,
    visible: Boolean = true,
) {
    val alpha by animateFloatAsState(if (visible) 1f else 0f, label = "chapter_edge_visibility")
    val infiniteTransition = rememberInfiniteTransition(label = "chapter_edge_bounce")
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "chapter_edge_bounce_value",
    )
    val bounceOffset = 4.dp * (if (iconAbove) -bounce else bounce)
    val hintColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .alpha(alpha)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = hintColor,
            modifier = Modifier.offset(y = bounceOffset),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = hintColor,
            textAlign = TextAlign.Center,
        )
    }
}
