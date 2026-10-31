package com.example.controlremotobasurero

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.controlremotobasurero.ui.theme.ControlRemotoBasureroTheme

// ============================================================================
// PALETA DE COLORES EXACTA DEL DISEÑO (OBSIDIANA + VERDE MENTA / TEAL)
// ============================================================================
private val BgObsidian = Color(0xFF080D11)
private val CardSlate = Color(0xFF121A22)
private val CardSlateBorder = Color(0xFF1F2C3A)
private val CardTealDark = Color(0xFF0C2624)
private val CardTealBorder = Color(0xFF1B4B45)
private val IconBoxTeal = Color(0xFF0E2927)
private val MintPrimary = Color(0xFF4CE0B3)
private val TrackInactive = Color(0xFF1D3B38)
private val TextWhite = Color(0xFFF1F5F9)
private val TextMuted = Color(0xFF94A3B8)
private val TextDim = Color(0xFF64748B)
private val DangerRed = Color(0xFFF43F5E)
private val WarnAmber = Color(0xFFF59E0B)

// ============================================================================
// DIMENSIONES RESPONSIVAS ADAPTATIVAS PARA CUALQUIER CELULAR
// ============================================================================
data class ResponsiveDimens(
    val isSmallPhone: Boolean,
    val isWideScreen: Boolean,
    val screenPadding: Dp,
    val cardPadding: Dp,
    val itemSpacing: Dp,
    val heroTitleSize: TextUnit,
    val heroLineHeight: TextUnit,
    val cardTitleSize: TextUnit,
    val bodySize: TextUnit,
    val badgeRingSize: Dp,
    val iconBoxSize: Dp,
    val buttonHeight: Dp
)

@Composable
fun rememberResponsiveDimens(): ResponsiveDimens {
    val config = LocalConfiguration.current
    val widthDp = config.screenWidthDp
    val isSmall = widthDp < 360
    val isWide = widthDp >= 600

    return remember(widthDp) {
        ResponsiveDimens(
            isSmallPhone = isSmall,
            isWideScreen = isWide,
            screenPadding = if (isSmall) 14.dp else if (isWide) 24.dp else 18.dp,
            cardPadding = if (isSmall) 14.dp else 18.dp,
            itemSpacing = if (isSmall) 10.dp else 14.dp,
            heroTitleSize = if (isSmall) 24.sp else 29.sp,
            heroLineHeight = if (isSmall) 30.sp else 35.sp,
            cardTitleSize = if (isSmall) 15.sp else 16.sp,
            bodySize = if (isSmall) 12.sp else 13.sp,
            badgeRingSize = if (isSmall) 58.dp else 72.dp,
            iconBoxSize = if (isSmall) 38.dp else 44.dp,
            buttonHeight = if (isSmall) 48.dp else 54.dp
        )
    }
}

// ============================================================================
// MODELOS DE DATOS
// ============================================================================
data class LessonItem(
    val index: Int,
    val badge: String,
    val duration: String,
    val title: String,
    val subtitle: String,
    val iconType: String
)

data class ThreatIndicator(
    val id: String,
    val title: String,
    val severity: String,
    val evidence: String,
    val explanation: String,
    val mitreCode: String
)

data class QrWalletAnalysisResult(
    val rawPayload: String,
    val detectedType: String,
    val networkOrBank: String,
    val destinationHost: String,
    val destinationPath: String,
    val amountDetected: String,
    val actionOrFunction: String,
    val verdict: String,
    val riskScore: Int,
    val recommendation: String,
    val poisoningComparison: Pair<String, String>?,
    val indicators: List<ThreatIndicator>
)

data class WifiAuditResult(
    val modeName: String,
    val ssid: String,
    val bssid: String,
    val encryption: String,
    val gateway: String,
    val dnsServers: String,
    val riskScore: Int,
    val statusTitle: String,
    val threats: List<ThreatIndicator>
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ControlRemotoBasureroTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BgObsidian
                ) {
                    MainSecurityApp()
                }
            }
        }
    }
}

// ============================================================================
// APLICACIÓN PRINCIPAL RESPONSIVA (SE ADAPTA A CUALQUIER PANTALLA DE CELULAR)
// ============================================================================
@Composable
fun MainSecurityApp() {
    val dimens = rememberResponsiveDimens()

    var currentTab by remember { mutableIntStateOf(0) }
    var activeLessonIndex by remember { mutableStateOf<Int?>(null) }
    var completedLessons by remember { mutableIntStateOf(1) }

    var qrInput by remember {
        mutableStateOf("https://menu-cafe.example/carta")
    }
    var trustedWallet by remember {
        mutableStateOf("0x71C845aB912cF40192837465aBcDeF12345689A2")
    }
    var qrAnalysis by remember {
        mutableStateOf(analyzeQrAndWallet(qrInput, trustedWallet))
    }

    var walletInput by remember {
        mutableStateOf("0x71C80000009999888877776666555500000089A2")
    }
    var walletAnalysis by remember {
        mutableStateOf(analyzeQrAndWallet(walletInput, trustedWallet))
    }

    val context = LocalContext.current
    var wifiState by remember { mutableStateOf(scanAndroidNetworkState(context)) }
    var linkInput by remember {
        mutableStateOf("URGENTE: Su cuenta Uninet ha sido bloqueada. Verifique su token en http://bancounion-seguridad-bo.xyz/login")
    }
    var linkAnalysis by remember {
        mutableStateOf(analyzeQrAndWallet(linkInput, trustedWallet))
    }

    val topTitle = when (currentTab) {
        0 -> if (activeLessonIndex == null) "Aprende a protegerte" else "Aprendizaje"
        1 -> "Verificador de QR"
        2 -> "Tu Wallet Segura"
        else -> "Protección Wi-Fi y Red"
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(), // Se adapta automáticamente cuando se abre el teclado del celular
        containerColor = BgObsidian,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgObsidian)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = dimens.screenPadding, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                if (currentTab == 0 && activeLessonIndex != null) {
                                    activeLessonIndex = null
                                } else {
                                    currentTab = 0
                                    activeLessonIndex = null
                                }
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "←",
                            color = TextMuted,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = topTitle,
                            color = TextWhite,
                            fontSize = if (dimens.isSmallPhone) 15.sp else 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF16202A))
                            .border(1.dp, Color(0xFF263545), RoundedCornerShape(50))
                            .padding(horizontal = 11.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "DEMO",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A1016))
                    .navigationBarsPadding() // Respeta la barra de gestos o botones de cualquier celular
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgObsidian)
                        .padding(horizontal = dimens.screenPadding, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "♟  Demostración educativa · no es actividad real",
                        color = TextDim,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                HorizontalDivider(color = Color(0xFF151F28), thickness = 1.dp)

                NavigationBar(
                    containerColor = Color(0xFF0A1016),
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(if (dimens.isSmallPhone) 60.dp else 66.dp)
                ) {
                    val navItems = listOf(
                        "Inicio" to "home",
                        "QR" to "qr",
                        "Wallet" to "wallet",
                        "Protección" to "shield"
                    )
                    navItems.forEachIndexed { idx, (label, iconType) ->
                        val selected = currentTab == idx
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                currentTab = idx
                                if (idx == 0) activeLessonIndex = null
                            },
                            icon = {
                                MinimalNavIcon(
                                    type = iconType,
                                    color = if (selected) MintPrimary else TextDim
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = if (dimens.isSmallPhone) 10.sp else 11.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selected) MintPrimary else TextDim,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // Contenedor centrado que se adapta desde celulares pequeños (320dp) hasta plegables/tablets
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
            ) {
                when (currentTab) {
                    0 -> {
                        if (activeLessonIndex == null) {
                            HomeLearningRouteScreen(
                                dimens = dimens,
                                completedLessons = completedLessons,
                                onOpenLesson = { lessonIdx -> activeLessonIndex = lessonIdx },
                                onContinueWallets = { activeLessonIndex = 2 }
                            )
                        } else {
                            LessonDetailScreen(
                                dimens = dimens,
                                lessonIndex = activeLessonIndex ?: 1,
                                onNextLesson = {
                                    val current = activeLessonIndex ?: 1
                                    if (current < 4) {
                                        completedLessons = maxOf(completedLessons, current + 1)
                                        activeLessonIndex = current + 1
                                    } else {
                                        completedLessons = 4
                                        currentTab = 1
                                    }
                                },
                                onBackToRoute = { activeLessonIndex = null }
                            )
                        }
                    }

                    1 -> QrInteractiveTab(
                        dimens = dimens,
                        qrInput = qrInput,
                        onQrChange = {
                            qrInput = it
                            qrAnalysis = analyzeQrAndWallet(it, trustedWallet)
                        },
                        result = qrAnalysis
                    )

                    2 -> WalletInteractiveTab(
                        dimens = dimens,
                        walletInput = walletInput,
                        onWalletChange = {
                            walletInput = it
                            walletAnalysis = analyzeQrAndWallet(it, trustedWallet)
                        },
                        trustedWallet = trustedWallet,
                        onTrustedChange = {
                            trustedWallet = it
                            walletAnalysis = analyzeQrAndWallet(walletInput, it)
                        },
                        result = walletAnalysis
                    )

                    3 -> ProtectionWifiAndLinksTab(
                        dimens = dimens,
                        wifiResult = wifiState,
                        onScanRealWifi = { wifiState = scanAndroidNetworkState(context) },
                        onSimulateWifi = { scen -> wifiState = getSimulatedWifiScenario(scen) },
                        linkInput = linkInput,
                        onLinkChange = {
                            linkInput = it
                            linkAnalysis = analyzeQrAndWallet(it, trustedWallet)
                        },
                        linkResult = linkAnalysis
                    )
                }
            }
        }
    }
}

// ============================================================================
// PANTALLA 1: "Más confianza. Un paso a la vez." (100% Responsiva)
// ============================================================================
@Composable
fun HomeLearningRouteScreen(
    dimens: ResponsiveDimens,
    completedLessons: Int,
    onOpenLesson: (Int) -> Unit,
    onContinueWallets: () -> Unit
) {
    val lessons = listOf(
        LessonItem(1, "VISTA · EJEMPLO", "2 min", "¿Qué es un QR?", "Mira el destino antes de abrir.", "qr"),
        LessonItem(2, "SIGUIENTE", "3 min", "Tu wallet, sin misterios", "Qué compartir y qué guardar.", "wallet"),
        LessonItem(3, "EJERCICIO", "3 min", "¿Detectas la estafa?", "Practica con un premio ficticio.", "chat"),
        LessonItem(4, "GUÍA PRÁCTICA", "4 min", "¿Sospechas una estafa?", "Pasos para actuar con calma.", "shield")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = dimens.screenPadding, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing)
    ) {
        item {
            Text(
                text = "CAPACITACIÓN · DESDE CERO",
                color = MintPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Más confianza.\nUn paso a la vez.",
                color = TextWhite,
                fontSize = dimens.heroTitleSize,
                lineHeight = dimens.heroLineHeight,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "No necesitas saber de tecnología. Empieza con lo básico, a tu ritmo.",
                color = TextMuted,
                fontSize = dimens.bodySize,
                lineHeight = 19.sp
            )
        }

        // Tarjeta Principal "Una pausa que ayuda"
        item {
            val pct = (completedLessons * 25).coerceAtMost(100)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF102D2A), Color(0xFF0B201E))
                        )
                    )
                    .border(1.dp, CardTealBorder, RoundedCornerShape(20.dp))
                    .clickable { onOpenLesson(1) }
                    .padding(dimens.cardPadding)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Una pausa que ayuda",
                            color = TextWhite,
                            fontSize = if (dimens.isSmallPhone) 16.sp else 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = "4 lecciones cortas para decidir con más claridad.",
                            color = TextMuted,
                            fontSize = dimens.bodySize,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "12 min en total · nivel inicial",
                            color = MintPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    CircularBookBadge(ringSize = dimens.badgeRingSize)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Progreso de ejemplo", color = TextMuted, fontSize = 12.sp)
                    Text(
                        text = "$completedLessons de 4 · $pct %",
                        color = MintPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (i in 1..4) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (i <= completedLessons) MintPrimary else TrackInactive)
                        )
                    }
                }
            }
        }

        // Encabezado "Tu ruta, desde lo esencial"
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tu ruta, desde lo esencial",
                    color = TextWhite,
                    fontSize = dimens.cardTitleSize,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "4 temas", color = TextDim, fontSize = 12.sp)
            }
        }

        // Tarjetas de los 4 temas
        items(lessons) { item ->
            val isHighlighted = item.index == 2
            val cardBg = if (isHighlighted) CardTealDark else CardSlate
            val cardBorder = if (isHighlighted) CardTealBorder else CardSlateBorder

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                    .clickable { onOpenLesson(item.index) }
                    .padding(dimens.cardPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(dimens.iconBoxSize)
                        .clip(RoundedCornerShape(12.dp))
                        .background(IconBoxTeal)
                        .border(1.dp, CardTealBorder, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    MinimalNavIcon(type = item.iconType, color = MintPrimary)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item.badge,
                            color = if (isHighlighted) MintPrimary else TextDim,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Text(text = item.duration, color = TextDim, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.title,
                        color = TextWhite,
                        fontSize = dimens.cardTitleSize,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.subtitle,
                        color = TextMuted,
                        fontSize = dimens.bodySize
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "›", color = TextMuted, fontSize = 20.sp)
            }
        }

        item {
            Text(
                text = "Aprender ayuda a reconocer riesgos; no garantiza seguridad ni sustituye la verificación.",
                color = TextDim,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Siguiente · Tu wallet, sin misterios",
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onContinueWallets,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimens.buttonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MintPrimary,
                    contentColor = Color(0xFF051814)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Continuar con wallets   →",
                    fontSize = if (dimens.isSmallPhone) 14.sp else 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Volver al inicio",
                color = MintPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenLesson(1) }
                    .padding(vertical = 4.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ============================================================================
// PANTALLA 2: VISTA DE LECCIÓN ("¿Qué es un QR?" y lecciones 1 a 4)
// ============================================================================
@Composable
fun LessonDetailScreen(
    dimens: ResponsiveDimens,
    lessonIndex: Int,
    onNextLesson: () -> Unit,
    onBackToRoute: () -> Unit
) {
    var selectedQuizOption by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = dimens.screenPadding, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing)
    ) {
        when (lessonIndex) {
            1 -> {
                item {
                    Text(
                        text = "LECCIÓN 1 DE 4 · 2 MIN",
                        color = MintPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "¿Qué es un QR?",
                        color = TextWhite,
                        fontSize = dimens.heroTitleSize,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Es un código que guarda información, como un texto o un enlace a una web.",
                        color = TextMuted,
                        fontSize = dimens.bodySize,
                        lineHeight = 19.sp
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(CardTealDark)
                            .border(1.dp, CardTealBorder, RoundedCornerShape(18.dp))
                            .padding(dimens.cardPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                            MinimalNavIcon(type = "qr", color = MintPrimary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Escanear no es confiar",
                                color = TextWhite,
                                fontSize = dimens.cardTitleSize,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Leer un QR no demuestra que su contenido sea seguro.",
                                color = TextMuted,
                                fontSize = dimens.bodySize,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(CardSlate)
                            .border(1.dp, CardSlateBorder, RoundedCornerShape(18.dp))
                            .padding(dimens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "EJEMPLO FICTICIO · NO ESCANEABLE",
                            color = TextDim,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(if (dimens.isSmallPhone) 54.dp else 62.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF1A2530))
                                    .border(1.dp, Color(0xFF2B3B4B), RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "回 ▪\n▪ 回",
                                    color = TextMuted,
                                    fontSize = 15.sp,
                                    lineHeight = 17.sp,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = TextAlign.Center
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "El QR podría llevarte a", color = TextDim, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "menu-cafe.example",
                                    color = TextWhite,
                                    fontSize = if (dimens.isSmallPhone) 14.sp else 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "/carta",
                                    color = TextDim,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        Text(
                            text = "El dominio es el nombre de la web: aquí, menu-cafe.example. Que parezca conocido no basta.",
                            color = TextMuted,
                            fontSize = dimens.bodySize,
                            lineHeight = 18.sp
                        )
                    }
                }

                item {
                    Text(
                        text = "Antes de abrir, haz una pausa",
                        color = TextWhite,
                        fontSize = dimens.cardTitleSize,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        NumberedStepItem(
                            number = "1",
                            title = "Revisa el dominio",
                            body = "Mira la vista previa antes de abrir. Busca letras cambiadas o un nombre que no coincida."
                        )
                        NumberedStepItem(
                            number = "2",
                            title = "Mira también el QR físico",
                            body = "¿Tiene una pegatina encima? Podrían haber sustituido el código original."
                        )
                        NumberedStepItem(
                            number = "3",
                            title = "Confirma por otra vía",
                            body = "Pregunta al local o entra por su app o web oficial, sin usar el enlace del QR."
                        )
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(CardSlate)
                            .border(1.dp, CardSlateBorder, RoundedCornerShape(18.dp))
                            .padding(dimens.cardPadding)
                    ) {
                        Text(
                            text = "Si no te cuadra, no lo abras",
                            color = TextWhite,
                            fontSize = dimens.cardTitleSize,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = "No pasa nada por detenerte y pedir ayuda. La prisa no tiene que decidir por ti.",
                            color = TextMuted,
                            fontSize = dimens.bodySize,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            2 -> {
                item {
                    Text(
                        text = "LECCIÓN 2 DE 4 · 3 MIN",
                        color = MintPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tu wallet, sin misterios",
                        color = TextWhite,
                        fontSize = dimens.heroTitleSize,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Distingue entre tu dirección pública para recibir y las claves que jamás debes entregar.",
                        color = TextMuted,
                        fontSize = dimens.bodySize,
                        lineHeight = 19.sp
                    )
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(CardTealDark)
                            .border(1.dp, CardTealBorder, RoundedCornerShape(18.dp))
                            .padding(dimens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Lo que SÍ puedes compartir: Dirección Pública",
                            color = MintPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "0x71C8...89A2",
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Es como tu número de cuenta. Sirve para recibir fondos, pero revisa siempre los caracteres del centro para evitar el 'Address Poisoning'.",
                            color = TextMuted,
                            fontSize = dimens.bodySize,
                            lineHeight = 18.sp
                        )
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(CardSlate)
                            .border(1.dp, DangerRed.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                            .padding(dimens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Lo que NUNCA debes compartir ni firmar",
                            color = DangerRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        NumberedStepItem(
                            number = "!",
                            title = "Frase Semilla (12 o 24 palabras)",
                            body = "Ningún soporte técnico ni sorteo legítimo te pedirá tus 12 palabras secretas."
                        )
                        NumberedStepItem(
                            number = "!",
                            title = "Permisos 'Approve Ilimitado' en un QR",
                            body = "Para recibir dinero NO necesitas aprobar contratos ni pagar comisiones por adelantado."
                        )
                    }
                }
            }

            3 -> {
                item {
                    Text(
                        text = "LECCIÓN 3 DE 4 · 3 MIN · EJERCICIO",
                        color = MintPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "¿Detectas la estafa?",
                        color = TextWhite,
                        fontSize = dimens.heroTitleSize,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Analiza este mensaje ficticio que promete un bono urgente:",
                        color = TextMuted,
                        fontSize = dimens.bodySize
                    )
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(CardSlate)
                            .border(1.dp, WarnAmber.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                            .padding(dimens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "MENSAJE FICTICIO DE PRÁCTICA",
                            color = WarnAmber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "\"¡Últimas 2 horas! Reclama tu bono universitario de \$500 escaneando este QR y aprobando tu billetera en: http://bono-utepsa-verificar.xyz/claim\"",
                            color = TextWhite,
                            fontSize = dimens.bodySize,
                            lineHeight = 19.sp
                        )
                    }
                }

                item {
                    val options = listOf(
                        "1. Mete prisa ('Últimas 2 horas'), usa un dominio extraño (.xyz) y pide aprobar un permiso para recibir dinero.",
                        "2. Es seguro porque incluye el nombre de la universidad en el enlace."
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        options.forEachIndexed { idx, text ->
                            val isSelected = selectedQuizOption == idx
                            val borderColor = if (isSelected) {
                                if (idx == 0) MintPrimary else DangerRed
                            } else CardSlateBorder

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) CardTealDark else CardSlate)
                                    .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
                                    .clickable { selectedQuizOption = idx }
                                    .padding(14.dp)
                            ) {
                                Text(text = text, color = TextWhite, fontSize = dimens.bodySize, lineHeight = 18.sp)
                            }
                        }
                    }

                    if (selectedQuizOption != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (selectedQuizOption == 0) {
                                "✓ ¡Exacto! Urgencia artificial + dominio no oficial (.xyz) + pedir firma para 'recibir' dinero es una estafa."
                            } else {
                                "⚠️ Cuidado: Cualquiera puede escribir un nombre conocido en un enlace falso. Mira siempre el dominio principal."
                            },
                            color = if (selectedQuizOption == 0) MintPrimary else WarnAmber,
                            fontSize = dimens.bodySize,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            else -> {
                item {
                    Text(
                        text = "LECCIÓN 4 DE 4 · 4 MIN · GUÍA PRÁCTICA",
                        color = MintPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "¿Sospechas una estafa?",
                        color = TextWhite,
                        fontSize = dimens.heroTitleSize,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Pasos concretos para actuar con calma si abriste un enlace dudoso o te conectaste a un Wi-Fi público.",
                        color = TextMuted,
                        fontSize = dimens.bodySize
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        NumberedStepItem(
                            number = "1",
                            title = "Desconecta el Wi-Fi o cierra la pestaña",
                            body = "Si estás en una red pública sospechosa, apaga el Wi-Fi para cortar cualquier intercepción de tráfico (MITM)."
                        )
                        NumberedStepItem(
                            number = "2",
                            title = "Usa las pestañas QR, Wallet y Protección",
                            body = "Verifica el contenido del QR, la dirección Wallet o el estado de tu red antes de autorizar cualquier movimiento."
                        )
                        NumberedStepItem(
                            number = "3",
                            title = "Revoca permisos y cambia tu clave",
                            body = "Si vinculaste un QR por error, cierra las sesiones activas en tu aplicación oficial desde una red segura."
                        )
                    }
                }
            }
        }

        item {
            val nextLabel = when (lessonIndex) {
                1 -> "Siguiente · Tu wallet, sin misterios"
                2 -> "Siguiente · ¿Detectas la estafa?"
                3 -> "Siguiente · ¿Sospechas una estafa?"
                else -> "Ir al Verificador Interactivo de QR"
            }
            Text(
                text = nextLabel,
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onNextLesson,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimens.buttonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MintPrimary,
                    contentColor = Color(0xFF051814)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Continuar   →",
                    fontSize = if (dimens.isSmallPhone) 14.sp else 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Volver al aprendizaje",
                color = MintPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBackToRoute() }
                    .padding(vertical = 4.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ============================================================================
// PESTAÑA 2 (QR): VERIFICADOR DE QR RESPONSIVO
// ============================================================================
@Composable
fun QrInteractiveTab(
    dimens: ResponsiveDimens,
    qrInput: String,
    onQrChange: (String) -> Unit,
    result: QrWalletAnalysisResult
) {
    val qrSamples = listOf(
        "Ejemplo Café (Limpio)" to "https://menu-cafe.example/carta",
        "🚨 QR Wallet Drainer" to "ethereum:0x71C999deadbeef000000000000000000000089A2@1/approve?spender=0xBadDrainer666&uint256=UNLIMITED",
        "🏦 Cambiazo QR (USD)" to "EMVCO://QR-SIMPLE-BO?dest=CUENTA-FANTASMA-9981&moneda=USD&monto=1500.00&glosa=premio_sorteo",
        "📱 Robo WhatsApp" to "whatsapp://link-device?token=robo_sesion_99812&browser=Remote"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = dimens.screenPadding, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing)
    ) {
        item { NativeQrControls(onQrChange) }
        item { NativeAlertControls(qrInput, "qr") }
        item {
            Text(
                text = "INSPECTOR PREVENTIVO DE QR",
                color = MintPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Mira el destino antes de abrir",
                color = TextWhite,
                fontSize = if (dimens.isSmallPhone) 22.sp else 25.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                qrSamples.forEach { (label, payload) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardSlate)
                            .border(1.dp, CardSlateBorder, RoundedCornerShape(12.dp))
                            .clickable { onQrChange(payload) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(text = label, color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        item {
            val isDanger = result.riskScore >= 60
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(CardSlate)
                    .border(
                        1.dp,
                        if (isDanger) DangerRed.copy(alpha = 0.6f) else CardSlateBorder,
                        RoundedCornerShape(18.dp)
                    )
                    .padding(dimens.cardPadding),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VISTA PREVIA DEL CONTENIDO QR",
                        color = TextDim,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Riesgo: ${result.riskScore}/100",
                        color = if (isDanger) DangerRed else MintPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(if (dimens.isSmallPhone) 50.dp else 60.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isDanger) DangerRed.copy(alpha = 0.15f) else IconBoxTeal)
                            .border(1.dp, if (isDanger) DangerRed else CardTealBorder, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        MinimalNavIcon(type = "qr", color = if (isDanger) DangerRed else MintPrimary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "El QR intenta llevarte a:", color = TextDim, fontSize = 11.sp)
                        Text(
                            text = result.destinationHost,
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${result.destinationPath} · (${result.actionOrFunction})",
                            color = if (isDanger) DangerRed else MintPrimary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                OutlinedTextField(
                    value = qrInput,
                    onValueChange = onQrChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Contenido del QR", color = TextDim, fontSize = 11.sp) },
                    textStyle = LocalTextStyle.current.copy(
                        color = TextWhite,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    maxLines = 4
                )

                Text(
                    text = result.recommendation,
                    color = if (isDanger) DangerRed else TextMuted,
                    fontSize = dimens.bodySize,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        items(result.indicators) { ind ->
            IndicatorCardStyled(ind)
        }
    }
}

// ============================================================================
// PESTAÑA 3 (WALLET): VERIFICADOR DE DIRECCIONES RESPONSIVO
// ============================================================================
@Composable
fun WalletInteractiveTab(
    dimens: ResponsiveDimens,
    walletInput: String,
    onWalletChange: (String) -> Unit,
    trustedWallet: String,
    onTrustedChange: (String) -> Unit,
    result: QrWalletAnalysisResult
) {
    val walletPresets = listOf(
        "🎭 Address Poisoning" to "0x71C80000009999888877776666555500000089A2",
        "💰 USDT Estafa" to "TQn9Y2khEsLJW1ChVWFMSMeRDow5KcbLSE",
        "✅ Wallet Segura" to "0x71C845aB912cF40192837465aBcDeF12345689A2"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = dimens.screenPadding, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing)
    ) {
        item {
            Text(
                text = "PROTECCIÓN DE BILLETERA · WEB3 Y PAGOS",
                color = MintPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tu wallet, sin misterios",
                color = TextWhite,
                fontSize = if (dimens.isSmallPhone) 22.sp else 25.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                walletPresets.forEach { (label, addr) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardSlate)
                            .border(1.dp, CardSlateBorder, RoundedCornerShape(12.dp))
                            .clickable { onWalletChange(addr) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(text = label, color = TextWhite, fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(CardSlate)
                    .border(1.dp, CardSlateBorder, RoundedCornerShape(18.dp))
                    .padding(dimens.cardPadding),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "1. Dirección a verificar:",
                    color = TextWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = walletInput,
                    onValueChange = onWalletChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(
                        color = TextWhite,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    maxLines = 2
                )

                Text(
                    text = "2. Tu dirección guardada (Anti-Address Poisoning):",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = trustedWallet,
                    onValueChange = onTrustedChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(
                        color = MintPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    maxLines = 2
                )
            }
        }

        if (result.poisoningComparison != null) {
            item {
                val (trusted, scanned) = result.poisoningComparison
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(DangerRed.copy(alpha = 0.12f))
                        .border(1.dp, DangerRed, RoundedCornerShape(18.dp))
                        .padding(dimens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🚨 ENVENENAMIENTO DE DIRECCIÓN (ADDRESS POISONING)",
                        color = DangerRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Coinciden las primeras y últimas letras, pero el centro fue alterado:",
                        color = TextWhite,
                        fontSize = 12.sp
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .clip(RoundedCornerShape(10.dp))
                            .background(BgObsidian)
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(SpanStyle(color = TextMuted)) { append("Confiable: ") }
                                    withStyle(SpanStyle(color = MintPrimary, fontWeight = FontWeight.Bold)) { append(trusted.take(6)) }
                                    withStyle(SpanStyle(color = TextWhite)) { append(trusted.substring(6, trusted.length - 4)) }
                                    withStyle(SpanStyle(color = MintPrimary, fontWeight = FontWeight.Bold)) { append(trusted.takeLast(4)) }
                                },
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(SpanStyle(color = TextMuted)) { append("Copiada:   ") }
                                    withStyle(SpanStyle(color = MintPrimary, fontWeight = FontWeight.Bold)) { append(scanned.take(6)) }
                                    withStyle(SpanStyle(color = DangerRed, fontWeight = FontWeight.ExtraBold)) { append(scanned.substring(6, scanned.length - 4)) }
                                    withStyle(SpanStyle(color = MintPrimary, fontWeight = FontWeight.Bold)) { append(scanned.takeLast(4)) }
                                },
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        items(result.indicators) { ind ->
            IndicatorCardStyled(ind)
        }
    }
}

// ============================================================================
// PESTAÑA 4 (PROTECCIÓN): ESCUDO WI-FI Y ENLACES RESPONSIVO
// ============================================================================
@Composable
fun ProtectionWifiAndLinksTab(
    dimens: ResponsiveDimens,
    wifiResult: WifiAuditResult,
    onScanRealWifi: () -> Unit,
    onSimulateWifi: (String) -> Unit,
    linkInput: String,
    onLinkChange: (String) -> Unit,
    linkResult: QrWalletAnalysisResult
) {
    LaunchedEffect(Unit) {
        while (true) {
            onScanRealWifi()
            kotlinx.coroutines.delay(5000)
        }
    }
    var showSignals by remember { mutableStateOf(false) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = dimens.screenPadding, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Tu conexión", color = TextWhite, fontSize = 25.sp, fontWeight = FontWeight.Bold) }
        item { NativeNetworkCard(wifiResult, onScanRealWifi) }
        item {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(CardSlate).padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Revisar enlace", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = linkInput, onValueChange = onLinkChange, modifier = Modifier.fillMaxWidth(), maxLines = 3, label = { Text("Pega un mensaje o enlace") })
                Text("${linkResult.riskScore}/100 · " + if (linkResult.riskScore >= 60) "Detente y verifica" else if (linkResult.riskScore >= 25) "Requiere revisión" else "Sin señales fuertes", color = if(linkResult.riskScore >= 60) DangerRed else MintPrimary)
                if(linkResult.indicators.isNotEmpty()) Text(linkResult.indicators.first().title, color = TextMuted, fontSize = 12.sp)
                TextButton(onClick = { showSignals = !showSignals }) { Text(if(showSignals) "Ocultar señales" else "Ver señales") }
            }
        }
        if(showSignals) items(linkResult.indicators) { IndicatorCardStyled(it) }
        item { NativeAlertControls(linkInput, "enlace") }
    }
}

// ============================================================================
// COMPONENTES VISUALES DEL ESTILO DE LA IMAGEN
// ============================================================================
@Composable
fun NumberedStepItem(number: String, title: String, body: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(IconBoxTeal)
                .border(1.dp, CardTealBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = MintPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = body,
                color = TextMuted,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun IndicatorCardStyled(ind: ThreatIndicator) {
    val accent = if (ind.severity == "CRITICO") DangerRed else WarnAmber
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardSlate)
            .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = ind.title,
                color = TextWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = ind.severity,
                color = accent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = ind.evidence,
            color = MintPrimary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = ind.explanation,
            color = TextMuted,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
    }
}

@Composable
fun CircularBookBadge(ringSize: Dp = 72.dp) {
    Box(
        modifier = Modifier.size(ringSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 2.dp.toPx()
            drawCircle(
                color = Color(0xFF1F4E48),
                style = Stroke(width = strokeWidth)
            )
            drawCircle(
                color = Color(0xFF163834),
                radius = size.minDimension / 2.5f,
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawCircle(
                color = Color(0xFF4CE0B3),
                radius = 4.dp.toPx(),
                center = Offset(size.width * 0.84f, size.height * 0.18f)
            )
        }
        Text(text = "📖", fontSize = 18.sp, color = MintPrimary)
    }
}

@Composable
fun MinimalNavIcon(type: String, color: Color) {
    Canvas(modifier = Modifier.size(21.dp)) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        val w = size.width
        val h = size.height
        when (type) {
            "home" -> {
                val path = Path().apply {
                    moveTo(w * 0.15f, h * 0.48f)
                    lineTo(w * 0.5f, h * 0.15f)
                    lineTo(w * 0.85f, h * 0.48f)
                    lineTo(w * 0.85f, h * 0.85f)
                    lineTo(w * 0.15f, h * 0.85f)
                    close()
                }
                drawPath(path, color = color, style = stroke)
            }

            "qr" -> {
                val len = w * 0.25f
                drawLine(color, Offset(w * 0.15f, h * 0.15f), Offset(w * 0.15f + len, h * 0.15f), stroke.width)
                drawLine(color, Offset(w * 0.15f, h * 0.15f), Offset(w * 0.15f, h * 0.15f + len), stroke.width)
                drawLine(color, Offset(w * 0.85f, h * 0.15f), Offset(w * 0.85f - len, h * 0.15f), stroke.width)
                drawLine(color, Offset(w * 0.85f, h * 0.15f), Offset(w * 0.85f, h * 0.15f + len), stroke.width)
                drawLine(color, Offset(w * 0.15f, h * 0.85f), Offset(w * 0.15f + len, h * 0.85f), stroke.width)
                drawLine(color, Offset(w * 0.15f, h * 0.85f), Offset(w * 0.15f, h * 0.85f - len), stroke.width)
                drawLine(color, Offset(w * 0.85f, h * 0.85f), Offset(w * 0.85f - len, h * 0.85f), stroke.width)
                drawLine(color, Offset(w * 0.85f, h * 0.85f), Offset(w * 0.85f, h * 0.85f - len), stroke.width)
                drawLine(color, Offset(w * 0.28f, h * 0.5f), Offset(w * 0.72f, h * 0.5f), stroke.width)
            }

            "wallet" -> {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(w * 0.12f, h * 0.22f),
                    size = Size(w * 0.76f, h * 0.58f),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                    style = stroke
                )
                drawCircle(
                    color = color,
                    radius = 2.dp.toPx(),
                    center = Offset(w * 0.70f, h * 0.51f)
                )
            }

            "chat" -> {
                drawCircle(
                    color = color,
                    radius = w * 0.36f,
                    center = Offset(w * 0.5f, h * 0.48f),
                    style = stroke
                )
            }

            else -> {
                val shield = Path().apply {
                    moveTo(w * 0.5f, h * 0.12f)
                    lineTo(w * 0.84f, h * 0.26f)
                    lineTo(w * 0.84f, h * 0.52f)
                    cubicTo(w * 0.84f, h * 0.74f, w * 0.5f, h * 0.88f, w * 0.5f, h * 0.88f)
                    cubicTo(w * 0.5f, h * 0.88f, w * 0.16f, h * 0.74f, w * 0.16f, h * 0.52f)
                    lineTo(w * 0.16f, h * 0.26f)
                    close()
                }
                drawPath(shield, color = color, style = stroke)
                drawLine(color, Offset(w * 0.36f, h * 0.52f), Offset(w * 0.46f, h * 0.62f), stroke.width)
                drawLine(color, Offset(w * 0.46f, h * 0.62f), Offset(w * 0.66f, h * 0.40f), stroke.width)
            }
        }
    }
}

// ============================================================================
// MOTOR DE AUDITORÍA QR, WALLET, ENLACES Y WI-FI
// ============================================================================
fun analyzeQrAndWallet(rawInput: String, trustedWalletInput: String): QrWalletAnalysisResult {
    val payload = rawInput.trim()
    val trusted = trustedWalletInput.trim()
    val indicators = mutableListOf<ThreatIndicator>()
    var risk = 0

    var detectedType = "Enlace / Código QR"
    var networkOrBank = "Web / Estándar"
    var destHost = payload
    var destPath = "/"
    var amount = "No aplica"
    var action = "Abrir destino"
    var poisoningComp: Pair<String, String>? = null

    val evmMatch = Regex("0x[a-fA-F0-9]{40}").find(payload)?.value
    val tronMatch = Regex("\\bT[1-9A-HJ-NP-Za-km-z]{33}\\b").find(payload)?.value

    if (evmMatch != null) {
        destHost = evmMatch
        destPath = if (payload.contains("approve", ignoreCase = true)) "/approve" else "/transfer"
        networkOrBank = "Ethereum / BNB / Polygon (EVM)"
        detectedType = "Transacción / Wallet Cripto"
    } else if (tronMatch != null) {
        destHost = tronMatch
        destPath = "/trc20-usdt"
        networkOrBank = "Red TRON (USDT)"
        detectedType = "Wallet USDT TRC-20"
    } else if (payload.startsWith("EMVCO://", ignoreCase = true)) {
        detectedType = "QR de Cobro Bancario"
        networkOrBank = "QR Simple Interbancario"
        destHost = Regex("dest=([^&]+)").find(payload)?.groupValues?.get(1) ?: "Cuenta Externa"
        destPath = "/cobro-qr"
        val cur = Regex("moneda=([^&]+)").find(payload)?.groupValues?.get(1) ?: "BOB"
        val amt = Regex("monto=([^&]+)").find(payload)?.groupValues?.get(1) ?: "0.00"
        amount = "$amt $cur"
        action = "Autorizar Pago"
    } else {
        val clean = payload.substringAfter("://")
        destHost = clean.substringBefore("/").substringAfterLast("@")
        destPath = "/" + clean.substringAfter("/", "")
    }

    if (payload.contains("UNLIMITED", ignoreCase = true) || payload.contains("ffffffffffffffff", ignoreCase = true) || payload.contains("setApprovalForAll", ignoreCase = true)) {
        risk += 95
        action = "APPROVE ILIMITADO (DRENAJE)"
        indicators.add(
            ThreatIndicator(
                id = "QR-DRAIN-01",
                title = "Permiso Ilimitado de Retiro ('Wallet Drainer')",
                severity = "CRITICO",
                evidence = "approve(spender, UNLIMITED)",
                explanation = "Este QR no te envía dinero: le da permiso al atacante para vaciar toda tu wallet sin volver a pedirte firma.",
                mitreCode = "T1204 (Malicious Contract Approval)"
            )
        )
    }

    val candidate = evmMatch ?: tronMatch
    if (candidate != null && trusted.length >= 16 && candidate.length == trusted.length) {
        val sameStart = candidate.take(5).equals(trusted.take(5), ignoreCase = true)
        val sameEnd = candidate.takeLast(4).equals(trusted.takeLast(4), ignoreCase = true)
        if (sameStart && sameEnd && !candidate.equals(trusted, ignoreCase = true)) {
            risk += 90
            poisoningComp = trusted to candidate
            indicators.add(
                ThreatIndicator(
                    id = "WAL-POISON-02",
                    title = "Suplantación de Extremos (Address Poisoning)",
                    severity = "CRITICO",
                    evidence = "${candidate.take(6)}...${candidate.takeLast(4)}",
                    explanation = "Los primeros y últimos caracteres coinciden con tu wallet habitual, pero el centro pertenece a un estafador.",
                    mitreCode = "T1036.005 (Address Masquerading)"
                )
            )
        }
    }

    if (candidate != null && (candidate.startsWith("TQn9Y2", ignoreCase = true) || candidate.contains("deadbeef", ignoreCase = true))) {
        risk += 85
        indicators.add(
            ThreatIndicator(
                id = "WAL-BLACK-03",
                title = "Dirección Reportada por Estafa / Phishing",
                severity = "CRITICO",
                evidence = candidate,
                explanation = "Esta dirección aparece en reportes de fraude cripto y drenaje de fondos.",
                mitreCode = "T1657 (Financial Theft)"
            )
        )
    }

    if (payload.contains("moneda=USD", ignoreCase = true) || payload.contains("FANTASMA", ignoreCase = true)) {
        risk += 85
        indicators.add(
            ThreatIndicator(
                id = "QR-BANK-04",
                title = "Cobro Encubierto en Dólares / Cuenta No Verificada",
                severity = "CRITICO",
                evidence = "$destHost ($amount)",
                explanation = "Recuerda: para recibir un premio o pago nunca debes escanear y autorizar un QR de cobro.",
                mitreCode = "T1566 (QR Payment Fraud)"
            )
        )
    }

    if (payload.contains("link-device", ignoreCase = true) || payload.startsWith("whatsapp://", ignoreCase = true)) {
        risk += 25
        action = "Confirmar vinculación solicitada"
        indicators.add(
            ThreatIndicator(
                id = "QR-SESS-05",
                title = "Vinculación: confirma que la solicitaste",
                severity = "CRITICO",
                evidence = "whatsapp://link-device",
                explanation = "Si escaneas este QR desde tu app de mensajería, le darás acceso a tus conversaciones a un equipo externo.",
                mitreCode = "T1185 (Session Hijacking)"
            )
        )
    }

    if (destHost.endsWith(".xyz") || destHost.endsWith(".top") || destHost.contains("bancounion-") || destHost.contains("micros0ft")) {
        risk += 80
        indicators.add(
            ThreatIndicator(
                id = "URL-PHISH-06",
                title = "Dominio No Oficial que Imita una Institución",
                severity = "CRITICO",
                evidence = destHost,
                explanation = "El nombre parece conocido, pero el dominio real no corresponde a la institución oficial.",
                mitreCode = "T1566.002 (Spearphishing Link)"
            )
        )
    }

    risk = risk.coerceAtMost(100)
    val verdict = when {
        risk >= 60 -> "ALTO RIESGO · NO ABRIR NI FIRMAR"
        risk >= 25 -> "PRECAUCIÓN · REVISA EL DESTINO"
        else -> "DESTINO SIN ALERTAS DETECTADAS"
    }
    val recommendation = when {
        risk >= 60 -> "Detente: el contenido intenta redirigirte a un sitio no oficial o autorizar un retiro de fondos."
        else -> "Siempre verifica el dominio completo y confirma por otra vía si implica dinero o contraseñas."
    }

    return QrWalletAnalysisResult(
        rawPayload = payload,
        detectedType = detectedType,
        networkOrBank = networkOrBank,
        destinationHost = destHost,
        destinationPath = destPath,
        amountDetected = amount,
        actionOrFunction = action,
        verdict = verdict,
        riskScore = risk,
        recommendation = recommendation,
        poisoningComparison = poisoningComp,
        indicators = indicators
    )
}

fun scanAndroidNetworkState(context: Context): WifiAuditResult {
    return try {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNet = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(activeNet)
        val linkProps = cm.getLinkProperties(activeNet)

        val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        val dnsList = linkProps?.dnsServers?.joinToString(", ") { it.hostAddress ?: "" } ?: "No disponible"
        val routes = linkProps?.routes?.mapNotNull { it.gateway?.hostAddress }?.filter { it != "0.0.0.0" && !it.contains(":") }
        val gateway = routes?.firstOrNull() ?: "No disponible"
        val httpProxy = linkProps?.httpProxy

        val threats = mutableListOf<ThreatIndicator>()
        var risk = 0
        if (httpProxy != null && !httpProxy.host.isNullOrEmpty()) {
            risk = 25
            threats.add(
                ThreatIndicator(
                    id = "WIFI-PROXY",
                    title = "Proxy configurado: confirma que lo conoces",
                    severity = "CRITICO",
                    evidence = "${httpProxy.host}:${httpProxy.port}",
                    explanation = "Un servidor intermedio puede estar inspeccionando tu tráfico.",
                    mitreCode = "T1557 (MITM)"
                )
            )
        }

        val security = nativeWifiSecurity(context)
        if (isWifi && (security.startsWith("Abierta") || security.startsWith("WEP"))) {
            risk = maxOf(risk, 60)
            threats.add(ThreatIndicator("WIFI-WEAK", "Red abierta o cifrado obsoleto", "ALTO", security, "Evita operaciones sensibles. Esto no demuestra una intrusión.", "T1557"))
        }
        WifiAuditResult(
            modeName = "LECTURA REAL · REVISIÓN LIMITADA",
            ssid = if (isWifi) nativeWifiName(context) else if (activeNet == null) "Sin conexión" else "Conexión no Wi-Fi",
            bssid = linkProps?.interfaceName ?: "wlan0",
            encryption = nativeWifiSecurity(context),
            gateway = gateway,
            dnsServers = dnsList.ifEmpty { "Automático" },
            riskScore = risk,
            statusTitle = if (activeNet == null) "Sin conexión activa" else if (risk > 0) "Revisa la configuración" else "Sin señales detectadas; revisión limitada",
            threats = threats
        )
    } catch (e: Exception) {
        WifiAuditResult("LECTURA NO DISPONIBLE", "No disponible", "No disponible", "No verificado", "No disponible", "No disponible", 0, "No se pudo leer la red", emptyList())
    }
}

fun getSimulatedWifiScenario(scenarioId: String): WifiAuditResult {
    return if (scenarioId == "mitm_arp") {
        WifiAuditResult(
            modeName = "DEMOSTRACIÓN DE RIESGO EN WI-FI PÚBLICO",
            ssid = "Cafeteria_Gratis_WiFi",
            bssid = "c8:3a:35:11:20:4f",
            encryption = "Abierta / Intervenida (ARP Spoofing)",
            gateway = "192.168.1.1 (MAC duplicada con .105)",
            dnsServers = "192.168.1.105 (DNS alterado)",
            riskScore = 92,
            statusTitle = "Alerta: Alguien podría leer tu tráfico en esta red",
            threats = listOf(
                ThreatIndicator(
                    id = "WIFI-MITM-01",
                    title = "Intercepción Man-in-the-Middle (ARP Spoofing)",
                    severity = "CRITICO",
                    evidence = "El equipo 192.168.1.105 clona la MAC del router",
                    explanation = "En redes Wi-Fi abiertas o intervenidas, un atacante puede situarse entre tu teléfono y el router para ver qué páginas visitas o redirigir tus pagos.",
                    mitreCode = "T1557.002 (ARP Cache Poisoning)"
                )
            )
        )
    } else {
        WifiAuditResult(
            modeName = "CONEXIÓN PROTEGIDA",
            ssid = "Red_Verificada_WPA3",
            bssid = "a0:36:9f:77:88:11",
            encryption = "WPA3-Personal (Cifrado fuerte)",
            gateway = "192.168.10.1",
            dnsServers = "1.1.1.1 (Cloudflare Seguro)",
            riskScore = 0,
            statusTitle = "Red Wi-Fi sin señales de intercepción",
            threats = emptyList()
        )
    }
}

@Preview(showBackground = true, widthDp = 340, heightDp = 720, name = "Celular Pequeño (340dp)")
@Preview(showBackground = true, widthDp = 412, heightDp = 892, name = "Celular Estándar (412dp)")
@Composable
fun MainSecurityAppPreview() {
    ControlRemotoBasureroTheme {
        MainSecurityApp()
    }
}
