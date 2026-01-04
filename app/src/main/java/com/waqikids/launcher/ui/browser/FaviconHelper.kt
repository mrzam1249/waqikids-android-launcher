package com.waqikids.launcher.ui.browser

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest

/**
 * Helper for loading website favicons with multiple fallback layers:
 * 1. Bundled drawable (instant, offline) - for popular sites
 * 2. Google Favicon API with Coil disk cache (cached after first load)
 * 3. Emoji fallback (always works)
 */
object FaviconHelper {
    
    /**
     * Map of domain -> drawable resource name for bundled icons
     * Resource files should be named: favicon_youtube.png, favicon_quran.png, etc.
     * Place PNG files (64x64 or 128x128) in res/drawable/
     */
    private val bundledIconNames: Map<String, String> = mapOf(
        // Islamic Sites
        "quran.com" to "favicon_quran",
        "islamqa.info" to "favicon_islamqa",
        "sunnah.com" to "favicon_sunnah",
        "myislam.org" to "favicon_myislam",
        "muslimcentral.com" to "favicon_muslimcentral",
        "seekersguidance.org" to "favicon_seekersguidance",
        "bayyinah.com" to "favicon_bayyinah",
        
        // Education & Video
        "youtube.com" to "favicon_youtube",
        "khanacademy.org" to "favicon_khanacademy",
        "duolingo.com" to "favicon_duolingo",
        "pbskids.org" to "favicon_pbskids",
        "nationalgeographic.com" to "favicon_natgeo",
        "nasa.gov" to "favicon_nasa",
        "wikipedia.org" to "favicon_wikipedia",
        
        // Kids Entertainment
        "nickjr.com" to "favicon_nickjr",
        "coolmathgames.com" to "favicon_coolmath",
        "funbrain.com" to "favicon_funbrain",
        "starfall.com" to "favicon_starfall",
        "abcmouse.com" to "favicon_abcmouse"
    )
    
    /**
     * Get bundled drawable resource ID for a domain, or null if not bundled/not found
     */
    fun getBundledIconRes(context: Context, domain: String): Int? {
        val cleanDomain = domain.lowercase().removePrefix("www.")
        val resName = bundledIconNames[cleanDomain] ?: return null
        
        // Try to get the resource ID - returns 0 if not found
        val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
        return if (resId != 0) resId else null
    }
    
    /**
     * Get Google Favicon API URL for a domain
     */
    fun getFaviconUrl(domain: String, size: Int = 128): String {
        return "https://www.google.com/s2/favicons?domain=$domain&sz=$size"
    }
}

/**
 * Composable that displays a website favicon with intelligent fallback:
 * 1. Bundled icon (if available) - instant, offline
 * 2. Google Favicon with disk cache - loads once, then cached
 * 3. Emoji fallback - always works
 */
@Composable
fun WebsiteFavicon(
    domain: String,
    fallbackEmoji: String,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    val context = LocalContext.current
    val bundledIconRes = remember(domain) { FaviconHelper.getBundledIconRes(context, domain) }
    
    if (bundledIconRes != null) {
        // Layer 1: Bundled icon - instant, works offline
        Image(
            painter = painterResource(id = bundledIconRes),
            contentDescription = domain,
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(8.dp))
        )
    } else {
        // Layer 2 & 3: Try Google Favicon with Coil caching, fallback to emoji
        val faviconUrl = remember(domain) { FaviconHelper.getFaviconUrl(domain) }
        
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(faviconUrl)
                .diskCachePolicy(CachePolicy.ENABLED)  // Persist to disk
                .memoryCachePolicy(CachePolicy.ENABLED) // Keep in memory
                .crossfade(true)
                .build(),
            contentDescription = domain,
            loading = {
                // Show emoji while loading
                Text(
                    text = fallbackEmoji,
                    fontSize = (size.value * 0.7f).sp
                )
            },
            error = {
                // Show emoji if favicon fails
                Text(
                    text = fallbackEmoji,
                    fontSize = (size.value * 0.7f).sp
                )
            },
            success = { state ->
                Image(
                    painter = state.painter,
                    contentDescription = domain,
                    modifier = modifier
                        .size(size)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        )
    }
}
