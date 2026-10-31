# WhatsApp por QR · Evolution API v2

Archivos preparados, sin instalación remota ni envíos realizados.

1. Instala Evolution API v2 en tu servidor siguiendo su documentación oficial. Este conector no usa Evolution GO, que tiene rutas diferentes.
2. En su administrador crea una instancia llamada `faro`, integración `WHATSAPP-BAILEYS`, con QR habilitado. También puedes vincularla desde ese administrador.
3. En el entorno del proceso Faro define EVOLUTION_URL (URL de tu servicio), EVOLUTION_API_KEY (secreto del servicio) y EVOLUTION_INSTANCE=faro. No pongas la clave en archivos del navegador. Reinicia Faro.
4. Abre http://127.0.0.1:5060/whatsapp, pulsa Obtener QR y escanea desde WhatsApp > Dispositivos vinculados. El QR real requiere el servicio en marcha; no se genera un QR de muestra.
5. Importa n8n-faro-whatsapp.json en n8n. En Enviar WhatsApp sustituye TU-EVOLUTION por la URL accesible desde n8n. Configura Header Auth con nombre apikey y valor de tu clave de Evolution. Sustituye CONFIGURAR_NUMERO_CON_PAIS por el número destinatario (solo dígitos, incluyendo país).
6. Configura Header Auth en Registro entrante. Publica y envía POST autenticado con {"risk_score":85} a la URL de producción que muestre n8n. Con riesgo >=60 envía WhatsApp; con riesgo inferior no envía. El remitente es la cuenta vinculada y el destino se configura aparte.

Este flujo recibe evaluaciones, no analiza mensajes por sí mismo. La app aún no envía automáticamente sus análisis al webhook. Comprueba versión, conexión y recepción antes de activar alertas reales. No hay bloqueo de transferencias.

La vinculación por QR usa un conector no oficial compatible con WhatsApp Web: puede desconectarse o estar sujeta a restricciones de WhatsApp. Usa una cuenta dedicada. No expongas el administrador ni su clave públicamente. El proceso Faro debe seguir escuchando solo en localhost; la ruta QR es local.
