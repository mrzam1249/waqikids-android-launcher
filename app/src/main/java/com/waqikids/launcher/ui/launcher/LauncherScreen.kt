package com.waqikids.launcher.ui.launcher

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import com.waqikids.launcher.domain.model.AppInfo
import com.waqikids.launcher.ui.browser.AllowedSitesActivity
import com.waqikids.launcher.ui.launcher.components.AnimatedCloud
import com.waqikids.launcher.ui.launcher.components.BrowseWebCard
import com.waqikids.launcher.ui.launcher.components.DhikrCard
import com.waqikids.launcher.ui.launcher.components.DidYouKnowCard
import com.waqikids.launcher.ui.theme.KidBlue
import com.waqikids.launcher.ui.theme.KidPink
import com.waqikids.launcher.ui.theme.KidPurple
import java.util.Calendar
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items

// Beautiful gradient colors
private val SkyGradient = listOf(
    Color(0xFFF0F9FF),  // Very light blue
    Color(0xFFFDF4FF),  // Very light purple
    Color(0xFFFFF7ED)   // Very light orange/cream
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LauncherScreen(
    viewModel: LauncherViewModel = hiltViewModel(),
    onNavigateToParentMode: () -> Unit = {}
) {
    val apps by viewModel.allowedApps.collectAsState(initial = emptyList())
    val childName by viewModel.childName.collectAsState(initial = "Child")
    val timeRemaining by viewModel.timeRemaining.collectAsState(initial = "2h 30m")
    val prayerTimes by viewModel.prayerTimes.collectAsState()
    val currentDhikr by viewModel.currentDhikr.collectAsState()
    val currentFact by viewModel.currentFact.collectAsState()
    val context = LocalContext.current
    
    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    
    val greeting = remember {
        when (hour) {
            in 0..5 -> "Good night"
            in 6..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }
    
    val greetingEmoji = remember {
        when (hour) {
            in 0..5 -> "🌙"
            in 6..11 -> "☀️"
            in 12..16 -> "🌤️"
            else -> "🌅"
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = SkyGradient))
    ) {
        // Floating decorative orbs
        FloatingOrb(
            color = KidPurple,
            size = 160.dp,
            blur = 80.dp,
            alpha = 0.25f,
            modifier = Modifier.offset(x = (-30).dp, y = (60 + floatOffset).dp)
        )
        FloatingOrb(
            color = KidBlue,
            size = 120.dp,
            blur = 60.dp,
            alpha = 0.2f,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = (120 - floatOffset * 0.8f).dp)
        )
        FloatingOrb(
            color = KidPink,
            size = 140.dp,
            blur = 70.dp,
            alpha = 0.2f,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 40.dp, y = (-80 + floatOffset * 0.5f).dp)
        )
        
        // Clouds
        AnimatedCloud(
            modifier = Modifier.offset(x = 20.dp, y = 60.dp).size(90.dp, 45.dp),
            duration = 12000
        )
        AnimatedCloud(
            modifier = Modifier.offset(x = 240.dp, y = 40.dp).size(70.dp, 35.dp),
            duration = 15000
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            
            // ══════════════════════════════════════
            // TOP BAR: Greeting + Settings button (scrolls together)
            // ══════════════════════════════════════
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Assalamu Alaykum $greetingEmoji",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = childName,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E293B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        WavingHand()
                    }
                }
                
                // Settings button - scrolls with greeting
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .shadow(12.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color(0xFF6366F1))
                        .clickable { onNavigateToParentMode() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Parent Settings",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // ══════════════════════════════════════
            // INFO CHIPS ROW - Kid-friendly labels
            // ══════════════════════════════════════
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Screen Time - Clear kid-friendly label
                InfoChip(
                    emoji = "⏰",
                    title = "Play Time Left",
                    value = timeRemaining,
                    gradientColors = listOf(Color(0xFF06B6D4), Color(0xFF0EA5E9)),
                    modifier = Modifier.weight(1f)
                )
                
                // Prayer Time - Clear kid-friendly label  
                InfoChip(
                    emoji = "🕌",
                    title = "${prayerTimes.nextPrayerName} Prayer",
                    value = "at ${prayerTimes.nextPrayer}",
                    gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFFA855F7)),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // ══════════════════════════════════════
            // BROWSE WEB CARD
            // ══════════════════════════════════════
            BrowseWebCard(
                onClick = { context.startActivity(AllowedSitesActivity.createIntent(context)) }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // ══════════════════════════════════════
            // MY APPS SECTION
            // ══════════════════════════════════════
            SectionHeader(emoji = "🚀", title = "My Apps")
            
            Spacer(modifier = Modifier.height(14.dp))
            
            if (apps.isEmpty()) {
                EmptyAppsCard()
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    lazyItems(apps, key = { it.packageName }) { app ->
                        AppBubble(
                            app = app,
                            onClick = { viewModel.launchApp(app.packageName) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // ══════════════════════════════════════
            // FUN FACT SECTION - With title
            // ══════════════════════════════════════
            SectionHeader(emoji = "💡", title = "Did You Know?")
            
            Spacer(modifier = Modifier.height(12.dp))
            
            FunFactCard(
                fact = currentFact,
                onRefresh = { viewModel.refreshFact() }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // ══════════════════════════════════════
            // DHIKR SECTION - With title
            // ══════════════════════════════════════
            SectionHeader(emoji = "📿", title = "Today's Dhikr")
            
            Spacer(modifier = Modifier.height(12.dp))
            
            DhikrCard(
                dhikr = currentDhikr,
                onTap = { viewModel.nextDhikr() }
            )
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════
// FLOATING ORB
// ═══════════════════════════════════════════════════════════
@Composable
private fun FloatingOrb(
    color: Color,
    size: androidx.compose.ui.unit.Dp,
    blur: androidx.compose.ui.unit.Dp,
    alpha: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .blur(blur)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = alpha), color.copy(alpha = 0f))
                ),
                shape = CircleShape
            )
    )
}

// ═══════════════════════════════════════════════════════════
// INFO CHIP - Kid-friendly with clear title
// ═══════════════════════════════════════════════════════════
@Composable
private fun InfoChip(
    emoji: String,
    title: String,
    value: String,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = gradientColors[0].copy(alpha = 0.3f)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.verticalGradient(colors = gradientColors))
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = emoji, fontSize = 32.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.95f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun WavingHand() {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )
    Text(
        text = "👋",
        fontSize = 24.sp,
        modifier = Modifier.graphicsLayer {
            rotationZ = rotation
            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.7f, 0.9f)
        }
    )
}

// ═══════════════════════════════════════════════════════════
// SECTION HEADER
// ═══════════════════════════════════════════════════════════
@Composable
private fun SectionHeader(emoji: String, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = emoji, fontSize = 28.sp)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1E293B)
        )
    }
}

// ═══════════════════════════════════════════════════════════
// APP BUBBLE - Circular playful app icons
// ═══════════════════════════════════════════════════════════
@Composable
private fun AppBubble(app: AppInfo, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(100),
        label = "scale"
    )
    
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )
    
    val colors = listOf(
        listOf(Color(0xFF8B5CF6), Color(0xFFC4B5FD)),
        listOf(Color(0xFF3B82F6), Color(0xFF93C5FD)),
        listOf(Color(0xFF10B981), Color(0xFF6EE7B7)),
        listOf(Color(0xFFF59E0B), Color(0xFFFCD34D)),
        listOf(Color(0xFFEC4899), Color(0xFFF9A8D4)),
        listOf(Color(0xFF06B6D4), Color(0xFF67E8F9))
    )
    val colorPair = remember { colors.random() }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .offset(y = floatY.dp)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        // Circular app icon with gradient ring
        Box(
            modifier = Modifier
                .size(76.dp)
                .shadow(12.dp, CircleShape, spotColor = colorPair[0].copy(alpha = 0.4f))
                .clip(CircleShape)
                .background(Color.White)
                .border(
                    width = 3.dp,
                    brush = Brush.linearGradient(colors = colorPair),
                    shape = CircleShape
                )
                .padding(3.dp),
            contentAlignment = Alignment.Center
        ) {
            val bitmap = remember(app.icon) { app.icon.toBitmap(96, 96) }
            Image(
                painter = BitmapPainter(bitmap.asImageBitmap()),
                contentDescription = app.name,
                modifier = Modifier.size(62.dp).clip(CircleShape)
            )
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        Text(
            text = app.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF334155),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(80.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════
// EMPTY APPS CARD
// ═══════════════════════════════════════════════════════════
@Composable
private fun EmptyAppsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "📱", fontSize = 56.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No apps yet!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF334155)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Ask your parent to add some 🙏",
                fontSize = 16.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// FUN FACT CARD - Full width, shows all text (no header - using SectionHeader)
// ═══════════════════════════════════════════════════════════
@Composable
private fun FunFactCard(
    fact: com.waqikids.launcher.data.islamic.IslamicDataRepository.IslamicFact,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFFFBBF24).copy(alpha = 0.3f)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A))
                    )
                )
                .padding(20.dp)
        ) {
            // Refresh button top right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.5f))
                    .clickable { onRefresh() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Refresh,
                    contentDescription = "New Fact",
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFF92400E)
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Emoji
                Text(
                    text = fact.emoji,
                    fontSize = 48.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Title
                Text(
                    text = fact.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF78350F),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Full fact text - increased size
                Text(
                    text = fact.fact,
                    fontSize = 16.sp,
                    color = Color(0xFF92400E),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }
        }
    }
}
