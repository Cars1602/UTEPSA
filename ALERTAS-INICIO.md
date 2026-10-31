# Ejecutar Faro y activar alertas

## Windows
Instala dependencias una vez: `python -m pip install -r requirements.txt`.
Haz doble clic en Iniciar-Faro.cmd y abre http://127.0.0.1:5060. Mantén la ventana abierta. No es un ejecutable independiente; requiere Python.

## WhatsApp en el servidor
No necesitas descargar WhatsApp Web. Abre el flujo Faro en n8n, ejecuta Obtener QR y visualiza Binary > View en Ver QR para escanear. Escanea con WhatsApp > Dispositivos vinculados. Evolution mantiene la sesión mientras el servicio siga funcionando y WhatsApp no la desconecte.

## Gmail y destinatarios
En el flujo del servidor configura el número con país en Enviar WhatsApp. En Enviar alerta Gmail crea o selecciona credencial Gmail OAuth2 y autoriza tu cuenta. Destino inicial DESTINATARIO@example.com; cámbialo si corresponde. La contraseña del acceso a n8n no sirve como credencial Gmail.
En Incidente Faro selecciona Header Auth con nombre X-Faro-Token y un secreto propio. En Configurar destinatario sustituye el código de parada por `return items;` solo después de configurar ambos canales. Publica el flujo.

## Conectar la app
Antes de iniciar la app define en PowerShell FARO_N8N_WEBHOOK con la URL de producción del webhook y FARO_N8N_TOKEN con el mismo secreto. Se definen como variables de entorno: `$env:FARO_N8N_WEBHOOK="URL"` y `$env:FARO_N8N_TOKEN="SECRETO"`. Ejecuta `python app.py` desde esa misma ventana.
Con riesgo >=60 Faro envía puntaje y categoría; no envía el texto o wallet analizados. La aceptación del webhook no confirma entrega: revisa Executions en n8n y la recepción en ambos canales.
El análisis ocurre cuando utilizas Faro; no hay vigilancia automática de todos tus chats. El servidor mantiene n8n/Evolution, pero la app local debe estar abierta para generar evaluaciones.
