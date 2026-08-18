package com.example

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.Shadow
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.ui.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.app.AlarmManager
import android.widget.Toast
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import ir.cafebazaar.poolakey.*

// Canvas particle representation
data class UIParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var alpha: Float,
    val size: Float,
    val text: String
)


private const val LICENSE_CODE_SALT = "zhero::lic::v1::a7Kq"

private val PERMANENT_LICENSE_HASHES: Set<String> = setOf(
    "3ce331e345454bbbdf19fb58cde601a2b99143e208ee32a5fd67ec3bd1d5da41", // SMH1361
    "de700f2b28e4a94255f04e4916e95a7ecf34bbe56629760fd6586f63239a2567", // HA3998
    "6b90eeb8b630b474843ec7032833e149fb3e303b8e13f81fd9c13d0621e24fc6", // M_H_7399
    "29c106c5ca5f2327f818df25dbfb7a0fde82874e92b7e34280f1e54829a0b7c1", // 3998335
    "68358e10cb8bf16a369b96541a1c3dcf77ba2ea63ea87cc9fc596aeee6d34bd2", // 1361
    "b29e5239b5f3892ec92f1a59360ee479455b4deea08313a0c0a7d7750ecea985", // HASANI_D_1405
    "a71d273a15607dad785fd507924f0d1b61cc073aaf57a653f179971358445a57", // PERM_M1
    "6e88b3eb0fe32a6fe2a9e7e1a126c828a008a74a5ef454a5b2548d92d6b5e008", // PERM_M3
    "863428af2e33f48c48e773e5a59c5572e197375d777b085fe091a82688921589", // PERM_M6
    "f6c28e282c1d621d950e3682eea56c5b993a54a405c82e83fb95f56bb3ea4675"  // PERM_Y1
)

private val TIMED_LICENSE_HASHES: Map<String, Int> = mapOf(
    "5b7cd2e94de44ac0c6273e498608fa542df9d4462c288ecb92dfdd9d0fb18dd7" to 30,  // ZARBDAR_M1
    "effeca9544e88b5b1a77ebaf825d231beb9693fcb6153b938cfa97e053760f9b" to 30,  // M1_30D
    "09ff45eb78837c76b2c50e4993671eb9f20d713fab57698884d6a7253588db7f" to 90,  // ZARBDAR_M3
    "6b0fddf5c8fe7e4b978193142201474dd04eb87a0b930f06b48aead5429cb141" to 90,  // M3_90D
    "08ae3efbd64e8b8d1107f937ca8192ae1f574ab2def0c59d3273d90d70ce2220" to 180, // ZARBDAR_M6
    "fc9d50c952aa688f7d5f98eacf21f49f842a9866b64d2565e0077e45acf377b2" to 180, // M6_180D
    "42ed13abbd84367bb9ac2ecb052ff91945488e101ae1a575f6fd8c00ff416b89" to 365, // ZARBDAR_Y1
    "6859302288da9020b51a9e6a297273b5553324a754ad9ae4fc92297302c763e6" to 365  // Y1_365D
)

private fun hashLicenseCode(code: String): String {
    return try {
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val input = LICENSE_CODE_SALT + code
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        digest.fold("") { str, it -> str + "%02x".format(it) }
    } catch (e: Exception) {
        ""
    }
}

class MainActivity : ComponentActivity() {

    private val db by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { GameRepository(db.dao()) }
    private val viewModel: GameViewModel by viewModels { GameViewModelFactory(repository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Set game to full screen immersive sticky mode
        hideSystemUI()

        // Run silent daily reminder scheduler automatically
        try {
            ReminderScheduler.scheduleDailyReminderAlarm(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Handle events using unified, thread-safe GameAudioManager
        val audioManager = GameAudioManager.getInstance(this)
        lifecycleScope.launch {
            viewModel.currentScreen.collect { screen ->
                audioManager.isWorldMapScreenActive = (screen == GameScreen.WorldMap)
                audioManager.isGameplayScreenActive = (screen == GameScreen.Gameplay)
            }
        }

        lifecycleScope.launch {
            viewModel.soundEvents.collect { event ->
                try {
                    audioManager.handleSoundEvent(this@MainActivity, event)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            com.example.ui.theme.MyApplicationTheme(darkTheme = isDarkMode) {
                // Force completely RTL layout direction for Farsi and scale up fonts everywhere
                val currentDensity = androidx.compose.ui.platform.LocalDensity.current
                CompositionLocalProvider(
                    LocalLayoutDirection provides LayoutDirection.Rtl,
                    androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(
                        density = currentDensity.density,
                        fontScale = (currentDensity.fontScale * 1.25f).coerceAtMost(1.5f) // Scale up fonts globally by 25% but cap at 1.5f to prevent clipping
                    )
                ) {
                    MultiplicationAppContent(viewModel)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Release MediaPlayer resources to prevent background playing or leaks/crashes
        try {
            GameAudioManager.getInstance(this).stopAndRelease()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // Auto-backup progress to the user's Bazaar cloud storage whenever the app goes to
        // the background — only when the user has actually logged in with Bazaar.
        try {
            val accountId = BazaarAuthManager.getSavedAccountId(this)
            if (!accountId.isNullOrEmpty()) {
                val json = viewModel.buildCloudBackupJson(this)
                if (json.isNotEmpty()) {
                    BazaarAuthManager.saveData(this, this, json.toByteArray())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            GameAudioManager.getInstance(this).onDestroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // Release the Bazaar auth/storage service connections.
        BazaarAuthManager.disconnect(this)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    private fun hideSystemUI() {
        try {
            val windowInsetsController = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            windowInsetsController.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Composable
fun ParticleOverlay(particlesList: List<UIParticle>, triggerTick: Long) {
    if (particlesList.isNotEmpty()) {
        val tick = triggerTick
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    textScaleX = 1.0f
                }
                particlesList.forEach { p ->
                    paint.textSize = p.size
                    paint.alpha = (p.alpha * 255).toInt().coerceIn(0, 255)
                    canvas.nativeCanvas.drawText(
                        p.text,
                        p.x,
                        p.y,
                        paint
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MultiplicationAppContent(viewModel: GameViewModel) {
    val screen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val stageStars by viewModel.stageStars.collectAsStateWithLifecycle()
    val tableStats by viewModel.tableStats.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? androidx.activity.ComponentActivity
    
    // ─── C2: Request POST_NOTIFICATIONS at runtime (Android 13+ / API 33) ───
    // The permission is declared in the manifest, but on API 33+ it must ALSO be
    // granted at runtime — otherwise every reminder notification is silently dropped.

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* if denied, reminders just won't display; nothing else to do */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            if (!granted) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    
    var firstLaunchTime by remember { mutableStateOf(com.example.data.SubscriptionManager.getFirstLaunchTime(context)) }
    var permanentLicensed by remember { mutableStateOf(com.example.data.SubscriptionManager.isPermanentLicensed(context)) }
    var licenseExpiryTime by remember { mutableStateOf((com.example.data.SubscriptionManager.getSubscriptionDetails(context)["expireTime"] as? Long) ?: 0L) }

    val rewardRepo = remember(context) {
        com.example.ui.luckywheel.RewardRepository.getInstance(
            context = context.applicationContext,
            gameRepository = com.example.data.GameRepository(com.example.data.AppDatabase.getDatabase(context.applicationContext).dao())
        )
    }

    val wheelViewModel: com.example.ui.luckywheel.WheelViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = com.example.ui.luckywheel.WheelViewModelFactory(
            context = context.applicationContext,
            gameRepository = com.example.data.GameRepository(com.example.data.AppDatabase.getDatabase(context.applicationContext).dao()),
            rewardRepository = rewardRepo,
            rewardEngine = com.example.ui.luckywheel.RewardEngine()
        )
    )

    LaunchedEffect(rewardRepo) {
        viewModel.attachWheelCloudSync(rewardRepo)
    }

    LaunchedEffect(screen) {
        permanentLicensed = com.example.data.SubscriptionManager.isPermanentLicensed(context)
        licenseExpiryTime = (com.example.data.SubscriptionManager.getSubscriptionDetails(context)["expireTime"] as? Long) ?: 0L
        wheelViewModel.refreshState()
    }

    LaunchedEffect(Unit) {
        com.example.data.SubscriptionManager.initFirstLaunchIfNeeded(context)
        firstLaunchTime = com.example.data.SubscriptionManager.getFirstLaunchTime(context)
        
        // Silently fetch cloud data from Bazaar at startup
        com.example.BazaarAuthManager.getData(context, activity) { cloudJson ->
            if (!cloudJson.isNullOrEmpty()) {
                viewModel.maybeRestoreFromCloud(context, cloudJson)
            }
        }
        
        // Periodically check license status to react to cloud sync or expiration
        while (true) {
            kotlinx.coroutines.delay(2000)
            firstLaunchTime = com.example.data.SubscriptionManager.getFirstLaunchTime(context)
            permanentLicensed = com.example.data.SubscriptionManager.isPermanentLicensed(context)
            licenseExpiryTime = (com.example.data.SubscriptionManager.getSubscriptionDetails(context)["expireTime"] as? Long) ?: 0L
        }
    }

    LaunchedEffect(Unit) {
        viewModel.cloudBackupRequests.collect {
            val savedId = BazaarAuthManager.getSavedAccountId(context)
            if (!savedId.isNullOrEmpty()) {
                val json = viewModel.buildCloudBackupJson(context)
                if (json.isNotEmpty()) {
                    BazaarAuthManager.saveData(context, activity, json.toByteArray()) { ok ->
                        // Silent auto-backup
                    }
                }
            }
        }
    }

    val lastBackTime = remember { mutableStateOf(0L) }
    androidx.activity.compose.BackHandler {
        if (screen == GameScreen.Onboarding) {
            // Do nothing, force user to complete onboarding name entry
        } else if (screen != GameScreen.MainMenu && screen != GameScreen.Login) {
            viewModel.navigateTo(GameScreen.MainMenu)
        } else {
            val now = System.currentTimeMillis()
            if (now - lastBackTime.value < 2000L) {
                (context as? android.app.Activity)?.finish()
            } else {
                lastBackTime.value = now
                android.widget.Toast.makeText(context, "برای خروج از بازی، دوباره دکمه برگشت را بزنید", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    val payment = remember(context) {
        val rsaKey = "MIHNMA0GCSqGSIb3DQEBAQUAA4G7ADCBtwKBrwDHRnnGbNC8cSDKxFac+ZHOkpqoneXVzZolZLSrpRtzekWI5AcKK7Hk+J7lNsEyxsMbwkLlgBS8UOM8iK1PCCOpWWfEkCooZvvKrpeZ7nVIao07nk1ZvR5LbwhFWwDkk0ibnxjIH5liCH0ra0TtFUGW95Tm0HPbKoLq96KBHfUo1V+HlWv/pb5cAnoSZG0Q0qgmbgWngGeE0/Jwc9EHV5w6r5SEaiLytf0KX7SGjzkCAwEAAQ=="
        val config = ir.cafebazaar.poolakey.config.PaymentConfiguration(
            localSecurityCheck = ir.cafebazaar.poolakey.config.SecurityCheck.Enable(rsaPublicKey = rsaKey)
        )
        ir.cafebazaar.poolakey.Payment(context, config)
    }

    var poolakeyConnected by remember { mutableStateOf(false) }

    DisposableEffect(payment) {
        val connection = payment.connect {
            connectionSucceed {
                poolakeyConnected = true
                android.util.Log.d("MULTIPLY_BILLING", "Connected to Cafe Bazaar Poolakey successfully!")
                com.example.data.SubscriptionManager.syncSubscription(context, payment) { success ->
                    permanentLicensed = com.example.data.SubscriptionManager.isPermanentLicensed(context)
                    licenseExpiryTime = (com.example.data.SubscriptionManager.getSubscriptionDetails(context)["expireTime"] as? Long) ?: 0L
                }
            }
            connectionFailed { error ->
                poolakeyConnected = false
                android.util.Log.w("MULTIPLY_BILLING", "Failed to connect to Poolakey: ${error.message}")
            }
            disconnected {
                poolakeyConnected = false
            }
        }
        onDispose {
            try {
                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val registry = activity?.activityResultRegistry

    // Bazaar Login
    val bazaarSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val account = BazaarAuthManager.parseAccount(result.data)
        if (account != null && account.accountId.isNotEmpty()) {
            BazaarAuthManager.saveAccountId(context, account.accountId)
            android.widget.Toast.makeText(context, "✅ ورود با بازار با موفقیت انجام شد", android.widget.Toast.LENGTH_SHORT).show()
            BazaarAuthManager.getData(context, activity) { cloudJson ->
                viewModel.onBazaarAuthenticated(context, cloudJson)
            }
        } else {
            android.widget.Toast.makeText(
                context, 
                "ورود انجام نشد، لطفاً دوباره تلاش کنید.", 
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
    val onBazaarLogin: () -> Unit = {
        when {
            !BazaarAuthManager.isBazaarInstalled(context) -> {
                android.widget.Toast.makeText(context, "برای ورود، ابتدا برنامهٔ کافه‌بازار را نصب کنید.", android.widget.Toast.LENGTH_LONG).show()
                BazaarAuthManager.showInstall(context)
            }
            BazaarAuthManager.needsUpdateForAuth(context) -> {
                android.widget.Toast.makeText(context, "برای ورود، کافه‌بازار را به‌روزرسانی کنید.", android.widget.Toast.LENGTH_LONG).show()
                BazaarAuthManager.showUpdate(context)
            }
            else -> {
                try {
                    bazaarSignInLauncher.launch(BazaarAuthManager.getSignInIntent(context))
                } catch (e: Exception) {
                    android.widget.Toast.makeText(context, "خطا در باز کردن صفحهٔ ورود بازار", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Manual cloud sync actions (used by the Support dialog).
    val onCloudBackup: () -> Unit = {
        val savedId = BazaarAuthManager.getSavedAccountId(context)
        if (savedId.isNullOrEmpty()) {
            android.widget.Toast.makeText(context, "برای همگامسازی، ابتدا با حساب بازار وارد شوید.", android.widget.Toast.LENGTH_SHORT).show()
        } else {
            val json = viewModel.buildCloudBackupJson(context)
            if (json.isEmpty()) {
                android.widget.Toast.makeText(context, "فعلاً پیشرفتی برای پشتیبانگیری وجود ندارد.", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                BazaarAuthManager.saveData(context, activity, json.toByteArray()) { ok ->
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        android.widget.Toast.makeText(
                            context,
                            if (ok) "✅ پشتیبانگیری در بازار انجام شد" else "پشتیبانگیری ناموفق بود. دوباره تلاش کنید.",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
    val onCloudRestore: () -> Unit = {
        val savedId = BazaarAuthManager.getSavedAccountId(context)
        if (savedId.isNullOrEmpty()) {
            android.widget.Toast.makeText(context, "برای بازیابی، ابتدا با حساب بازار وارد شوید.", android.widget.Toast.LENGTH_SHORT).show()
        } else {
            BazaarAuthManager.getData(context, activity) { cloudJson ->
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    if (cloudJson.isNullOrEmpty()) {
                        android.widget.Toast.makeText(context, "نسخهٔ پشتیبانی در بازار پیدا نشد.", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.forceRestoreFromCloud(context, cloudJson)
                        android.widget.Toast.makeText(context, "✅ پیشرفت از بازار بازیابی شد", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    val onBuySubscription: (String) -> Unit = { productId ->
        if (registry == null) {
            android.widget.Toast.makeText(context, "خطا در بارگذاری سامانه بازار", android.widget.Toast.LENGTH_SHORT).show()
        } else {
            val purchaseRequest = ir.cafebazaar.poolakey.request.PurchaseRequest(
                productId = productId,
                payload = "multiply_user_sub"
            )
            payment.subscribeProduct(registry, purchaseRequest) {
                purchaseFlowBegan {
                    android.util.Log.d("MULTIPLY_BILLING", "Purchase flow began for $productId")
                }
                failedToBeginFlow { throwable ->
                    android.widget.Toast.makeText(context, "خطا در شروع فرآیند خرید: ${throwable.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
                purchaseSucceed { purchaseInfo ->
                    android.widget.Toast.makeText(context, "🎉 خرید اشتراک با موفقیت انجام شد!", android.widget.Toast.LENGTH_LONG).show()
                    com.example.data.SubscriptionManager.setPermanentLicensed(context, false)
                    permanentLicensed = false
                    
                    // Securely record purchase details via SubscriptionManager
                    com.example.data.SubscriptionManager.recordPurchase(
                        context = context,
                        productId = productId,
                        purchaseToken = purchaseInfo.purchaseToken,
                        purchaseTime = purchaseInfo.purchaseTime
                    )
                    
                    licenseExpiryTime = (com.example.data.SubscriptionManager.getSubscriptionDetails(context)["expireTime"] as? Long) ?: 0L
                }
                purchaseCanceled {
                    android.widget.Toast.makeText(context, "فرآیند خرید لغو شد.", android.widget.Toast.LENGTH_SHORT).show()
                }
                purchaseFailed { throwable ->
                    android.widget.Toast.makeText(context, "خطا در انجام خرید اشتراک: ${throwable.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    var remainingTimeMillis by remember(firstLaunchTime, permanentLicensed, licenseExpiryTime) {
        mutableStateOf(com.example.data.SubscriptionManager.getRemainingTimeMs(context))
    }

    LaunchedEffect(firstLaunchTime, permanentLicensed, licenseExpiryTime) {
        if (!permanentLicensed) {
            while (true) {
                kotlinx.coroutines.delay(10000)
                remainingTimeMillis = com.example.data.SubscriptionManager.getRemainingTimeMs(context)
            }
        }
    }

    val isLicenseExpired = remember(permanentLicensed, remainingTimeMillis) {
        com.example.data.SubscriptionManager.isLicenseExpired(context)
    }

    val processActivationCode: (String) -> Boolean = { code ->
        val trimmed = code.trim()
        val hashed = hashLicenseCode(trimmed)
        var valid = false
        
        if (hashed in PERMANENT_LICENSE_HASHES) {
            com.example.data.SubscriptionManager.recordPermanentActivationCode(context)
            permanentLicensed = true
            licenseExpiryTime = 0L
            valid = true
        } else {
            val days = TIMED_LICENSE_HASHES[hashed]
            if (days != null) {
                com.example.data.SubscriptionManager.recordTimedActivationCode(context, days)
                permanentLicensed = false
                licenseExpiryTime = (com.example.data.SubscriptionManager.getSubscriptionDetails(context)["expireTime"] as? Long) ?: 0L
                valid = true
            }
        }
        
        valid
    }

    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val activeThemeId = profile?.activeTheme ?: "default"
    val baseTheme = GameConfig.THEMES.firstOrNull { it.id == activeThemeId } ?: GameConfig.THEMES[0]

    val themeConfig = remember(isDarkMode, baseTheme) {
        if (isDarkMode) {
            baseTheme
        } else {
            val lightBgStart = when (baseTheme.id) {
                "ocean" -> Color(0xFFE0F2FE) // Elegant sky blue
                "forest" -> Color(0xFFDCFCE7) // Minty green
                "sunset" -> Color(0xFFFCE7F3) // Cute Rose Pink
                else -> Color(0xFFF3E8FF) // Dreamy Lavender
            }
            val lightBgEnd = when (baseTheme.id) {
                "ocean" -> Color(0xFFBAE6FD) // Light Azure Blue
                "forest" -> Color(0xFFBBF7D0) // Pastel Leaf Green
                "sunset" -> Color(0xFFFBCFE8) // Warm Tulip Pink
                else -> Color(0xFFE9D5FF) // Light Amethyst Purple
            }
            val cardBgColor = when (baseTheme.id) {
                "ocean" -> Color(0xFFF0F9FF)
                "forest" -> Color(0xFFF0FDF4)
                "sunset" -> Color(0xFFFFF5F7)
                else -> Color(0xFFFAF5FF)
            }
            GameTheme(
                id = baseTheme.id,
                name = baseTheme.name,
                preview = baseTheme.preview,
                price = baseTheme.price,
                bgStart = lightBgStart,
                bgEnd = lightBgEnd,
                cardBg = cardBgColor,
                primaryColor = baseTheme.primaryColor
            )
        }
    }

    var showSupportDialog by remember { mutableStateOf(false) }

    // Screen-by-Screen particles trigger list
    val particlesList = remember { mutableListOf<UIParticle>() }
    var particleTriggerTick by remember { androidx.compose.runtime.mutableStateOf(0L) }
    val coroutineScope = rememberCoroutineScope()

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    // Particles trigger effect on right answer
    LaunchedEffect(Unit) {
        viewModel.soundEvents.collectLatest { event ->
            if (event == GameSoundEvent.StarSound && viewModel.levelUpTrigger.value == null) {
                val icon = when (profile?.activeEffect ?: "stars") {
                    "fire" -> listOf("🔥", "💥", "⚡").random()
                    "snow" -> listOf("❄️", "⛄", "🌨️").random()
                    "rainbow" -> listOf("🌈", "✨", "💜", "💙").random()
                    else -> listOf("⭐", "🌟", "💫", "✨").random()
                }
                coroutineScope.launch {
                    repeat(32) {
                        particlesList.add(
                            UIParticle(
                                x = (0..(screenWidthPx.toInt().coerceAtLeast(100))).random().toFloat(),
                                y = (screenHeightPx * 0.15f).toInt().coerceAtLeast(50).let { startY ->
                                    (startY..(startY + 150)).random().toFloat()
                                },
                                vx = (-12..12).random().toFloat(),
                                vy = (-18..-2).random().toFloat(),
                                alpha = 1.0f,
                                size = (26..46).random().toFloat(),
                                text = icon
                            )
                        )
                    }
                    particleTriggerTick = System.currentTimeMillis()
                }
            }
        }
    }

    // Tick simulation for rendering particles
    LaunchedEffect(Unit) {
        while (true) {
            androidx.compose.runtime.withFrameMillis { time ->
                if (particlesList.isNotEmpty()) {
                    val iterator = particlesList.iterator()
                    while (iterator.hasNext()) {
                        val p = iterator.next()
                        p.x += p.vx
                        p.y += p.vy
                        p.vy += 0.45f // gravity
                        p.alpha -= 0.018f
                        if (p.alpha <= 0f) {
                            iterator.remove()
                        }
                    }
                    particleTriggerTick = time
                }
            }
        }
    }

    if (isLicenseExpired) {
        LicenseBlockOverlay(
            viewModel = viewModel,
            profile = profile,
            themeConfig = themeConfig,
            remainingTimeMs = remainingTimeMillis,
            licenseExpiryTime = licenseExpiryTime,
            onBuySubscription = onBuySubscription
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (screen != GameScreen.Gameplay && screen != GameScreen.SpeedChallenge && screen != GameScreen.ReverseChallenge && screen != GameScreen.DuelSetup && screen != GameScreen.DuelGame && screen != GameScreen.DuelResult && screen != GameScreen.Login && screen != GameScreen.Subscription && screen != GameScreen.Onboarding) {
                        BottomNavigationWidget(currentScreen = screen, onNavigate = { viewModel.navigateTo(it) }, theme = themeConfig)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "طراح و توسعه: سید مجید حسینی".toFa(),
                                color = if (isDarkMode) Color.White.copy(alpha = 0.55f) else Color(0xFF475569).copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(themeConfig.bgStart, themeConfig.bgEnd)
                        )
                    )
                    .padding(innerPadding)
            ) {
                // Twinkling stars or space dots behind content
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Background ambient circles
                    drawCircle(
                        color = themeConfig.primaryColor.copy(alpha = 0.05f),
                        radius = 350.dp.toPx(),
                        center = Offset(size.width * 0.2f, size.height * 0.3f)
                    )
                    drawCircle(
                        color = Color.Yellow.copy(alpha = 0.03f),
                        radius = 280.dp.toPx(),
                        center = Offset(size.width * 0.8f, size.height * 0.7f)
                    )
                }

                // Main Core Content screen navigator
                Crossfade(targetState = screen, label = "ScreenTransition") { activeScreen ->
                    when (activeScreen) {
                        GameScreen.Login -> LoginScreen(viewModel, themeConfig, onBazaarLogin)
                        GameScreen.Onboarding -> OnboardingScreen(viewModel, themeConfig)
                        GameScreen.MainMenu -> MainMenuScreen(
                            viewModel = viewModel,
                            profile = profile,
                            themeConfig = themeConfig,
                            permanentLicensed = permanentLicensed,
                            licenseExpiryTime = licenseExpiryTime,
                            remainingTimeMs = remainingTimeMillis,
                            onShowSupport = { showSupportDialog = it }
                        )
                        GameScreen.WorldMap -> WorldMapScreen(viewModel, profile, stageStars, themeConfig)
                        GameScreen.Gameplay -> GameplayScreen(viewModel, themeConfig)
                        GameScreen.StageResult -> StageResultScreen(viewModel, themeConfig)
                        GameScreen.SpeedChallenge -> SpeedChallengeScreen(viewModel, themeConfig)
                        GameScreen.ReverseChallenge -> ReverseChallengeScreen(viewModel, themeConfig)
                        GameScreen.DuelSetup -> DuelSetupScreen(viewModel, themeConfig)
                        GameScreen.DuelGame -> DuelGameScreen(viewModel, themeConfig)
                        GameScreen.DuelResult -> {
                            var showStats by remember { mutableStateOf(false) }
                            
                            if (showStats) {
                                DuelStatsScreen(viewModel, themeConfig)
                            } else {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Box(modifier = Modifier.blur(16.dp)) {
                                        DuelGameScreen(viewModel, themeConfig)
                                    }
                                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f))
                                        .pointerInput(Unit) { detectTapGestures { } } // Block touches
                                    )
                                    DuelResultScreen(viewModel, themeConfig, onShowStats = { showStats = true })
                                }
                            }
                        }
                        GameScreen.Shop -> ShopScreen(viewModel, profile, themeConfig)
                        GameScreen.Achievements -> AchievementsScreen(viewModel, profile, themeConfig)
                        GameScreen.ProgressReport -> ProgressReportScreen(viewModel, profile, tableStats, themeConfig)
                        GameScreen.LearnTables -> LearnTablesScreen(viewModel, themeConfig)
                        GameScreen.LuckyWheel -> com.example.ui.luckywheel.LuckyWheelScreen(
                            viewModel = wheelViewModel,
                            themeConfig = themeConfig,
                            onBackClick = { viewModel.navigateTo(GameScreen.MainMenu) },
                            onSpinComplete = {
                                try {
                                    val savedId = BazaarAuthManager.getSavedAccountId(context)
                                    if (!savedId.isNullOrEmpty()) {
                                        val json = viewModel.buildCloudBackupJson(context)
                                        if (json.isNotEmpty()) {
                                            BazaarAuthManager.saveData(context, activity, json.toByteArray()) { ok ->
                                                // Silent auto-backup immediate after lucky wheel spin finished
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        )
                        GameScreen.Subscription -> SubscriptionScreen(
                            viewModel = viewModel,
                            profile = profile,
                            themeConfig = themeConfig,
                            permanentLicensed = permanentLicensed,
                            licenseExpiryTime = licenseExpiryTime,
                            remainingTimeMs = remainingTimeMillis,
                            onBuySubscription = onBuySubscription
                        )
                    }
                }

                if (showSupportDialog) {
                    SupportDialog(
                        onDismiss = { showSupportDialog = false },
                        themeConfig = themeConfig,
                        permanentLicensed = permanentLicensed,
                        licenseExpiryTime = licenseExpiryTime,
                        remainingTimeMs = remainingTimeMillis,
                        onActivateCode = { code ->
                            processActivationCode(code)
                        },
                        onBuySubscription = onBuySubscription,
                        isLight = !isDarkMode,
                        onBazaarLogin = onBazaarLogin,
                        onCloudBackup = onCloudBackup,
                        onCloudRestore = onCloudRestore
                    )
                }

                // Canvas Floating Particle Overlay
                ParticleOverlay(particlesList = particlesList, triggerTick = particleTriggerTick)

                // Active daily reward overlay popup
                val dailyCoinReward by viewModel.activeDailyAward.collectAsStateWithLifecycle()
                val localDailyCoinReward = dailyCoinReward
                if (localDailyCoinReward != null) {
                    DailyRewardOverlay(
                        coinsReward = localDailyCoinReward,
                        onDismiss = { viewModel.dismissDailyOverlay() },
                        themeConfig = themeConfig
                    )
                }

                // Active unlocked Achievement overlay popup
                val achievementUnlocked by viewModel.activeAchievementAward.collectAsStateWithLifecycle()
                val localAchievementUnlocked = achievementUnlocked
                if (localAchievementUnlocked != null) {
                    AchievementUnlockedOverlay(
                        achievement = localAchievementUnlocked,
                        onDismiss = { viewModel.dismissAchievementOverlay() },
                        themeConfig = themeConfig
                    )
                }

                // Active level-up overlay popup
                val levelUpTriggerValue by viewModel.levelUpTrigger.collectAsStateWithLifecycle()
                val localLevelUpTriggerValue = levelUpTriggerValue
                if (localLevelUpTriggerValue != null) {
                    LevelUpOverlay(
                        level = localLevelUpTriggerValue,
                        onDismiss = { viewModel.dismissLevelUp() },
                        themeConfig = themeConfig
                    )
                }
            }
        }
    }
}

// ─── COMPOSABLE COMPONENT OVERLAYS ───

@Composable
fun DailyRewardOverlay(coinsReward: Int, onDismiss: () -> Unit, themeConfig: GameTheme) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable(enabled = true, onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .shadow(24.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = themeConfig.cardBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎁",
                    fontSize = 52.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = "جایزه روزانه!",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "آفرین قهرمان! که هر روز میا‌ی درس می‌خونی. این عادت آدم‌های موفقه! 💪",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🪙",
                        fontSize = 24.sp,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = "+${coinsReward.toFa()} سکه",
                        color = Color(0xFFFFD700),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = themeConfig.primaryColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("دمت گرم! دشت کردم 🪙", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun AchievementUnlockedOverlay(achievement: GameAchievement, onDismiss: () -> Unit, themeConfig: GameTheme) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable(enabled = true, onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .shadow(24.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = themeConfig.cardBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = achievement.icon,
                    fontSize = 58.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = "دستاورد جدید باز شد! 🏆",
                    color = Color(0xFFFFD700),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = achievement.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = achievement.desc,
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 18.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "جایزه دستاورد: 🪙",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = "+${achievement.reward.toFa()} سکه طلایی",
                        color = Color(0xFFFFD700),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = themeConfig.primaryColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ادامه مبارزه ریاضی 🚀", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─── SCREEN 1: MAIN MENU SCREEN ───

@Composable
fun MainMenuScreen(
    viewModel: GameViewModel,
    profile: UserProfileEntity?,
    themeConfig: GameTheme,
    permanentLicensed: Boolean,
    licenseExpiryTime: Long,
    remainingTimeMs: Long,
    onShowSupport: (Boolean) -> Unit
) {
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val bannerText by viewModel.dailyRewardBannerText.collectAsStateWithLifecycle()
    val isDailyClickable by viewModel.dailyRewardClickable.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    val coinLabel = (profile?.coins ?: 50).toFa()
    val gemLabel = (profile?.gems ?: 0).toFa()
    val bestSpeed = (profile?.bestSpeed ?: 0).toFa()

    val currentAvatar = profile?.activeAvatar ?: "owl"
    val avatarIcon = GameConfig.AVATARS.find { it.id == currentAvatar }?.icon ?: "🦉"

    val subscriptionPillText = remember(permanentLicensed, remainingTimeMs) {
        if (permanentLicensed) {
            "✨ اشتراک دائمی فعال و بی‌زمان"
        } else {
            val days = (remainingTimeMs / (1000 * 60 * 60 * 24)).toInt()
            val hours = ((remainingTimeMs / (1000 * 60 * 60)) % 24).toInt()
            "${days.toFa()} روز و ${hours.toFa()} ساعت باقی مانده"
        }
    }

    val subscriptionPillColors = remember(permanentLicensed, remainingTimeMs, isDarkMode) {
        val baseColor = if (permanentLicensed) {
            Color(0xFFFFD700) // Golden Yellow
        } else {
            val days = (remainingTimeMs / (1000.0 * 60.0 * 60.0 * 24.0)).toFloat()
            if (days >= 7f) {
                Color(0xFFFFD700) // Golden Yellow (more than 7 days)
            } else if (days <= 0f) {
                Color(0xFFEF4444) // Bright Red
            } else {
                // Interpolate between Gold (255, 215, 0) and Red (239, 68, 68)
                val fraction = (days / 7f).coerceIn(0f, 1f)
                val r = (239 + (255 - 239) * fraction).toInt()
                val g = (68 + (215 - 68) * fraction).toInt()
                val b = (68 + (0 - 68) * fraction).toInt()
                Color(r, g, b)
            }
        }
        
        val bg = baseColor.copy(alpha = if (isDarkMode) 0.25f else 0.85f)
        val border = baseColor
        val text = if (isDarkMode) {
            if (permanentLicensed || remainingTimeMs > 2 * 24 * 60 * 60 * 1000L) {
                Color(0xFFFFEEAA) // Pale golden
            } else {
                Color(0xFFFECACA) // Pale red
            }
        } else {
            if (permanentLicensed || remainingTimeMs > 2 * 24 * 60 * 60 * 1000L) {
                Color(0xFF785900) // Dark gold
            } else {
                Color(0xFF7F1D1D) // Dark red
            }
        }
        Triple(bg, border, text)
    }

    val bgGradient = if (isDarkMode) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0D0B21), // Deep cosmic purple
                Color(0xFF161233), // Midnight dark violet
                Color(0xFF261042)  // Low deep royal neon purple
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF3E8FF), // Pastel purple-pink gradients
                Color(0xFFE9D5FF), 
                Color(0xFFDDD6FE)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = bgGradient)
    ) {
        // Soft Translucent Glowing Circles, Blurred magical particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (isDarkMode) {
                // Soft neon violet circle top-right
                drawCircle(
                    color = Color(0x1C9333EA),
                    radius = 200.dp.toPx(),
                    center = Offset(size.width * 0.85f, size.height * 0.15f)
                )
                // Soft deep pink circle bottom-left
                drawCircle(
                    color = Color(0x13EC4899),
                    radius = 240.dp.toPx(),
                    center = Offset(size.width * 0.15f, size.height * 0.8f)
                )
            } else {
                // Soft pastel purple circle top-right
                drawCircle(
                    color = Color(0x1F8B5CF6),
                    radius = 180.dp.toPx(),
                    center = Offset(size.width * 0.85f, size.height * 0.2f)
                )
                // Soft pastel pink circle bottom-left
                drawCircle(
                    color = Color(0x1BEC4899),
                    radius = 220.dp.toPx(),
                    center = Offset(size.width * 0.15f, size.height * 0.75f)
                )
            }
        }

        // Beautiful cosmic stars decoration
        Box(modifier = Modifier.fillMaxSize()) {
            if (isDarkMode) {
                Text("⭐", fontSize = 15.sp, modifier = Modifier.align(Alignment.TopStart).padding(start = 32.dp, top = 140.dp).alpha(0.35f))
                Text("✨", fontSize = 18.sp, modifier = Modifier.align(Alignment.TopEnd).padding(end = 40.dp, top = 220.dp).alpha(0.45f))
                Text("🌟", fontSize = 22.sp, modifier = Modifier.align(Alignment.CenterStart).padding(start = 60.dp, top = 80.dp).alpha(0.5f))
                Text("⭐", fontSize = 14.sp, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 45.dp, bottom = 160.dp).alpha(0.4f))
            } else {
                Text("✨", fontSize = 16.sp, modifier = Modifier.align(Alignment.TopStart).padding(start = 32.dp, top = 140.dp).alpha(0.5f))
                Text("🌸", fontSize = 18.sp, modifier = Modifier.align(Alignment.TopEnd).padding(end = 40.dp, top = 220.dp).alpha(0.4f))
                Text("🌟", fontSize = 20.sp, modifier = Modifier.align(Alignment.CenterStart).padding(start = 60.dp, top = 80.dp).alpha(0.55f))
                Text("✨", fontSize = 15.sp, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 45.dp, bottom = 160.dp).alpha(0.5f))
            }
        }

        // Main content column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Right Side: Player Profile Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF1E1B4B).copy(alpha = 0.8f) else Color.White),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .widthIn(max = 180.dp)
                        .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = Color(0x269B4DFF), spotColor = Color(0x269B4DFF))
                        .border(1.dp, if (isDarkMode) Color(0xFF312E81) else Color(0xFFEFE4FF), RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Wise Owl Avatar
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(if (isDarkMode) Color(0xFF312E81) else Color(0xFFEFE4FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(avatarIcon, fontSize = 28.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = profile?.name ?: "مجید",
                                color = if (isDarkMode) Color.White else Color(0xFF9B4DFF),
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "قهرمان جوان",
                                color = if (isDarkMode) Color(0xFFA78BFA) else Color(0xFF706FD3),
                                fontWeight = FontWeight.Medium,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                // Left Side: Column with glass tools and counters below
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Glassmorphism controls card
                    Surface(
                        color = if (isDarkMode) Color(0x33000000) else Color(0xA6FFFFFF),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, if (isDarkMode) Color(0x33FFFFFF) else Color(0x73FFFFFF)),
                        modifier = Modifier.shadow(4.dp, RoundedCornerShape(24.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Moon Mode Button
                            IconButton(
                                onClick = { viewModel.toggleDarkMode() },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Text(if (isDarkMode) "☀️" else "🌙", fontSize = 18.sp)
                            }
                            


                            // Gift Button
                            IconButton(
                                onClick = {
                                    if (isDailyClickable) {
                                        viewModel.claimDailyReward()
                                    } else {
                                        android.widget.Toast.makeText(context, "$bannerText 📦⭐", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Text(if (isDailyClickable) "🎁" else "📦", fontSize = 18.sp)
                            }
                        }
                    }

                    // Diamond & Coin counters below
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Diamond Card
                        Surface(
                            color = if (isDarkMode) Color(0xFF1E1B4B).copy(alpha = 0.8f) else Color.White,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF312E81) else Color(0xFFC084FC).copy(alpha = 0.3f)),
                            modifier = Modifier.shadow(2.dp, RoundedCornerShape(14.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("💎", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = gemLabel,
                                    color = if (isDarkMode) Color(0xFFC084FC) else Color(0xFF9B4DFF),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Coin Card
                        Surface(
                            color = if (isDarkMode) Color(0xFF1E1B4B).copy(alpha = 0.8f) else Color.White,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF312E81) else Color(0xFFFFD700).copy(alpha = 0.3f)),
                            modifier = Modifier.shadow(2.dp, RoundedCornerShape(14.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🪙", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = coinLabel,
                                    color = Color(0xFFFFB300),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Branding Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "قهرمان جدول ضرب",
                    fontSize = 28.sp,
                    color = if (isDarkMode) Color(0xFFC084FC) else Color(0xFF5B10D3),
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        shadow = Shadow(
                            color = if (isDarkMode) Color(0x80C084FC) else Color(0x335B10D3),
                            offset = Offset(0f, 4f),
                            blurRadius = 8f
                        )
                    )
                )
            }

            // Center play button CTA
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // Small glowing particles around button
                    Text("✨", fontSize = 16.sp, modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp).alpha(0.8f))
                    Text("🌟", fontSize = 14.sp, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp).alpha(0.7f))
                    Canvas(modifier = Modifier.size(8.dp).align(Alignment.TopStart).padding(start = 48.dp, top = 2.dp)) {
                        drawCircle(color = Color(0xFFFFD84D), radius = 3.dp.toPx())
                    }
                    Canvas(modifier = Modifier.size(8.dp).align(Alignment.BottomEnd).padding(end = 48.dp, bottom = 2.dp)) {
                        drawCircle(color = Color(0xFF66E3C4), radius = 4.dp.toPx())
                    }

                    Button(
                        onClick = { viewModel.navigateTo(GameScreen.WorldMap) },
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(32.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(60.dp)
                            .shadow(
                                elevation = 16.dp,
                                shape = RoundedCornerShape(32.dp),
                                ambientColor = if (isDarkMode) Color(0x80A855F7) else Color(0x4D8C46FF),
                                spotColor = if (isDarkMode) Color(0x80A855F7) else Color(0x4D8C46FF)
                            )
                            .background(
                                brush = Brush.linearGradient(
                                    colors = if (isDarkMode) listOf(Color(0xFFA855F7), Color(0xFF9333EA)) else listOf(Color(0xFFB44DFF), Color(0xFF8A3DFF))
                                ),
                                shape = RoundedCornerShape(32.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("🗺️", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "شروع بازی روی نقشه!",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Subscription timer pill below button
                Surface(
                    color = subscriptionPillColors.first,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.2.dp, subscriptionPillColors.second),
                    modifier = Modifier
                        .shadow(3.dp, RoundedCornerShape(20.dp))
                        .clickable { viewModel.navigateTo(GameScreen.Subscription) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("⏳", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = subscriptionPillText,
                            color = subscriptionPillColors.third,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            // Grid cards section (2 columns, 2 rows)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        onClick = { viewModel.navigateTo(GameScreen.WorldMap) },
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(1.dp, if (isDarkMode) Color(0x26FFFFFF) else Color.White.copy(alpha = 0.5f)),
                        colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF1E1B4B).copy(alpha = 0.75f) else Color.White.copy(alpha = 0.75f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(116.dp)
                            .shadow(
                                elevation = 10.dp,
                                shape = RoundedCornerShape(28.dp),
                                ambientColor = if (isDarkMode) Color(0x1A000000) else Color(0x1A785AB4),
                                spotColor = if (isDarkMode) Color(0x1A000000) else Color(0x1A785AB4)
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(Color(0xFF66E3C4).copy(alpha = 0.25f), Color(0xFF66E3C4).copy(alpha = 0.05f))
                                        ),
                                        shape = CircleShape
                                    )
                                    .border(1.dp, Color(0xFF66E3C4).copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🗺️", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "مراحل نقشه",
                                color = if (isDarkMode) Color(0xFFE9D5FF) else Color(0xFF5B10D3),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "سفر گام به گام",
                                color = if (isDarkMode) Color(0xFFA78BFA) else Color(0xFF706FD3),
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Card(
                        onClick = { viewModel.navigateTo(GameScreen.LearnTables) },
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(1.dp, if (isDarkMode) Color(0x26FFFFFF) else Color.White.copy(alpha = 0.5f)),
                        colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF1E1B4B).copy(alpha = 0.75f) else Color.White.copy(alpha = 0.75f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(116.dp)
                            .shadow(
                                elevation = 10.dp,
                                shape = RoundedCornerShape(28.dp),
                                ambientColor = if (isDarkMode) Color(0x1A000000) else Color(0x1A785AB4),
                                spotColor = if (isDarkMode) Color(0x1A000000) else Color(0x1A785AB4)
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(Color(0xFF9B4DFF).copy(alpha = 0.25f), Color(0xFF9B4DFF).copy(alpha = 0.05f))
                                        ),
                                        shape = CircleShape
                                    )
                                    .border(1.dp, Color(0xFF9B4DFF).copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📚", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "کلاس یادگیری",
                                color = if (isDarkMode) Color(0xFFE9D5FF) else Color(0xFF5B10D3),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "مرور و تمرین",
                                color = if (isDarkMode) Color(0xFFA78BFA) else Color(0xFF706FD3),
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        onClick = { viewModel.startSpeedChallenge() },
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(1.dp, if (isDarkMode) Color(0x26FFFFFF) else Color.White.copy(alpha = 0.5f)),
                        colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF1E1B4B).copy(alpha = 0.75f) else Color.White.copy(alpha = 0.75f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(116.dp)
                            .shadow(
                                elevation = 10.dp,
                                shape = RoundedCornerShape(28.dp),
                                ambientColor = if (isDarkMode) Color(0x1A000000) else Color(0x1A785AB4),
                                spotColor = if (isDarkMode) Color(0x1A000000) else Color(0x1A785AB4)
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(Color(0xFFFFD84D).copy(alpha = 0.25f), Color(0xFFFFD84D).copy(alpha = 0.05f))
                                        ),
                                        shape = CircleShape
                                    )
                                    .border(1.dp, Color(0xFFFFD84D).copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⚡", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "چالش سرعت",
                                color = if (isDarkMode) Color(0xFFE9D5FF) else Color(0xFF5B10D3),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "رکورد: $bestSpeed امتیاز",
                                color = if (isDarkMode) Color(0xFFA78BFA) else Color(0xFF706FD3),
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Card(
                        onClick = { viewModel.navigateTo(GameScreen.ReverseChallenge) },
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(1.dp, if (isDarkMode) Color(0x26FFFFFF) else Color.White.copy(alpha = 0.5f)),
                        colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF1E1B4B).copy(alpha = 0.75f) else Color.White.copy(alpha = 0.75f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(116.dp)
                            .shadow(
                                elevation = 10.dp,
                                shape = RoundedCornerShape(28.dp),
                                ambientColor = if (isDarkMode) Color(0x1A000000) else Color(0x1A785AB4),
                                spotColor = if (isDarkMode) Color(0x1A000000) else Color(0x1A785AB4)
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(Color(0xFF00D2D3).copy(alpha = 0.25f), Color(0xFF00D2D3).copy(alpha = 0.05f))
                                        ),
                                        shape = CircleShape
                                    )
                                    .border(1.dp, Color(0xFF00D2D3).copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🔄", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "جدول معکوس",
                                color = if (isDarkMode) Color(0xFFE9D5FF) else Color(0xFF5B10D3),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "جواب را پیدا کن",
                                color = if (isDarkMode) Color(0xFFA78BFA) else Color(0xFF706FD3),
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Row 3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        onClick = { viewModel.goToDuelSetup() },
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(1.dp, if (isDarkMode) Color(0x26FFFFFF) else Color.White.copy(alpha = 0.5f)),
                        colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF1E1B4B).copy(alpha = 0.75f) else Color.White.copy(alpha = 0.75f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(116.dp)
                            .shadow(
                                elevation = 10.dp,
                                shape = RoundedCornerShape(28.dp),
                                ambientColor = if (isDarkMode) Color(0x1A000000) else Color(0x1A785AB4),
                                spotColor = if (isDarkMode) Color(0x1A000000) else Color(0x1A785AB4)
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(Color(0xFF7F77DD).copy(alpha = 0.25f), Color(0xFF7F77DD).copy(alpha = 0.05f))
                                        ),
                                        shape = CircleShape
                                    )
                                    .border(1.dp, Color(0xFF7F77DD).copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👥", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "چالش دو نفره",
                                color = if (isDarkMode) Color(0xFFE9D5FF) else Color(0xFF5B10D3),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "نبرد دو نفره",
                                color = if (isDarkMode) Color(0xFFA78BFA) else Color(0xFF706FD3),
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Card(
                        onClick = { viewModel.navigateTo(GameScreen.Shop) },
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(1.dp, if (isDarkMode) Color(0x26FFFFFF) else Color.White.copy(alpha = 0.5f)),
                        colors = CardDefaults.cardColors(containerColor = if (isDarkMode) Color(0xFF1E1B4B).copy(alpha = 0.75f) else Color.White.copy(alpha = 0.75f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(116.dp)
                            .shadow(
                                elevation = 10.dp,
                                shape = RoundedCornerShape(28.dp),
                                ambientColor = if (isDarkMode) Color(0x1A000000) else Color(0x1A785AB4),
                                spotColor = if (isDarkMode) Color(0x1A000000) else Color(0x1A785AB4)
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(Color(0xFFD16CFF).copy(alpha = 0.25f), Color(0xFFD16CFF).copy(alpha = 0.05f))
                                        ),
                                        shape = CircleShape
                                    )
                                    .border(1.dp, Color(0xFFD16CFF).copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🏪", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "فروشگاه جادویی",
                                color = if (isDarkMode) Color(0xFFE9D5FF) else Color(0xFF5B10D3),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "خرید آواتار",
                                color = if (isDarkMode) Color(0xFFA78BFA) else Color(0xFF706FD3),
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

            }

            // Bottom section: Subscription button capsule
            Button(
                onClick = { viewModel.navigateTo(GameScreen.Subscription) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                border = BorderStroke(2.dp, Color(0xFFDAA520)),
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(64.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(50.dp),
                        ambientColor = Color(0x4DDAA520),
                        spotColor = Color(0x4DDAA520)
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("💎", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "خرید اشتراک ویژه",
                        color = Color(0xFF4A3C00),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ─── SCREEN 2: WORLD MAP SCREEN ───

@Composable
fun WorldMapScreen(
    viewModel: GameViewModel,
    profile: UserProfileEntity?,
    stageStars: Map<Int, Int>,
    themeConfig: GameTheme
) {
    val totalCoins = (profile?.coins ?: 50).toFa()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isWhiteCard = !isDarkMode

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Simple Top Header back navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(GameScreen.MainMenu) },
                modifier = Modifier.background(themeConfig.cardBg, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = if (isWhiteCard) themeConfig.primaryColor else Color.White
                )
            }
            Text(
                "نقشه و دنیای مراحل 🗺",
                color = if (isWhiteCard) Color(0xFF0F172A) else Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier
                    .background(themeConfig.cardBg, RoundedCornerShape(50.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🪙", modifier = Modifier.padding(end = 4.dp))
                Text(totalCoins, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
            }
        }

        val unlockedStage = profile?.unlockedStage ?: 0
        val mapListState = androidx.compose.foundation.lazy.rememberLazyListState()
        
        LaunchedEffect(unlockedStage) {
            if (unlockedStage > 1) {
                try {
                    mapListState.scrollToItem((unlockedStage - 1).coerceAtLeast(0))
                } catch (e: Exception) {
                    // fallthrough
                }
            }
        }

        LazyColumn(
            state = mapListState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(GameConfig.STAGES) { stage ->
                // Sequential Unlocking: Stage is unlocked if it's the very first stage (id = 0) or the previous stage has been unlocked with > 0 stars
                val isUnlocked = stage.id == 0 || (stageStars[stage.id - 1] ?: 0) > 0
                val starsGained = stageStars[stage.id] ?: 0
                
                // Identify the "active/current" stage to play: unlocked, but not beaten yet (0 stars) OR the maximum unlocked stage
                val isCurrent = isUnlocked && (starsGained == 0) && (stage.id == 0 || (stageStars[stage.id - 1] ?: 0) > 0)

                // Node pulsate scale & shadow glow animations for current active stage
                val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1.0f,
                    targetValue = 1.025f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "CardScalePulse"
                )
                val cardScale = if (isCurrent) pulseScale else 1.0f

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (stage.id > 0) {
                        // Beautiful node join connector line
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(16.dp)
                                .background(
                                    brush = if (isUnlocked) {
                                        Brush.verticalGradient(listOf(themeConfig.primaryColor, themeConfig.primaryColor.copy(alpha = 0.5f)))
                                    } else {
                                        Brush.verticalGradient(listOf(Color.Gray.copy(alpha = 0.3f), Color.Gray.copy(alpha = 0.1f)))
                                    },
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                    }

                    Card(
                        onClick = { if (isUnlocked) viewModel.startStage(stage.id) },
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .graphicsLayer(scaleX = cardScale, scaleY = cardScale)
                            .shadow(
                                elevation = if (isCurrent) 12.dp else (if (isUnlocked) 6.dp else 0.dp),
                                shape = RoundedCornerShape(26.dp),
                                ambientColor = if (isCurrent) Color(0xFF8C5CFF).copy(alpha = 0.35f) else Color(0x0F000000),
                                spotColor = if (isCurrent) Color(0xFF8C5CFF).copy(alpha = 0.35f) else Color(0x0F000000)
                            )
                            .border(
                                width = if (isCurrent) 2.dp else 1.dp,
                                color = if (isCurrent) Color(0xFF9333EA) else if (isUnlocked) Color.White.copy(alpha = 0.7f) else Color.Transparent,
                                shape = RoundedCornerShape(26.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isCurrent -> if (isWhiteCard) Color(0xFFFAF5FF) else Color(0xFF2E1A47)
                                isUnlocked -> themeConfig.cardBg
                                else -> themeConfig.cardBg.copy(alpha = 0.5f)
                            }
                        ),
                        shape = RoundedCornerShape(26.dp),
                        enabled = isUnlocked
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular icon box
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(
                                        brush = when {
                                            !isUnlocked -> Brush.radialGradient(listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1)))
                                            stage.boss -> Brush.radialGradient(listOf(Color(0xFFFFE3E3), Color(0xFFFFC9C9)))
                                            isCurrent -> Brush.radialGradient(listOf(Color(0xFFF3E8FF), Color(0xFFE9D5FF)))
                                            else -> Brush.radialGradient(listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD)))
                                        },
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (stage.boss) "🐲" else stage.icon,
                                    fontSize = 28.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = stage.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isUnlocked) {
                                            if (isWhiteCard) Color(0xFF0F172A) else Color.White
                                        } else Color.Gray.copy(alpha = 0.8f)
                                    )
                                    if (stage.boss) {
                                        Box(
                                            modifier = Modifier
                                                .padding(start = 8.dp)
                                                .background(
                                                    brush = Brush.horizontalGradient(listOf(Color(0xFFEF4444), Color(0xFFDC2626))),
                                                    shape = RoundedCornerShape(50.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("غول مرحله 🐲", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                        }
                                    } else if (isCurrent) {
                                        Box(
                                            modifier = Modifier
                                                .padding(start = 8.dp)
                                                .background(
                                                    brush = Brush.horizontalGradient(listOf(Color(0xFFA855F7), Color(0xFF9333EA))),
                                                    shape = RoundedCornerShape(50.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("مرحله فعلی 🚀", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = stage.label,
                                    fontSize = 12.sp,
                                    color = if (isUnlocked) {
                                        if (isWhiteCard) Color(0xFF475569) else Color.LightGray
                                    } else Color.Gray.copy(alpha = 0.6f)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Stars underlay
                                if (isUnlocked) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0x0A8C5CFF), RoundedCornerShape(50.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                            .widthIn(max = 110.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            repeat(3) { index ->
                                                Text(
                                                    text = if (index < starsGained) "⭐" else "☆",
                                                    fontSize = 14.sp,
                                                    color = if (index < starsGained) Color(0xFFFFD700) else Color.Gray.copy(alpha = 0.3f),
                                                    modifier = Modifier.padding(horizontal = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            if (!isUnlocked) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color.Gray.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = Color.Gray.copy(alpha = 0.6f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(themeConfig.primaryColor.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowLeft,
                                        contentDescription = "Launch",
                                        tint = themeConfig.primaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── SCREEN 3: GAMEPLAY SCREEN ───

@Composable
fun GameplayScreen(viewModel: GameViewModel, themeConfig: GameTheme) {
    val state by viewModel.stageState.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val isLight = !isDarkMode

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Game active top HUD
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Heart lives
            Row {
                repeat(3) { index ->
                    Text(
                        text = "❤️",
                        fontSize = 20.sp,
                        color = Color.White,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .animateContentSize()
                            .alpha(if (index < state.lives) 1.0f else 0.25f)
                    )
                }
            }

            // Score Tracker
            Row(
                modifier = Modifier
                    .background(themeConfig.cardBg, RoundedCornerShape(50.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⭐", modifier = Modifier.padding(end = 4.dp))
                Text(state.score.toFa(), fontWeight = FontWeight.Black, color = Color(0xFFFFD700))
            }

            // End/Quit
            Button(
                onClick = { viewModel.navigateTo(GameScreen.WorldMap) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f)),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                modifier = Modifier.height(28.dp),
                shape = RoundedCornerShape(50.dp)
            ) {
                Text("خروج 🚪", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Fully animated and glass-smooth timer bar
        val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
            targetValue = state.timerProgress,
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 50, easing = androidx.compose.animation.core.LinearEasing),
            label = "SmoothTimerBar"
        )

        // Timer Bar
        LinearProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = if (animatedProgress < 0.3f) Color.Red else themeConfig.primaryColor,
            trackColor = Color.White.copy(alpha = 0.08f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Boss Health Bar representation (if Boss Stage)
        if (state.isBoss) {
            val totalQ = GameConfig.STAGES.first { it.id == state.stageId }.q
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🐲 سلامتیِ غول ریاضی:", color = Color(0xFFFF6348), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("${state.bossHp.toFa()} / ${totalQ.toFa()}", color = Color.White, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = state.bossHp.toFloat() / totalQ.toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = Color(0xFFFF6348),
                    trackColor = Color.White.copy(alpha = 0.08f)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Mascot with Dialogue speaking bubble
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Start
        ) {
            val currentAvatarIcon = GameConfig.AVATARS.find { it.id == (viewModel.userProfile.value?.activeAvatar ?: "owl") }?.icon ?: "🦉"

            Text(
                text = currentAvatarIcon,
                fontSize = 46.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .background(themeConfig.cardBg, RoundedCornerShape(14.dp, 14.dp, 14.dp, 0.dp))
                    .border(1.dp, themeConfig.primaryColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp, 14.dp, 14.dp, 0.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = state.charMessage,
                    color = if (isLight) Color(0xFF0F172A) else Color.White,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Question display Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(28.dp), ambientColor = Color(0x1A8C5CFF), spotColor = Color(0x1A8C5CFF))
                .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(28.dp))
                .background(
                    brush = if (isLight) {
                        Brush.verticalGradient(colors = listOf(Color(0xFFFFFFFF), Color(0xFFFAF5FF)))
                    } else {
                        Brush.verticalGradient(colors = listOf(themeConfig.cardBg, Color(0xFF1E103E)))
                    },
                    shape = RoundedCornerShape(28.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .background(if (isLight) Color(0xFFF1F5F9) else Color.White.copy(alpha = 0.05f), RoundedCornerShape(50.dp))
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    val qNum = (state.currentQuestionIndex + 1).toFa()
                    val totalStageQ = GameConfig.STAGES.first { it.id == state.stageId }.q.toFa()
                    Text(
                        text = "سوال $qNum از $totalStageQ",
                        color = if (isLight) Color(0xFF475569) else Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Big Formula Layout
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.activeQuestion.first.toFa(),
                            color = Color(0xFF00D2D3),
                            fontSize = 68.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = " × ",
                            color = Color(0xFFFF6D81),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Text(
                            text = state.activeQuestion.second.toFa(),
                            color = Color(0xFF00D2D3),
                            fontSize = 68.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = " = ",
                            color = Color(0xFFFFD32A),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Text(
                            text = "?",
                            color = Color(0xFFFFD32A),
                            fontSize = 68.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid selection options (4 alternatives)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.options.size) { index ->
                val option = state.options[index]
                val itemSelected = state.selectedOptionIndex == index
                val isCorrectAnswer = option == state.activeQuestion.first * state.activeQuestion.second
                val isLight = !isDarkMode

                // Solid color feedback for correct/incorrect answers
                val borderCol = when {
                    state.selectResult != null && isCorrectAnswer -> Color(0xFF22C55E) // Bright Green
                    state.selectResult != null && itemSelected && state.selectResult == AnswerResult.Wrong -> Color(0xFFEF4444) // Bright Red
                    else -> if (isLight) themeConfig.primaryColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.15f)
                }

                val backgroundCol = when {
                    state.selectResult != null && isCorrectAnswer -> Color(0xFF22C55E) // Solid Bright Green
                    state.selectResult != null && itemSelected && state.selectResult == AnswerResult.Wrong -> Color(0xFFEF4444) // Solid Bright Red
                    else -> themeConfig.cardBg
                }

                val txtColor = when {
                    state.selectResult != null && (isCorrectAnswer || (itemSelected && state.selectResult == AnswerResult.Wrong)) -> Color.White
                    else -> if (isLight) Color(0xFF0F172A) else Color.White
                }

                Card(
                    onClick = {
                        if (!state.isLocked) {
                            viewModel.submitStageAnswer(option, index)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(86.dp)
                        .shadow(8.dp, RoundedCornerShape(26.dp), ambientColor = Color(0x14000000), spotColor = Color(0x14000000))
                        .border(1.5.dp, borderCol, RoundedCornerShape(26.dp)),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = backgroundCol,
                        contentColor = txtColor,
                        disabledContainerColor = backgroundCol,
                        disabledContentColor = txtColor
                    ),
                    enabled = true
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option.toFa(),
                            color = txtColor,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        val quotes = remember(profile) {
            val userName = profile?.name ?: "قهرمان"
            listOf(
                "تو می‌تونی $userName!",
                "عالی داری پیش میری $userName!",
                "ادامه بده $userName، تو فوق‌العاده‌ای!",
                "به خودت ایمان داشته باش $userName!",
                "هوش ریاضیت بی‌نظیره $userName!",
                "دقت و سرعتت حرف نداره $userName!"
            )
        }
        val currentQuote = remember(state.activeQuestion) { quotes.random() }
        
        Text(
            text = currentQuote,
            color = themeConfig.primaryColor.copy(alpha = 0.8f),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

// ─── SCREEN 4: STAGE RESULT SCREEN ───

@Composable
fun StageResultScreen(viewModel: GameViewModel, themeConfig: GameTheme) {
    val state by viewModel.stageState.collectAsStateWithLifecycle()
    val stage = GameConfig.STAGES.first { it.id == state.stageId }

    val stateCompleted = state.correctAnswersCount.toFloat() / stage.q.toFloat()
    val victory = state.lives > 0 && stateCompleted >= 0.4f

    val starsCount = remember(state.lives, stateCompleted) {
        if (state.lives <= 0) 0 else {
            when {
                stateCompleted == 1.0f -> 3
                stateCompleted >= 0.7f -> 2
                stateCompleted >= 0.4f -> 1
                else -> 0
            }
        }
    }

    val earnedCoins = if (victory) stage.coinReward + (starsCount - 1) * 20 else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(28.dp)),
            colors = CardDefaults.cardColors(containerColor = themeConfig.cardBg),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Emoji Victory or retry
                Text(
                    text = if (victory) (if (starsCount == 3) "🏆" else "⭐️") else "😢",
                    fontSize = 72.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = if (victory) "مرحله کامل شد!" else "باختی قهرمان!",
                    color = if (victory) Color(0xFFFFD32A) else Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = "${stage.name} - ${stage.label}",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Stars display row
                Row(modifier = Modifier.padding(bottom = 24.dp)) {
                    repeat(3) { index ->
                        Text(
                            text = if (index < starsCount) "⭐" else "☆",
                            fontSize = 38.sp,
                            color = Color(0xFFFFD700)
                        )
                    }
                }

                // Stats summary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(state.correctAnswersCount.toFa(), color = Color(0xFF2ED573), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("درست ✅", color = Color.LightGray, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(state.wrongAnswersCount.toFa(), color = Color.Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("اشتباه ❌", color = Color.LightGray, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(state.score.toFa(), color = Color(0xFFFFD700), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("امتیاز ⭐️", color = Color.LightGray, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
                    }
                }

                // Earned reward banner
                if (victory) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(themeConfig.primaryColor.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🪙", fontSize = 20.sp, modifier = Modifier.padding(end = 4.dp))
                        Text(
                            text = "جایزه کسب شده: +${earnedCoins.toFa()} سکه",
                            color = Color(0xFFFFD700),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (stage.gemReward > 0) {
                            Text(" و +${stage.gemReward.toFa()} 💎", color = Color(0xFFC084FC), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Text(
                        text = "هیچی ار ارزش‌هات کم نمیشه قهرمان! همیشه تمرین بیشتر باعث پیروزی میشه. دوباره تلاش کن! 💪",
                        color = Color.LightGray,
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(26.dp))

                // Navigation actions
                Button(
                    onClick = {
                        val nextId = state.stageId + 1
                        if (victory && nextId < GameConfig.STAGES.size) {
                            viewModel.startStage(nextId)
                        } else {
                            viewModel.startStage(state.stageId)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeConfig.primaryColor),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (victory && (state.stageId + 1) < GameConfig.STAGES.size) "مرحله بعدی! 🚀" else "امتحان مجدد مرحله 🔄",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { viewModel.navigateTo(GameScreen.WorldMap) },
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("بازگشت به دنیای مراحل 🗺", color = Color.White)
                }
            }
        }
    }

    // Café Bazaar rating dialog triggers when defeating the boss of table 2 (Stage ID 19) successfully.
    val isTable2BossVictory = (state.stageId == 19 && victory)
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPrefs = remember(context) { context.getSharedPreferences("bazaar_rate_prefs", android.content.Context.MODE_PRIVATE) }
    var hasShownRateDialog by remember { mutableStateOf(sharedPrefs.getBoolean("has_shown_rate_dialog", false)) }
    var showRateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isTable2BossVictory, hasShownRateDialog) {
        if (isTable2BossVictory && !hasShownRateDialog) {
            showRateDialog = true
        }
    }

    if (showRateDialog) {
        BazaarRateDialog(
            themeConfig = themeConfig,
            onDismiss = {
                showRateDialog = false
                hasShownRateDialog = true
                sharedPrefs.edit().putBoolean("has_shown_rate_dialog", true).apply()
            }
        )
    }
}

@Composable
fun BazaarRateDialog(
    onDismiss: () -> Unit,
    themeConfig: GameTheme
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "alpha"
    )

    fun performDismiss() {
        isVisible = false
        coroutineScope.launch {
            delay(300)
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = { /* Prevent dismiss on back or outside click */ },
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    alpha = alpha
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = themeConfig.cardBg),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        themeConfig.primaryColor,
                                        themeConfig.primaryColor.copy(alpha = 0.6f)
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⭐",
                            fontSize = 32.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "⭐ از بازی خوشت اومد؟",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "اگر از بازی لذت بردی، با یک امتیاز ⭐⭐⭐⭐⭐ در کافه بازار از ما حمایت کن.",
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                val packageName = "com.aistudio.multiplicationhero.kqpwbx"
                                try {
                                    val intent = Intent(Intent.ACTION_EDIT).apply {
                                        data = android.net.Uri.parse("bazaar://details?id=$packageName")
                                        setPackage("com.farsitel.bazaar")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    try {
                                        val webIntent = Intent(Intent.ACTION_VIEW).apply {
                                            data = android.net.Uri.parse("https://cafebazaar.ir/app/$packageName")
                                        }
                                        context.startActivity(webIntent)
                                    } catch (ex: Exception) {
                                        Toast.makeText(context, "خطا در باز کردن کافه بازار", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                performDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeConfig.primaryColor),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .shadow(8.dp, RoundedCornerShape(16.dp))
                        ) {
                            Text(
                                text = "⭐ امتیاز میدم",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                performDismiss()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Text(
                                text = "بعداً",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        TextButton(
                            onClick = {
                                performDismiss()
                            },
                            modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                        ) {
                            Text(
                                text = "نه، ممنون",
                                color = Color.Gray,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}


// ─── SCREEN 5: SPEED CHALLENGE SCREEN ───

@Composable
fun SpeedChallengeScreen(viewModel: GameViewModel, themeConfig: GameTheme) {
    val state by viewModel.speedState.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isLightMode = !isDarkMode

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Upper stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = state.secondsLeft.toFa(),
                    fontSize = 32.sp,
                    color = if (state.secondsLeft < 10) Color.Red else Color(0xFFFFD32A),
                    fontWeight = FontWeight.Black
                )
                Text("ثانیه باقی مانده", color = if (isLightMode) Color(0xFF475569) else Color.LightGray, fontSize = 9.sp)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = state.score.toFa(),
                    fontSize = 32.sp,
                    color = if (isLightMode) Color(0xFF0F172A) else Color.White,
                    fontWeight = FontWeight.Black
                )
                Text("امتیاز نهایی", color = if (isLightMode) Color(0xFF475569) else Color.LightGray, fontSize = 9.sp)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🔥 ${state.streak.toFa()}",
                    fontSize = 24.sp,
                    color = Color(0xFFFF6348),
                    fontWeight = FontWeight.Black
                )
                Text("ضرب متوالی", color = if (isLightMode) Color(0xFF475569) else Color.LightGray, fontSize = 9.sp)
            }

            IconButton(
                onClick = { viewModel.navigateTo(GameScreen.WorldMap) },
                modifier = Modifier.background(themeConfig.cardBg, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Exit",
                    tint = if (isLightMode) themeConfig.primaryColor else Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Fully animated and glass-smooth timer bar
        val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
            targetValue = state.timerProgress,
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 100, easing = androidx.compose.animation.core.LinearEasing),
            label = "SmoothSpeedTimer"
        )

        // Timer progress indicator
        LinearProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = if (state.secondsLeft < 10) Color.Red else Color(0xFF2ED573),
            trackColor = Color.White.copy(alpha = 0.08f)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Dialogue mascot helper
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Start
        ) {
            val currentAvatarIcon = GameConfig.AVATARS.find { it.id == (viewModel.userProfile.value?.activeAvatar ?: "owl") }?.icon ?: "🦉"

            Text(
                text = currentAvatarIcon,
                fontSize = 46.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .background(themeConfig.cardBg, RoundedCornerShape(14.dp, 14.dp, 14.dp, 0.dp))
                    .border(1.dp, themeConfig.primaryColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp, 14.dp, 14.dp, 0.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = state.charMessage,
                    color = if (isLightMode) Color(0xFF0F172A) else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Formula representation Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = themeConfig.cardBg),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.currentQuestion.first.toFa(),
                            color = Color(0xFF00D2D3),
                            fontSize = 62.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = " × ",
                            color = Color(0xFFFF6D81),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Text(
                            text = state.currentQuestion.second.toFa(),
                            color = Color(0xFF00D2D3),
                            fontSize = 62.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = " = ",
                            color = Color(0xFFFFD32A),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Text(
                            text = "?",
                            color = Color(0xFFFFD32A),
                            fontSize = 62.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Answers
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.options.size) { index ->
                val option = state.options[index]
                val isCorrectVal = option == state.currentQuestion.first * state.currentQuestion.second
                val itemSelected = state.lastSelectedOption == option
                val isLight = isLightMode

                val backgroundCol = when {
                    state.isLocked && isCorrectVal -> Color(0xFF2ED573) // Solid Vivid Green
                    state.isLocked && itemSelected && !isCorrectVal -> Color(0xFFEA2027) // Solid Vivid Red
                    else -> themeConfig.cardBg
                }

                val borderCol = when {
                    state.isLocked && isCorrectVal -> Color(0xFF2ED573)
                    state.isLocked && itemSelected && !isCorrectVal -> Color(0xFFEA2027)
                    else -> if (isLight) themeConfig.primaryColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.15f)
                }

                val txtColor = when {
                    state.isLocked && (isCorrectVal || (itemSelected && !isCorrectVal)) -> Color.White
                    else -> if (isLight) Color(0xFF0F172A) else Color.White
                }

                Card(
                    onClick = {
                        if (!state.isLocked) {
                            viewModel.submitSpeedAnswer(option)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(86.dp)
                        .border(2.dp, borderCol, RoundedCornerShape(16.dp))
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = backgroundCol,
                        contentColor = txtColor,
                        disabledContainerColor = backgroundCol,
                        disabledContentColor = txtColor
                    ),
                    enabled = true
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option.toFa(),
                            color = txtColor,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

// ─── SCREEN 6: SHOP SCREEN ───

@Composable
fun ShopScreen(viewModel: GameViewModel, profile: UserProfileEntity?, themeConfig: GameTheme) {
    val totalCoins = profile?.coins ?: 50
    val ownedAvatars = remember(profile?.ownedAvatarsJson) {
        GameRepository.parseStringList(profile?.ownedAvatarsJson ?: "[\"owl\"]")
    }
    val ownedThemes = remember(profile?.ownedThemesJson) {
        GameRepository.parseStringList(profile?.ownedThemesJson ?: "[\"default\"]")
    }
    val ownedEffects = remember(profile?.ownedEffectsJson) {
        GameRepository.parseStringList(profile?.ownedEffectsJson ?: "[\"stars\"]")
    }

    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isLight = !isDarkMode
    val screenTitleColor = if (isLight) Color(0xFF0F172A) else Color.White
    val itemTitleColor = if (isLight) Color(0xFF1E293B) else Color.White
    val itemSubColor = if (isLight) Color(0xFF475569) else Color.LightGray

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🏪 فروشگاه جادویی", color = screenTitleColor, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier
                    .background(themeConfig.cardBg, RoundedCornerShape(50.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🪙", modifier = Modifier.padding(end = 4.dp))
                Text(totalCoins.toFa(), color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp)
        ) {
            // mascot section
            item {
                Text("🦸 همیاران قهرمان (آواتارها)", color = themeConfig.primaryColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(GameConfig.AVATARS.chunked(2)) { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    pair.forEach { avatar ->
                        val isOwned = avatar.id in ownedAvatars
                        val isActive = (profile?.activeAvatar ?: "owl") == avatar.id

                        val scale = if (isActive) 1.03f else 1.0f
                        val borderShape = RoundedCornerShape(20.dp)

                        Card(
                            onClick = { viewModel.buyOrSelectAvatar(avatar.id) },
                            modifier = Modifier
                                .weight(1f)
                                .height(140.dp)
                                .graphicsLayer(scaleX = scale, scaleY = scale)
                                .shadow(
                                    elevation = if (isActive) 12.dp else 4.dp,
                                    shape = borderShape,
                                    ambientColor = if (isActive) Color(0xFFA855F7) else Color(0x0F000000),
                                    spotColor = if (isActive) Color(0xFFA855F7) else Color(0x0F000000)
                                )
                                .border(
                                    width = if (isActive) 2.dp else 1.dp,
                                    color = if (isActive) Color(0xFFA855F7) else Color.White.copy(alpha = 0.5f),
                                    shape = borderShape
                                ),
                            colors = CardDefaults.cardColors(containerColor = themeConfig.cardBg),
                            shape = borderShape
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(avatar.icon, fontSize = 38.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(avatar.name, color = itemTitleColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                if (isActive) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF22C55E), RoundedCornerShape(50.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text("فعال شده ✅", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else if (isOwned) {
                                    Box(
                                        modifier = Modifier
                                            .border(1.dp, Color(0xFF2ED573).copy(alpha = 0.5f), RoundedCornerShape(50.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text("آماده استفاده", color = Color(0xFF2ED573), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🪙", fontSize = 11.sp, modifier = Modifier.padding(end = 2.dp))
                                        Text("${avatar.price.toFa()} سکه", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Themes section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("🎨 قالب‌های ظاهری بازی", color = themeConfig.primaryColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(GameConfig.THEMES.chunked(2)) { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    pair.forEach { gameTheme ->
                        val isOwned = gameTheme.id in ownedThemes
                        val isActive = (profile?.activeTheme ?: "default") == gameTheme.id

                        Card(
                            onClick = { viewModel.buyOrSelectTheme(gameTheme.id) },
                            modifier = Modifier
                                .weight(1f)
                                .height(140.dp)
                                .border(
                                    width = 1.5.dp,
                                    color = if (isActive) themeConfig.primaryColor else Color.Transparent,
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            colors = CardDefaults.cardColors(containerColor = themeConfig.cardBg),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(gameTheme.preview, fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(gameTheme.name, color = itemTitleColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                if (isActive) {
                                    Text("فعال شده ✅", color = themeConfig.primaryColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                } else if (isOwned) {
                                    Text("کلیک برای انتخاب", color = Color(0xFF2ED573), fontSize = 10.sp)
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🪙", fontSize = 11.sp, modifier = Modifier.padding(end = 2.dp))
                                        Text("${gameTheme.price.toFa()} سکه", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Correct effects section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("✨ باران افکت‌های جادویی درست", color = themeConfig.primaryColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(GameConfig.EFFECTS.chunked(2)) { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    pair.forEach { effect ->
                        val isOwned = effect.id in ownedEffects
                        val isActive = (profile?.activeEffect ?: "stars") == effect.id

                        Card(
                            onClick = { viewModel.buyOrSelectEffect(effect.id) },
                            modifier = Modifier
                                .weight(1f)
                                .height(140.dp)
                                .border(
                                    width = 1.5.dp,
                                    color = if (isActive) themeConfig.primaryColor else Color.Transparent,
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            colors = CardDefaults.cardColors(containerColor = themeConfig.cardBg),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(effect.icon, fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(effect.name, color = itemTitleColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                if (isActive) {
                                    Text("فعال شده ✅", color = themeConfig.primaryColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                } else if (isOwned) {
                                    Text("کلیک برای انتخاب", color = Color(0xFF2ED573), fontSize = 10.sp)
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🪙", fontSize = 11.sp, modifier = Modifier.padding(end = 2.dp))
                                        Text("${effect.price.toFa()} سکه", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── SCREEN 7: ACHIEVEMENTS SCREEN ───

@Composable
fun AchievementsScreen(viewModel: GameViewModel, profile: UserProfileEntity?, themeConfig: GameTheme) {
    val unlockedAchs = remember(profile?.unlockedAchsJson) {
        GameRepository.parseStringList(profile?.unlockedAchsJson ?: "[]")
    }

    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isLight = !isDarkMode
    val mainTitleColor = if (isLight) Color(0xFF0F172A) else Color.White
    val achNameColor = if (isLight) Color(0xFF1E293B) else Color.White
    val achDescColor = if (isLight) Color(0xFF475569) else Color.LightGray

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("🏆 دستاوردهای من", color = mainTitleColor, fontSize = 21.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            items(GameConfig.ACHIEVEMENTS) { ach ->
                val isUnlocked = ach.id in unlockedAchs

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (isUnlocked) 1.0f else 0.5f)
                        .border(
                            width = 1.dp,
                            color = if (isUnlocked) Color(0xFFFFD700).copy(alpha = 0.4f) else Color.Transparent,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = themeConfig.cardBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(ach.icon, fontSize = 34.sp, modifier = Modifier.padding(end = 12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(ach.name, color = achNameColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(ach.desc, color = achDescColor, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                        }
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isUnlocked) Color(0xFFFFD700) else Color.White.copy(alpha = 0.1f),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isUnlocked) "ادعا شده ✅" else "قفل 🔒",
                                color = if (isUnlocked) Color.Black else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── SCREEN 8: PROGRESS REPORT SCREEN ───

@Composable
fun ProgressReportScreen(
    viewModel: GameViewModel,
    profile: UserProfileEntity?,
    stats: List<TableStatEntity>,
    themeConfig: GameTheme
) {
    val correctTotal = profile?.totalCorrect ?: 0
    val wrongTotal = profile?.totalWrong ?: 0
    val streakHigh = profile?.maxStreak ?: 0

    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isLight = !isDarkMode
    val mainTitleColor = if (isLight) Color(0xFF0F172A) else Color.White
    val textTitleColor = if (isLight) Color(0xFF1E293B) else Color.White
    val subtitleColor = if (isLight) Color(0xFF475569) else Color.LightGray

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("📊 کارنامه پیشرفت ریاضی من", color = mainTitleColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        // Active profile registered name banner
        var showReportNameDialog by remember { mutableStateOf(false) }
        var reportNameInputValue by remember { mutableStateOf(profile?.name ?: "") }

        Card(
            onClick = {
                reportNameInputValue = profile?.name ?: ""
                showReportNameDialog = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .border(1.dp, themeConfig.primaryColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = themeConfig.cardBg),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("👤", fontSize = 18.sp, modifier = Modifier.padding(end = 6.dp))
                    Text(
                        text = "کارنامه قهرمان: " + (profile?.name ?: "بدون نام"),
                        color = mainTitleColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Text(
                    text = "✏️ ویرایش نام",
                    color = themeConfig.primaryColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        if (showReportNameDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showReportNameDialog = false },
                containerColor = themeConfig.cardBg,
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text(
                        text = "ویرایش نام قهرمان",
                        color = textTitleColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                        Text(
                            text = "نام مستعار خود را تغییر دهید تا اطلاعات با نام جدید ثبت شود:",
                            color = subtitleColor,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Right
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = reportNameInputValue,
                            onValueChange = { reportNameInputValue = it },
                            placeholder = { Text("نام مستعار جدید...", color = subtitleColor.copy(alpha = 0.5f)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textTitleColor,
                                unfocusedTextColor = textTitleColor,
                                focusedBorderColor = themeConfig.primaryColor,
                                unfocusedBorderColor = subtitleColor.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                },
                confirmButton = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            if (reportNameInputValue.trim().isNotEmpty()) {
                                viewModel.finishAuth(reportNameInputValue.trim(), "student", "")
                                showReportNameDialog = false
                            }
                        }
                    ) {
                        Text("💾 ذخیره", color = themeConfig.primaryColor, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { showReportNameDialog = false }) {
                        Text("انصراف", color = subtitleColor)
                    }
                }
            )
        }

        // Total counters Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = themeConfig.cardBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(correctTotal.toFa(), color = Color(0xFF2ED573), fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Text("پاسخ درست", color = subtitleColor, fontSize = 10.sp)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = themeConfig.cardBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(wrongTotal.toFa(), color = Color.Red, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Text("پاسخ غلط", color = subtitleColor, fontSize = 10.sp)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = themeConfig.cardBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(streakHigh.toFa(), color = Color(0xFF00D2D3), fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Text("رکورد متوالی", color = subtitleColor, fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // List tables mapping stats (1 to 9 progress bars)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Student statistical bars
            items(9) { numIdx ->
                val num = numIdx + 1
                val stat = stats.find { it.number == num }
                val cor = stat?.correctCount ?: 0
                val wrg = stat?.wrongCount ?: 0
                val total = cor + wrg
                val progressPct = if (total > 0) cor.toFloat() / total.toFloat() else 0.0f

                val colorAccent = when (num) {
                    1, 2, 3 -> Color(0xFF2ED573)
                    4, 5, 6 -> Color(0xFF00D2D3)
                    7, 8 -> Color(0xFFFFD32A)
                    else -> Color(0xFFC084FC)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = themeConfig.cardBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ضربدرِ عددِ ${num.toFa()}", color = textTitleColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${(progressPct * 100).toInt().toFa()} درصد یادگیری", color = colorAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = progressPct,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = colorAccent,
                            trackColor = if (isLight) Color(0xFFE2E8F0) else Color.White.copy(alpha = 0.08f)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${cor.toFa()} جواب صحیح از ${total.toFa()} سوال حل شده",
                            color = subtitleColor,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Seyed Majid Contact Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    border = BorderStroke(1.dp, themeConfig.primaryColor.copy(alpha = 0.25f)),
                    colors = CardDefaults.cardColors(containerColor = themeConfig.cardBg),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎨 طراح و توسعه‌دهنده بازی", color = themeConfig.primaryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("سید مجید حسینی", color = textTitleColor, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            // Reset Game Data Block
            item {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = { viewModel.resetAllData() },
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("پاک کردن کارنامه و ریست بازی ⚠️", color = Color.Red, fontSize = 12.sp)
                }
            }
        }
    }
}

// ─── SCREEN 9: LEARN TABLES SCREEN (9X9 GRID WITH VOICE) ───

@Composable
fun LearnTablesScreen(viewModel: GameViewModel, themeConfig: GameTheme) {
    var selectedRow by remember { mutableStateOf(1) }
    var activeSelectedCard by remember { mutableStateOf<Int?>(null) }
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isLight = !isDarkMode
    val mainTitleColor = if (isLight) Color(0xFF0F172A) else Color.White
    val subtitleColor = if (isLight) Color(0xFF475569) else Color.LightGray

    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("learn_tables_prefs", android.content.Context.MODE_PRIVATE) }
    var revealedCards by remember {
        mutableStateOf(
            prefs.getStringSet("revealed_cards", emptySet()) ?: emptySet()
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("📖 کلاس یادگیری جدول ضرب", color = mainTitleColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            if (revealedCards.isNotEmpty()) {
                Text(
                    text = "مخفی کردن همه 🔄",
                    color = themeConfig.primaryColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable {
                            revealedCards = emptySet()
                            prefs.edit().putStringSet("revealed_cards", emptySet()).apply()
                        }
                        .padding(4.dp)
                )
            }
        }


        // Row selector buttons row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (r in 1..9) {
                val isActive = selectedRow == r
                Box(
                    modifier = Modifier
                        .background(
                            if (isActive) themeConfig.primaryColor else themeConfig.cardBg,
                            RoundedCornerShape(50.dp)
                        )
                        .clickable {
                            selectedRow = r
                            activeSelectedCard = null
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "جدول ${r.toFa()}",
                        color = if (isActive) Color.White else (if (isLight) Color(0xFF0F172A) else Color.White),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Big vertical grid of chosen row factor products
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 50.dp)
        ) {
            items(9) { fIdx ->
                val factor = fIdx + 1
                val product = selectedRow * factor
                val isSelected = activeSelectedCard == factor
                val cardKey = "${selectedRow}_${factor}"
                val isRevealed = revealedCards.contains(cardKey)

                Card(
                    onClick = {
                        activeSelectedCard = factor
                        GameAudioManager.getInstance(context).playLearnClipDirectly(context, selectedRow, factor)
                        if (!isRevealed) {
                            val newRevealed = revealedCards + cardKey
                            revealedCards = newRevealed
                            prefs.edit().putStringSet("revealed_cards", newRevealed).apply()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.0f)
                        .shadow(if (isSelected) 8.dp else 4.dp, RoundedCornerShape(16.dp))
                        .border(
                            1.5.dp,
                            if (isSelected) themeConfig.primaryColor else Color.Transparent,
                            RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) themeConfig.primaryColor.copy(alpha = 0.12f) else themeConfig.cardBg
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.compose.runtime.CompositionLocalProvider(
                            androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr
                        ) {
                            Text(
                                text = "${selectedRow.toFa()} × ${factor.toFa()}",
                                color = if (isSelected) themeConfig.primaryColor else (if (isLight) Color(0xFF475569) else Color.LightGray),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isRevealed) product.toFa() else "?",
                            color = if (isRevealed) themeConfig.primaryColor else (if (isLight) Color(0xFF94A3B8) else Color.Gray),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

// ─── BOTTOM NAVIGATION COMPONENT ───

@Composable
fun BottomNavigationWidget(currentScreen: GameScreen, onNavigate: (GameScreen) -> Unit, theme: GameTheme) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            color = Color(0xE6FFFFFF), // Semi-transparent glass background
            shape = RoundedCornerShape(36.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(36.dp),
                    ambientColor = Color(0x339B4DFF),
                    spotColor = Color(0x339B4DFF)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val navItems = listOf(
                    Triple(GameScreen.MainMenu, "خانه", Icons.Default.Home),
                    Triple(GameScreen.WorldMap, "مراحل", Icons.Default.Place),
                    Triple(GameScreen.LuckyWheel, "گردونه شانس", Icons.Default.Refresh),
                    Triple(GameScreen.Shop, "بازار", Icons.Default.ShoppingCart),
                    Triple(GameScreen.ProgressReport, "عملکرد", Icons.Default.AccountBox)
                )

                navItems.forEach { (screen, label, icon) ->
                    val active = screen == currentScreen
                    
                    val scale by animateFloatAsState(
                        targetValue = if (active) 1.06f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "TabScale"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onNavigate(screen) }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .graphicsLayer(scaleX = scale, scaleY = scale),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (active) Color(0xFF9333EA) else Color(0xFF706FD3).copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = label,
                                color = if (active) Color(0xFF9333EA) else Color(0xFF706FD3).copy(alpha = 0.6f),
                                fontWeight = if (active) FontWeight.Black else FontWeight.Bold,
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── UTILITY FOR DYNAMIC TIME DISPLAY ───
fun formatLeftTime(millis: Long): String {
    if (millis <= 0) return "۰ روز و ۰ ساعت و ۰ دقیقه"
    val days = (millis / (1000 * 60 * 60 * 24)).toInt()
    val hours = ((millis / (1000 * 60 * 60)) % 24).toInt()
    val minutes = ((millis / (1000 * 60)) % 60).toInt()
    return "${days.toFa()} روز و ${hours.toFa()} ساعت و ${minutes.toFa()} دقیقه"
}

// ─── PARSING MATH INTEGER TO PERSIAN TEXT EXTENSIONS ───
fun Int.toFa(): String {
    val faDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return this.toString().map { char ->
        if (char in '0'..'9') faDigits[char - '0'] else char
    }.joinToString("")
}

fun String.toFa(): String {
    val faDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return this.map { char ->
        if (char in '0'..'9') faDigits[char - '0'] else char
    }.joinToString("")
}

fun String.toEn(): String {
    val faDigits = "۰۱۲۳۴۵۶۷۸۹"
    return this.map { char ->
        val idx = faDigits.indexOf(char)
        if (idx != -1) ('0' + idx) else char
    }.joinToString("")
}

// ─── REUSABLE PREMIUM GLASSMORPHIC CARD ───
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(24.dp),
    borderAlpha: Float = 0.25f,
    bgAlpha: Float = 0.08f,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        border = BorderStroke(
            1.dp,
            Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = borderAlpha),
                    Color.White.copy(alpha = borderAlpha * 0.2f)
                )
            )
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = bgAlpha)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

// ─── REUSABLE 3D PHYSICAL CLICK BUTTON ───
@Composable
fun ThreeDButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    shadowColor: Color = containerColor.copy(alpha = 0.45f),
    enabled: Boolean = true,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp),
    content: @Composable RowScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val offsetAnim by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 6.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "PressOffset"
    )

    Box(
        modifier = modifier
            .pointerInput(enabled) {
                if (enabled) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            try {
                                awaitRelease()
                            } finally {
                                isPressed = false
                            }
                            onClick()
                        }
                    )
                }
            }
    ) {
        // Shadow representation
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 6.dp)
                .background(shadowColor, shape)
        )
        // Surface representation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = offsetAnim)
                .background(containerColor, shape)
                .padding(vertical = 12.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                content()
            }
        }
    }
}

// ─── LOGIN & ONBOARDING ENTRY GATE ───
@Composable
fun LoginScreen(viewModel: GameViewModel, themeConfig: GameTheme, onBazaarLogin: () -> Unit) {
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isLight = !isDarkMode
    val mainTitleColor = if (isLight) Color(0xFF0F172A) else Color.White
    val subtitleColor = if (isLight) Color(0xFF475569) else Color.LightGray

    Box(modifier = Modifier.fillMaxSize()) {
        // Soft Translucent Glowing Circles, Blurred magical particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (isDarkMode) {
                // Soft neon violet circle top-right
                drawCircle(
                    color = Color(0x1C9333EA),
                    radius = 200.dp.toPx(),
                    center = Offset(size.width * 0.85f, size.height * 0.15f)
                )
                // Soft deep pink circle bottom-left
                drawCircle(
                    color = Color(0x13EC4899),
                    radius = 240.dp.toPx(),
                    center = Offset(size.width * 0.15f, size.height * 0.8f)
                )
            } else {
                // Soft pastel purple circle top-right
                drawCircle(
                    color = Color(0x1F8B5CF6),
                    radius = 180.dp.toPx(),
                    center = Offset(size.width * 0.85f, size.height * 0.2f)
                )
                // Soft pastel pink circle bottom-left
                drawCircle(
                    color = Color(0x1BEC4899),
                    radius = 220.dp.toPx(),
                    center = Offset(size.width * 0.15f, size.height * 0.75f)
                )
            }
        }

        // Beautiful cosmic stars decoration
        Box(modifier = Modifier.fillMaxSize()) {
            if (isDarkMode) {
                Text("⭐", fontSize = 15.sp, modifier = Modifier.align(Alignment.TopStart).padding(start = 32.dp, top = 140.dp).alpha(0.35f))
                Text("✨", fontSize = 18.sp, modifier = Modifier.align(Alignment.TopEnd).padding(end = 40.dp, top = 220.dp).alpha(0.45f))
                Text("🌟", fontSize = 22.sp, modifier = Modifier.align(Alignment.CenterStart).padding(start = 60.dp, top = 80.dp).alpha(0.5f))
                Text("⭐", fontSize = 14.sp, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 45.dp, bottom = 160.dp).alpha(0.4f))
            } else {
                Text("✨", fontSize = 16.sp, modifier = Modifier.align(Alignment.TopStart).padding(start = 32.dp, top = 140.dp).alpha(0.5f))
                Text("🌸", fontSize = 18.sp, modifier = Modifier.align(Alignment.TopEnd).padding(end = 40.dp, top = 220.dp).alpha(0.4f))
                Text("🌟", fontSize = 20.sp, modifier = Modifier.align(Alignment.CenterStart).padding(start = 60.dp, top = 80.dp).alpha(0.55f))
                Text("✨", fontSize = 15.sp, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 45.dp, bottom = 160.dp).alpha(0.5f))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "🦉",
                fontSize = 90.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                text = "قهرمان جدول ضرب",
                color = mainTitleColor,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = "به دنیای هیجان‌انگیز یادگیری ریاضی خوش آمدی!",
                color = subtitleColor,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                bgAlpha = if (isLight) 0.8f else 0.12f,
                borderAlpha = 0.25f
            ) {
                Text(
                    text = "ورود به بازی",
                    color = themeConfig.primaryColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "برای ذخیره‌شدن امن پیشرفت، سکه‌ها و جوایزت، با حساب کافه‌بازار وارد شو. حتی با تعویض گوشی یا نصب دوباره، پیشرفتت باقی می‌ماند! ☁️",
                    color = subtitleColor,
                    fontSize = 12.sp,
                    lineHeight = 19.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Spacer(modifier = Modifier.height(22.dp))

                // Official "Login with Bazaar" button (brand color: #0EA960)
                Button(
                    onClick = onBazaarLogin,
                    modifier = Modifier
                        .width(220.dp)
                        .height(48.dp)
                        .align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA960)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF09663A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "ورود با بازار",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = R.drawable.ic_bazaar_logo),
                        contentDescription = "Bazaar Logo",
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "قفل امنیتی",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = buildAnnotatedString {
                            append("اطلاعات شما ")
                            withStyle(
                                style = SpanStyle(
                                    color = Color(0xFFFFD700), // Yellow color for highlight
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("محفوظ و امن")
                            }
                            append(" است.")
                        },
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ─── ONBOARDING NAME SCREEN ───
@Composable
fun OnboardingScreen(viewModel: GameViewModel, themeConfig: GameTheme) {
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isLight = !isDarkMode
    val mainTitleColor = if (isLight) Color(0xFF0F172A) else Color.White
    val subtitleColor = if (isLight) Color(0xFF475569) else Color.LightGray

    var nameText by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        // Ambient background glow circles matching game theme
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (isDarkMode) {
                drawCircle(
                    color = Color(0x1C9333EA),
                    radius = 200.dp.toPx(),
                    center = Offset(size.width * 0.85f, size.height * 0.15f)
                )
                drawCircle(
                    color = Color(0x13EC4899),
                    radius = 240.dp.toPx(),
                    center = Offset(size.width * 0.15f, size.height * 0.8f)
                )
            } else {
                drawCircle(
                    color = Color(0x1F8B5CF6),
                    radius = 180.dp.toPx(),
                    center = Offset(size.width * 0.85f, size.height * 0.2f)
                )
                drawCircle(
                    color = Color(0x1BEC4899),
                    radius = 220.dp.toPx(),
                    center = Offset(size.width * 0.15f, size.height * 0.75f)
                )
            }
        }

        // Beautiful cosmic stars decoration
        Box(modifier = Modifier.fillMaxSize()) {
            if (isDarkMode) {
                Text("⭐", fontSize = 15.sp, modifier = Modifier.align(Alignment.TopStart).padding(start = 32.dp, top = 140.dp).alpha(0.35f))
                Text("✨", fontSize = 18.sp, modifier = Modifier.align(Alignment.TopEnd).padding(end = 40.dp, top = 220.dp).alpha(0.45f))
                Text("🌟", fontSize = 22.sp, modifier = Modifier.align(Alignment.CenterStart).padding(start = 60.dp, top = 80.dp).alpha(0.5f))
            } else {
                Text("✨", fontSize = 16.sp, modifier = Modifier.align(Alignment.TopStart).padding(start = 32.dp, top = 140.dp).alpha(0.5f))
                Text("🌸", fontSize = 18.sp, modifier = Modifier.align(Alignment.TopEnd).padding(end = 40.dp, top = 220.dp).alpha(0.4f))
                Text("🌟", fontSize = 20.sp, modifier = Modifier.align(Alignment.CenterStart).padding(start = 60.dp, top = 80.dp).alpha(0.55f))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "🚀",
                fontSize = 90.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                text = "اسمت چیه قهرمان؟",
                color = mainTitleColor,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = "یک نام برای کاربری خودت انتخاب کن تا وارد دنیای ضرب بشی!",
                color = subtitleColor,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                bgAlpha = if (isLight) 0.8f else 0.12f,
                borderAlpha = 0.25f
            ) {
                Text(
                    text = "نام کاربری",
                    color = themeConfig.primaryColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = nameText,
                    onValueChange = { if (it.length <= 20) nameText = it },
                    placeholder = { Text("مثلاً: قهرمان ضرب", color = subtitleColor.copy(alpha = 0.7f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = mainTitleColor,
                        unfocusedTextColor = mainTitleColor,
                        focusedBorderColor = themeConfig.primaryColor,
                        unfocusedBorderColor = subtitleColor.copy(alpha = 0.3f),
                        focusedLabelColor = themeConfig.primaryColor,
                        unfocusedLabelColor = subtitleColor
                    )
                )

                Button(
                    onClick = { viewModel.submitOnboardingName(nameText) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = themeConfig.primaryColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "شروع بازی 🚀",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}



// ─── REWARDING LEVEL-UP DIALOG ───
@Composable
fun LevelUpOverlay(level: Int, onDismiss: () -> Unit, themeConfig: GameTheme) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                bgAlpha = 0.95f,
                borderAlpha = 0.6f
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val txtColor = Color(0xFF0F172A)
                    val subTxtColor = Color(0xFF475569)
                    
                    Text(
                        text = "🚀 صعود به سطح جدید 🚀",
                        color = Color(0xFF00BFA5), // Neon Cyan accent
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "سطح شما ارتقا یافت!",
                        color = txtColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF8B5CF6), Color(0xFFEC4899)) // Beautiful Violet-Pink gradient
                                ),
                                shape = CircleShape
                            )
                            .border(4.dp, Color(0xFF00BFA5), CircleShape), // Glowing Cyan border
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = level.toFa(),
                            color = Color.White,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "آفرین قهرمان ریاضی! با همین دست فرمان ادامه بده تا همه مراحل جدول ضرب رو فتح کنی! 💪🎈",
                        color = subTxtColor,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)), // Vivid Violet CTA button
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("ادامه بازی با انگیزه بیشتر! 🔥🚀", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SupportDialog(
    onDismiss: () -> Unit,
    themeConfig: GameTheme,
    permanentLicensed: Boolean,
    licenseExpiryTime: Long,
    remainingTimeMs: Long,
    onActivateCode: (String) -> Boolean,
    onBuySubscription: (String) -> Unit,
    isLight: Boolean,
    onBazaarLogin: () -> Unit,
    onCloudBackup: () -> Unit,
    onCloudRestore: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isBazaarLoggedIn = remember { BazaarAuthManager.getSavedAccountId(context) != null }
    var showRestoreConfirm by remember { mutableStateOf(false) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = themeConfig.cardBg),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "📞 تماس با طراح و پشتیبانی بازی",
                    color = themeConfig.primaryColor,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "سید مجید حسینی",
                    color = if (isLight) Color(0xFF0F172A) else Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                
                Text(
                    text = "ارتباط با دبیران و هماهنگی مدارس جهت خرید گروهی، شخصی‌سازی، همگام‌سازی و کدهای فعال‌سازی اشتراک تیمی.",
                    color = if (isLight) Color(0xFF475569) else Color.LightGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                // ☁️ Cloud progress sync section (Bazaar Storage)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isLight) Color(0xFFF0FDF4) else Color.White.copy(alpha = 0.05f),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "☁️ همگامسازی ابری پیشرفت",
                        color = themeConfig.primaryColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isBazaarLoggedIn) {
                        Text(
                            text = "پیشرفتت را همین حالا در حساب بازار ذخیره کن، یا آخرین نسخهٔ ذخیرهشده را بازیابی کن.",
                            color = if (isLight) Color(0xFF475569) else Color.LightGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = onCloudBackup,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA960)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("پشتیبانگیری", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { showRestoreConfirm = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("بازیابی", color = themeConfig.primaryColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Text(
                            text = "برای همگامسازی ابری، ابتدا با حساب کافهبازار وارد شو.",
                            color = if (isLight) Color(0xFF475569) else Color.LightGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp
                        )
                        Button(
                            onClick = onBazaarLogin,
                            modifier = Modifier
                                .width(180.dp)
                                .height(42.dp)
                                .align(Alignment.CenterHorizontally),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA960)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("ورود با بازار", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            androidx.compose.foundation.Image(
                                painter = painterResource(id = R.drawable.ic_bazaar_logo),
                                contentDescription = "Bazaar Logo",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "قفل امنیتی",
                                tint = if (isLight) Color(0xFF475569) else Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = buildAnnotatedString {
                                    append("اطلاعات شما ")
                                    withStyle(
                                        style = SpanStyle(
                                            color = if (isLight) Color(0xFFD97706) else Color(0xFFFFD700), // proper yellow/amber contrast
                                            fontWeight = FontWeight.Bold
                                        )
                                    ) {
                                        append("محفوظ و امن")
                                    }
                                    append(" است.")
                                },
                                color = if (isLight) Color(0xFF475569) else Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                if (showRestoreConfirm) {
                    AlertDialog(
                        onDismissRequest = { showRestoreConfirm = false },
                        title = { Text("بازیابی از بازار؟") },
                        text = { Text("پیشرفت فعلی روی این دستگاه با نسخهٔ ذخیرهشده در بازار جایگزین میشود. ادامه میدهی؟") },
                        confirmButton = {
                            TextButton(onClick = {
                                showRestoreConfirm = false
                                onCloudRestore()
                            }) { Text("بله، بازیابی کن") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showRestoreConfirm = false }) { Text("انصراف") }
                        }
                    )
                }

                // Mobile phone details row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isLight) Color(0xFFF1F5F9) else Color.White.copy(alpha = 0.05f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(10.dp)
                        .clickable {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                    data = android.net.Uri.parse("tel:09173998335")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📱 شماره پشتیبانی (تماس): ",
                        color = if (isLight) Color(0xFF475569) else Color.LightGray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "09173998335".toFa(),
                        color = Color(0xFFFFD32A),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                val bodyText = if (isLight) Color(0xFF475569) else Color.LightGray
                val mainTitle = if (isLight) Color(0xFF0F172A) else Color.White

                // Remaining Subscription / License badge - dynamic countdown
                val stateLicenseRemaining = remember(permanentLicensed, licenseExpiryTime, remainingTimeMs) {
                    if (permanentLicensed) {
                        "✨ اشتراک دائمی فعال و بی‌زمان"
                    } else {
                        val formatted = formatLeftTime(remainingTimeMs)
                        if (licenseExpiryTime > 0L) {
                            if (remainingTimeMs <= 0L) {
                                "⏳ پایان مهلت اشتراک فعال شده"
                            } else {
                                "⏳ اعتبار اشتراک: $formatted باقی مانده"
                            }
                        } else {
                            if (remainingTimeMs <= 0L) {
                                "⏳ پایان مهلت آزمایشی ۳ روزه"
                            } else {
                                "⏳ زمان باقی مانده نسخه آزمایشی: $formatted"
                            }
                        }
                    }
                }

                Surface(
                    color = if (permanentLicensed || remainingTimeMs > 0L) Color(0xFF10B981).copy(alpha = 0.12f) else themeConfig.primaryColor.copy(alpha = 0.1f),
                    contentColor = if (permanentLicensed || remainingTimeMs > 0L) Color(0xFF10B981) else themeConfig.primaryColor,
                    border = BorderStroke(1.dp, if (permanentLicensed || remainingTimeMs > 0L) Color(0xFF10B981).copy(alpha = 0.3f) else themeConfig.primaryColor.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stateLicenseRemaining,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                // Subscription Packages
                var plansExpanded by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { plansExpanded = !plansExpanded },
                    colors = CardDefaults.cardColors(containerColor = themeConfig.primaryColor.copy(alpha = 0.08f)),
                    border = BorderStroke(1.2.dp, themeConfig.primaryColor.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📦", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "طرح‌ها و بسته‌های تمدید اشتراک 💎",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = mainTitle
                            )
                        }
                        Icon(
                            imageVector = if (plansExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = themeConfig.primaryColor
                        )
                    }
                }

                AnimatedVisibility(
                    visible = plansExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val plansList = listOf(
                            Triple("sub_1month", "۱ ماهه (۳۰ روزه)", "۱۵,۰۰۰ تومان"),
                            Triple("sub_3month", "۳ ماهه (۹۰ روزه) 🔥 محبوب", "۲۸,۰۰۰ تومان"),
                            Triple("sub_6month", "۶ ماهه (۱۸۰ روزه)", "۵۸,۰۰۰ تومان"),
                            Triple("sub_1year", "۱ ساله (۳۶۵ روزه) 💎 به صرفه", "۸۹,۰۰۰ تومان")
                        )

                        plansList.forEach { plan ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onBuySubscription(plan.first)
                                    },
                                colors = CardDefaults.cardColors(containerColor = if (isLight) Color(0xFFF8FAFC) else Color.White.copy(alpha = 0.04f)),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, themeConfig.primaryColor.copy(alpha = 0.15f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = "طرح ${plan.second}", 
                                                fontSize = 12.sp, 
                                                fontWeight = FontWeight.Bold, 
                                                color = mainTitle
                                            )
                                            Text(
                                                text = "مبلغ: ${plan.third}", 
                                                fontSize = 11.sp, 
                                                color = bodyText
                                            )
                                        }
                                        Text(
                                            text = "خرید مستقیم 🛍️",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = themeConfig.primaryColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = when (plan.first) {
                                            "sub_1month" -> "🔑 فعال‌سازی آنی اشتراک یک ماهه با اتصال مستقیم به کافه بازار"
                                            "sub_3month" -> "🔑 دسترسی نامحدود سه ماهه به تمامی ۱۲۰ مرحله بازی + بروزرسانی‌ها"
                                            "sub_6month" -> "🔑 دسترسی کامل شش ماهه و بدون محدودیت به تمام بخش‌ها"
                                            else -> "🔑 اشتراک طلایی یک ساله با بیشترین میزان تخفیف و صرفه اقتصادی"
                                        },
                                        fontSize = 10.sp,
                                        color = themeConfig.primaryColor.copy(alpha = 0.85f),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                TextButton(onClick = onDismiss) {
                    Text("بستن صفحه پشتیبانی", color = themeConfig.primaryColor, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─── PREMIUM LICENSE BLOCK OVERLAY (3-DAY EXPIRY TRIAL FLOW) ───
@Composable
fun LicenseBlockOverlay(
    viewModel: GameViewModel,
    profile: UserProfileEntity?,
    themeConfig: GameTheme,
    remainingTimeMs: Long,
    licenseExpiryTime: Long,
    onBuySubscription: (String) -> Unit
) {
    val plans = remember {
        listOf(
            SubscriptionPlan("sub_1month", "⭐", "۱ ماهه", "۳۰ روز", "", "۰٪ صرفه‌جویی", "خرید"),
            SubscriptionPlan("sub_3month", "🔥", "۳ ماهه", "۹۰ روز", "", "۳۸٪ صرفه‌جویی", "انتخاب"),
            SubscriptionPlan("sub_6month", "💎", "۶ ماهه", "۱۸۰ روز", "", "۳۵٪ صرفه‌جویی", "انتخاب"),
            SubscriptionPlan("sub_1year", "👑 VIP", "۱ ساله", "۳۶۵ روز", "", "۵۰٪ صرفه‌جویی", "انتخاب")
        )
    }

    var selectedPlanIndex by remember { mutableStateOf(1) } // Plan 2 (3-month 🔥 plan) is default selected

    // Prevent going back or closing the block
    androidx.activity.compose.BackHandler {
        // Do nothing to block back presses completely!
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF3E8FF)) // background color #F3E8FF
        ) {
            // Two large semi-transparent purple glowing circles (opacity 0.08, Blur 40px)
            Box(
                modifier = Modifier
                    .offset(x = (-80).dp, y = (-40).dp)
                    .size(320.dp)
                    .blur(40.dp)
                    .background(Color(0xFFA855F7).copy(alpha = 0.08f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 100.dp, y = 80.dp)
                    .size(380.dp)
                    .blur(40.dp)
                    .background(Color(0xFFA855F7).copy(alpha = 0.08f), CircleShape)
            )

            // Sparkle stars layout (Premium & Magical Effect)
            Box(modifier = Modifier.fillMaxSize()) {
                Text("✨", fontSize = 24.sp, color = Color(0xFFFACC15).copy(alpha = 0.6f), modifier = Modifier.offset(x = 40.dp, y = 180.dp))
                Text("⭐", fontSize = 16.sp, color = Color(0xFFFACC15).copy(alpha = 0.5f), modifier = Modifier.offset(x = 300.dp, y = 120.dp))
                Text("💫", fontSize = 20.sp, color = Color(0xFFC084FC).copy(alpha = 0.4f), modifier = Modifier.offset(x = 80.dp, y = 450.dp))
                Text("✨", fontSize = 18.sp, color = Color(0xFFFACC15).copy(alpha = 0.6f), modifier = Modifier.offset(x = 320.dp, y = 600.dp))
                Text("⭐", fontSize = 14.sp, color = Color(0xFFFACC15).copy(alpha = 0.5f), modifier = Modifier.offset(x = 50.dp, y = 800.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // 🔒 Header Title for Expiration
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    Text(
                        text = "🔒",
                        fontSize = 28.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (licenseExpiryTime > 0L) "اشتراک شما به پایان رسیده است" else "مهلت آزمایشی شما به پایان رسیده است",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFDC2626), // beautiful bold red
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "برای ادامه استفاده از تمام امکانات بازی، یکی از اشتراک‌ها را انتخاب کنید.",
                    color = Color(0xFF4B5563), // Slate gray/dark gray
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // User Status Card
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.2.dp, Color(0xFFA855F7).copy(alpha = 0.15f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(28.dp),
                            spotColor = Color(0xFFA855F7).copy(alpha = 0.12f)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(Color(0xFFF3E8FF), CircleShape)
                                        .border(2.dp, Color(0xFF7E22CE), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🦉", fontSize = 24.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = profile?.name ?: "قهرمان ضربدر",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF1F2937)
                                    )
                                    Text(
                                        text = if (licenseExpiryTime > 0L) "اشتراک منقضی شده" else "پایان دوره آزمایشی",
                                        color = Color(0xFFEF4444),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "اعتبار باقی‌مانده",
                                    fontSize = 11.sp,
                                    color = Color(0xFF6B7280),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "منقضی شده",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text("⭐", fontSize = 16.sp, color = Color(0xFFFACC15))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "انتخاب اشتراک ویژه",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF7E22CE)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("⭐", fontSize = 16.sp, color = Color(0xFFFACC15))
                }

                // 2x2 Subscription Cards Grid
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    plans.chunked(2).forEachIndexed { rowIndex, pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            pair.forEachIndexed { colIndex, plan ->
                                val index = rowIndex * 2 + colIndex
                                val isSelected = index == selectedPlanIndex
                                
                                val cardScale by animateFloatAsState(
                                    targetValue = if (isSelected) 1.03f else 0.98f,
                                    label = "card_scale"
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .graphicsLayer {
                                            scaleX = cardScale
                                            scaleY = cardScale
                                        }
                                        .padding(top = 10.dp)
                                ) {
                                    Card(
                                        shape = RoundedCornerShape(30.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(
                                            width = if (isSelected) 2.5.dp else if (index == 2) 1.5.dp else 1.2.dp,
                                            color = if (isSelected) Color(0xFFFACC15) else if (index == 2) Color(0xFFFACC15).copy(alpha = 0.5f) else Color(0xFFA855F7).copy(alpha = 0.15f)
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedPlanIndex = index }
                                            .shadow(
                                                elevation = if (isSelected) 12.dp else 4.dp,
                                                shape = RoundedCornerShape(30.dp),
                                                spotColor = if (isSelected) Color(0xFFA855F7).copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.08f)
                                            )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(
                                                    start = 12.dp,
                                                    end = 12.dp,
                                                    top = 16.dp,
                                                    bottom = 14.dp
                                                ),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            val labelText = when (index) {
                                                0 -> "شروع سریع"
                                                1 -> "محبوب‌ترین"
                                                2 -> "به‌صرفه"
                                                3 -> "بیشترین تخفیف"
                                                else -> ""
                                            }
                                            
                                            val labelGradient = when (index) {
                                                0 -> Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)))
                                                1 -> Brush.horizontalGradient(listOf(Color(0xFFEC4899), Color(0xFFBE185D)))
                                                2 -> Brush.horizontalGradient(listOf(Color(0xFF0D9488), Color(0xFF0F766E)))
                                                3 -> Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))
                                                else -> Brush.horizontalGradient(listOf(Color.Gray, Color.DarkGray))
                                            }
                                            
                                            val labelTextColor = Color.White

                                            Box(
                                                modifier = Modifier
                                                    .shadow(
                                                        elevation = 4.dp,
                                                        shape = RoundedCornerShape(999.dp),
                                                        spotColor = Color.Black.copy(alpha = 0.08f)
                                                    )
                                                    .background(labelGradient, RoundedCornerShape(999.dp))
                                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                                            ) {
                                                Text(
                                                    text = labelText,
                                                    color = labelTextColor,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Text(plan.icon, fontSize = 18.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = plan.title,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color(0xFF1F2937)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = plan.duration,
                                                fontSize = 12.sp,
                                                color = Color(0xFF6B7280),
                                                fontWeight = FontWeight.Bold
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Text(
                                                text = plan.feature,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF22C55E),
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(999.dp),
                            clip = false,
                            ambientColor = Color(0xFF9333EA).copy(alpha = 0.35f),
                            spotColor = Color(0xFF9333EA).copy(alpha = 0.35f)
                        )
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFFA855F7), Color(0xFF9333EA))
                            ),
                            shape = RoundedCornerShape(999.dp)
                        )
                        .clickable {
                            val selectedPlan = plans[selectedPlanIndex]
                            onBuySubscription(selectedPlan.id)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✨ خرید اشتراک",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "🛡 پرداخت امن از طریق کافه بازار",
                    color = Color(0xFF6B7280),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }
        }
    }
}

private data class SubscriptionPlan(
    val id: String,
    val icon: String,
    val title: String,
    val duration: String,
    val price: String,
    val feature: String,
    val buttonText: String
)

@Composable
fun SubscriptionScreen(
    viewModel: GameViewModel,
    profile: UserProfileEntity?,
    themeConfig: GameTheme,
    permanentLicensed: Boolean,
    licenseExpiryTime: Long,
    remainingTimeMs: Long,
    onBuySubscription: (String) -> Unit
) {
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val plans = remember {
        listOf(
            SubscriptionPlan("sub_1month", "⭐", "۱ ماهه", "۳۰ روز", "", "۰٪ صرفه‌جویی", "خرید"),
            SubscriptionPlan("sub_3month", "🔥", "۳ ماهه", "۹۰ روز", "", "۳۸٪ صرفه‌جویی", "انتخاب"),
            SubscriptionPlan("sub_6month", "💎", "۶ ماهه", "۱۸۰ روز", "", "۳۵٪ صرفه‌جویی", "انتخاب"),
            SubscriptionPlan("sub_1year", "👑 VIP", "۱ ساله", "۳۶۵ روز", "", "۵۰٪ صرفه‌جویی", "انتخاب")
        )
    }
    
    var selectedPlanIndex by remember { mutableStateOf(1) } // Plan 2 (3-month 🔥 plan) is default selected

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF3E8FF)) // background color #F3E8FF
        ) {
            // Two large semi-transparent purple glowing circles (opacity 0.08, Blur 40px)
            Box(
                modifier = Modifier
                    .offset(x = (-80).dp, y = (-40).dp)
                    .size(320.dp)
                    .blur(40.dp)
                    .background(Color(0xFFA855F7).copy(alpha = 0.08f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 100.dp, y = 80.dp)
                    .size(380.dp)
                    .blur(40.dp)
                    .background(Color(0xFFA855F7).copy(alpha = 0.08f), CircleShape)
            )

            // Sparkle stars layout (Premium & Magical Effect)
            Box(modifier = Modifier.fillMaxSize()) {
                Text("✨", fontSize = 24.sp, color = Color(0xFFFACC15).copy(alpha = 0.6f), modifier = Modifier.offset(x = 40.dp, y = 180.dp))
                Text("⭐", fontSize = 16.sp, color = Color(0xFFFACC15).copy(alpha = 0.5f), modifier = Modifier.offset(x = 300.dp, y = 120.dp))
                Text("💫", fontSize = 20.sp, color = Color(0xFFC084FC).copy(alpha = 0.4f), modifier = Modifier.offset(x = 80.dp, y = 450.dp))
                Text("✨", fontSize = 18.sp, color = Color(0xFFFACC15).copy(alpha = 0.6f), modifier = Modifier.offset(x = 320.dp, y = 600.dp))
                Text("⭐", fontSize = 14.sp, color = Color(0xFFFACC15).copy(alpha = 0.5f), modifier = Modifier.offset(x = 50.dp, y = 800.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row with Title & Back Button (aligned physically to the left side)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Back button on the Left side (AbsoluteAlignment.CenterLeft)
                    IconButton(
                        onClick = { viewModel.navigateTo(GameScreen.MainMenu) },
                        modifier = Modifier
                            .align(AbsoluteAlignment.CenterLeft)
                            .size(38.dp)
                            .shadow(3.dp, CircleShape)
                            .background(Color.White, CircleShape)
                            .border(1.2.dp, Color(0xFFC084FC).copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "بازگشت",
                            tint = Color(0xFF7E22CE),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Title centered in the header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "👑",
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "اشتراک طلایی",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF7E22CE)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // User Status Card (کارت وضعیت کاربر)
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.2.dp, Color(0xFFA855F7).copy(alpha = 0.15f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(28.dp),
                            spotColor = Color(0xFFA855F7).copy(alpha = 0.12f)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Owl Avatar, Name and Remaining validity
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(Color(0xFFF3E8FF), CircleShape)
                                        .border(2.dp, Color(0xFF7E22CE), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🦉", fontSize = 24.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = profile?.name ?: "مجید",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF1F2937)
                                    )
                                    Text(
                                        text = "کاربر ویژه",
                                        color = Color(0xFF22C55E),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "اعتبار باقی‌مانده",
                                    fontSize = 11.sp,
                                    color = Color(0xFF6B7280),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (permanentLicensed) {
                                        "✨ دائمی بی‌زمان".toFa()
                                    } else {
                                        val days = (remainingTimeMs / (1000 * 60 * 60 * 24)).toInt()
                                        val hours = ((remainingTimeMs / (1000 * 60 * 60)) % 24).toInt()
                                        if (days > 0 || hours > 0) {
                                            "${days.toFa()} روز و ${hours.toFa()} ساعت"
                                        } else {
                                            "۲۲ روز و ۱۴ ساعت".toFa() // spec default
                                        }
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF1F2937)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title Section (انتخاب اشتراک ویژه with golden stars)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text("⭐", fontSize = 16.sp, color = Color(0xFFFACC15))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "انتخاب اشتراک ویژه",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF7E22CE)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("⭐", fontSize = 16.sp, color = Color(0xFFFACC15))
                }

                // 2x2 Subscription Cards Grid
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    plans.chunked(2).forEachIndexed { rowIndex, pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            pair.forEachIndexed { colIndex, plan ->
                                val index = rowIndex * 2 + colIndex
                                val isSelected = index == selectedPlanIndex
                                
                                val cardScale by animateFloatAsState(
                                    targetValue = if (isSelected) 1.03f else 0.98f,
                                    label = "card_scale"
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .graphicsLayer {
                                            scaleX = cardScale
                                            scaleY = cardScale
                                        }
                                        .padding(top = 10.dp)
                                ) {
                                    Card(
                                        shape = RoundedCornerShape(30.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(
                                            width = if (isSelected) 2.5.dp else if (index == 2) 1.5.dp else 1.2.dp,
                                            color = if (isSelected) Color(0xFFFACC15) else if (index == 2) Color(0xFFFACC15).copy(alpha = 0.5f) else Color(0xFFA855F7).copy(alpha = 0.15f)
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedPlanIndex = index }
                                            .shadow(
                                                elevation = if (isSelected) 12.dp else 4.dp,
                                                shape = RoundedCornerShape(30.dp),
                                                spotColor = if (isSelected) Color(0xFFA855F7).copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.08f)
                                            )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(
                                                    start = 12.dp,
                                                    end = 12.dp,
                                                    top = 16.dp,
                                                    bottom = 14.dp
                                                ),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            val labelText = when (index) {
                                                0 -> "شروع سریع"
                                                1 -> "محبوب‌ترین"
                                                2 -> "به‌صرفه"
                                                3 -> "بیشترین تخفیف"
                                                else -> ""
                                            }
                                            
                                            val labelGradient = when (index) {
                                                0 -> Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)))
                                                1 -> Brush.horizontalGradient(listOf(Color(0xFFEC4899), Color(0xFFBE185D)))
                                                2 -> Brush.horizontalGradient(listOf(Color(0xFF0D9488), Color(0xFF0F766E)))
                                                3 -> Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))
                                                else -> Brush.horizontalGradient(listOf(Color.Gray, Color.DarkGray))
                                            }
                                            
                                            val labelTextColor = Color.White

                                            // Integrated Label Badge (gorgeous uniform design and style)
                                            Box(
                                                modifier = Modifier
                                                    .shadow(
                                                        elevation = 4.dp,
                                                        shape = RoundedCornerShape(999.dp),
                                                        spotColor = Color.Black.copy(alpha = 0.08f)
                                                    )
                                                    .background(labelGradient, RoundedCornerShape(999.dp))
                                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                                            ) {
                                                Text(
                                                    text = labelText,
                                                    color = labelTextColor,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            // Icon & Title
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Text(plan.icon, fontSize = 18.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = plan.title,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color(0xFF1F2937)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            // Duration Text
                                            Text(
                                                text = plan.duration,
                                                fontSize = 12.sp,
                                                color = Color(0xFF6B7280),
                                                fontWeight = FontWeight.Bold
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Saving Percent (no price is shown)
                                            Text(
                                                text = plan.feature,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF22C55E),
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }


                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Main CTA Button (Premium & Cartoonish)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(999.dp),
                            clip = false,
                            ambientColor = Color(0xFF9333EA).copy(alpha = 0.35f),
                            spotColor = Color(0xFF9333EA).copy(alpha = 0.35f)
                        )
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFFA855F7), Color(0xFF9333EA))
                            ),
                            shape = RoundedCornerShape(999.dp)
                        )
                        .clickable {
                            val selectedPlan = plans[selectedPlanIndex]
                            onBuySubscription(selectedPlan.id)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✨ خرید اشتراک",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom safe payment seal
                Text(
                    text = "🛡 پرداخت امن از طریق کافه بازار",
                    color = Color(0xFF6B7280),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
fun ReverseChallengeScreen(viewModel: GameViewModel, themeConfig: GameTheme) {
    val state by viewModel.reverseState.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isLightMode = !isDarkMode

    LaunchedEffect(Unit) { viewModel.startReverseChallenge() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Upper stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = state.secondsLeft.toFa(),
                    fontSize = 32.sp,
                    color = if (state.secondsLeft < 10) Color.Red else Color(0xFFFFD32A),
                    fontWeight = FontWeight.Black
                )
                Text("ثانیه باقی مانده", color = if (isLightMode) Color(0xFF475569) else Color.LightGray, fontSize = 9.sp)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = state.score.toFa(),
                    fontSize = 32.sp,
                    color = if (isLightMode) Color(0xFF0F172A) else Color.White,
                    fontWeight = FontWeight.Black
                )
                Text("امتیاز نهایی", color = if (isLightMode) Color(0xFF475569) else Color.LightGray, fontSize = 9.sp)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🔥 ${state.streak.toFa()}",
                    fontSize = 24.sp,
                    color = Color(0xFFFF6348),
                    fontWeight = FontWeight.Black
                )
                Text("ضرب متوالی", color = if (isLightMode) Color(0xFF475569) else Color.LightGray, fontSize = 9.sp)
            }

            IconButton(
                onClick = { viewModel.navigateTo(GameScreen.MainMenu) },
                modifier = Modifier.background(themeConfig.cardBg, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Exit",
                    tint = if (isLightMode) themeConfig.primaryColor else Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Fully animated and glass-smooth timer bar
        val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
            targetValue = state.timerProgress,
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 100, easing = androidx.compose.animation.core.LinearEasing),
            label = "SmoothReverseTimer"
        )

        // Timer progress indicator
        LinearProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = if (state.secondsLeft < 10) Color.Red else Color(0xFF2ED573),
            trackColor = Color.White.copy(alpha = 0.08f)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Dialogue mascot helper
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Start
        ) {
            val currentAvatarIcon = GameConfig.AVATARS.find { it.id == (viewModel.userProfile.value?.activeAvatar ?: "owl") }?.icon ?: "🦉"

            Text(
                text = currentAvatarIcon,
                fontSize = 46.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .background(themeConfig.cardBg, RoundedCornerShape(14.dp, 14.dp, 14.dp, 0.dp))
                    .border(1.dp, themeConfig.primaryColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp, 14.dp, 14.dp, 0.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                val msg = if (state.isLocked && state.lastSelectedOptionIndex != null && state.options[state.lastSelectedOptionIndex!!] == state.correctOption) "آفرین! 🎉" else state.charMessage
                Text(
                    text = msg,
                    color = if (isLightMode) Color(0xFF0F172A) else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Formula representation Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = themeConfig.cardBg),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "?",
                            color = Color(0xFF00D2D3),
                            fontSize = 62.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = " × ",
                            color = Color(0xFFFF6D81),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Text(
                            text = "?",
                            color = Color(0xFF00D2D3),
                            fontSize = 62.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = " = ",
                            color = Color(0xFFFFD32A),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Text(
                            text = state.answerToShow.toFa(),
                            color = Color(0xFFFFD32A),
                            fontSize = 62.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Answers
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.options.size) { index ->
                val option = state.options[index]
                val isCorrectVal = option == state.correctOption
                val itemSelected = state.lastSelectedOptionIndex == index
                val isLight = isLightMode

                val backgroundCol = when {
                    state.isLocked && isCorrectVal -> Color(0xFF2ED573) // Solid Vivid Green
                    state.isLocked && itemSelected && !isCorrectVal -> Color(0xFFEA2027) // Solid Vivid Red
                    else -> themeConfig.cardBg
                }

                val borderCol = when {
                    state.isLocked && isCorrectVal -> Color(0xFF2ED573)
                    state.isLocked && itemSelected && !isCorrectVal -> Color(0xFFEA2027)
                    else -> if (isLight) themeConfig.primaryColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.15f)
                }

                val txtColor = when {
                    state.isLocked && (isCorrectVal || (itemSelected && !isCorrectVal)) -> Color.White
                    else -> if (isLight) Color(0xFF0F172A) else Color.White
                }

                Card(
                    onClick = {
                        if (!state.isLocked) {
                            viewModel.submitReverseAnswer(option, index)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(86.dp)
                        .border(2.dp, borderCol, RoundedCornerShape(16.dp))
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = backgroundCol,
                        contentColor = txtColor,
                        disabledContainerColor = backgroundCol,
                        disabledContentColor = txtColor
                    ),
                    enabled = true
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = option.first.toFa(),
                                    color = txtColor,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = " × ",
                                    color = txtColor.copy(alpha = 0.6f),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                                Text(
                                    text = option.second.toFa(),
                                    color = txtColor,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

// ─── صفحه ۱: ورود اسم ───────────────────────────────────────
@Composable
fun DuelSetupScreen(viewModel: com.example.ui.GameViewModel, themeConfig: GameTheme) {
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isLight = !isDark

    val duelPrefs by viewModel.duelPrefs.collectAsStateWithLifecycle()
    
    var name1 by remember(duelPrefs) { mutableStateOf(duelPrefs?.player1Name ?: "") }
    var name2 by remember(duelPrefs) { mutableStateOf(duelPrefs?.player2Name ?: "") }
    var avatar1 by remember(duelPrefs) { mutableStateOf(duelPrefs?.player1Avatar ?: "👦") }
    var avatar2 by remember(duelPrefs) { mutableStateOf(duelPrefs?.player2Avatar ?: "👧") }
    var selectedDuration by remember(duelPrefs) { mutableStateOf(duelPrefs?.matchDuration ?: 60) }
    
    val p1Avatars = listOf("👦", "👧", "🧒", "👱‍♂️", "👩", "👨", "👩‍🦱", "👨‍🦱")
    val p2Avatars = listOf("🐯", "🐻", "🐶", "🐱", "🐰", "🦊", "🐼", "🐨", "🐸", "🦁")

    val bgBrush = Brush.verticalGradient(
        listOf(
            Color(0xFF1E1B4B),
            Color(0xFF312E81),
            Color(0xFF1E1B4B)
        )
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(modifier = Modifier.fillMaxSize().background(bgBrush)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(color = Color(0xFF6366F1).copy(alpha = 0.15f), radius = 250.dp.toPx(), center = Offset(size.width * 0.2f, size.height * 0.2f))
                drawCircle(color = Color(0xFFEC4899).copy(alpha = 0.1f), radius = 350.dp.toPx(), center = Offset(size.width * 0.8f, size.height * 0.8f))
                // Minimal sparkles
                drawCircle(color = Color.White.copy(alpha = 0.4f), radius = 4.dp.toPx(), center = Offset(size.width * 0.15f, size.height * 0.25f))
                drawCircle(color = Color.White.copy(alpha = 0.3f), radius = 3.dp.toPx(), center = Offset(size.width * 0.85f, size.height * 0.15f))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(32.dp))

                Text(
                    text = "⚔️ چالش دو نفره",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color(0xFF6366F1).copy(alpha = 0.8f),
                            blurRadius = 12f
                        )
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "آماده نبرد هستید؟ نام بازیکنان را وارد کنید",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFC7D2FE),
                    lineHeight = 28.sp
                )

                Spacer(Modifier.height(24.dp))

                // Player 1 Card (Blue)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A8A).copy(alpha = 0.35f)),
                    border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0xFF3B82F6), CircleShape)
                                .border(2.dp, Color(0xFFBFDBFE), CircleShape)
                                .clickable {
                                    viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelClick)
                                    val next = p1Avatars[(p1Avatars.indexOf(avatar1) + 1) % p1Avatars.size]
                                    avatar1 = next
                                    viewModel.updateDuelPrefs(player1Avatar = next)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(avatar1, fontSize = 28.sp)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("بازیکن اول", fontSize = 18.sp, color = Color(0xFF93C5FD), fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = name1,
                                onValueChange = { if (it.length <= 18) { name1 = it; viewModel.updateDuelPrefs(player1Name = it) } },
                                placeholder = { Text("نام...", color = Color(0xFF93C5FD).copy(alpha=0.5f), fontSize = 20.sp, fontWeight = FontWeight.Medium) },
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                                modifier = Modifier.fillMaxWidth().height(64.dp),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF60A5FA),
                                    unfocusedBorderColor = Color(0xFF3B82F6).copy(alpha=0.3f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Player 2 Card (Green)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B).copy(alpha = 0.35f)),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0xFF10B981), CircleShape)
                                .border(2.dp, Color(0xFFA7F3D0), CircleShape)
                                .clickable {
                                    viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelClick)
                                    val next = p2Avatars[(p2Avatars.indexOf(avatar2) + 1) % p2Avatars.size]
                                    avatar2 = next
                                    viewModel.updateDuelPrefs(player2Avatar = next)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(avatar2, fontSize = 28.sp)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("بازیکن دوم", fontSize = 18.sp, color = Color(0xFF6EE7B7), fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = name2,
                                onValueChange = { if (it.length <= 18) { name2 = it; viewModel.updateDuelPrefs(player2Name = it) } },
                                placeholder = { Text("نام...", color = Color(0xFF6EE7B7).copy(alpha=0.5f), fontSize = 20.sp, fontWeight = FontWeight.Medium) },
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                                modifier = Modifier.fillMaxWidth().height(64.dp),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF34D399),
                                    unfocusedBorderColor = Color(0xFF10B981).copy(alpha=0.3f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Duration Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⏱ مدت مسابقه", fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(30, 60, 90, 120).forEach { dur ->
                                val selected = selectedDuration == dur
                                val scale by animateFloatAsState(targetValue = if (selected) 1.05f else 1f, label = "")
                                val bg = if (selected) Color(0xFF8B5CF6) else Color.White.copy(alpha=0.1f)
                                val txtColor = if (selected) Color.White else Color(0xFFA5B4FC)
                                val bdColor = if (selected) Color(0xFFC4B5FD) else Color.Transparent

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .graphicsLayer { scaleX = scale; scaleY = scale }
                                        .background(bg, RoundedCornerShape(12.dp))
                                        .border(1.dp, bdColor, RoundedCornerShape(12.dp))
                                        .clickable(
                                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                            indication = androidx.compose.foundation.LocalIndication.current
                                        ) {
                                            viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelClick)
                                            selectedDuration = dur
                                            viewModel.updateDuelPrefs(matchDuration = dur)
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${with(viewModel) { dur.toFa() }} ثانیه",
                                        color = txtColor,
                                        fontSize = 18.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Start Button
                val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val btnScale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, animationSpec = spring(), label = "")
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .graphicsLayer { scaleX = btnScale; scaleY = btnScale }
                        .shadow(if (isPressed) 4.dp else 16.dp, RoundedCornerShape(32.dp), spotColor = Color(0xFF8B5CF6))
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))),
                            RoundedCornerShape(32.dp)
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = androidx.compose.foundation.LocalIndication.current
                        ) {
                            viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelClick)
                            viewModel.startDuel(name1, name2, selectedDuration)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("🚀 شروع مسابقه", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                }

                Spacer(Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelClick)
                        viewModel.navigateTo(com.example.ui.GameScreen.MainMenu)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, Color(0xFFA5B4FC).copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack, 
                        contentDescription = "بازگشت", 
                        tint = Color(0xFFA5B4FC), 
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("بازگشت به منو", color = Color(0xFFA5B4FC), fontSize = 20.sp, fontWeight = FontWeight.Medium)
                }
                
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun NumericKeypad(
    currentValue: String,
    onNumberPressed: (Int) -> Unit,
    onDelete: () -> Unit,
    onSubmit: () -> Unit,
    enabled: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onPlayClickSound: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current

    val keys = listOf(
        listOf(3, 2, 1),
        listOf(6, 5, 4),
        listOf(9, 8, 7),
        listOf(-1, 0, -2) // -1 for Delete, -2 for Submit
    )

    Card(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF312E81).copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            keys.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { key ->
                        val isAction = key < 0
                        val isSubmit = key == -2
                        val isDelete = key == -1
                        
                        val buttonColor = when {
                            isSubmit -> Color(0xFF10B981) // Green
                            isDelete -> Color(0xFFEF4444) // Red
                            else -> Color(0xFF5B21B6).copy(alpha = 0.8f) // Deep Fantasy Purple for better contrast
                        }
                        
                        val contentColor = Color.White
                        
                        val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        
                        val scale by animateFloatAsState(
                            targetValue = if (isPressed) 0.96f else 1f,
                            animationSpec = tween(durationMillis = 100),
                            label = "scaleAnim"
                        )
                        
                        val elevation by animateDpAsState(
                            targetValue = if (isPressed) 1.dp else 4.dp,
                            animationSpec = tween(durationMillis = 100),
                            label = "elevationAnim"
                        )
                        
                        val glowColor = when {
                            isSubmit -> Color(0xFF22C55E).copy(alpha = 0.5f)
                            isDelete -> Color(0xFFEF4444).copy(alpha = 0.5f)
                            else -> accentColor.copy(alpha = 0.4f)
                        }
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                                .shadow(
                                    elevation = elevation,
                                    shape = RoundedCornerShape(22.dp),
                                    ambientColor = glowColor,
                                    spotColor = glowColor
                                )
                                .background(buttonColor, RoundedCornerShape(22.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
                                .clip(RoundedCornerShape(22.dp))
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = androidx.compose.foundation.LocalIndication.current,
                                    enabled = enabled
                                ) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onPlayClickSound()
                                    if (isDelete) onDelete()
                                    else if (isSubmit) onSubmit()
                                    else onNumberPressed(key)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDelete) {
                                Text(
                                    text = "⌫", 
                                    fontSize = 26.sp, 
                                    color = contentColor,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    style = androidx.compose.ui.text.TextStyle(
                                        platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                                            includeFontPadding = false
                                        ),
                                        lineHeightStyle = androidx.compose.ui.text.style.LineHeightStyle(
                                            alignment = androidx.compose.ui.text.style.LineHeightStyle.Alignment.Center,
                                            trim = androidx.compose.ui.text.style.LineHeightStyle.Trim.Both
                                        )
                                    ),
                                    modifier = Modifier.wrapContentSize(Alignment.Center).offset(y = (-3).dp)
                                )
                            } else if (isSubmit) {
                                Text(
                                    text = "✓", 
                                    fontSize = 28.sp, 
                                    color = contentColor, 
                                    fontWeight = FontWeight.Black,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    style = androidx.compose.ui.text.TextStyle(
                                        platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                                            includeFontPadding = false
                                        ),
                                        lineHeightStyle = androidx.compose.ui.text.style.LineHeightStyle(
                                            alignment = androidx.compose.ui.text.style.LineHeightStyle.Alignment.Center,
                                            trim = androidx.compose.ui.text.style.LineHeightStyle.Trim.Both
                                        )
                                    ),
                                    modifier = Modifier.wrapContentSize(Alignment.Center).offset(y = (-3).dp)
                                )
                            } else {
                                Text(
                                    text = key.toFa(),
                                    fontSize = 34.sp,
                                    color = contentColor,
                                    fontWeight = FontWeight.Black,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    style = androidx.compose.ui.text.TextStyle(
                                        platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                                            includeFontPadding = false
                                        ),
                                        lineHeightStyle = androidx.compose.ui.text.style.LineHeightStyle(
                                            alignment = androidx.compose.ui.text.style.LineHeightStyle.Alignment.Center,
                                            trim = androidx.compose.ui.text.style.LineHeightStyle.Trim.Both
                                        )
                                    ),
                                    modifier = Modifier.wrapContentSize(Alignment.Center).offset(y = (-5).dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DuelGameScreen(viewModel: com.example.ui.GameViewModel, themeConfig: GameTheme) {
    val state by viewModel.duelState.collectAsStateWithLifecycle()
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isLight = !isDark
    val context = androidx.compose.ui.platform.LocalContext.current

    var countdownFinished by remember(state.phase) { mutableStateOf(state.phase != com.example.ui.DuelPhase.Playing) }
    var countdownStep by remember(state.phase) { mutableStateOf<String?>(if (state.phase == com.example.ui.DuelPhase.Playing) "۳" else null) }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    LaunchedEffect(state.phase) {
        if (state.phase == com.example.ui.DuelPhase.Playing && !countdownFinished) {
            viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelTick)
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
            delay(1000)
            
            countdownStep = "۲"
            viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelTick)
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
            delay(1000)
            
            countdownStep = "۱"
            viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelTick)
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
            delay(1000)
            
            countdownStep = "🚀 شروع!"
            viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelStart)
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            delay(1000)
            
            countdownStep = null
            countdownFinished = true
        }
    }

    val duelPrefs by viewModel.duelPrefs.collectAsStateWithLifecycle()
    val avatar1 = duelPrefs?.player1Avatar ?: "👦"
    val avatar2 = duelPrefs?.player2Avatar ?: "👧"


    // Modern AAA mobile game background: Premium Fantasy Purple
    val bgBrush = Brush.verticalGradient(
        listOf(
            Color(0xFF0F0C29), // Deepest purple/black
            Color(0xFF302B63), // Mid purple
            Color(0xFF24243E)  // Dark slate purple
        )
    )

    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "bgAnim")
    
    val ambientGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.9f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(4000, easing = androidx.compose.animation.core.LinearOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "ambientGlow"
    )
    
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(8000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "floatAnim"
    )
    
    val starGlow1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(2500, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "starGlow1"
    )

    val starGlow2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(3500, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "starGlow2"
    )

    Box(modifier = Modifier.fillMaxSize().background(bgBrush)) {
        // Magical Particles and Ambient Glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            
            // Top glowing orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF8B5CF6).copy(alpha = 0.3f * ambientGlow), Color.Transparent),
                    center = Offset(width * 0.2f, height * 0.15f + (floatAnim * 60f)),
                    radius = width * 0.7f
                ),
                center = Offset(width * 0.2f, height * 0.15f + (floatAnim * 60f)),
                radius = width * 0.7f
            )
            
            // Bottom glowing orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFEC4899).copy(alpha = 0.25f * ambientGlow), Color.Transparent),
                    center = Offset(width * 0.85f, height * 0.85f - (floatAnim * 50f)),
                    radius = width * 0.8f
                ),
                center = Offset(width * 0.85f, height * 0.85f - (floatAnim * 50f)),
                radius = width * 0.8f
            )
            
            // Center subtle highlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF6366F1).copy(alpha = 0.2f), Color.Transparent),
                    center = Offset(width * 0.5f, height * 0.5f),
                    radius = width * 0.6f
                ),
                center = Offset(width * 0.5f, height * 0.5f),
                radius = width * 0.6f
            )
            

            
            // Vignette effect overlay
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color(0xFF03001C).copy(alpha = 0.5f)),
                    center = Offset(width / 2, height / 2),
                    radius = maxOf(width, height) * 0.8f
                ),
                size = size
            )
        }

        @Composable
        fun PlayerHalf(
            player: com.example.ui.DuelPlayerState,
            playerNum: Int,
            isFlipped: Boolean,
            accentColor: Color,
            modifierWeight: Modifier = Modifier,
            timeLeft: Int,
            totalTime: Int,
            avatarEmoji: String,
            showGameplayElements: Boolean
        ) {
            var input by remember { mutableStateOf("") }
            LaunchedEffect(player.a, player.b) { input = "" }
            
            var lastScore by remember { mutableStateOf(player.score) }
            var scoreIncrement by remember { mutableStateOf(0) }
            var scoreTrigger by remember { mutableStateOf(0) }
            
            LaunchedEffect(player.score) {
                if (player.score > lastScore) {
                    scoreIncrement = player.score - lastScore
                    lastScore = player.score
                    scoreTrigger++
                }
            }
            
            val scoreScale by animateFloatAsState(
                targetValue = if (scoreTrigger % 2 == 0) 1f else 1.3f,
                animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f),
                finishedListener = { if (scoreTrigger % 2 != 0) scoreTrigger++ },
                label = "scoreScale"
            )
            
            var inputTrigger by remember { mutableStateOf(0) }
            LaunchedEffect(input) {
                if (input.isNotEmpty()) {
                    inputTrigger++
                }
            }
            val interactionScale by animateFloatAsState(
                targetValue = if (inputTrigger % 2 == 0) 1f else 1.02f,
                animationSpec = tween(durationMillis = 150, easing = androidx.compose.animation.core.LinearOutSlowInEasing),
                finishedListener = { if (inputTrigger % 2 != 0) inputTrigger++ },
                label = "interactionScale"
            )
            
            val cardBorderColor = if (inputTrigger % 2 != 0) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.15f)
            val cardShadow = if (inputTrigger % 2 != 0) 12.dp else 4.dp
            
            val progress = if (totalTime > 0) timeLeft.toFloat() / totalTime else 0f
            val timerColor = when {
                progress > 0.5f -> Color(0xFF4ADE80) // Green
                progress > 0.25f -> Color(0xFFFFD700) // Gold
                progress > 0.1f -> Color(0xFFF97316) // Orange
                else -> Color(0xFFEF4444) // Red
            }
            
            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(1000, easing = androidx.compose.animation.core.LinearEasing),
                label = "animatedProgress"
            )

            val rotation = if (isFlipped) 180f else 0f

            Box(
                modifier = modifierWeight
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .graphicsLayer { rotationZ = rotation }
            ) {
                Column(Modifier.fillMaxSize()) {
                    // Header Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { scaleX = interactionScale; scaleY = interactionScale }
                            .shadow(cardShadow, RoundedCornerShape(24.dp), ambientColor = accentColor, spotColor = accentColor),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF312E81).copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(1.dp, cardBorderColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar and Name
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier
                                        .size(56.dp)
                                        .background(Color.White.copy(alpha=0.15f), CircleShape)
                                        .border(2.dp, Color.White.copy(alpha=0.8f), CircleShape)
                                        .shadow(4.dp, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(avatarEmoji, fontSize = 32.sp)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(player.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 1.sp)
                                    
                                    // Score Row
                                    Box(contentAlignment = Alignment.BottomStart) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.graphicsLayer { scaleX = scoreScale; scaleY = scoreScale }) {
                                            Text("🏆", fontSize = 18.sp)
                                            Spacer(Modifier.width(4.dp))
                                            Text(with(viewModel) { player.score.toFa() }, fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFFFFD700))
                                        }
                                        
                                        // Floating score feedback
                                        androidx.compose.animation.AnimatedVisibility(
                                            visible = scoreTrigger % 2 != 0,
                                            enter = slideInVertically(initialOffsetY = { 50 }) + fadeIn(),
                                            exit = fadeOut(tween(400)) + slideOutVertically(targetOffsetY = { -50 }),
                                            modifier = Modifier.offset(x = 60.dp, y = (-20).dp)
                                        ) {
                                            Text("+" + with(viewModel) { scoreIncrement.toFa() }, color = Color(0xFF4ADE80), fontWeight = FontWeight.Black, fontSize = 24.sp)
                                        }
                                    }
                                }
                            }
                            
                            // Timer Ring
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(64.dp)
                            ) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    progress = { animatedProgress },
                                    modifier = Modifier.fillMaxSize(),
                                    color = if (showGameplayElements) timerColor else Color.Transparent,
                                    trackColor = if (showGameplayElements) Color.White.copy(alpha=0.1f) else Color.Transparent,
                                    strokeWidth = 6.dp,
                                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                                    gapSize = 0.dp
                                )
                                Text(
                                    with(viewModel) { timeLeft.toFa() }, 
                                    fontSize = 22.sp, 
                                    fontWeight = FontWeight.Black, 
                                    color = if (showGameplayElements) timerColor else Color.Transparent
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Question Card
                    var questionShakeTrigger by remember { mutableStateOf(0) }
                    var questionBounceTrigger by remember { mutableStateOf(0) }
                    var currentQuestionKey by remember { mutableStateOf("${player.a}_${player.b}") }
                    
                    LaunchedEffect(player.a, player.b) {
                        currentQuestionKey = "${player.a}_${player.b}"
                    }

                    LaunchedEffect(player.feedbackIsCorrect) {
                        if (player.feedbackIsCorrect == false) {
                            questionShakeTrigger++
                        } else if (player.feedbackIsCorrect == true) {
                            questionBounceTrigger++
                        }
                    }

                    val shakeOffset by animateFloatAsState(
                        targetValue = if (questionShakeTrigger % 2 == 0) 0f else 15f,
                        animationSpec = spring(dampingRatio = 0.15f, stiffness = 3000f),
                        finishedListener = { if (questionShakeTrigger % 2 != 0) questionShakeTrigger++ },
                        label = "shake"
                    )

                    val bounceScale by animateFloatAsState(
                        targetValue = if (questionBounceTrigger % 2 == 0) 1f else 1.05f,
                        animationSpec = spring(dampingRatio = 0.4f, stiffness = 600f),
                        finishedListener = { if (questionBounceTrigger % 2 != 0) questionBounceTrigger++ },
                        label = "bounce"
                    )

                    val isCorrect = player.feedbackIsCorrect == true
                    val isWrong = player.feedbackIsCorrect == false
                    
                    val questionBgColor = when {
                        isCorrect -> Color(0xFF10B981).copy(alpha = 0.2f)
                        isWrong -> Color(0xFFEF4444).copy(alpha = 0.2f)
                        else -> Color(0xFF312E81).copy(alpha = 0.4f)
                    }
                    
                    val questionBorderColor = when {
                        isCorrect -> Color(0xFF10B981).copy(alpha = 0.8f)
                        isWrong -> Color(0xFFEF4444).copy(alpha = 0.8f)
                        else -> Color.White.copy(alpha = 0.15f)
                    }
                    
                    val shadowGlowColor = when {
                        isCorrect -> Color(0xFF10B981)
                        isWrong -> Color(0xFFEF4444)
                        else -> accentColor
                    }

                    Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(0.3f)
                            .padding(horizontal = 16.dp)
                            .offset(x = shakeOffset.dp)
                            .graphicsLayer {
                                scaleX = bounceScale
                                scaleY = bounceScale
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (showGameplayElements) {
                            AnimatedContent(
                                targetState = currentQuestionKey,
                                transitionSpec = {
                                    (scaleIn(initialScale = 0.8f) + fadeIn(tween(200))) togetherWith (scaleOut(targetScale = 1.2f) + fadeOut(tween(200)))
                                },
                                label = "questionAnim"
                            ) { _ ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(0.9f)
                                        .shadow(8.dp, RoundedCornerShape(24.dp), ambientColor = shadowGlowColor, spotColor = shadowGlowColor),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = questionBgColor),
                                    border = BorderStroke(1.dp, questionBorderColor)
                                ) {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(with(viewModel) { player.a.toFa() }, fontSize = 48.sp, fontWeight = FontWeight.Black, color = Color.White)
                                                Text(" × ", fontSize = 36.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                                                Text(with(viewModel) { player.b.toFa() }, fontSize = 48.sp, fontWeight = FontWeight.Black, color = Color.White)
                                                Text(" = ", fontSize = 36.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                                                
                                                AnimatedContent(
                                                    targetState = if (input.isEmpty()) "?" else input,
                                                    transitionSpec = {
                                                        (scaleIn(initialScale = 0.5f) + fadeIn(tween(200))) togetherWith (scaleOut(targetScale = 1.5f) + fadeOut(tween(200)))
                                                    },
                                                    label = "answerAnim"
                                                ) { text ->
                                                    val faText = if (text == "?") "?" else with(viewModel) { text.toIntOrNull()?.toFa() ?: text }
                                                    Text(
                                                        text = faText,
                                                        fontSize = 52.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = if (text == "?") accentColor else Color(0xFFFFD700)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Keypad
                    Box(Modifier.fillMaxWidth().weight(0.7f)) {
                        if (showGameplayElements) {
                            NumericKeypad(
                                modifier = Modifier.fillMaxSize(),
                                currentValue = input,
                                onNumberPressed = { num -> if (input.length < 3) input += num.toString() },
                                onDelete = { if (input.isNotEmpty()) input = input.dropLast(1) },
                                onSubmit = { 
                                    if (!player.answered) {
                                        input.toIntOrNull()?.let { 
                                            viewModel.submitDuelAnswer(playerNum, it)
                                        } 
                                    }
                                },
                                enabled = !player.answered,
                                accentColor = accentColor,
                                onPlayClickSound = { viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelClick) }
                            )
                        }
                    }
                    
                }
            }
        }

        val insets = androidx.compose.foundation.layout.WindowInsets.safeDrawing.asPaddingValues()
        val bottomInset = insets.calculateBottomPadding()
        val topInset = insets.calculateTopPadding()
        val maxInset = androidx.compose.ui.unit.max(bottomInset, topInset).coerceAtLeast(16.dp)
        
        Column(Modifier.fillMaxSize().padding(top = maxInset, bottom = maxInset)) {
            // Top Half (Player 2, Flipped)
            PlayerHalf(
                player = state.player2, playerNum = 2, isFlipped = true,
                accentColor = Color(0xFF10B981), // Emerald Green
                modifierWeight = Modifier.weight(1f),
                timeLeft = state.secondsLeft,
                totalTime = state.totalSeconds,
                avatarEmoji = avatar2,
                showGameplayElements = countdownFinished
            )

            // Splitter
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(2.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(alpha=0.3f), Color.Transparent))))
            Spacer(Modifier.height(8.dp))

            // Bottom Half (Player 1, Normal)
            PlayerHalf(
                player = state.player1, playerNum = 1, isFlipped = false,
                accentColor = Color(0xFF3B82F6), // Blue
                modifierWeight = Modifier.weight(1f),
                timeLeft = state.secondsLeft,
                totalTime = state.totalSeconds,
                avatarEmoji = avatar1,
                showGameplayElements = countdownFinished
            )
        }

        
        if (!countdownFinished) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .pointerInput(Unit) { detectTapGestures { } },
                contentAlignment = Alignment.Center
            ) {
                countdownStep?.let { step ->
                    AnimatedContent(
                        targetState = step,
                        transitionSpec = {
                            (scaleIn(initialScale = 0.5f, animationSpec = spring(dampingRatio = 0.5f, stiffness = 200f)) + fadeIn(tween(200))) togetherWith (scaleOut(targetScale = 1.5f, animationSpec = tween(200)) + fadeOut(tween(200)))
                        },
                        label = "countdown"
                    ) { currentStep ->
                        Text(
                            text = currentStep,
                            fontSize = if (currentStep.length > 1) 72.sp else 120.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            style = androidx.compose.ui.text.TextStyle(
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color(0xFF6366F1).copy(alpha = 0.8f),
                                    blurRadius = 24f
                                )
                            )
                        )
                    }
                }
            }
        }
    }
}
// ─── صفحه ۳: نتیجه (Winner Celebration Overlay) ──────────────────────────────────────────
@Composable
fun DuelResultScreen(viewModel: com.example.ui.GameViewModel, themeConfig: GameTheme, onShowStats: () -> Unit = {}) {
    val result by viewModel.duelResult.collectAsStateWithLifecycle()
    val state  by viewModel.duelState.collectAsStateWithLifecycle()
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isLight = !isDark
    
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
        if (result.winner == null) {
            viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelDraw)
        } else {
            viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelVictory)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Confetti Canvas
            ConfettiEffect()
            
            AnimatedVisibility(
                visible = visible,
                enter = scaleIn(initialScale = 0.5f, animationSpec = spring(dampingRatio = 0.5f, stiffness = 200f)) + 
                        fadeIn(tween(400)),
                exit = fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF2E1065).copy(alpha = 0.85f),
                                    Color(0xFF4C1D95).copy(alpha = 0.95f)
                                )
                            ),
                            RoundedCornerShape(32.dp)
                        )
                        .border(2.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(32.dp))
                        .padding(32.dp)
                ) {
                    
                    val isDraw = result.winner == null
                    
                    // Floating Trophy
                    val infiniteTransition = rememberInfiniteTransition(label = "trophyFloat")
                    val offset by infiniteTransition.animateFloat(
                        initialValue = -10f,
                        targetValue = 10f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "trophyFloat"
                    )
                    val glow by infiniteTransition.animateFloat(
                        initialValue = 0.5f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "trophyGlow"
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .offset(y = offset.dp)
                            .padding(bottom = 16.dp)
                    ) {
                        // Glow
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .graphicsLayer { scaleX = glow; scaleY = glow }
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Color(0xFFFFD700).copy(alpha = 0.6f), Color.Transparent)
                                    ),
                                    CircleShape
                                )
                        )
                        Text(
                            text = if (isDraw) "🤝" else "🏆",
                            fontSize = 80.sp
                        )
                        // Sparkles
                        SparkleEffect()
                    }

                    Text(
                        text = if (isDraw) "مسابقه مساوی شد" else "برنده مسابقه",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFD700), // Golden
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black, blurRadius = 8f
                            )
                        ),
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    if (isDraw) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
                        ) {
                            PlayerScoreDisplay(
                                name = state.player1.name,
                                score = result.score1,
                                viewModel = viewModel
                            )
                            PlayerScoreDisplay(
                                name = state.player2.name,
                                score = result.score2,
                                viewModel = viewModel
                            )
                        }
                    } else {
                        val winnerName = if (result.winner == 1) state.player1.name else state.player2.name
                        val winnerScore = if (result.winner == 1) result.score1 else result.score2
                        val duelPrefs by viewModel.duelPrefs.collectAsStateWithLifecycle()
                        val avatar = if (result.winner == 1) {
                            duelPrefs?.player1Avatar ?: "👦"
                        } else {
                            duelPrefs?.player2Avatar ?: "🐯"
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(bottom = 32.dp)
                        ) {
                            Text(text = avatar, fontSize = 64.sp, modifier = Modifier.padding(bottom = 8.dp))
                            Text(
                                text = winnerName,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "امتیاز: ${with(viewModel){ winnerScore.toFa() }}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }

                    // Buttons
                    Button(
                        onClick = { onShowStats() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text(
                            text = "نمایش عملکرد 📊",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Button(
                        onClick = { viewModel.rematchDuel() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                    ) {
                        Text(
                            text = "بازی مجدد ⚔️",
                            color = Color(0xFF1E1B4B),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Button(
                        onClick = { viewModel.exitDuelMode() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.15f)
                        ),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "بازگشت به منوی اصلی 🏠",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerScoreDisplay(name: String, score: Int, viewModel: com.example.ui.GameViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = name,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "امتیاز: ${with(viewModel){ score.toFa() }}",
            fontSize = 18.sp,
            color = Color(0xFFE2E8F0)
        )
    }
}

data class Confetti(val x: Float, val y: Float, val color: Color, val radius: Float, val speed: Float, val angle: Float)

@Composable
fun ConfettiEffect() {
    var particles by remember { mutableStateOf(emptyList<Confetti>()) }
    val colors = listOf(Color(0xFFFFD700), Color(0xFFEC4899), Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFF8B5CF6))
    
    LaunchedEffect(Unit) {
        val initialParticles = List(50) {
            Confetti(
                x = (0..100).random() / 100f,
                y = (0..100).random() / 100f - 1f, // start slightly above or at top
                color = colors.random(),
                radius = (8..15).random().toFloat(),
                speed = (2..6).random().toFloat() / 1000f,
                angle = (-10..10).random().toFloat()
            )
        }
        particles = initialParticles
        
        while (true) {
            withFrameNanos { 
                particles = particles.map { p ->
                    val newY = p.y + p.speed
                    val newX = p.x + (kotlin.math.sin(newY * 10f + p.angle) * 0.005).toFloat()
                    if (newY > 1.2f) {
                        p.copy(y = -0.1f, x = (0..100).random() / 100f)
                    } else {
                        p.copy(y = newY, x = newX)
                    }
                }
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { p ->
            drawCircle(
                color = p.color,
                radius = p.radius,
                center = androidx.compose.ui.geometry.Offset(p.x * size.width, p.y * size.height)
            )
        }
    }
}

data class Sparkle(val x: Float, val y: Float, val alpha: Float, val size: Float)

@Composable
fun SparkleEffect() {
    var sparkles by remember { mutableStateOf(emptyList<Sparkle>()) }
    
    LaunchedEffect(Unit) {
        sparkles = List(8) {
            Sparkle(
                x = (-50..50).random().toFloat(),
                y = (-50..50).random().toFloat(),
                alpha = (0..100).random() / 100f,
                size = (3..8).random().toFloat()
            )
        }
        
        while (true) {
            withFrameNanos {
                sparkles = sparkles.map { s ->
                    val newAlpha = s.alpha - 0.02f
                    if (newAlpha <= 0) {
                        s.copy(
                            x = (-60..60).random().toFloat(),
                            y = (-60..60).random().toFloat(),
                            alpha = 1f,
                            size = (3..8).random().toFloat()
                        )
                    } else {
                        s.copy(alpha = newAlpha)
                    }
                }
            }
        }
    }
    
    Canvas(modifier = Modifier.size(120.dp)) {
        val centerPoint = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
        sparkles.forEach { s ->
            drawCircle(
                color = Color.White.copy(alpha = s.alpha),
                radius = s.size,
                center = androidx.compose.ui.geometry.Offset(centerPoint.x + s.x, centerPoint.y + s.y)
            )
        }
    }
}

// ─── صفحه ۴: آمار مسابقه (Duel Statistics) ──────────────────────────────────────────
@Composable
fun DuelStatsScreen(viewModel: com.example.ui.GameViewModel, themeConfig: GameTheme) {
    val result by viewModel.duelResult.collectAsStateWithLifecycle()
    val state by viewModel.duelState.collectAsStateWithLifecycle()
    val duelPrefs by viewModel.duelPrefs.collectAsStateWithLifecycle()
    
    val p1Score = result.score1
    val p2Score = result.score2
    val p1Name = state.player1.name
    val p2Name = state.player2.name
    val avatar1 = duelPrefs?.player1Avatar ?: "👦"
    val avatar2 = duelPrefs?.player2Avatar ?: "👧"
    val isDraw = result.winner == null
    val winnerName = if (isDraw) "مساوی" else result.winnerName
    val scoreDiff = kotlin.math.abs(p1Score - p2Score)
    val duration = state.totalSeconds
    val p1 = state.player1
    val p2 = state.player2
    
    var visible by remember { mutableStateOf(false) }
    val animProgress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(1500, easing = FastOutSlowInEasing), label = "countUp"
    )
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    fun fa(num: Any): String = num.toString().replace("0", "۰").replace("1", "۱").replace("2", "۲").replace("3", "۳").replace("4", "۴").replace("5", "۵").replace("6", "۶").replace("7", "۷").replace("8", "۸").replace("9", "۹")
    
    val p1Total = p1.correctCount + p1.wrongCount
    val p2Total = p2.correctCount + p2.wrongCount
    val p1Acc = if (p1Total > 0) ((p1.correctCount.toFloat() / p1Total) * 100).toInt() else 0
    val p2Acc = if (p2Total > 0) ((p2.correctCount.toFloat() / p2Total) * 100).toInt() else 0
    val p1Fast = if (p1.fastestTimeMs == Long.MAX_VALUE) 0f else p1.fastestTimeMs / 1000f
    val p2Fast = if (p2.fastestTimeMs == Long.MAX_VALUE) 0f else p2.fastestTimeMs / 1000f
    val p1Avg = if (p1Total > 0) (p1.totalTimeMs.toFloat() / p1Total) / 1000f else 0f
    val p2Avg = if (p2Total > 0) (p2.totalTimeMs.toFloat() / p2Total) / 1000f else 0f

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF1E1B4B), Color(0xFF312E81))))
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) + slideInVertically(initialOffsetY = { it / 4 }, animationSpec = tween(600)),
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🏆 آمار مسابقه", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 8.dp, top = 8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(avatar1, fontSize = 48.sp)
                            Text(p1Name, fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Text("🆚", fontSize = 24.sp)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(avatar2, fontSize = 48.sp)
                            Text(p2Name, fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    val cardBorder = if (!isDraw) BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)) else BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    val cardShadow = if (!isDraw) androidx.compose.ui.graphics.Shadow(color = Color(0xFFFFD700).copy(alpha = 0.6f), blurRadius = 16f) else androidx.compose.ui.graphics.Shadow(color = Color.Transparent, blurRadius = 0f)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                        border = cardBorder,
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            Modifier.padding(vertical = 12.dp, horizontal = 16.dp), 
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (isDraw) {
                                Text("🤝 مسابقه مساوی شد", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🏆", fontSize = 36.sp, style = androidx.compose.ui.text.TextStyle(shadow = cardShadow))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = winnerName, 
                                        fontSize = 28.sp, 
                                        fontWeight = FontWeight.ExtraBold, 
                                        color = Color(0xFFFFD700), 
                                        style = androidx.compose.ui.text.TextStyle(shadow = androidx.compose.ui.graphics.Shadow(color = Color(0xFFFFD700).copy(alpha = 0.5f), blurRadius = 12f))
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("اختلاف امتیاز", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                                    Text(fa((scoreDiff * animProgress).toInt()), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("مدت مسابقه", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                                    Text("${fa((duration * animProgress).toInt())} ثانیه", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Column(Modifier.padding(vertical = 8.dp, horizontal = 12.dp)) {
                            ComparisonRow("✅ پاسخ صحیح", fa((p1.correctCount * animProgress).toInt()), fa((p2.correctCount * animProgress).toInt()), p1.correctCount > p2.correctCount, p2.correctCount > p1.correctCount)
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            ComparisonRow("❌ پاسخ غلط", fa((p1.wrongCount * animProgress).toInt()), fa((p2.wrongCount * animProgress).toInt()), p1.wrongCount < p2.wrongCount && p1Total>0, p2.wrongCount < p1.wrongCount && p2Total>0)
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            ComparisonRow("🎯 درصد دقت", "${fa((p1Acc * animProgress).toInt())}٪", "${fa((p2Acc * animProgress).toInt())}٪", p1Acc > p2Acc, p2Acc > p1Acc)
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            ComparisonRow("🔥 بیشترین کمبو", fa((p1.maxCombo * animProgress).toInt()), fa((p2.maxCombo * animProgress).toInt()), p1.maxCombo > p2.maxCombo, p2.maxCombo > p1.maxCombo)
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            
                            val p1FastStr = if (p1Fast > 0) "${fa(String.format(java.util.Locale.US, "%.1f", p1Fast * animProgress))} ثانیه" else "-"
                            val p2FastStr = if (p2Fast > 0) "${fa(String.format(java.util.Locale.US, "%.1f", p2Fast * animProgress))} ثانیه" else "-"
                            val winFast1 = p1Fast > 0 && (p1Fast < p2Fast || p2Fast == 0f)
                            val winFast2 = p2Fast > 0 && (p2Fast < p1Fast || p1Fast == 0f)
                            ComparisonRow("⚡ سریع‌ترین", p1FastStr, p2FastStr, winFast1, winFast2)
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            
                            val p1AvgStr = if (p1Avg > 0) "${fa(String.format(java.util.Locale.US, "%.1f", p1Avg * animProgress))} ثانیه" else "-"
                            val p2AvgStr = if (p2Avg > 0) "${fa(String.format(java.util.Locale.US, "%.1f", p2Avg * animProgress))} ثانیه" else "-"
                            val winAvg1 = p1Avg > 0 && (p1Avg < p2Avg || p2Avg == 0f)
                            val winAvg2 = p2Avg > 0 && (p2Avg < p1Avg || p1Avg == 0f)
                            ComparisonRow("⏱ میانگین زمان", p1AvgStr, p2AvgStr, winAvg1, winAvg2)
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            
                            ComparisonRow("⭐ امتیاز نهایی", fa((p1Score * animProgress).toInt()), fa((p2Score * animProgress).toInt()), p1Score > p2Score, p2Score > p1Score)
                        }
                    }
                    
                    Button(
                        onClick = { 
                            viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelClick)
                            viewModel.rematchDuel() 
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 8.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                    ) {
                        Text("بازی مجدد 🔄", color = Color(0xFF1E1B4B), fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                    
                    Button(
                        onClick = { 
                            viewModel.triggerSound(com.example.ui.GameSoundEvent.PlayDuelClick)
                            viewModel.exitDuelMode() 
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Text("بازگشت به منوی اصلی 🏠", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ComparisonRow(label: String, val1: String, val2: String, win1: Boolean, win2: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = val1,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = if (win1) Color(0xFFFFD700) else Color.White,
            style = if (win1) androidx.compose.ui.text.TextStyle(shadow = androidx.compose.ui.graphics.Shadow(color = Color(0xFFFFD700).copy(alpha = 0.5f), blurRadius = 8f)) else androidx.compose.ui.text.TextStyle.Default
        )
        Text(
            text = label,
            modifier = Modifier.weight(1.2f),
            textAlign = TextAlign.Center,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Visible
        )
        Text(
            text = val2,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = if (win2) Color(0xFFFFD700) else Color.White,
            style = if (win2) androidx.compose.ui.text.TextStyle(shadow = androidx.compose.ui.graphics.Shadow(color = Color(0xFFFFD700).copy(alpha = 0.5f), blurRadius = 8f)) else androidx.compose.ui.text.TextStyle.Default
        )
    }
}


