"""Ejecutar desde la copia destinada al servidor, antes de iniciar Gunicorn."""
from pathlib import Path
root = Path(__file__).resolve().parent.parent
for name in ("faro.html", "faro.js", "whatsapp.html"):
    p = root / "static" / name
    s = p.read_text(encoding="utf-8-sig")
    if "/faro/static/" not in s and "/faro/api/" not in s:
        s = s.replace("/static/", "/faro/static/").replace("/api/", "/faro/api/")
    s = s.replace('href="/whatsapp"', 'href="https://mainn8n.duckdns.org/"')
    p.write_text(s, encoding="utf-8")
p = root / "app.py"
s = p.read_text(encoding="utf-8")
if "def public_routes():" not in s:
    s = s.replace("CORS(app)", """app.config['MAX_CONTENT_LENGTH'] = 32768

@app.before_request
def public_routes():
    allowed = request.path == '/' or request.path.startswith('/static/') or request.path in ('/api/analyze-url', '/api/analyze-qr-wallet')
    if not allowed:
        return jsonify(error='Ruta no disponible en la version publica'), 404
""")
p.write_text(s, encoding="utf-8")
