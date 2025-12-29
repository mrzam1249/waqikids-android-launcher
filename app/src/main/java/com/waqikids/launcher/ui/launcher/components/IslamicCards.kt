package com.waqikids.launcher.ui.launcher.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.waqikids.launcher.data.islamic.IslamicDataRepository

/**
 * Prayer Time Card - Shows next prayer with countdown
 */
@Composable
fun PrayerTimeCard(
    prayerTimes: IslamicDataRepository.PrayerTimes,
    modifier: Modifier = Modifier
) {
    // Pulse animation for the icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF7C3AED),
                            Color(0xFF9333EA)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mosque icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🕌",
                        fontSize = 28.sp
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Next Prayer",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = prayerTimes.nextPrayerName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = prayerTimes.nextPrayer,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "in ${prayerTimes.timeUntilNext}",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * Dhikr Card - Shows current adhkar with beautiful animation
 */
@Composable
fun DhikrCard(
    dhikr: IslamicDataRepository.Dhikr,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tapCount by remember { mutableStateOf(0) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { 
                tapCount++
                if (tapCount >= dhikr.count) {
                    onTap()
                    tapCount = 0
                }
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0FDF4)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Category badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF10B981).copy(alpha = 0.2f))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "📿 ${dhikr.category} Adhkar",
                    fontSize = 12.sp,
                    color = Color(0xFF047857),
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Arabic text
            Text(
                text = dhikr.arabic,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                textAlign = TextAlign.Center,
                lineHeight = 40.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Transliteration
            Text(
                text = dhikr.transliteration,
                fontSize = 14.sp,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Translation
            Text(
                text = dhikr.translation,
                fontSize = 13.sp,
                color = Color(0xFF4B5563),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Counter
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Tap to count: ",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$tapCount/${dhikr.count}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Did You Know Card - Fun Islamic facts for kids
 */
@Composable
fun DidYouKnowCard(
    fact: IslamicDataRepository.IslamicFact,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFEF3C7)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Emoji with bounce
            Box(
                modifier = Modifier
                    .offset(y = bounce.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFBBF24).copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = fact.emoji,
                    fontSize = 24.sp
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "💡 Did You Know?",
                        fontSize = 12.sp,
                        color = Color(0xFF92400E),
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = fact.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = fact.fact,
                    fontSize = 13.sp,
                    color = Color(0xFF4B5563),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

/**
 * Parent Mode Access Button - Visible and accessible for parents
 */
@Composable
fun ParentModeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rotate")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    // Pulse animation to draw attention
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Card(
        modifier = modifier
            .scale(pulse)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF6366F1)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Parent Mode",
                tint = Color.White,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(rotation)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Parent",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

/**
 * Quick Action Button - For launcher actions
 */
@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF4B5563),
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
