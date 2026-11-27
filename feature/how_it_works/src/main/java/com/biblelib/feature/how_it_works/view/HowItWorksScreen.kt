package com.biblelib.feature.how_it_works.view

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.biblelib.core.ui.components.action.AppTopBar

data class HowItWorksSection(
    val icon: ImageVector,
    val title: String,
    val description: String
)

private val sections = listOf(
    HowItWorksSection(
        icon = Icons.Default.MenuBook,
        title = "Selecting Bibles",
        description = "When you first open BibleLib, you'll be presented with a list of available Bible " +
                "translations. Tap on any translation to select or deselect it. You can choose one or more " +
                "translations to download and read side by side. Once you're happy with your selection, tap " +
                "the confirm button to start reading. You can always add or remove translations later from " +
                "Manage Bibles in the menu."
    ),
    HowItWorksSection(
        icon = Icons.Default.Search,
        title = "Reading & Navigating",
        description = "Use the book selector to jump between the Old and New Testaments and pick any book " +
                "or chapter. Swipe left or right on a verse to bookmark it or add a note. Long press a verse " +
                "to select multiple verses at once, then choose a highlight colour or share your selection. " +
                "If you've enabled more than one translation, you can read them side by side in the same view."
    ),
    HowItWorksSection(
        icon = Icons.Default.Search,
        title = "Searching",
        description = "Tap the search icon to look up any word or phrase across your downloaded translations. " +
                "Results update as you type and show the verse in context. Tap any result to open it directly " +
                "in the reader at that verse."
    ),
    HowItWorksSection(
        icon = Icons.Default.Bookmarks,
        title = "Bookmarks & Notes",
        description = "Every verse you bookmark or add a note to is collected under Bookmarks & Notes. Tap " +
                "any entry to jump straight back to that verse in the reader, or open it to edit your note " +
                "and change its highlight colour."
    ),
    HowItWorksSection(
        icon = Icons.Default.LibraryBooks,
        title = "Scripture Lists",
        description = "The Scripture Opener lets you build a queue of specific books, chapters, and verses — " +
                "handy for sermon prep or study plans. Select multiple references using the floating queue, " +
                "then save them as a Scripture List so you can revisit or present the same set of passages " +
                "again later."
    ),
    HowItWorksSection(
        icon = Icons.Default.History,
        title = "Your History",
        description = "BibleLib keeps track of the verses and chapters you've read so you can pick up right " +
                "where you left off. Open Your History from the menu to see a timeline of your recent reading " +
                "and tap any entry to return to it."
    ),
)

@Composable
fun HowItWorksScreen(
    navController: NavHostController,
    showGoBack: Boolean = true,
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "How It Works",
                showGoBack = showGoBack,
                onNavIconClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Learn how to get the most out of BibleLib",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            sections.forEach { section ->
                HowItWorksCard(section = section)
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun HowItWorksCard(section: HowItWorksSection) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = section.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = section.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )
            }
        }
    }
}
