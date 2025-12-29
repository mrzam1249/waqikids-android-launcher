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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Refresh
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
import com.waqikids.launcher.ui.launcher.components.DhikrCard
import com.waqikids.launcher.ui.launcher.components.DidYouKnowCard
import com.waqikids.launcher.ui.launcher.components.ParentModeButton
import com.waqikids.launcher.ui.launcher.components.PrayerTimeCard
import com.waqikids.launcher.ui.theme.BackgroundEnd
import com.waqikids.launcher.ui.theme.BackgroundStart
import com.waqikids.launcher.ui.theme.KidBlue
import com.waqikids.launcher.ui.theme.Primary
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
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(BackgroundStart, BackgroundEnd)
                )
            )
    ) {
        // Animated clouds in background
        AnimatedCloud(
            modifier = Modifier
                .offset(x = 20.dp, y = 120.dp)
                .size(80.dp, 40.dp),
            duration = 8000
        )
        AnimatedCloud(
            modifier = Modifier
                .offset(x = 280.dp, y = 80.dp)
                .size(60.dp, 30.dp),
            duration = 10000
        )
        AnimatedCloud(
            modifier = Modifier
                .offset(x = 150.dp, y = 200.dp)
                .size(70.dp, 35.dp),
            duration = 12000
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Top Bar with Parent Mode Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Greeting
                Column {
                    Text(
                        text = "$greeting,",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Primary.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "$childName! 👋",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Primary
                    )
                }
                
                // Parent Mode Button
                ParentModeButton(onClick = onNavigateToParentMode)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Time Remaining Card - Compact
            TimeRemainingCompact(timeRemaining = timeRemaining)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Prayer Time Card
            PrayerTimeCard(prayerTimes = prayerTimes)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Did You Know Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✨ Islamic Facts",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Primary
                )
                IconButton(onClick = { viewModel.refreshFact() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "New Fact",
                        tint = Primary.copy(alpha = 0.6f)
                    )
                }
            }
            
            DidYouKnowCard(fact = currentFact)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Dhikr Card
            Text(
                text = "📿 Daily Dhikr",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            DhikrCard(
                dhikr = currentDhikr,
                onTap = { viewModel.nextDhikr() }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // App Grid Header
            Text(
                text = "🎮 My Apps",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // App Grid
            if (apps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "📱",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No apps available yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Primary.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Ask your parent to add some apps!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Primary.copy(alpha = 0.4f)
                        )
                    }
                }
            } else {
                // Horizontal scrolling app row for better UX
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(apps, key = { it.packageName }) { app ->
                        AppIconItem(
                            app = app,
                            onClick = { viewModel.launchApp(app.packageName) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TimeRemainingCompact(timeRemaining: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(KidBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = KidBlue,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Time remaining today",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = timeRemaining,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = KidBlue
                )
            }
            
            // Progress indicator could go here
            Text(
                text = "⏰",
                fontSize = 24.sp
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
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(100),
        label = "scale"
    )
    
    // Bounce animation
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )
    
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
        // App icon with shadow
        Box(
            modifier = Modifier
                .size(68.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(18.dp),
                    spotColor = Primary.copy(alpha = 0.3f)
                )
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            val bitmap = remember(app.icon) {
                app.icon.toBitmap(128, 128)
            }
            Image(
                painter = BitmapPainter(bitmap.asImageBitmap()),
                contentDescription = app.name,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(14.dp))
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // App name
        Text(
            text = app.name,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            ),
            color = Primary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(76.dp)
        )
    }
}
