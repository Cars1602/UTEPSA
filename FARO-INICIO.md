# Faro
Ejecuta `python app.py` y abre http://127.0.0.1:5060.
La interfaz anterior sigue en /static/index.html.

## n8n
Importa n8n-faro-alertas.json. Configura credencial Header Auth en el webhook y credencial Twilio en el nodo SMS. Sustituye remitente y destino por números autorizados en formato E.164. Publica el flujo y utiliza su URL de producción.
Envía un POST autenticado con JSON {"risk_score":85}. Riesgos desde 60 generan SMS; el resto termina sin envío. El webhook recibe evaluaciones: no investiga enlaces por sí mismo. Conecta tu recolector de registros y el detector a este webhook para automatizar la entrada. No envíes semillas ni claves privadas.

## Alcance
Detector heurístico existente para mensajes y wallets, evidencia visible, precios online de BTC/ETH/USDT/BNB/USDC bajo demanda desde CoinGecko. No hay modelo LLM, monitor continuo, base de inteligencia actualizada automáticamente ni bloqueo de fondos conectado. El flujo aún no se importó ni probó en tu servidor. Los precios no son una base de amenazas.
No se guardaron las credenciales compartidas en el chat.
