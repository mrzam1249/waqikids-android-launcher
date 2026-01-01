package com.waqikids.launcher.ui.launcher

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import com.waqikids.launcher.domain.model.AppInfo
import com.waqikids.launcher.ui.launcher.components.AnimatedCloud
import com.waqikids.launcher.ui.launcher.components.BrowseWebCard
import com.waqikids.launcher.ui.launcher.components.DhikrCard
import com.waqikids.launcher.ui.launcher.components.DidYouKnowCard
import com.waqikids.launcher.ui.launcher.components.ParentModeButton
import com.waqikids.launcher.ui.launcher.components.PrayerTimeCard
import com.waqikids.launcher.ui.theme.BackgroundEnd
import com.waqikids.launcher.ui.theme.BackgroundStart
import com.waqikids.launcher.ui.theme.KidBlue
import com.waqikids.launcher.ui.theme.KidGreen
import com.waqikids.launcher.ui.theme.KidOrange
import com.waqikids.launcher.ui.theme.KidPink
import com.waqikids.launcher.ui.theme.KidPurple
import com.waqikids.launcher.ui.theme.KidTeal
import com.waqikids.launcher.ui.theme.KidYellow
import com.waqikids.launcher.ui.theme.Primary
import com.waqikids.launcher.util.BrowserHelper
import java.util.Calendar

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
    
    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..5 -> "Good night"
            in 6..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }
    
    val greetingEmoji = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..5 -> "🌙"
            in 6..11 -> "☀️"
            in 12..16 -> "🌤️"
            else -> "🌅"
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE0F2FE),  // Light sky blue
                        Color(0xFFFCE7F3),  // Light pink
                        Color(0xFFFEF3C7)   // Light warm yellow
                    )
                )
            )
    ) {
        // Decorative background elements
        Box(
            modifier = Modifier
                .offset(x = (-50).dp, y = 100.dp)
                .size(200.dp)
                .blur(60.dp)
                .background(
                    color = KidPurple.copy(alpha = 0.3f),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 50.dp, y = 200.dp)
                .size(150.dp)
                .blur(50.dp)
                .background(
                    color = KidBlue.copy(alpha = 0.3f),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 30.dp, y = (-100).dp)
                .size(180.dp)
                .blur(55.dp)
                .background(
                    color = KidPink.copy(alpha = 0.25f),
                    shape = CircleShape
                )
        )
        
        // Animated clouds
        AnimatedCloud(
            modifier = Modifier
                .offset(x = 20.dp, y = 80.dp)
                .size(90.dp, 45.dp),
            duration = 9000
        )
        AnimatedCloud(
            modifier = Modifier
                .offset(x = 260.dp, y = 60.dp)
                .size(70.dp, 35.dp),
            duration = 11000
        )
        AnimatedCloud(
            modifier = Modifier
                .offset(x = 140.dp, y = 140.dp)
                .size(60.dp, 30.dp),
            duration = 13000
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            
            // Top Bar with Parent Mode Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Greeting with fun styling
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$greeting $greetingEmoji",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Primary.copy(alpha = 0.7f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = childName,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 32.sp
                            ),
                            color = Primary
                        )
                        Text(
                            text = " 👋",
                            fontSize = 28.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Parent Mode Button
                ParentModeButton(onClick = onNavigateToParentMode)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Time Remaining Card - Fun Design
            TimeRemainingCard(timeRemaining = timeRemaining)
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Browse Web Card - Opens browser with WaqiKids homepage
            BrowseWebCard(
                onClick = { BrowserHelper.openHomePage(context) }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Prayer Time Card
            PrayerTimeCard(prayerTimes = prayerTimes)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Did You Know Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "✨", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Fun Facts",
                        style = MaterialTheme.typography.titleLarge,
                        color = Primary
                    )
                }
                IconButton(
                    onClick = { viewModel.refreshFact() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "New Fact",
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            DidYouKnowCard(fact = currentFact)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Dhikr Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "📿", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Daily Dhikr",
                    style = MaterialTheme.typography.titleLarge,
                    color = Primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            DhikrCard(
                dhikr = currentDhikr,
                onTap = { viewModel.nextDhikr() }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // App Grid Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "📲", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "My Apps",
                    style = MaterialTheme.typography.titleLarge,
                    color = Primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // App Grid
            if (apps.isEmpty()) {
                EmptyAppsCard()
            } else {
                // Horizontal scrolling app row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
                ) {
                    lazyItems(apps, key = { it.packageName }) { app ->
                        AppIconItem(
                            app = app,
                            onClick = { viewModel.launchApp(app.packageName) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun TimeRemainingCard(timeRemaining: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = KidBlue.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF06B6D4),  // Cyan
                            Color(0xFF3B82F6)   // Blue
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animated icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .scale(pulse)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⏰",
                        fontSize = 28.sp
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Screen Time Left",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = timeRemaining,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color.White
                    )
                }
                
                // Fun badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "🎯 Focus!",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyAppsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "📱",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No apps yet!",
                style = MaterialTheme.typography.titleMedium,
                color = Primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Ask your parent to add some apps 🙏",
                style = MaterialTheme.typography.bodyMedium,
                color = Primary.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun AppIconItem(
    app: AppInfo,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(100),
        label = "scale"
    )
    
    // Gentle bounce animation
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )
    
    // Get a fun color based on app index
    val appColors = listOf(KidBlue, KidGreen, KidPurple, KidPink, KidOrange, KidTeal, KidYellow)
    val colorIndex = remember { (0..appColors.lastIndex).random() }
    val appColor = appColors[colorIndex]
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .offset(y = bounce.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        // App icon with colorful shadow
        Box(
            modifier = Modifier
                .size(76.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(22.dp),
                    spotColor = appColor.copy(alpha = 0.4f)
                )
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White)
                .border(
                    width = 3.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            appColor.copy(alpha = 0.5f),
                            appColor.copy(alpha = 0.2f)
                        )
                    ),
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            val bitmap = remember(app.icon) {
                app.icon.toBitmap(128, 128)
            }
            Image(
                painter = BitmapPainter(bitmap.asImageBitmap()),
                contentDescription = app.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
            )
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // App name with background pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.8f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = app.name,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Primary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.width(72.dp)
            )
        }
    }
}
