package com.waqikids.launcher.ui.browser

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar

// Beautiful gradient colors matching launcher
private val SkyGradient = listOf(
    Color(0xFFF0F9FF),
    Color(0xFFFDF4FF),
    Color(0xFFFFF7ED)
)

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
    
    // Categorize websites
    val islamicSites = websites.filter { it.category == "islamic" }
    val learningSites = websites.filter { it.category == "learning" }
    val entertainmentSites = websites.filter { it.category == "entertainment" }
    val otherSites = websites.filter { it.category == "other" || it.category.isBlank() }
    
    // Floating animation
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
            color = Color(0xFF8B5CF6),
            size = 140.dp,
            blur = 70.dp,
            alpha = 0.2f,
            modifier = Modifier.offset(x = (-20).dp, y = (80 + floatOffset).dp)
        )
        FloatingOrb(
            color = Color(0xFF06B6D4),
            size = 100.dp,
            blur = 50.dp,
            alpha = 0.15f,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 20.dp, y = (150 - floatOffset * 0.8f).dp)
        )
        FloatingOrb(
            color = Color(0xFFEC4899),
            size = 120.dp,
            blur = 60.dp,
            alpha = 0.15f,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 50.dp, y = (-100 + floatOffset * 0.5f).dp)
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
            // BACK BUTTON + SCREEN TITLE
            // ══════════════════════════════════════
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                BackButton(onClick = onGoHome)
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "My Websites",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E293B)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // ══════════════════════════════════════
            // HEADER - Bismillah & Greeting
            // ══════════════════════════════════════
            BismillahHeader()
            
            Spacer(modifier = Modifier.height(20.dp))
            
            IslamicGreeting(childName = childName)
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // ══════════════════════════════════════
            // NEXT PRAYER CARD - Clean, bright, kid-friendly
            // ══════════════════════════════════════
            NextPrayerCard(nextPrayer = nextPrayer, prayerTimes = prayerTimes)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // ══════════════════════════════════════
            // WEBSITE CATEGORIES
            // ══════════════════════════════════════
            if (websites.isEmpty()) {
                EmptyWebsitesPlaceholder()
            } else {
                // Islamic Websites
                if (islamicSites.isNotEmpty()) {
                    WebsiteCategorySection(
                        emoji = "🕌",
                        title = "Islamic",
                        websites = islamicSites,
                        gradientColors = listOf(Color(0xFF10B981), Color(0xFF34D399)),
                        onWebsiteClick = onWebsiteClick
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // Learning Websites
                if (learningSites.isNotEmpty()) {
                    WebsiteCategorySection(
                        emoji = "📚",
                        title = "Learning",
                        websites = learningSites,
                        gradientColors = listOf(Color(0xFF3B82F6), Color(0xFF60A5FA)),
                        onWebsiteClick = onWebsiteClick
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // Entertainment Websites
                if (entertainmentSites.isNotEmpty()) {
                    WebsiteCategorySection(
                        emoji = "🎮",
                        title = "Entertainment",
                        websites = entertainmentSites,
                        gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFFBBF24)),
                        onWebsiteClick = onWebsiteClick
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // Other Websites
                if (otherSites.isNotEmpty()) {
                    WebsiteCategorySection(
                        emoji = "🌐",
                        title = "Other",
                        websites = otherSites,
                        gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFFA78BFA)),
                        onWebsiteClick = onWebsiteClick
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // ══════════════════════════════════════
            // DAILY WISDOM
            // ══════════════════════════════════════
            DailyWisdomCard(verse = dailyVerse)
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════
// FLOATING ORB - Decorative background element
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
// BACK BUTTON - Arrow icon only, matching launcher style
// ═══════════════════════════════════════════════════════════
@Composable
private fun BackButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .shadow(12.dp, CircleShape)
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF8B5CF6), Color(0xFFA78BFA))
                )
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.ChevronLeft,
            contentDescription = "Back",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════
// BISMILLAH HEADER
// ═══════════════════════════════════════════════════════════
@Composable
private fun BismillahHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "﷽",
            fontSize = 56.sp,
            color = Color(0xFFD4A574),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "In the name of Allah, the Most Gracious, the Most Merciful",
            fontSize = 14.sp,
            color = Color(0xFFD4A574).copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

// ═══════════════════════════════════════════════════════════
// ISLAMIC GREETING
// ═══════════════════════════════════════════════════════════
@Composable
private fun IslamicGreeting(childName: String) {
    val greetingEmoji = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..5 -> "🌙"
            in 6..11 -> "☀️"
            in 12..16 -> "🌤️"
            else -> "🌅"
        }
    }
    
    val displayName = if (childName.isNotBlank()) childName else "Explorer"
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Assalamu Alaikum",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = greetingEmoji, fontSize = 22.sp)
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = displayName,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF8B5CF6)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "May Allah bless your journey ✨",
            fontSize = 14.sp,
            color = Color(0xFF64748B)
        )
    }
}

// ═══════════════════════════════════════════════════════════
// NEXT PRAYER CARD - Clean, bright, kid-friendly
// ═══════════════════════════════════════════════════════════
@Composable
private fun NextPrayerCard(
    nextPrayer: PrayerInfo?,
    prayerTimes: List<PrayerInfo>
) {
    Column {
        // Section title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "🕌", fontSize = 28.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Prayer Times",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E293B)
            )
        }
        
        Spacer(modifier = Modifier.height(14.dp))
        
        // Main card with soft cream/white background
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Next Prayer highlight section
                if (nextPrayer != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF10B981), Color(0xFF34D399))
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Mosque icon with white bg
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🕌", fontSize = 32.sp)
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Next Prayer",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = nextPrayer.name,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = nextPrayer.time,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.25f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "in ${nextPrayer.timeRemaining}",
                                        fontSize = 13.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // All 5 prayers in a row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    prayerTimes.forEach { prayer ->
                        PrayerTimeChip(prayer = prayer)
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// PRAYER TIME CHIP - Colorful individual cards
// ═══════════════════════════════════════════════════════════
@Composable
private fun PrayerTimeChip(prayer: PrayerInfo) {
    val (bgColor, emoji) = when (prayer.name.lowercase()) {
        "fajr" -> Color(0xFF6366F1) to "🌙"      // Indigo - dawn
        "dhuhr" -> Color(0xFFF59E0B) to "☀️"     // Amber - midday
        "asr" -> Color(0xFFF97316) to "🌤️"       // Orange - afternoon
        "maghrib" -> Color(0xFFEC4899) to "🌅"   // Pink - sunset
        "isha" -> Color(0xFF3B82F6) to "🌃"      // Blue - night
        else -> Color(0xFF10B981) to "🕌"
    }
    
    val isNext = prayer.isNext
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(58.dp)
    ) {
        // Circular icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(if (isNext) 8.dp else 4.dp, CircleShape)
                .clip(CircleShape)
                .background(bgColor)
                .then(
                    if (isNext) Modifier.border(3.dp, Color(0xFF10B981), CircleShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 22.sp)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = prayer.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF374151)
        )
        
        Text(
            text = prayer.time,
            fontSize = 11.sp,
            color = Color(0xFF6B7280),
            fontWeight = FontWeight.Medium
        )
    }
}

// ═══════════════════════════════════════════════════════════
// WEBSITE CATEGORY SECTION - Clean with title
// ═══════════════════════════════════════════════════════════
@Composable
private fun WebsiteCategorySection(
    emoji: String,
    title: String,
    websites: List<WebsiteInfo>,
    gradientColors: List<Color>,
    onWebsiteClick: (String) -> Unit
) {
    Column {
        // Section title
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
        
        Spacer(modifier = Modifier.height(14.dp))
        
        // Horizontal scrollable websites
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(websites) { website ->
                WebsiteCard(
                    website = website,
                    accentColor = gradientColors[0],
                    onClick = { onWebsiteClick(website.domain) }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// WEBSITE CARD - Clean white card with favicon + fallback
// ═══════════════════════════════════════════════════════════
@Composable
private fun WebsiteCard(
    website: WebsiteInfo,
    accentColor: Color,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "scale"
    )
    
    // Check for bundled icon first (instant, offline)
    val bundledIconRes = remember(website.domain) { 
        FaviconHelper.getBundledIconRes(context, website.domain) 
    }
    
    // Google Favicon URL (with caching) - used if no bundled icon
    val faviconUrl = remember(website.domain) {
        FaviconHelper.getFaviconUrl(website.domain)
    }
    
    Card(
        modifier = Modifier
            .width(110.dp)
            .height(140.dp)
            .scale(scale)
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Favicon with colored background
            // Layer 1: Bundled icon (instant, offline)
            // Layer 2: Google Favicon with disk cache
            // Layer 3: Emoji fallback
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                if (bundledIconRes != null) {
                    // Layer 1: Bundled icon - instant, works offline
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = bundledIconRes),
                        contentDescription = website.name,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    // Layer 2 & 3: Google Favicon with disk cache, fallback to emoji
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(faviconUrl)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .crossfade(true)
                            .build(),
                        contentDescription = website.name,
                        loading = {
                            // Show emoji while loading
                            Text(
                                text = website.icon,
                                fontSize = 28.sp
                            )
                        },
                        error = {
                            // Show emoji if favicon fails to load
                            Text(
                                text = website.icon,
                                fontSize = 28.sp
                            )
                        },
                        success = { state ->
                            // Show the favicon
                            androidx.compose.foundation.Image(
                                painter = state.painter,
                                contentDescription = website.name,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Website name
            Text(
                text = website.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Domain subtitle
            Text(
                text = website.domain.removePrefix("www."),
                fontSize = 10.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// EMPTY WEBSITES PLACEHOLDER - Fun & engaging
// ═══════════════════════════════════════════════════════════
@Composable
private fun EmptyWebsitesPlaceholder() {
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated rocket
            Text(
                text = "🚀",
                fontSize = 72.sp,
                modifier = Modifier.offset(y = (-bounce).dp)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "Ready for Adventure!",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E293B),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Your websites will appear here",
                fontSize = 18.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Ask your parent to add some fun sites! 🎉",
                fontSize = 16.sp,
                color = Color(0xFF8B5CF6),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Decorative icons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                listOf("🕌", "📚", "🎮", "🌐").forEach { emoji ->
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 28.sp)
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// DAILY WISDOM CARD
// ═══════════════════════════════════════════════════════════
@Composable
private fun DailyWisdomCard(verse: DailyVerse?) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "✨", fontSize = 28.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Daily Wisdom",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E293B)
            )
        }
        
        Spacer(modifier = Modifier.height(14.dp))
        
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
                    .padding(28.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "📖", fontSize = 40.sp)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (verse != null) {
                        Text(
                            text = "\"${verse.text}\"",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF78350F),
                            textAlign = TextAlign.Center,
                            lineHeight = 28.sp
                        )
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        Text(
                            text = verse.reference,
                            fontSize = 15.sp,
                            color = Color(0xFF92400E),
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "\"Be kind and gentle in all that you do\"",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF78350F),
                            textAlign = TextAlign.Center,
                            lineHeight = 28.sp
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// DATA CLASSES
// ═══════════════════════════════════════════════════════════

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
    val icon: String = "🌐",
    val category: String = "other"  // islamic, learning, entertainment, other
)

data class DailyVerse(
    val text: String,
    val reference: String
)
