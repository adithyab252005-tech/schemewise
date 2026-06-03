package com.schemewise.app.ui.components

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.schemewise.app.ui.theme.Brand500
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

data class TrackerStep(val id: Int, val label: String)

val TRACKER_STEPS = listOf(
    TrackerStep(1, "Documents Gathered"),
    TrackerStep(2, "Application Submitted"),
    TrackerStep(3, "Under Official Review"),
    TrackerStep(4, "Application Approved"),
    TrackerStep(5, "Funds Disbursed")
)

@Composable
fun ApplicationTracker(schemeId: String) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("schemewise_tracker", Context.MODE_PRIVATE) }
    val moshi = remember { Moshi.Builder().build() }
    val type = Types.newParameterizedType(List::class.java, Integer::class.javaObjectType)
    val adapter = remember { moshi.adapter<List<Int>>(type) }

    val prefKey = "tracker_$schemeId"
    
    var completedSteps by remember {
        mutableStateOf<List<Int>>(
            try {
                prefs.getString(prefKey, null)?.let { adapter.fromJson(it) } ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        )
    }

    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
        HorizontalDivider(color = Color(0xFFF1F5F9))
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("MY PROGRESS CHECKLIST", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), letterSpacing = 1.sp)
            Surface(shape = RoundedCornerShape(4.dp), color = Brand500.copy(alpha = 0.1f)) {
                Text("${completedSteps.size}/${TRACKER_STEPS.size}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Brand500, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            TRACKER_STEPS.forEach { step ->
                val isCompleted = completedSteps.contains(step.id)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val newSteps = if (isCompleted) {
                                completedSteps.filter { it != step.id }
                            } else {
                                completedSteps + step.id
                            }
                            completedSteps = newSteps
                            prefs.edit().putString(prefKey, adapter.toJson(newSteps)).apply()
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isCompleted) Brand500 else Color(0xFFCBD5E1),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = step.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isCompleted) Color(0xFF94A3B8) else Color(0xFF334155),
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                    )
                }
            }
        }
    }
}
