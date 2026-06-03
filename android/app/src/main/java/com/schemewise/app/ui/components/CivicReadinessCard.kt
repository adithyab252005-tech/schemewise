package com.schemewise.app.ui.components

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.schemewise.app.ui.theme.Brand50
import com.schemewise.app.ui.theme.Brand500
import com.schemewise.app.ui.theme.Brand600
import com.schemewise.app.ui.theme.Muted
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

data class DocItem(val id: String, val label: String, val required: Boolean)

@Composable
fun CivicReadinessCard(
    profile: com.schemewise.app.data.model.Profile?
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("schemewise_prefs", Context.MODE_PRIVATE) }
    val moshi = remember { Moshi.Builder().build() }
    val type = Types.newParameterizedType(Map::class.java, String::class.java, Boolean::class.javaObjectType)
    val adapter = remember { moshi.adapter<Map<String, Boolean>>(type) }

    val prefKey = "civic_readiness_${profile?.id ?: "guest"}"
    var checkedDocs by remember(profile?.id) {
        mutableStateOf<Map<String, Boolean>>(
            try {
                prefs.getString(prefKey, null)?.let { adapter.fromJson(it) } ?: emptyMap()
            } catch (e: Exception) {
                emptyMap()
            }
        )
    }

    val activeDocs = remember(profile) {
        val docs = mutableListOf<DocItem>()
        docs.add(DocItem("doc_aadhaar", "Aadhaar Card (Self-Attested)", true))
        docs.add(DocItem("doc_pan", "PAN Card", true))
        docs.add(DocItem("doc_bank", "Bank Passbook", true))
        docs.add(DocItem("doc_domicile", "Domicile / Resident Cert.", true))
        
        if (profile?.category != "General" && profile?.category != "UR") {
            docs.add(DocItem("doc_caste", "Caste / Category Cert.", true))
        }
        if ((profile?.income ?: 500000.0) < 250000.0) {
            docs.add(DocItem("doc_income", "Income Certificate", true))
        }
        if (profile?.isBPL == "Yes") {
            docs.add(DocItem("doc_ration", "BPL Ration Card", true))
        }
        if (profile?.isStudent == "Yes") {
            docs.add(DocItem("doc_student", "Student ID / Bonafide", true))
        }
        if (profile?.isDifferentlyAbled == "Yes") {
            docs.add(DocItem("doc_pwd", "Disability Certificate", true))
        }
        docs
    }

    val completedCount = activeDocs.count { checkedDocs[it.id] == true }
    val progressPercent = if (activeDocs.isNotEmpty()) (completedCount.toFloat() / activeDocs.size) else 0f

    val animateProgress by animateFloatAsState(
        targetValue = progressPercent,
        animationSpec = tween(durationMillis = 700),
        label = "progress"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp).padding(bottom = 8.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Civic Readiness Score", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), fontSize = 16.sp)
                    Text("Check off physical documents you possess to enable fast applications.", color = Muted, fontSize = 12.sp, lineHeight = 16.sp)
                }
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFFF1F5F9),
                        strokeWidth = 6.dp,
                    )
                    CircularProgressIndicator(
                        progress = { animateProgress },
                        modifier = Modifier.fillMaxSize(),
                        color = Brand500,
                        strokeWidth = 6.dp,
                        strokeCap = StrokeCap.Round
                    )
                    Text(
                        "${(progressPercent * 100).toInt()}%",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        fontSize = 14.sp
                    )
                }
            }

            // Checklist
            Column(modifier = Modifier.padding(12.dp)) {
                activeDocs.forEach { doc ->
                    val isChecked = checkedDocs[doc.id] == true
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val current = checkedDocs.toMutableMap()
                                current[doc.id] = !isChecked
                                checkedDocs = current
                                prefs.edit().putString(prefKey, adapter.toJson(current)).apply()
                            }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isChecked) Icons.Filled.CheckCircle else Icons.Default.CheckCircle, // using filled interchangeably for mock, actual logic relies on tint
                            contentDescription = null,
                            tint = if (isChecked) Brand500 else Color(0xFFCBD5E1),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            doc.label,
                            fontSize = 14.sp,
                            color = if (isChecked) Color(0xFF94A3B8) else Color(0xFF334155),
                            modifier = Modifier.weight(1f),
                            textDecoration = if (isChecked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                            fontWeight = FontWeight.Medium
                        )
                        if (doc.required && !isChecked) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFFF7ED)
                            ) {
                                Text(
                                    "REQUIRED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF97316),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            if (completedCount == activeDocs.size && activeDocs.isNotEmpty()) {
                Surface(color = Color(0xFFECFDF5), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("You are 100% prepared to apply!", color = Color(0xFF065F46), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
