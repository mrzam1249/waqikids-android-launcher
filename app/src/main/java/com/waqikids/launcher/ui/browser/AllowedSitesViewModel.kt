package com.waqikids.launcher.ui.browser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waqikids.launcher.data.local.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * ViewModel for AllowedSitesScreen
 * Provides prayer times, allowed websites, and daily verses
 */
@HiltViewModel
class AllowedSitesViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    // Default Islamic websites - always shown to all users
    private val defaultIslamicDomains = setOf(
        "quran.com",
        "sunnah.com",
        "islamqa.info",
        "islamicfinder.org",
        "myislam.org",
        "muslim.sg",
        "aladhan.com"
    )
    
    // Infrastructure domains to filter out (not user-added websites)
    private val infrastructureDomains = setOf(
        // Firebase & Google Cloud
        "fcm.googleapis.com",
        "firebase.google.com",
        "firebaseinstallations.googleapis.com",
        "firebaselogging.googleapis.com",
        "firebaseremoteconfig.googleapis.com",
        "crashlyticsreports-pa.googleapis.com",
        "googleapis.com",
        "gstatic.com",
        "googleusercontent.com",
        
        // Google Services
        "google.com",
        "play.google.com",
        "android.com",
        "android.clients.google.com",
        "connectivitycheck.gstatic.com",
        "clients.google.com",
        "mtalk.google.com",
        
        // WaqiKids infrastructure
        "waqikids.com",
        "api.waqikids.com",
        "dns.waqikids.com",
        "www.waqikids.com",
        
        // DNS & connectivity
        "dns.google",
        "cloudflare-dns.com",
        "1.1.1.1",
        "8.8.8.8",
        
        // Common CDNs
        "cloudfront.net",
        "akamaized.net",
        "fastly.net",
        "jsdelivr.net",
        "unpkg.com",
        "cdnjs.cloudflare.com",
        "bootstrapcdn.com",
        "fontawesome.com",
        "fonts.googleapis.com",
        "fonts.gstatic.com"
    )
    
    // Child's name from preferences
    val childName: StateFlow<String> = preferencesManager.childName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    
    // Allowed websites from cache
    private val _allowedWebsites = MutableStateFlow<List<WebsiteInfo>>(emptyList())
    val allowedWebsites: StateFlow<List<WebsiteInfo>> = _allowedWebsites
    
    // Prayer times for today
    private val _prayerTimes = MutableStateFlow<List<PrayerInfo>>(emptyList())
    val prayerTimes: StateFlow<List<PrayerInfo>> = _prayerTimes
    
    // Next prayer info
    private val _nextPrayer = MutableStateFlow<PrayerInfo?>(null)
    val nextPrayer: StateFlow<PrayerInfo?> = _nextPrayer
    
    // Daily verse
    private val _dailyVerse = MutableStateFlow<DailyVerse?>(null)
    val dailyVerse: StateFlow<DailyVerse?> = _dailyVerse
    
    init {
        loadData()
        startPrayerCountdown()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            // Use parentDomains (from backend) which contains ONLY parent-added websites
            preferencesManager.parentDomains.collect { parentDomains ->
                // Only use parent-added domains that are NOT infrastructure
                val parentAddedSites = parentDomains
                    .filterNot { isInfrastructureDomain(it) }
                
                // Combine parent-added sites with default Islamic sites (avoiding duplicates)
                // If no parent domains, user still sees the default Islamic sites
                val allDomains = (parentAddedSites + defaultIslamicDomains)
                    .map { it.lowercase().trim() }
                    .distinct()
                
                _allowedWebsites.value = allDomains.map { domain ->
                    WebsiteInfo(
                        domain = domain,
                        name = formatDomainName(domain),
                        icon = getIconForDomain(domain),
                        category = getCategoryForDomain(domain)
                    )
                }
            }
        }
        
        // Load prayer times (using approximate times for now)
        loadPrayerTimes()
        
        // Load daily verse
        loadDailyVerse()
    }
    
    private fun loadPrayerTimes() {
        // For now, using sample prayer times
        // TODO: Integrate with Aladhan API or local calculation based on location
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentTotalMinutes = currentHour * 60 + currentMinute
        
        // Sample prayer times (approximate)
        val prayers = listOf(
            PrayerTimeData("Fajr", 5, 30),
            PrayerTimeData("Dhuhr", 12, 30),
            PrayerTimeData("Asr", 15, 45),
            PrayerTimeData("Maghrib", 18, 15),
            PrayerTimeData("Isha", 19, 45)
        )
        
        var nextPrayerFound = false
        val prayerInfoList = prayers.map { prayer ->
            val prayerMinutes = prayer.hour * 60 + prayer.minute
            val isPast = prayerMinutes < currentTotalMinutes
            val isNext = !isPast && !nextPrayerFound
            
            if (isNext) {
                nextPrayerFound = true
                val diff = prayerMinutes - currentTotalMinutes
                val hours = diff / 60
                val minutes = diff % 60
                val timeRemaining = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
                
                _nextPrayer.value = PrayerInfo(
                    name = prayer.name,
                    time = formatTime(prayer.hour, prayer.minute),
                    timeRemaining = timeRemaining,
                    isNext = true
                )
            }
            
            PrayerInfo(
                name = prayer.name,
                time = formatTime(prayer.hour, prayer.minute),
                isPast = isPast,
                isNext = isNext
            )
        }
        
        _prayerTimes.value = prayerInfoList
        
        // If all prayers passed, next is Fajr tomorrow
        if (!nextPrayerFound && prayers.isNotEmpty()) {
            val fajr = prayers.first()
            _nextPrayer.value = PrayerInfo(
                name = fajr.name,
                time = formatTime(fajr.hour, fajr.minute),
                timeRemaining = "tomorrow",
                isNext = true
            )
        }
    }
    
    private fun startPrayerCountdown() {
        viewModelScope.launch {
            while (true) {
                delay(60_000) // Update every minute
                loadPrayerTimes()
            }
        }
    }
    
    private fun loadDailyVerse() {
        // Kid-friendly Quran verses - rotate by day of year
        val verses = listOf(
            DailyVerse(
                text = "Be kind to your parents and speak to them gently",
                reference = "Surah Al-Isra 17:23"
            ),
            DailyVerse(
                text = "Allah is with those who are patient",
                reference = "Surah Al-Baqarah 2:153"
            ),
            DailyVerse(
                text = "Speak good words to people",
                reference = "Surah Al-Baqarah 2:83"
            ),
            DailyVerse(
                text = "And be grateful to Me and do not deny Me",
                reference = "Surah Al-Baqarah 2:152"
            ),
            DailyVerse(
                text = "Indeed, Allah loves those who do good",
                reference = "Surah Al-Baqarah 2:195"
            ),
            DailyVerse(
                text = "Help one another in goodness and righteousness",
                reference = "Surah Al-Ma'idah 5:2"
            ),
            DailyVerse(
                text = "Be truthful, for truthfulness leads to righteousness",
                reference = "Hadith - Bukhari & Muslim"
            ),
            DailyVerse(
                text = "The best of you are those who learn the Quran and teach it",
                reference = "Hadith - Bukhari"
            ),
            DailyVerse(
                text = "Whoever believes in Allah and the Last Day, let him speak good or remain silent",
                reference = "Hadith - Bukhari & Muslim"
            ),
            DailyVerse(
                text = "A smile to your brother is charity",
                reference = "Hadith - Tirmidhi"
            ),
            DailyVerse(
                text = "Make things easy and do not make them difficult",
                reference = "Hadith - Bukhari"
            ),
            DailyVerse(
                text = "The strong person is not the one who can wrestle, but the one who controls himself when angry",
                reference = "Hadith - Bukhari"
            ),
            DailyVerse(
                text = "Be merciful to those on earth, and the One in heaven will be merciful to you",
                reference = "Hadith - Tirmidhi"
            ),
            DailyVerse(
                text = "Cleanliness is half of faith",
                reference = "Hadith - Muslim"
            )
        )
        
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        _dailyVerse.value = verses[dayOfYear % verses.size]
    }
    
    /**
     * Check if a domain is infrastructure (Firebase, Google, DNS, etc.)
     * These are technical domains needed for the app to work, not user-added websites
     */
    private fun isInfrastructureDomain(domain: String): Boolean {
        val lowerDomain = domain.lowercase().trim()
        
        // Exact match
        if (lowerDomain in infrastructureDomains) return true
        
        // Check if it's a subdomain of an infrastructure domain
        for (infra in infrastructureDomains) {
            if (lowerDomain.endsWith(".$infra")) return true
            if (lowerDomain == infra) return true
        }
        
        // Check for common infrastructure patterns
        if (lowerDomain.contains("googleapis.com")) return true
        if (lowerDomain.contains("gstatic.com")) return true
        if (lowerDomain.contains("firebase")) return true
        if (lowerDomain.contains("crashlytics")) return true
        if (lowerDomain.contains("google.com")) return true
        if (lowerDomain.contains("android.com")) return true
        if (lowerDomain.contains("waqikids")) return true
        if (lowerDomain.contains("cloudflare")) return true
        if (lowerDomain.contains("akamai")) return true
        if (lowerDomain.contains("cdn.")) return true
        if (lowerDomain.contains(".cdn.")) return true
        
        // IP addresses
        if (lowerDomain.matches(Regex("^\\d+\\.\\d+\\.\\d+\\.\\d+$"))) return true
        
        return false
    }
    
    private fun formatDomainName(domain: String): String {
        // Convert domain to friendly name
        // e.g., "youtube.com" -> "YouTube"
        val knownNames = mapOf(
            "youtube.com" to "YouTube",
            "khanacademy.org" to "Khan Academy",
            "quran.com" to "Quran",
            "pbskids.org" to "PBS Kids",
            "nickjr.com" to "Nick Jr",
            "nasa.gov" to "NASA",
            "google.com" to "Google",
            "wikipedia.org" to "Wikipedia",
            "duolingo.com" to "Duolingo",
            "coolmathgames.com" to "Cool Math",
            "funbrain.com" to "Fun Brain",
            "starfall.com" to "Starfall",
            "abcmouse.com" to "ABC Mouse",
            "brainpop.com" to "BrainPOP",
            "nationalgeographic.com" to "Nat Geo",
            "seerah.app" to "Seerah",
            "myislam.org" to "My Islam",
            "islamicfinder.org" to "Islamic Finder"
        )
        
        return knownNames[domain.lowercase()] 
            ?: domain.removePrefix("www.")
                .substringBefore(".")
                .replaceFirstChar { it.uppercase() }
    }
    
    private fun getIconForDomain(domain: String): String {
        val lowerDomain = domain.lowercase()
        
        // Islamic sites
        if (lowerDomain.contains("quran") || lowerDomain.contains("islam") || 
            lowerDomain.contains("muslim") || lowerDomain.contains("seerah") ||
            lowerDomain.contains("dua") || lowerDomain.contains("hadith")) {
            return "🕌"
        }
        
        // Video/Entertainment
        if (lowerDomain.contains("youtube") || lowerDomain.contains("netflix") ||
            lowerDomain.contains("disney") || lowerDomain.contains("video")) {
            return "📺"
        }
        
        // Games
        if (lowerDomain.contains("game") || lowerDomain.contains("play") ||
            lowerDomain.contains("fun")) {
            return "🎮"
        }
        
        // Education
        if (lowerDomain.contains("khan") || lowerDomain.contains("learn") ||
            lowerDomain.contains("edu") || lowerDomain.contains("school") ||
            lowerDomain.contains("study") || lowerDomain.contains("math") ||
            lowerDomain.contains("science") || lowerDomain.contains("brain")) {
            return "📚"
        }
        
        // Kids
        if (lowerDomain.contains("kid") || lowerDomain.contains("child") ||
            lowerDomain.contains("pbs") || lowerDomain.contains("nick") ||
            lowerDomain.contains("disney") || lowerDomain.contains("sesame")) {
            return "👶"
        }
        
        // Science/Space
        if (lowerDomain.contains("nasa") || lowerDomain.contains("space") ||
            lowerDomain.contains("science") || lowerDomain.contains("geo")) {
            return "🔬"
        }
        
        // Reading
        if (lowerDomain.contains("book") || lowerDomain.contains("read") ||
            lowerDomain.contains("story") || lowerDomain.contains("wiki")) {
            return "📖"
        }
        
        // Music/Audio
        if (lowerDomain.contains("music") || lowerDomain.contains("audio") ||
            lowerDomain.contains("nasheed")) {
            return "🎵"
        }
        
        // Default
        return "🌐"
    }
    
    /**
     * Categorize domain into: islamic, learning, entertainment, other
     */
    private fun getCategoryForDomain(domain: String): String {
        val lowerDomain = domain.lowercase()
        
        // Islamic sites
        if (lowerDomain.contains("quran") || lowerDomain.contains("islam") || 
            lowerDomain.contains("muslim") || lowerDomain.contains("seerah") ||
            lowerDomain.contains("dua") || lowerDomain.contains("hadith") ||
            lowerDomain.contains("salah") || lowerDomain.contains("prayer") ||
            lowerDomain.contains("mosque") || lowerDomain.contains("masjid") ||
            lowerDomain.contains("nasheed") || lowerDomain.contains("halal") ||
            lowerDomain.contains("ramadan") || lowerDomain.contains("eid") ||
            lowerDomain.contains("hijri") || lowerDomain.contains("mecca") ||
            lowerDomain.contains("madina") || lowerDomain.contains("allah") ||
            lowerDomain.contains("prophet") || lowerDomain.contains("sunnah") ||
            lowerDomain.contains("fiqh") || lowerDomain.contains("tafsir") ||
            lowerDomain.contains("zakat") || lowerDomain.contains("hajj") ||
            lowerDomain.contains("umrah") || lowerDomain.contains("azan") ||
            lowerDomain.contains("adhan") || lowerDomain.contains("athan")) {
            return "islamic"
        }
        
        // Learning/Educational sites
        if (lowerDomain.contains("khan") || lowerDomain.contains("learn") ||
            lowerDomain.contains("edu") || lowerDomain.contains("school") ||
            lowerDomain.contains("study") || lowerDomain.contains("math") ||
            lowerDomain.contains("science") || lowerDomain.contains("brain") ||
            lowerDomain.contains("academy") || lowerDomain.contains("course") ||
            lowerDomain.contains("duolingo") || lowerDomain.contains("language") ||
            lowerDomain.contains("coding") || lowerDomain.contains("program") ||
            lowerDomain.contains("wiki") || lowerDomain.contains("encyclopedia") ||
            lowerDomain.contains("dictionary") || lowerDomain.contains("national") ||
            lowerDomain.contains("geographic") || lowerDomain.contains("nasa") ||
            lowerDomain.contains("pbs") || lowerDomain.contains("starfall") ||
            lowerDomain.contains("abcmouse") || lowerDomain.contains("ixl") ||
            lowerDomain.contains("prodigy") || lowerDomain.contains("scratch") ||
            lowerDomain.contains("typing") || lowerDomain.contains("reading")) {
            return "learning"
        }
        
        // Entertainment sites
        if (lowerDomain.contains("youtube") || lowerDomain.contains("netflix") ||
            lowerDomain.contains("disney") || lowerDomain.contains("video") ||
            lowerDomain.contains("game") || lowerDomain.contains("play") ||
            lowerDomain.contains("fun") || lowerDomain.contains("nick") ||
            lowerDomain.contains("cartoon") || lowerDomain.contains("animation") ||
            lowerDomain.contains("movie") || lowerDomain.contains("tv") ||
            lowerDomain.contains("stream") || lowerDomain.contains("music") ||
            lowerDomain.contains("spotify") || lowerDomain.contains("roblox") ||
            lowerDomain.contains("minecraft") || lowerDomain.contains("lego") ||
            lowerDomain.contains("coolmath") || lowerDomain.contains("funbrain") ||
            lowerDomain.contains("sesame") || lowerDomain.contains("peppa") ||
            lowerDomain.contains("bluey")) {
            return "entertainment"
        }
        
        // Default to other
        return "other"
    }

    private fun formatTime(hour: Int, minute: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        val format = SimpleDateFormat("h:mm a", Locale.getDefault())
        return format.format(cal.time)
    }
    
    private data class PrayerTimeData(
        val name: String,
        val hour: Int,
        val minute: Int
    )
}
