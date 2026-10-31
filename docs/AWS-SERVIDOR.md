# Proceso de creación y despliegue del servidor AWS

## Qué se hizo y qué ya existía
El 31 de octubre de 2026 se accedió a un servidor Ubuntu existente en AWS. Ya tenía Docker, n8n, PostgreSQL, Caddy y Evolution API. Durante este trabajo se instaló Faro, se configuró Gunicorn/systemd, se añadió la ruta HTTPS `/faro/`, se creó la instancia WhatsApp `faro` y se importó el flujo de alertas. No se creó la máquina EC2 desde la consola durante esta sesión.

Las secciones de creación de EC2 explican cómo preparar un servidor equivalente. No son un registro de acciones realizadas en la cuenta AWS ni garantizan que la instancia original se haya creado exactamente con esos valores. No se verificaron su tipo de instancia, región, VPC ni reglas originales.

## 1. Crear la instancia EC2
1. Entra a la consola AWS y selecciona una región.
2. Abre EC2 → Instancias → Lanzar instancia.
3. Asigna un nombre, por ejemplo `faro-utepsa`.
4. Elige Ubuntu Server 24.04 LTS, arquitectura x86_64 para mantener compatibilidad con el servidor observado.
5. Selecciona capacidad para Python, n8n, dos servicios PostgreSQL si fueran necesarios y Evolution/Redis. Como punto de partida para una instalación nueva de demostración, considera 4 GiB de RAM y revisa consumo y costos; no es un requisito medido ni el tamaño confirmado de la máquina original.
6. Crea o selecciona un par de claves SSH. Descarga la clave privada y consérvala fuera del repositorio.
7. Elige una VPC, subred pública con ruta a Internet y dirección IPv4 pública. Para un dominio estable, considera asociar una Elastic IP y revisar sus costos.
8. Selecciona almacenamiento EBS suficiente para imágenes Docker, bases de datos y respaldos. El tamaño depende del volumen de registros y mensajes.
9. Revisa costos y lanza la instancia. Espera a que pase las comprobaciones de estado.

Guía oficial: [Comenzar con Amazon EC2](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/EC2_GetStarted.html). Una instancia puede generar costos aunque no se esté usando activamente.

## 2. Configurar el grupo de seguridad
Para una instalación nueva:

| Puerto TCP | Origen | Propósito |
| --- | --- | --- |
| 22 | Tu IP pública/32 | Administración SSH |
| 80 | Internet | HTTP y validación/redirección de HTTPS |
| 443 | Internet | Acceso HTTPS a Faro y n8n |

No abras a Internet los puertos de PostgreSQL, Redis, Evolution, Gunicorn ni el puerto interno de n8n. Conecta esos servicios mediante redes Docker privadas y el proxy. Verifica las reglas de salida necesarias para descargar paquetes y conectar Google/WhatsApp. Estas son recomendaciones para un servidor nuevo; no se auditó ni corrigió todo el perímetro de la instancia original.

Referencia: [Grupos de seguridad de EC2](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/creating-security-group.html).

## 3. Conectar por SSH
Desde PowerShell o una terminal:

```powershell
ssh -i "RUTA_A_TU_CLAVE.pem" ubuntu@IP_PUBLICA
```

Comprueba la huella del servidor antes de aceptar la primera conexión. No publiques la clave ni una contraseña en GitHub. En esta sesión se utilizó el acceso existente facilitado por el propietario; no se documentan sus secretos.

## 4. Preparar Ubuntu y Docker
En un servidor nuevo:

```bash
sudo apt-get update
sudo apt-get install -y git python3 python3-venv ca-certificates curl
```

Instala Docker Engine y el plugin Compose siguiendo el repositorio oficial para tu versión de Ubuntu: [Docker Engine en Ubuntu](https://docs.docker.com/engine/install/ubuntu/) y [Docker Compose](https://docs.docker.com/compose/install/linux/). No ejecutes una reinstalación sobre un servidor con cargas existentes sin revisar su configuración.

```bash
sudo docker version
sudo docker compose version
```

## 5. Dominio y servicios base
1. Apunta el registro DNS A del dominio a la IP pública estable del servidor. En el proyecto se usa `mainn8n.duckdns.org`.
2. Instala n8n mediante Docker Compose y una base PostgreSQL persistente, siguiendo la [guía oficial de n8n](https://docs.n8n.io/hosting/installation/server-setups/docker-compose/).
3. Configura el nombre de host, HTTPS, URL pública de webhooks y una clave de cifrado persistente de n8n. Conserva los secretos fuera de Git.
4. Instala Caddy como proxy HTTPS y comparte una red Docker con n8n.
5. Para WhatsApp por QR, instala Evolution API v2 con sus dependencias PostgreSQL y Redis según su [proyecto oficial](https://github.com/EvolutionAPI/evolution-api). No confundas Evolution API v2 con Evolution GO: usan rutas diferentes.
6. Configura volúmenes persistentes y reinicio automático. No publiques directamente los puertos de las bases de datos.

En el servidor observado, n8n y Caddy compartían `ubuntu_default`; Evolution también estaba conectado a esa red y tenía su propia red privada para dependencias. La versión observada de Evolution era `v2.3.4`; eso describe el entorno, no constituye una recomendación de versión actual. Usa versiones compatibles y revisa sus notas antes de actualizar.

## 6. Descargar e instalar Faro
En Ubuntu:

```bash
cd /home/ubuntu
git clone https://github.com/Cars1602/UTEPSA.git faro-app
cd faro-app
python3 -m venv .venv
.venv/bin/pip install -r requirements.txt gunicorn
python3 deploy/prepare_server.py
```

El preparador adapta las rutas a `/faro/` y limita la versión pública al inicio, recursos y análisis. Se ejecuta en la copia del servidor. El código local funciona desde `/`.

## 7. Ejecutar automáticamente con systemd
La plantilla `deploy/faro.service.example` reproduce el servicio utilizado. Su dirección `172.19.0.1` corresponde a la puerta de enlace Docker del servidor observado. Comprueba tu red antes de copiarla:

```bash
sudo docker network inspect ubuntu_default
```

Adapta usuario, rutas y dirección de escucha según tu instalación. Después:

```bash
sudo cp deploy/faro.service.example /etc/systemd/system/faro.service
sudo systemctl daemon-reload
sudo systemctl enable --now faro
sudo systemctl status faro --no-pager
```

Gunicorn usa un proceso y cuatro hilos. El servicio tiene reinicio automático. Para revisar fallos:

```bash
sudo journalctl -u faro -n 100 --no-pager
```

## 8. Publicar con Caddy y HTTPS
La plantilla `deploy/Caddyfile.example` mantiene n8n en `/` y dirige `/faro/*` a Gunicorn. Adapta dominio y dirección interna; no reemplaces a ciegas una configuración con otros sitios.

En el servidor del proyecto, `/home/ubuntu/Caddyfile` se montaba dentro del contenedor `ubuntu-caddy-1`. Tras respaldar el archivo y añadir la ruta de Faro, se validó y recargó:

```bash
sudo docker exec ubuntu-caddy-1 caddy validate --config /etc/caddy/Caddyfile
sudo docker exec ubuntu-caddy-1 caddy reload --config /etc/caddy/Caddyfile
```

Los nombres de contenedores dependen del nombre de tu proyecto Compose. Caddy necesita DNS correcto y conectividad en 80/443 para emitir y renovar certificados. No es necesario publicar el puerto 5060 a Internet.

Resultado del proyecto: https://mainn8n.duckdns.org/faro/

## 9. Importar el flujo n8n y vincular canales
1. En n8n importa la plantilla `n8n-faro-whatsapp-gmail.json`.
2. Configura Header Auth del webhook: nombre `X-Faro-Token` y un secreto propio.
3. Configura la credencial de Evolution: cabecera `apikey` con su clave del servidor.
4. Sustituye URL de Evolution, instancia y número destinatario con código de país.
5. En el flujo remoto creado durante el desarrollo existe además una rama manual: Obtener QR → consultar Evolution → convertir base64 a imagen binaria. Ejecuta esa rama y escanea desde WhatsApp → Dispositivos vinculados.
6. Configura Gmail OAuth2 con un cliente web de Google Cloud, Gmail API habilitada, pantalla de consentimiento y la URL de retorno exacta que muestra n8n. Autoriza la cuenta y configura el correo destinatario.
7. Elimina o completa los pasos de parada del flujo remoto únicamente cuando cada canal esté listo. Publica el flujo y copia la URL de producción.

El flujo remoto se dejó en borrador. Se generó QR, pero no se confirmó una vinculación de Faro. Gmail tampoco se autorizó. Importar una plantilla no activa por sí solo esos servicios.

## 10. Conectar Faro con el webhook
En el servidor crea un archivo de entorno privado, por ejemplo `/home/ubuntu/faro-app/.env`, con valores reales:

```dotenv
FARO_N8N_WEBHOOK=https://TU_DOMINIO/webhook/faro-whatsapp
FARO_N8N_TOKEN=TU_SECRETO
```

Protege ese archivo con `chmod 600`. Añade a la sección `[Service]` de la unidad systemd:

```ini
EnvironmentFile=/home/ubuntu/faro-app/.env
```

Después:

```bash
sudo systemctl daemon-reload
sudo systemctl restart faro
```

El archivo `.env` está excluido de Git. La app por sí misma no lo carga; en este caso lo carga systemd. Una app pública conectada a notificaciones necesita controles de acceso, límites de frecuencia y duplicados antes de habilitar envíos, para evitar abuso.

## 11. Validar el despliegue
```bash
curl -I https://TU_DOMINIO/faro/
sudo systemctl is-active faro
sudo docker ps
```

Abre la app, analiza un caso de prueba y revisa la ejecución de n8n y la recepción de mensajes. Prueba también un caso de riesgo bajo para verificar que no envíe alertas. No confundas respuesta HTTP 200 del webhook con entrega de WhatsApp/Gmail.

En esta sesión se verificaron la página, CSS, JavaScript y análisis por HTTPS. No se verificaron envíos reales ni se completó el enlace final de notificaciones.

## 12. Mantenimiento
Respalda PostgreSQL, volúmenes de Evolution/n8n, configuración Caddy y claves de cifrado de n8n en almacenamiento privado. Comprueba restauración, consumo de disco y memoria, certificados y costos AWS. Actualiza dependencias de forma controlada. No borres volúmenes para actualizar. Conserva una copia de la configuración anterior para poder revertir.
