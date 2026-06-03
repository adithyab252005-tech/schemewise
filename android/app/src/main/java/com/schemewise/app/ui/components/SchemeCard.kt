package com.schemewise.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.schemewise.app.data.model.Scheme
import com.schemewise.app.ui.theme.*

fun getCategoryIcon(cat: String): String = when {
    cat.contains("Agriculture", true) -> "🌱"
    cat.contains("Business", true)    -> "💼"
    cat.contains("Education", true)   -> "🎓"
    cat.contains("Health", true)      -> "🏥"
    cat.contains("Housing", true)     -> "🏠"
    cat.contains("Social", true)      -> "🤝"
    cat.contains("Science", true)     -> "🔬"
    cat.contains("Sports", true)      -> "🏅"
    cat.contains("Women", true)       -> "👩"
    cat.contains("Skills", true)      -> "⚡"
    else                              -> "📄"
}

/**
 * Premium Scheme Card — features:
 *  • Category-colored left gradient accent bar
 *  • Press-scale micro-interaction via graphicsLayer
 *  • Financial value badge
 *  • Score percentage fill bar
 *  • Animated bookmark heart-beat on save
 */
@Composable
fun SchemeCard(
    scheme:           Scheme,
    isSaved:          Boolean  = false,
    eligibilityScore: Int?     = null,
    onCardClick:      () -> Unit,
    onSaveClick:      (() -> Unit)? = null,
    modifier:         Modifier  = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue  = if (isPressed) 0.975f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label        = "cardScale",
    )

    // Bookmark bounce animation
    val bookmarkScale = remember { Animatable(1f) }
    val wasSaved by rememberUpdatedState(isSaved)

    // Determine category for colour
    val rawCat = scheme.schemeCategory
        ?.replace("[\\[\\]\"]".toRegex(), "")
        ?.split(",")?.firstOrNull()?.trim() ?: ""
    val catGradient = categoryGradient(rawCat)
    val catColor = catGradient.first()

    val scopeType = if (scheme.stateApplicable.equals("ALL", ignoreCase = true)) "Central" else "State"

    Card(
        modifier  = modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(if (isPressed) 2.dp else 4.dp, RoundedCornerShape(18.dp), ambientColor = catColor.copy(0.1f)),
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(0.dp),
        onClick   = onCardClick,
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {

            // ── Left category accent bar ──────────────────────────
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(Brush.verticalGradient(catGradient))
            )

            Column(modifier = Modifier.padding(14.dp).weight(1f)) {

                // Top row: scope badge + score badge + bookmark
                Row(
                    modifier       = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Scope pill
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Surface3,
                    ) {
                        Text(
                            scopeType,
                            fontSize      = 9.sp,
                            fontWeight    = FontWeight.Bold,
                            color         = Muted,
                            modifier      = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            letterSpacing = 0.4.sp,
                        )
                    }

                    // Eligibility badge
                    if (eligibilityScore != null) {
                        val (badgeBg, badgeFg) = when {
                            eligibilityScore >= 80 -> BadgeGreenBg  to BadgeGreenText
                            eligibilityScore >= 50 -> BadgeAmberBg  to BadgeAmberText
                            else                   -> BadgeRedBg    to BadgeRedText
                        }
                        val badgeLabel = when {
                            eligibilityScore >= 80 -> "✓ Eligible"
                            eligibilityScore >= 50 -> "~ Partial"
                            else                   -> "✗ Low match"
                        }
                        Surface(shape = RoundedCornerShape(6.dp), color = badgeBg) {
                            Text(
                                badgeLabel,
                                fontSize      = 9.sp,
                                fontWeight    = FontWeight.Black,
                                color         = badgeFg,
                                modifier      = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                letterSpacing = 0.4.sp,
                            )
                        }
                    } else {
                        scheme.status?.let { StatusBadge(status = it) }
                    }

                    Spacer(Modifier.weight(1f))

                    // Bookmark
                    if (onSaveClick != null) {
                        val bookmarkColor = if (isSaved) Brand500 else MutedLight
                        IconButton(
                            onClick = {
                                onSaveClick()
                            },
                            modifier = Modifier
                                .size(30.dp)
                                .graphicsLayer { scaleX = bookmarkScale.value; scaleY = bookmarkScale.value }
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = if (isSaved) "Unsave" else "Save",
                                tint        = bookmarkColor,
                                modifier    = Modifier.size(18.dp),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Scheme name
                Text(
                    text       = scheme.schemeName,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 14.sp,
                    color      = OnSurface,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                    lineHeight = 19.sp,
                )

                // Ministry
                scheme.ministry?.let {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text     = it,
                        fontSize = 11.sp,
                        color    = Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Description
                scheme.description?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text     = it,
                        fontSize = 12.sp,
                        color    = Muted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp,
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Bottom row: category + financial value + arrow
                Row(
                    modifier      = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Category pill with emoji
                        if (rawCat.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(99.dp),
                                color = catColor.copy(0.10f),
                            ) {
                                Text(
                                    "${getCategoryIcon(rawCat)} $rawCat",
                                    fontSize  = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color     = catColor,
                                    modifier  = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                )
                            }
                        }

                        // Financial value pill (uses income_max as a proxy)
                        val incomeMax = scheme.incomeMax
                        if (incomeMax != null && incomeMax > 0) {
                            Surface(shape = RoundedCornerShape(99.dp), color = BadgeGreenBg) {
                                Text(
                                    "≤ ₹${formatAmount(incomeMax.toInt())}",
                                    fontSize  = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color     = BadgeGreenText,
                                    modifier  = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                )
                            }
                        }
                    }

                    // Score percentage bar
                    if (eligibilityScore != null) {
                        val barColor = when {
                            eligibilityScore >= 80 -> Success500
                            eligibilityScore >= 50 -> WarningAmber
                            else                   -> ErrorRed
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "$eligibilityScore%",
                                fontSize   = 11.sp,
                                fontWeight = FontWeight.Black,
                                color      = barColor,
                            )
                            Spacer(Modifier.height(3.dp))
                            Box(
                                modifier = Modifier.width(56.dp).height(4.dp).clip(CircleShape).background(Border)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(eligibilityScore / 100f)
                                        .clip(CircleShape)
                                        .background(barColor)
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(Surface3),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.ChevronRight, null,
                                tint     = MutedLight,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatAmount(amount: Int): String = when {
    amount >= 100_000 -> "${amount / 100_000}L"
    amount >= 1_000   -> "${amount / 1_000}K"
    else              -> "$amount"
}

@Composable
fun SchemeCardSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ShimmerBox(modifier = Modifier.width(60.dp), height = 20.dp, radius = 6.dp)
                ShimmerBox(modifier = Modifier.width(40.dp), height = 20.dp, radius = 6.dp)
            }
            Spacer(Modifier.height(14.dp))
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.8f), height = 18.dp)
            Spacer(Modifier.height(6.dp))
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.5f), height = 14.dp)
            Spacer(Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ShimmerBox(modifier = Modifier.width(80.dp), height = 20.dp, radius = 99.dp)
                ShimmerBox(modifier = Modifier.width(30.dp), height = 20.dp, radius = 6.dp)
            }
        }
    }
}
