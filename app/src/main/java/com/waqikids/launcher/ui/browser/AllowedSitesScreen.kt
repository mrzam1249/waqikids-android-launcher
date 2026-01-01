package com.waqikids.launcher.ui.browser

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.Calendar

/**
 * Main screen for the kid-friendly Islamic browser homepage
 */
@Composable
fun AllowedSitesScreen(
    viewModel: AllowedSitesViewModel = hiltViewModel(),
    onWebsiteClick: (String) -> Unit,
    onGoHome: () -> Unit
) {
    val websites by viewModel.allowedWebsites.collectAsState(initial = emptyList())
    val childName by viewModel.childName.collectAsState(initial = "")
    val prayerTimes by viewModel.prayerTimes.collectAsState()
    val nextPrayer by viewModel.nextPrayer.collectAsState()
    val dailyVerse by viewModel.dailyVerse.collectAsState()
    
    // Beautiful Islamic color palette
    val backgroundColor = Color(0xFFFEF9F3)  // Warm cream
    val goldColor = Color(0xFFD4A574)         // Elegant gold
    val tealColor = Color(0xFF0D9488)         // Prayer card teal
    val peachColor = Color(0xFFFED7AA)        // Verse card peach
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // ========== BACK BUTTON ==========
            BackButton(onClick = onGoHome)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // ========== BISMILLAH HEADER ==========
            BismillahHeader(goldColor = goldColor)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // ========== ISLAMIC GREETING ==========
            IslamicGreeting(childName = childName)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // ========== NEXT PRAYER CARD ==========
            NextPrayerCard(
                nextPrayer = nextPrayer,
                tealColor = tealColor
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // ========== TODAY'S PRAYERS (Horizontal Scroll) ==========
            TodaysPrayersSection(prayerTimes = prayerTimes)
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // ========== MY WEBSITES GRID ==========
            MyWebsitesSection(
                websites = websites,
                onWebsiteClick = onWebsiteClick
            )
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // ========== DAILY WISDOM VERSE ==========
            DailyWisdomCard(
                verse = dailyVerse,
                peachColor = peachColor
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // ========== GO HOME BUTTON ==========
            GoHomeButton(onClick = onGoHome)
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Back button to return to launcher
 */
@Composable
private fun BackButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back arrow circle
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFF0D9488)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "←",
                fontSize = 24.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = "Back to Home",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF2D3748)
        )
    }
}

/**
 * Bismillah header with elegant Arabic calligraphy style
 */
@Composable
private fun BismillahHeader(goldColor: Color) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Arabic Bismillah
        Text(
            text = "﷽",
            fontSize = 48.sp,
            color = goldColor,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Translation
        Text(
            text = "In the name of Allah, the Most Gracious, the Most Merciful",
            fontSize = 12.sp,
            color = goldColor.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}

/**
 * Islamic greeting with child's name
 */
@Composable
private fun IslamicGreeting(childName: String) {
    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..5 -> "🌙"
            in 6..11 -> "☀️"
            in 12..16 -> "🌤️"
            else -> "🌅"
        }
    }
    
    val displayName = if (childName.isNotBlank()) childName else "little one"
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Assalamu Alaikum",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D3748)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = greeting, fontSize = 28.sp)
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = displayName,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF0D9488)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "May Allah bless your day ✨",
            fontSize = 14.sp,
            color = Color(0xFF718096)
        )
    }
}

/**
 * Next prayer countdown card with animation
 */
@Composable
private fun NextPrayerCard(
    nextPrayer: PrayerInfo?,
    tealColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(pulse)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = tealColor.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF0D9488),
                            Color(0xFF14B8A6)
                        )
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🕌",
                    fontSize = 36.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Next Prayer",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                if (nextPrayer != null) {
                    Text(
                        text = "${nextPrayer.name} in ${nextPrayer.timeRemaining}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = nextPrayer.time,
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                } else {
                    Text(
                        text = "Loading...",
                        fontSize = 20.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Today's prayers horizontal scrollable section
 */
@Composable
private fun TodaysPrayersSection(prayerTimes: List<PrayerInfo>) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "📿", fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Today's Prayers",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2D3748)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(prayerTimes) { prayer ->
                PrayerTimeChip(prayer = prayer)
            }
        }
    }
}

/**
 * Individual prayer time chip - Colorful kid-friendly design
 */
@Composable
private fun PrayerTimeChip(prayer: PrayerInfo) {
    // Each prayer gets its own unique gradient colors and emoji
    val (gradientColors, emoji) = when (prayer.name.lowercase()) {
        "fajr" -> listOf(Color(0xFF7C3AED), Color(0xFF9333EA)) to "🌙"     // Purple - dawn
        "dhuhr" -> listOf(Color(0xFFF59E0B), Color(0xFFFBBF24)) to "☀️"    // Gold - midday sun
        "asr" -> listOf(Color(0xFFF97316), Color(0xFFFB923C)) to "🌤️"      // Orange - afternoon
        "maghrib" -> listOf(Color(0xFFEC4899), Color(0xFFF472B6)) to "🌅"   // Pink - sunset
        "isha" -> listOf(Color(0xFF3B82F6), Color(0xFF60A5FA)) to "🌃"      // Blue - night
        else -> listOf(Color(0xFF0D9488), Color(0xFF14B8A6)) to "🕌"
    }
    
    val backgroundColor = when {
        prayer.isPast -> Color(0xFFE2E8F0)  // Gray for past
        else -> Color.Transparent           // Will use gradient
    }
    
    val textColor = when {
        prayer.isPast -> Color(0xFF718096)
        else -> Color.White
    }
    
    val cardModifier = if (prayer.isPast) {
        Modifier
            .width(if (prayer.isNext) 95.dp else 85.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp))
    } else {
        Modifier
            .width(if (prayer.isNext) 95.dp else 85.dp)
            .shadow(if (prayer.isNext) 12.dp else 6.dp, RoundedCornerShape(16.dp))
    }
    
    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (!prayer.isPast) {
                        Modifier.background(
                            brush = Brush.verticalGradient(colors = gradientColors)
                        )
                    } else {
                        Modifier
                    }
                )
                .padding(vertical = if (prayer.isNext) 16.dp else 12.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Prayer emoji
                Text(
                    text = if (prayer.isPast) "✓" else emoji,
                    fontSize = if (prayer.isNext) 24.sp else 20.sp
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = prayer.name,
                    fontSize = if (prayer.isNext) 15.sp else 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = prayer.time,
                    fontSize = if (prayer.isNext) 13.sp else 12.sp,
                    color = textColor.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )
                
                // "Next" badge for current prayer
                if (prayer.isNext) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.25f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "NEXT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * My websites grid section
 */
@Composable
private fun MyWebsitesSection(
    websites: List<WebsiteInfo>,
    onWebsiteClick: (String) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🌐", fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "My Websites",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2D3748)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (websites.isEmpty()) {
            // Empty state
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "📭", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No websites yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF718096)
                    )
                    Text(
                        text = "Ask your parent to add some!",
                        fontSize = 14.sp,
                        color = Color(0xFFA0AEC0)
                    )
                }
            }
        } else {
            // Websites grid (3 columns)
            val rows = websites.chunked(3)
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { website ->
                            WebsiteCard(
                                website = website,
                                onClick = { onWebsiteClick(website.domain) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Fill empty slots
                        repeat(3 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual website card
 */
@Composable
private fun WebsiteCard(
    website: WebsiteInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = website.icon,
                fontSize = 32.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = website.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2D3748),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

/**
 * Daily wisdom card with Quran verse
 */
@Composable
private fun DailyWisdomCard(
    verse: DailyVerse?,
    peachColor: Color
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "✨", fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Daily Wisdom",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2D3748)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = peachColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📖",
                    fontSize = 28.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (verse != null) {
                    Text(
                        text = "\"${verse.text}\"",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF744210),
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = verse.reference,
                        fontSize = 13.sp,
                        color = Color(0xFF975A16),
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = "\"Be kind and gentle in all that you do\"",
                        fontSize = 16.sp,
                        color = Color(0xFF744210),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Go home button
 */
@Composable
private fun GoHomeButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2D3748)
        )
    ) {
        Text(text = "🏠", fontSize = 20.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Go Home",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ========== DATA CLASSES ==========

data class PrayerInfo(
    val name: String,
    val time: String,
    val timeRemaining: String = "",
    val isPast: Boolean = false,
    val isNext: Boolean = false
)

data class WebsiteInfo(
    val domain: String,
    val name: String,
    val icon: String = "🌐"
)

data class DailyVerse(
    val text: String,
    val reference: String
)
