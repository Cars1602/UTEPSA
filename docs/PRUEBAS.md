# Pruebas de laboratorio · Faro

Ejecutadas el 31 de octubre de 2026. Catorce casos de clasificación, dos validaciones de entrada y un webhook simulado. Estos casos pequeños no miden precisión general ni garantizan seguridad. Umbral evaluado: alerta desde 60. No se visitaron los enlaces sospechosos ni se enviaron mensajes reales.

| Caso | Riesgo | Coincide con lo esperado |
| --- | --- | --- |
| Suplantacion con @ | 95/100 | Sí |
| Urgencia y dominio falso | 100/100 | Sí |
| Descarga APK falsa | 100/100 | Sí |
| Microsoft con cero | 100/100 | Sí |
| Portal universidad | 0/100 | Sí |
| Documentacion Microsoft | 0/100 | Sí |
| GitHub OWASP | 0/100 | Sí |
| Mensaje sin enlace | 0/100 | Sí |
| Permiso ilimitado | 95/100 | Sí |
| Direccion envenenada | 90/100 | Sí |
| Direccion coincidente | 0/100 | Sí |
| Vinculacion WhatsApp legitima posible | 95/100 | No |
| Approve limitado no necesariamente fraude | 95/100 | No |
| Direccion distinta a referencia | 0/100 | No |

## Hallazgos pendientes
- Falso positivo: un QR de vinculación de WhatsApp no demuestra por sí mismo secuestro de cuenta, pero recibe 95.
- Falso positivo: un approve limitado recibe 95; el motor no distingue permiso limitado de ilimitado.
- Omisión: wallet distinta de la referencia recibe 0 si no coincide en sus extremos. Debería advertir la diferencia sin afirmar necesariamente fraude.
- El endpoint admite texto vacío y responde 200, aunque la interfaz lo impide.
- Texto con tipo numérico provoca HTTP 500 en vez de un error de validación 400.
- El veredicto de riesgo bajo dice seguro/verificado sin que exista verificación externa; es una afirmación demasiado fuerte.

## Alertas
El webhook se probó con respuesta simulada y recibió únicamente puntaje y categoría. Eso no acredita entrega de Gmail/WhatsApp. Ambas integraciones requieren completar configuración y autorización. La web publicada se evalúa por separado en pruebas-web.json cuando está disponible.

## Repetir localmente
Desde la raíz en PowerShell:
```powershell
$env:PYTHONPATH="."
python tests/laboratorio.py
```
El script genera un informe y deja visibles los casos fallidos; no es una suite que garantice un código de salida fallido ante cada discrepancia. Los escenarios Wi-Fi del motor son independientes de estas pruebas. No se auditó una red real ni se realizaron transferencias blockchain.

## Revisión posterior: interfaz simplificada y correcciones
Se corrigieron la validación de entradas, el falso positivo de vinculación WhatsApp, la distinción de permisos limitados y la advertencia por direcciones distintas. La referencia distinta exige revisión (25), no una acusación de fraude. Los 14 casos de clasificación, dos validaciones y el webhook simulado coinciden con los criterios revisados; ver pruebas-resultados.json. Se comprobó el ejemplo de phishing desde el navegador en la web publicada: 100/100, recomendación breve y evidencia desplegable. Se verificó también la navegación a Aprender. No se confirmó entrega de WhatsApp/Gmail.
