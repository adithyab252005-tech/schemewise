package com.schemewise.app.ui.screens.saved

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.schemewise.app.ui.components.*
import com.schemewise.app.ui.theme.*

@Composable
fun SavedScreen(
    onSchemeClick:  (String) -> Unit,
    onSettingsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    viewModel: SavedViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(topBar = { SchemeWiseTopBar(
        title = "My Saved Schemes",
        onSettingsClick = onSettingsClick,
        onProfileClick = onProfileClick
    ) }) { padding ->
        when {
            state.isLoading -> LazyColumn(
                modifier       = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) { items(4) { SchemeCardSkeleton() } }

            state.schemes.isEmpty() -> Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                EmptyState(
                    icon     = Icons.Filled.Bookmark,
                    title    = "No saved schemes yet",
                    subtitle = "Browse the Explore tab and bookmark schemes you're interested in.",
                )
            }

            else -> LazyColumn(
                modifier            = Modifier.padding(padding).fillMaxSize(),
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text("${state.schemes.size} Saved Schemes",
                        fontWeight = FontWeight.SemiBold, color = Muted,
                        style = MaterialTheme.typography.labelLarge)
                }
                items(state.schemes, key = { it.schemeId }) { scheme ->
                    SchemeCard(
                        scheme       = scheme,
                        isSaved      = true,
                        onCardClick  = { onSchemeClick(scheme.schemeId.toString()) },
                        onSaveClick  = { viewModel.remove(scheme.schemeId.toString()) },
                    )
                }
            }
        }
    }
}
