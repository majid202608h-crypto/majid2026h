package com.example.data

import androidx.compose.ui.graphics.Color

data class GameStage(
    val id: Int,
    val name: String,
    val label: String,
    val icon: String,
    val nums: List<Int>,
    val q: Int,
    val time: Int,
    val boss: Boolean,
    val coinReward: Int,
    val gemReward: Int
)

data class GameAvatar(
    val id: String,
    val icon: String,
    val name: String,
    val price: Int
)

data class GameTheme(
    val id: String,
    val name: String,
    val preview: String,
    val price: Int,
    val bgStart: Color,
    val bgEnd: Color,
    val cardBg: Color,
    val primaryColor: Color
)

data class GameEffect(
    val id: String,
    val icon: String,
    val name: String,
    val price: Int
)

data class GameAchievement(
    val id: String,
    val icon: String,
    val name: String,
    val desc: String,
    val reward: Int
)

object GameConfig {
    val STAGES = List(120) { s ->
        val table = (s / 10) + 1
        val sub = (s % 10) + 1
        val isBoss = (sub == 10)
        
        val nums = listOf(table)
        
        val nameRaw = if (isBoss) "باس جدول $table" else "مرحله ${s + 1}"
        val name = nameRaw.toFaDigits()
        val labelRaw = if (isBoss) "نگهبان اژدهای بزرگ ضرب جدول $table! 🏆" else "ضربِ عدد $table (آزمون $sub از ۱۰)"
        val label = labelRaw.toFaDigits()
        val icon = if (isBoss) "🐉" else if (sub % 3 == 0) "🌿" else if (sub % 3 == 1) "🌱" else "🌳"
        
        // Progressive difficulty: more questions and less time as sub-stage and stage index increases
        val q = 10
        val time = if (isBoss) 15 else (30 - sub).coerceAtLeast(10)
        
        val coinReward = if (isBoss) 80 + s else 20 + (s / 3)
        val gemReward = if (isBoss) table else 0
        
        GameStage(
            id = s,
            name = name,
            label = label,
            icon = icon,
            nums = nums,
            q = q,
            time = time,
            boss = isBoss,
            coinReward = coinReward,
            gemReward = gemReward
        )
    }

    val AVATARS = listOf(
        GameAvatar("owl", "🦉", "جغد دانا", 0),
        GameAvatar("fox", "🦊", "روباه باهوش", 50),
        GameAvatar("tiger", "🐯", "ببر شجاع", 100),
        GameAvatar("robot", "🤖", "ربات مکانیکی", 150),
        GameAvatar("unicorn", "🦄", "تک‌شاخ جادویی", 250),
        GameAvatar("dragon", "🐲", "اژدهای قهرمان", 400)
    )

    val THEMES = listOf(
        GameTheme("default", "فضای تاریک", "🌌", 0, Color(0xFF0F0B26), Color(0xFF221144), Color(0xFF29164F), Color(0xFF9E47FF)),
        GameTheme("ocean", "اعماق اقیانوس", "🌊", 80, Color(0xFF050F1E), Color(0xFF0A2246), Color(0xFF0F2C54), Color(0xFF00D2FF)),
        GameTheme("forest", "جنگل جادویی", "🌲", 80, Color(0xFF04140B), Color(0xFF092C1A), Color(0xFF143F24), Color(0xFF00F59B)),
        GameTheme("sunset", "غروب آفتاب", "🌅", 120, Color(0xFF1D0905), Color(0xFF38140D), Color(0xFF451E13), Color(0xFFFF7B00))
    )

    val EFFECTS = listOf(
        GameEffect("stars", "⭐", "باران ستاره", 0),
        GameEffect("fire", "🔥", "جرقه‌های آتش", 50),
        GameEffect("snow", "❄️", "دانه‌های برف", 50),
        GameEffect("rainbow", "🌈", "رنگین کمان", 120)
    )

    val ACHIEVEMENTS = listOf(
        GameAchievement("first", "🌟", "اولین قدم", "اولین پاسخ درست جدول ضرب!", 20),
        GameAchievement("streak5", "🔥", "پنج پشت سر هم", "۵ پاسخ درست پشت سر هم بده", 50),
        GameAchievement("streak10", "⚡", "ده تای پشت سر هم", "۱۰ پاسخ درست پشت سر هم بده", 100),
        GameAchievement("stage1", "🌱", "مرحله اول", "مرحله ۱ را با موفقیت تمام کن", 30),
        GameAchievement("stage5", "🌳", "نصف راه", "۵ مرحله را کامل کن", 100),
        GameAchievement("allStages", "👑", "قهرمان بزرگ", "همه مراحل بازی را تمام کن!", 500),
        GameAchievement("boss1", "🐉", "شکست اژدها", "اولین باس را شکست بده", 80),
        GameAchievement("shopItem", "🛍️", "خریدار باهوش", "یک آیتم از فروشگاه بخر", 20),
        GameAchievement("speed100", "⚡", "سرعت نور", "بیش از ۱۰۰ امتیاز در چالش سرعت کسب کن", 60),
        GameAchievement("coins200", "🪙", "ثروتمند ضرب", "بیش از ۲۰۰ سکه پس‌انداز داشته باش", 50),
        GameAchievement("perfect", "💎", "بی‌نقص و عالی", "حداقل یک مرحله را بدون اشتباه تمام کن", 150),
        GameAchievement("daily3", "📅", "جدیت و استمرار", "۳ روز متوالی جایزه روزانه دریافت کن", 100)
    )

    val CHAR_MSGS = mapOf(
        "correct" to listOf("آفرین! صدآفرین! 🌟", "عالی پاسخ دادی! 🎉", "فوق‌العاده و محشر! 💪", "یک ستاره درخشان دیگر! ✨", "ریاضیدان کوچک من! 🏆"),
        "wrong" to listOf("اشکالی نداره پسر/دختر خوبم! 💙", "دفعه بعد حتماً درست می‌زنی 🤗", "با تمرین بیشتر قوی‌تر می‌شی! 💪", "ناامید نشو، تو فوق‌العاده‌ای! ⭐"),
        "boss" to listOf("باس اژدها خیلی قویه ولی تو ازش باهوش‌تری! 🐲", "سعی کن به تمام سوالات درست پاسخ بدی! 💪"),
        "speed" to listOf("سریع‌تر پاسخ بده! وقت طلاست! ⚡", "ساعت داره تیک‌تاک می‌کنه! کجایی؟ ⏰", "سرعتت عالیه! ادامه بده! 🚀"),
        "milestone" to listOf("یک قدم دیگر تا قهرمانی نهایی! 🚀", "داری قهرمان بزرگ جدول ضرب می‌شی! 🏆")
    )

    fun getMsg(type: String): String {
        val list = CHAR_MSGS[type] ?: listOf("آماده‌ای؟")
        return list.random()
    }
}

private fun String.toFaDigits(): String {
    val faDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return this.map { char ->
        if (char in '0'..'9') faDigits[char - '0'] else char
    }.joinToString("")
}
