package com.biblelib.feature.bibles.view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.biblelib.core.database.entities.BibleEntity

private val PillBadgeOrange = Color(0xFFE1550F)
private val PillMutedOrange = Color(0xFFB05A2E)
private val PillDarkBrown = Color(0xFF3A1300)
private val PillOnDark = Color(0xFFFFFFFF)
private val PillShape = RoundedCornerShape(18.dp)

@Composable
fun BiblePill(
    bible: BibleEntity,
    progress: Float,
) {
    val badgeSize = 60.dp
    val isActivelyDownloading = !bible.isDownloaded && !bible.downloadFailed
    val borderColor = if (bible.downloadFailed) MaterialTheme.colorScheme.error else PillBadgeOrange

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(PillShape)
            .border(BorderStroke(1.5.dp, borderColor), PillShape)
    ) {
        val totalWidthPx = constraints.maxWidth.toFloat()
        val fillFraction = when {
            bible.downloadFailed -> 0f
            bible.isDownloaded -> 0f
            else -> progress
        }

        Box(
            Modifier
                .matchParentSize()
                .background(PillDarkBrown)
        )

        if (fillFraction > 0f && totalWidthPx > 0f) {
            val fillWidthFraction = fillFraction.coerceIn(0.08f, 1f)

            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fillWidthFraction)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(PillMutedOrange, PillMutedOrange, PillDarkBrown)
                        )
                    )
            ) {
                ShimmerSweep()
            }
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(badgeSize),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    bible.downloadFailed -> Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.error),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = "Download failed",
                            tint = PillOnDark,
                        )
                    }

                    isActivelyDownloading -> {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxSize(),
                            color = PillOnDark,
                            trackColor = PillOnDark.copy(alpha = 0.25f),
                            strokeWidth = 3.dp,
                        )
                        Box(
                            modifier = Modifier
                                .padding(7.dp)
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(PillBadgeOrange),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = PillOnDark,
                            )
                        }
                    }

                    else -> Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                            .background(PillBadgeOrange),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = bible.abbreviation.uppercase().take(3),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PillOnDark,
                        )
                    }
                }
            }

            Spacer(Modifier.width(4.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = bible.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PillOnDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${bible.abbreviation.uppercase()} BIBLE",
                        style = MaterialTheme.typography.labelMedium,
                        color = PillOnDark.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val statusText = when {
                        bible.downloadFailed -> "Download failed"
                        !bible.isDownloaded -> "Fetching verses"
                        else -> null
                    }
                    if (statusText != null) {
                        Text(
                            text = " · $statusText",
                            style = MaterialTheme.typography.labelMedium,
                            color = PillOnDark.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    } else {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Downloaded",
                            tint = PillOnDark.copy(alpha = 0.85f),
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .size(14.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))
        }
    }
}
