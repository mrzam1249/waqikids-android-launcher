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
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.waqikids.launcher.ui.theme.BackgroundEnd
import com.waqikids.launcher.ui.theme.BackgroundStart
import com.waqikids.launcher.ui.theme.CloudWhite
import com.waqikids.launcher.ui.theme.KidBlue
import com.waqikids.launcher.ui.theme.KidPink
import com.waqikids.launcher.ui.theme.KidPurple
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
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Greeting Header
            GreetingHeader(
                greeting = greeting,
                childName = childName,
                timeRemaining = timeRemaining
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // App Grid
            if (apps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No apps available yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Primary.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(apps, key = { it.packageName }) { app ->
                        AppIconItem(
                            app = app,
                            onClick = { viewModel.launchApp(app.packageName) }
                        )
                    }
                }
            }
            
            // Decorative bottom elements (long-press for parent mode)
            DecorativeFooter(onLongPress = onNavigateToParentMode)
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun GreetingHeader(
    greeting: String,
    childName: String,
    timeRemaining: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Greeting
        Text(
            text = "$greeting,",
            style = MaterialTheme.typography.headlineSmall,
            color = Primary.copy(alpha = 0.7f)
        )
        
        Text(
            text = "$childName! 👋",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Time remaining card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.9f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                androidx.compose.foundation.layout.Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(KidBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = KidBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = "Time remaining today",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = timeRemaining,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = KidBlue
                        )
                    }
                }
            }
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
                .size(64.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = Primary.copy(alpha = 0.3f)
                )
                .clip(RoundedCornerShape(16.dp))
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
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // App name
        Text(
            text = app.name,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp
            ),
            color = Primary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(72.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DecorativeFooter(
    onLongPress: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { /* no-op */ },
                onLongClick = onLongPress,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "🌈  🦋  ✨  🌻  🐝  ⭐  🌸",
            style = MaterialTheme.typography.titleLarge,
            letterSpacing = 4.sp
        )
    }
}
