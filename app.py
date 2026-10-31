"""
Servidor Web NetLink & Wallet Guard AI — Hackathon UTEPSA
Detector de Enlaces Maliciosos, Auditor de Transacciones QR / Wallets Cripto y Escudo Wi-Fi Anti-MITM
Corre por defecto en http://127.0.0.1:5060
"""

from __future__ import annotations
import os
from flask import Flask, jsonify, request, send_from_directory
from flask_cors import CORS

from detector_engine import (
    analyze_url_and_message,
    analyze_qr_and_wallet,
    scan_real_windows_wifi_and_network,
    simulate_wifi_scenario,
    SIMULATED_WIFI_SCENARIOS,
    run_full_benchmark,
)

app = Flask(__name__, static_folder="static", static_url_path="/static")
CORS(app)

URL_PRESETS = [
    {
        "id": "URL-P1",
        "badge": "Phishing Bancario + Urgencia",
        "title": "1. Suplantación Banco Unión (Uninet) con dominio .xyz",
        "text": "URGENTE: Su cuenta Uninet ha sido bloqueada temporalmente. Verifique su token de seguridad en las próximas 24 horas en: http://bancounion-seguridad-bo.xyz/login-uninet",
    },
    {
        "id": "URL-P2",
        "badge": "Malware APK + Suplantación UTEPSA",
        "title": "2. Falsa Beca UTEPSA que descarga un Troyano (.apk)",
        "text": "Felicidades estudiante, has sido seleccionado para el bono estudiantil UTEPSA 2026. Descarga la aplicación de cobro aquí: http://becas-utepsa-bolivia.top/instalar-bono.apk",
    },
    {
        "id": "URL-P3",
        "badge": "Engaño con '@' + IP Directa",
        "title": "3. Enlace Trampa con '@' fingiendo ser Google Drive",
        "text": "Te compartieron un archivo confidencial en Google Drive: https://accounts.google.com@185.220.101.45/login-credenciales",
    },
    {
        "id": "URL-P4",
        "badge": "Typosquatting Visual (0 por o)",
        "title": "4. Suplantación de Microsoft Office 365 (micros0ft)",
        "text": "Tu contraseña de correo expirará hoy. Mantén tu misma clave entrando a https://login-micros0ft-office365.support/verify-account",
    },
    {
        "id": "URL-P5",
        "badge": "Enlace Oficial Legítimo",
        "title": "5. Portal Oficial de UTEPSA (Prueba de Cero Falsos Positivos)",
        "text": "Consulta las materias y horarios oficiales en https://www.utepsa.edu/v2/",
    },
]

QR_WALLET_PRESETS = [
    {
        "id": "QR-P1",
        "badge": "Wallet Drainer QR",
        "title": "1. QR con Contrato 'Approve Ilimitado' (Vacía toda tu Wallet)",
        "payload": "ethereum:0x71C999deadbeef000000000000000000000089A2@1/approve?spender=0xBadDrainer666&uint256=UNLIMITED",
        "trusted_wallet": "0x71C845aB912cF40192837465aBcDeF12345689A2",
    },
    {
        "id": "QR-P2",
        "badge": "Address Poisoning",
        "title": "2. Wallet Clonada en Extremos (Mismo inicio 0x71C8... y final ...89A2)",
        "payload": "0x71C80000009999888877776666555500000089A2",
        "trusted_wallet": "0x71C845aB912cF40192837465aBcDeF12345689A2",
    },
    {
        "id": "QR-P3",
        "badge": "Fraude QR Bancario",
        "title": "3. 'Cambiazo de QR' con Cobro en Dólares Encubierto",
        "payload": "EMVCO://QR-SIMPLE-BO?dest=CUENTA-FANTASMA-9981&moneda=USD&monto=1500.00&glosa=premio_sorteo_urgente",
        "trusted_wallet": "",
    },
    {
        "id": "QR-P4",
        "badge": "Blacklist USDT TRC-20",
        "title": "4. Wallet USDT (TRC-20) Reportada por Estafa Cripto",
        "payload": "TQn9Y2khEsLJW1ChVWFMSMeRDow5KcbLSE",
        "trusted_wallet": "",
    },
    {
        "id": "QR-P5",
        "badge": "Wallet Segura",
        "title": "5. Wallet Verificada y Coincidente con Referencia Oficial",
        "payload": "0x71C845aB912cF40192837465aBcDeF12345689A2",
        "trusted_wallet": "0x71C845aB912cF40192837465aBcDeF12345689A2",
    },
]


def with_notification(result, category):
    """Solo transmite puntaje y tipo; no comparte el contenido analizado."""
    import requests
    url = os.environ.get("FARO_N8N_WEBHOOK", "")
    token = os.environ.get("FARO_N8N_TOKEN", "")
    result["notification"] = "not_configured"
    if result.get("risk_score", 0) < 60:
        result["notification"] = "below_threshold"
        return result
    if not url or not token:
        return result
    try:
        r = requests.post(url, json={"risk_score": result["risk_score"], "category": category},
                          headers={"X-Faro-Token": token}, timeout=8, allow_redirects=False)
        result["notification"] = "accepted" if 200 <= r.status_code < 300 else "failed"
    except requests.RequestException:
        result["notification"] = "failed"
    return result


@app.route("/")
def index():
    return send_from_directory(app.static_folder, "faro.html")


@app.route("/api/presets", methods=["GET"])
def api_get_presets():
    wifi_list = [
        {
            "id": k,
            "title": v["title"],
            "badge": v["badge"],
            "ssid": v["wifi_info"]["ssid"],
            "risk_score": v["risk_score"],
        }
        for k, v in SIMULATED_WIFI_SCENARIOS.items()
    ]
    return jsonify({
        "url_presets": URL_PRESETS,
        "qr_wallet_presets": QR_WALLET_PRESETS,
        "wifi_scenarios": wifi_list,
    })


@app.route("/api/analyze-url", methods=["POST"])
def api_analyze_url():
    data = request.get_json(silent=True)
    if not isinstance(data, dict):
        return jsonify(error="Introduce datos válidos"), 400
    text = data.get("text", "")
    if not isinstance(text, str) or not text.strip() or len(text) > 12000:
        return jsonify(error="Introduce un mensaje o enlace válido"), 400
    return jsonify(with_notification(analyze_url_and_message(text), "enlace"))


@app.route("/api/analyze-qr-wallet", methods=["POST"])
def api_analyze_qr_wallet():
    data = request.get_json(silent=True)
    if not isinstance(data, dict):
        return jsonify(error="Introduce datos válidos"), 400
    payload = data.get("payload", "")
    trusted_wallet = data.get("trusted_wallet", "")
    if not isinstance(payload, str) or not payload.strip() or len(payload) > 12000 or not isinstance(trusted_wallet, str):
        return jsonify(error="Introduce un contenido de wallet válido"), 400
    return jsonify(with_notification(analyze_qr_and_wallet(payload, trusted_wallet), "wallet"))


@app.route("/api/wifi/scan-real", methods=["GET", "POST"])
def api_scan_real_wifi():
    return jsonify(scan_real_windows_wifi_and_network())


@app.route("/api/wifi/simulate", methods=["POST"])
def api_simulate_wifi():
    data = request.get_json(silent=True)
    if not isinstance(data, dict):
        return jsonify(error="Introduce datos válidos"), 400
    scenario_id = data.get("scenario_id", "mitm_arp")
    return jsonify(simulate_wifi_scenario(scenario_id))


@app.route("/api/benchmark", methods=["GET", "POST"])
def api_benchmark():
    return jsonify(run_full_benchmark())


@app.route("/whatsapp")
def whatsapp_page():
    return send_from_directory(app.static_folder, "whatsapp.html")


@app.route("/api/whatsapp/connect", methods=["POST"])
def whatsapp_connect():
    # Solo interfaz local: no exponer una sesión de WhatsApp a la red.
    if request.remote_addr not in ("127.0.0.1", "::1"):
        return jsonify(error="Solo disponible desde este equipo"), 403
    import requests
    import re
    base = os.environ.get("EVOLUTION_URL", "").rstrip("/")
    key = os.environ.get("EVOLUTION_API_KEY", "")
    instance = os.environ.get("EVOLUTION_INSTANCE", "faro")
    if not base or not key:
        return jsonify(error="Configura EVOLUTION_URL y EVOLUTION_API_KEY en el servidor de Faro."), 503
    if not re.fullmatch(r"[a-zA-Z0-9_-]+", instance):
        return jsonify(error="Nombre de instancia no válido"), 400
    try:
        r = requests.get(base + "/instance/connect/" + instance,
                         headers={"apikey": key}, timeout=15)
        if not r.ok:
            return jsonify(error="No se pudo conectar. Crea la instancia en Evolution y verifica sus credenciales."), 502
        data = r.json()
        qr = data.get("base64") or (data.get("qrcode") or {}).get("base64")
        response = jsonify(qr=qr, connected=data.get("instance", {}).get("state") == "open")
        response.headers["Cache-Control"] = "no-store"
        return response
    except (requests.RequestException, ValueError, TypeError):
        return jsonify(error="Evolution no responde o devolvió una respuesta inválida."), 502


if __name__ == "__main__":
    port = int(os.environ.get("PORT", 5060))
    print(f"NetLink & Wallet Guard AI (Hackathon UTEPSA) corriendo en http://127.0.0.1:{port}")
    app.run(host="127.0.0.1", port=port, debug=False)
