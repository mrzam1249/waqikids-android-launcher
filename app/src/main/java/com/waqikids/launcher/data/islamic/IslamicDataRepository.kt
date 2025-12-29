package com.waqikids.launcher.data.islamic

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*
import java.util.*

/**
 * Repository for Islamic content - Prayer times, Adhkar, and Islamic facts
 * Enterprise-grade implementation with accurate prayer time calculations
 */
@Singleton
class IslamicDataRepository @Inject constructor() {

    // ============================================
    // PRAYER TIME CALCULATIONS (Simplified Adhan)
    // ============================================
    
    data class PrayerTimes(
        val fajr: String,
        val sunrise: String,
        val dhuhr: String,
        val asr: String,
        val maghrib: String,
        val isha: String,
        val nextPrayer: String,
        val nextPrayerName: String,
        val timeUntilNext: String
    )
    
    fun getPrayerTimes(latitude: Double = 21.4225, longitude: Double = 39.8262): PrayerTimes {
        // Default to Mecca coordinates
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        
        // Julian date calculation
        val jd = julianDate(year, month, day)
        
        // Sun position
        val sunPos = sunPosition(jd)
        val decl = sunPos.first
        val eqTime = sunPos.second
        
        // Prayer times calculation (Muslim World League method)
        val fajrAngle = 18.0
        val ishaAngle = 17.0
        
        val noon = 12.0 + (-longitude / 15.0) - (eqTime / 60.0)
        val fajr = noon - hourAngle(latitude, decl, fajrAngle) / 15.0
        val sunrise = noon - hourAngle(latitude, decl, 0.833) / 15.0
        val dhuhr = noon
        val asr = noon + asrTime(latitude, decl) / 15.0
        val maghrib = noon + hourAngle(latitude, decl, 0.833) / 15.0
        val isha = noon + hourAngle(latitude, decl, ishaAngle) / 15.0
        
        // Format times
        val fajrStr = formatTime(fajr)
        val sunriseStr = formatTime(sunrise)
        val dhuhrStr = formatTime(dhuhr)
        val asrStr = formatTime(asr)
        val maghribStr = formatTime(maghrib)
        val ishaStr = formatTime(isha)
        
        // Find next prayer
        val currentHour = cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60.0
        val prayers = listOf(
            "Fajr" to fajr,
            "Sunrise" to sunrise,
            "Dhuhr" to dhuhr,
            "Asr" to asr,
            "Maghrib" to maghrib,
            "Isha" to isha
        )
        
        var nextPrayerName = "Fajr"
        var nextPrayerTime = fajr + 24 // Tomorrow's Fajr
        var timeUntil = nextPrayerTime - currentHour
        
        for ((name, time) in prayers) {
            if (time > currentHour) {
                nextPrayerName = name
                nextPrayerTime = time
                timeUntil = time - currentHour
                break
            }
        }
        
        val hours = timeUntil.toInt()
        val minutes = ((timeUntil - hours) * 60).toInt()
        val timeUntilStr = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
        
        return PrayerTimes(
            fajr = fajrStr,
            sunrise = sunriseStr,
            dhuhr = dhuhrStr,
            asr = asrStr,
            maghrib = maghribStr,
            isha = ishaStr,
            nextPrayer = formatTime(nextPrayerTime),
            nextPrayerName = nextPrayerName,
            timeUntilNext = timeUntilStr
        )
    }
    
    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = y / 100
        val b = 2 - a + a / 4
        return (365.25 * (y + 4716)).toInt() + (30.6001 * (m + 1)).toInt() + day + b - 1524.5
    }
    
    private fun sunPosition(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = (357.529 + 0.98560028 * d) % 360
        val q = (280.459 + 0.98564736 * d) % 360
        val l = (q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g))) % 360
        val e = 23.439 - 0.00000036 * d
        val decl = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))
        val ra = Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l)))) / 15
        val eqTime = (q / 15 - ra) * 60
        return Pair(decl, eqTime)
    }
    
    private fun hourAngle(lat: Double, decl: Double, angle: Double): Double {
        val latRad = Math.toRadians(lat)
        val declRad = Math.toRadians(decl)
        val angleRad = Math.toRadians(angle)
        val cosHA = (-sin(angleRad) - sin(latRad) * sin(declRad)) / (cos(latRad) * cos(declRad))
        return Math.toDegrees(acos(cosHA.coerceIn(-1.0, 1.0)))
    }
    
    private fun asrTime(lat: Double, decl: Double): Double {
        val factor = 1.0 // Shafi'i (use 2.0 for Hanafi)
        val latRad = Math.toRadians(lat)
        val declRad = Math.toRadians(decl)
        val angle = -atan(1.0 / (factor + tan(abs(latRad - declRad))))
        return hourAngle(lat, decl, Math.toDegrees(angle))
    }
    
    private fun formatTime(time: Double): String {
        var t = time
        if (t < 0) t += 24
        if (t >= 24) t -= 24
        val hours = t.toInt()
        val minutes = ((t - hours) * 60).toInt()
        val amPm = if (hours < 12) "AM" else "PM"
        val displayHour = if (hours == 0) 12 else if (hours > 12) hours - 12 else hours
        return String.format("%d:%02d %s", displayHour, minutes, amPm)
    }

    // ============================================
    // ADHKAR (Morning/Evening Remembrances)
    // ============================================
    
    data class Dhikr(
        val arabic: String,
        val transliteration: String,
        val translation: String,
        val count: Int,
        val category: String
    )
    
    fun getMorningAdhkar(): List<Dhikr> = listOf(
        Dhikr(
            arabic = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ",
            transliteration = "Asbahna wa asbahal-mulku lillah",
            translation = "We have reached the morning and at this time the kingdom belongs to Allah",
            count = 1,
            category = "Morning"
        ),
        Dhikr(
            arabic = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
            transliteration = "SubhanAllahi wa bihamdihi",
            translation = "Glory be to Allah and praise Him",
            count = 100,
            category = "Morning"
        ),
        Dhikr(
            arabic = "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
            transliteration = "La ilaha illallahu wahdahu la sharika lah",
            translation = "None has the right to be worshipped except Allah alone",
            count = 10,
            category = "Morning"
        ),
        Dhikr(
            arabic = "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ",
            transliteration = "Astaghfirullaha wa atubu ilayh",
            translation = "I seek forgiveness from Allah and repent to Him",
            count = 100,
            category = "Morning"
        )
    )
    
    fun getEveningAdhkar(): List<Dhikr> = listOf(
        Dhikr(
            arabic = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ",
            transliteration = "Amsayna wa amsal-mulku lillah",
            translation = "We have reached the evening and at this time the kingdom belongs to Allah",
            count = 1,
            category = "Evening"
        ),
        Dhikr(
            arabic = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
            transliteration = "SubhanAllahi wa bihamdihi",
            translation = "Glory be to Allah and praise Him",
            count = 100,
            category = "Evening"
        ),
        Dhikr(
            arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَافِيَةَ",
            transliteration = "Allahumma inni as'alukal-'afiyah",
            translation = "O Allah, I ask You for well-being",
            count = 3,
            category = "Evening"
        )
    )
    
    fun getCurrentDhikr(): Dhikr {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val adhkar = if (hour < 12) getMorningAdhkar() else getEveningAdhkar()
        return adhkar.random()
    }

    // ============================================
    // DID YOU KNOW (Islamic Facts for Kids)
    // ============================================
    
    data class IslamicFact(
        val emoji: String,
        val title: String,
        val fact: String,
        val category: String
    )
    
    private val islamicFacts = listOf(
        IslamicFact(
            emoji = "🕋",
            title = "The Kaaba",
            fact = "The Kaaba in Mecca is the most sacred place in Islam. Muslims around the world face it when they pray!",
            category = "Places"
        ),
        IslamicFact(
            emoji = "🌙",
            title = "Ramadan",
            fact = "Ramadan is the month when the Quran was first revealed to Prophet Muhammad ﷺ. Muslims fast from dawn to sunset!",
            category = "Months"
        ),
        IslamicFact(
            emoji = "📖",
            title = "The Quran",
            fact = "The Quran has 114 chapters called Surahs. The longest is Al-Baqarah and the shortest is Al-Kawthar!",
            category = "Quran"
        ),
        IslamicFact(
            emoji = "🤲",
            title = "Five Pillars",
            fact = "Islam has 5 pillars: Shahada (faith), Salah (prayer), Zakat (charity), Sawm (fasting), and Hajj (pilgrimage)!",
            category = "Basics"
        ),
        IslamicFact(
            emoji = "⭐",
            title = "99 Names",
            fact = "Allah has 99 beautiful names! Some are Ar-Rahman (The Most Merciful) and Al-Wadud (The Most Loving)!",
            category = "Allah"
        ),
        IslamicFact(
            emoji = "🕌",
            title = "Masjid An-Nabawi",
            fact = "The Prophet's Mosque in Madinah was built by Prophet Muhammad ﷺ himself. It's the second holiest mosque!",
            category = "Places"
        ),
        IslamicFact(
            emoji = "🐫",
            title = "Prophet Ibrahim",
            fact = "Prophet Ibrahim (Abraham) built the Kaaba with his son Ismail. He's called the 'Friend of Allah'!",
            category = "Prophets"
        ),
        IslamicFact(
            emoji = "🌊",
            title = "Prophet Musa",
            fact = "Allah parted the Red Sea for Prophet Musa (Moses) to save his people from Pharaoh!",
            category = "Prophets"
        ),
        IslamicFact(
            emoji = "🐋",
            title = "Prophet Yunus",
            fact = "Prophet Yunus (Jonah) was swallowed by a whale but was saved when he prayed to Allah!",
            category = "Prophets"
        ),
        IslamicFact(
            emoji = "🎁",
            title = "Eid",
            fact = "There are two Eids in Islam: Eid al-Fitr after Ramadan and Eid al-Adha during Hajj season!",
            category = "Celebrations"
        ),
        IslamicFact(
            emoji = "💝",
            title = "Sadaqah",
            fact = "Even a smile is charity in Islam! Being kind to others is rewarded by Allah!",
            category = "Good Deeds"
        ),
        IslamicFact(
            emoji = "🕊️",
            title = "Salam",
            fact = "Saying 'Assalamu Alaikum' means 'Peace be upon you'. It's the greeting of Muslims!",
            category = "Manners"
        )
    )
    
    fun getRandomFact(): IslamicFact = islamicFacts.random()
    
    fun getFactOfTheDay(): IslamicFact {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return islamicFacts[dayOfYear % islamicFacts.size]
    }
    
    // ============================================
    // DUA OF THE DAY
    // ============================================
    
    data class Dua(
        val arabic: String,
        val transliteration: String,
        val translation: String,
        val occasion: String
    )
    
    private val duas = listOf(
        Dua(
            arabic = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ",
            transliteration = "Bismillahil-ladhi la yadurru ma'asmihi shay'un",
            translation = "In the Name of Allah, with Whose Name nothing can cause harm",
            occasion = "Morning Protection"
        ),
        Dua(
            arabic = "رَبِّ زِدْنِي عِلْمًا",
            transliteration = "Rabbi zidni 'ilma",
            translation = "O my Lord, increase me in knowledge",
            occasion = "Before Studying"
        ),
        Dua(
            arabic = "اللَّهُمَّ بَارِكْ لَنَا فِيهِ",
            transliteration = "Allahumma barik lana fihi",
            translation = "O Allah, bless it for us",
            occasion = "Before Eating"
        )
    )
    
    fun getDuaOfTheDay(): Dua {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return duas[dayOfYear % duas.size]
    }
}
