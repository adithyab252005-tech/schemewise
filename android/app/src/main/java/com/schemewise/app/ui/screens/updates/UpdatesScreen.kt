package com.schemewise.app.ui.screens.updates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.schemewise.app.ui.theme.*

import androidx.hilt.navigation.compose.hiltViewModel

data class SchemeUpdate(
    val title: String,
    val type:  String, // New, Enhanced, Depreciated
    val date:  String,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatesScreen(
    onBack: () -> Unit,
    viewModel: UpdatesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Updates", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Predictive Alerts Feed", fontSize = 20.sp, fontWeight = FontWeight.Black, color = OnSurface)
                Text("AI-analyzed alerts for upcoming and changed schemes.", color = Muted, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
            }
            
            if (state.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Brand500)
                    }
                }
            } else if (state.error != null) {
                item {
                    Text("Failed to load updates: ${state.error}", color = Color.Red, fontSize = 14.sp)
                }
            } else {
                items(state.updates) { update ->
                    val su = SchemeUpdate(
                        title = update["title"] ?: "Unknown Update",
                        type = update["tag"] ?: "Info",
                        date = update["date"] ?: "Recently",
                        description = update["description"] ?: ""
                    )
                    UpdateCard(su)
                }
            }
        }
    }
}

@Composable
fun UpdateCard(update: SchemeUpdate) {
    val badgeColor = when(update.type) {
        "New" -> Color(0xFF22C55E) // Success
        "Enhanced" -> Color(0xFF3B82F6) // Blue
        else -> Color(0xFFF43F5E) // Red
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = badgeColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = update.type.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
                Text(update.date, fontSize = 11.sp, color = Muted)
            }
            
            Spacer(Modifier.height(12.dp))
            Text(update.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OnSurface)
            Spacer(Modifier.height(4.dp))
            Text(update.description, fontSize = 13.sp, color = Color(0xFF64748B), lineHeight = 18.sp)
            
            Spacer(Modifier.height(12.dp))
            TextButton(
                onClick = {},
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("View Details →", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Brand500)
            }
        }
    }
}
