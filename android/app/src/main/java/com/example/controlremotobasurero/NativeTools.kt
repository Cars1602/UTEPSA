package com.example.controlremotobasurero

import android.Manifest
import android.content.Context
import android.graphics.BitmapFactory
import android.net.wifi.WifiManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

@Suppress("DEPRECATION")
fun nativeWifiName(context: Context): String = try {
    val info = (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager).connectionInfo
    info.ssid?.trim('"')?.takeUnless { it == "<unknown ssid>" || it.isBlank() }
        ?: "Nombre restringido: autoriza ubicación y activa ubicación del dispositivo"
} catch (_: Exception) { "Nombre no disponible" }

@Suppress("DEPRECATION")
fun nativeWifiSecurity(context: Context): String = try {
    if (Build.VERSION.SDK_INT >= 31) {
        val info = (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager).connectionInfo
        when (info.currentSecurityType) {
            0 -> "Abierta (sin cifrado Wi-Fi)"
            1 -> "WEP (obsoleto)"
            2 -> "WPA/WPA2 Personal"
            3 -> "Enterprise"
            4 -> "WPA3 Personal"
            5 -> "Enhanced Open"
            else -> "No disponible / no verificado"
        }
    } else "No disponible en esta versión Android"
} catch (_: Exception) { "No disponible / no verificado" }

@Composable
fun NativeQrControls(onDecoded: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val camera = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.let { text ->
            if(text.length <= 12000) { onDecoded(text); status = "QR leído. No se abrió el destino." }
            else status = "QR demasiado largo."
        }
    }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if(uri != null) scope.launch {
            loading = true
            status = "Leyendo QR…"
            val decoded = withContext(Dispatchers.IO) {
                runCatching {
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                    val options = BitmapFactory.Options().apply {
                        var sample = 1
                        while(bounds.outWidth / sample > 2048 || bounds.outHeight / sample > 2048) sample *= 2
                        inSampleSize = sample
                    }
                    val bitmap = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
                        ?: error("Imagen inválida")
                    try {
                        val pixels = IntArray(bitmap.width * bitmap.height)
                        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
                        val source = RGBLuminanceSource(bitmap.width, bitmap.height, pixels)
                        MultiFormatReader().decode(BinaryBitmap(HybridBinarizer(source)), mapOf(DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE), DecodeHintType.TRY_HARDER to true)).text
                    } finally { bitmap.recycle() }
                }.getOrNull()
            }
            if(decoded != null && decoded.length <= 12000) { onDecoded(decoded); status = "QR leído. Revisa el resultado." }
            else status = "No se encontró un QR legible."
            loading = false
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(enabled = !loading, onClick = {
                camera.launch(ScanOptions().setDesiredBarcodeFormats(ScanOptions.QR_CODE).setPrompt("Escanea un QR · no se abrirá el enlace").setBeepEnabled(false).setOrientationLocked(false))
            }) { Text("Escanear QR") }
            OutlinedButton(enabled = !loading, onClick = { gallery.launch("image/*") }) { Text("Galería") }
        }
        if(status.isNotEmpty()) Text(status, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun NativeAlertControls(content: String, category: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var number by rememberSaveable { mutableStateOf("") }
    var consent by rememberSaveable { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    val location = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        status = if(it) "Permiso concedido. Pulsa Auditar Mi Red." else "Sin permiso: el nombre de Wi-Fi puede estar oculto."
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if(category == "enlace") OutlinedButton(onClick = { location.launch(Manifest.permission.ACCESS_FINE_LOCATION) }) { Text("Permitir nombre Wi-Fi") }
        OutlinedTextField(value = number, onValueChange = { number = it.take(20) }, label = { Text("WhatsApp · +591…") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Row { Checkbox(checked = consent, onCheckedChange = { consent = it }); Text("Recibir alertas en este número") }
        Button(enabled = consent && !sending, onClick = {
            val phone = number.replace(Regex("[\\s()-]"), "")
            if(!phone.matches(Regex("\\+[1-9]\\d{7,14}"))) { status = "Ingresa +, código de país y número."; return@Button }
            if(content.isBlank()) { status = "Primero analiza un contenido."; return@Button }
            scope.launch {
                sending = true
                status = "Comprobando y enviando…"
                status = withContext(Dispatchers.IO) {
                    runCatching {
                        val endpoint = if(content.trim().startsWith("http", true) || category == "enlace") "analyze-url" else "analyze-qr-wallet"
                        val body = JSONObject().put(if(endpoint == "analyze-url") "text" else "payload", content)
                            .put("notification_consent", true).put("whatsapp_number", phone)
                        val connection = URL("https://mainn8n.duckdns.org/faro/api/$endpoint").openConnection() as HttpURLConnection
                        try {
                            connection.requestMethod = "POST"; connection.doOutput = true
                            connection.connectTimeout = 15000; connection.readTimeout = 25000
                            connection.setRequestProperty("Content-Type", "application/json")
                            connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
                            if(connection.responseCode !in 200..299) error("Servidor no disponible")
                            val data = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
                            when(data.optString("notification")) {
                                "accepted" -> "n8n recibió la alerta; entrega pendiente de confirmar."
                                "below_threshold" -> "Sin alerta: riesgo menor a 60."
                                "recipient_not_enabled" -> "Número pendiente de habilitar por el administrador."
                                "not_configured" -> "El servidor aún no tiene configurado n8n."
                                else -> "No se pudo enviar la alerta."
                            }
                        } finally { connection.disconnect() }
                    }.getOrElse { "No se pudo conectar. Reintenta." }
                }
                sending = false
            }
        }) { Text(if(sending) "Enviando…" else "Enviar alerta WhatsApp") }
        if(status.isNotEmpty()) Text(status, style = MaterialTheme.typography.bodySmall)
    }
}
