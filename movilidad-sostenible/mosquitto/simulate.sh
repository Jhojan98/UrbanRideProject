#!/usr/bin/env bash
# =============================================================
# Simulador externo de telemetría para una bicicleta (IoT mock)
# Publica mensajes JSON al broker MQTT usando mosquitto_pub.
# Topic: bikes/<ID_BICICLETA>/telemetry
# =============================================================
# Requisitos: paquete mosquitto-clients (mosquitto_pub), awk.
# Si quieres detenerlo: Ctrl+C.
# -------------------------------------------------------------
# FORZAR LOCALE C PARA NUMEROS CON PUNTO DECIMAL
export LC_NUMERIC=C
export LC_ALL=C
# -------------------------------------------------------------
# Variables configurables (puedes exportarlas antes de ejecutar
# o editarlas directamente). Cada una tiene un valor por defecto.
# -------------------------------------------------------------
BIKE_ID="${BIKE_ID:-123456}"            # ID de la bicicleta (debe existir en la BD para que bicis-service actualice)
BROKER_HOST="${BROKER_HOST:-localhost}" # Host del broker
BROKER_PORT="${BROKER_PORT:-1883}"      # Puerto del broker (1883 = contenedor mosquitto expuesto en tu caso)
INTERVAL="${INTERVAL:-5}"              # Intervalo entre envíos (segundos)
QOS="${QOS:-1}"                          # Calidad de servicio MQTT
RETAIN="${RETAIN:-0}"                   # 1 para retained, 0 para no retained
BATTERY_START="${BATTERY_START:-95.0}"   # Nivel inicial de batería
BATTERY_MIN="${BATTERY_MIN:-5.0}"        # Umbral de batería mínima para detener simulación
BIKE_MODEL="${BIKE_MODEL:-ModeloSim}"    # Nombre del modelo (informativo en log)
MAX_MESSAGES="${MAX_MESSAGES:-500}"        # 0 = infinito; >0 = cantidad máxima de mensajes
DRIFT_FACTOR="${DRIFT_FACTOR:-0.0005}"   # Amplitud de movimiento aleatorio
BASE_LAT="${BASE_LAT:-4.710900}"         # Latitud base inicial (Bogotá ejemplo)
BASE_LON="${BASE_LON:--74.072090}"       # Longitud base inicial (sin espacio extra)
TIMEZONE="${TIMEZONE:-America/Bogota}"   # Zona horaria (Colombia UTC-5)
LOG_PREFIX="[IOT-SIM]"

# -------------------------------------------------------------
# Función ayuda
# -------------------------------------------------------------
usage() {
  cat <<EOF
Uso: ./simulate.sh [opciones]
Variables (puedes exportarlas o pasarlas inline):
  BIKE_ID        ID bici (default 123456)
  BROKER_HOST    Host broker (default localhost)
  BROKER_PORT    Puerto broker (default 1883)
  INTERVAL       Segundos entre publicaciones (default 30)
  QOS            QoS MQTT (default 1)
  RETAIN         1 retained / 0 no (default 0)
  BATTERY_START  Batería inicial (default 95.0)
  BATTERY_MIN    Batería mínima para parar (default 5.0)
  MAX_MESSAGES   0 infinito, >0 límite (default 3)
  DRIFT_FACTOR   Amplitud de desplazamiento (default 0.0005)
  BASE_LAT       Latitud base (default 4.710900 - Bogotá)
  BASE_LON       Longitud base (default -74.072090 - Bogotá)
  TIMEZONE       Zona horaria (default America/Bogota)
Ejemplo de uso rápido (3 mensajes cada 5 seg):
  INTERVAL=5 MAX_MESSAGES=3 ./simulate.sh
Ejemplo infinito cada 30s (Ctrl+C para detener):
  MAX_MESSAGES=0 ./simulate.sh
EOF
}

if [[ "$1" == "-h" || "$1" == "--help" ]]; then
  usage; exit 0
fi

# Validar presencia de mosquitto_pub
if ! command -v mosquitto_pub >/dev/null 2>&1; then
  echo "$LOG_PREFIX ERROR: mosquitto_pub no encontrado. Instala mosquitto-clients." >&2
  exit 1
fi

# Inicializar estado
battery="$BATTERY_START"
lat="$BASE_LAT"
lon="$BASE_LON"
count=0
retain_flag=""
[[ "$RETAIN" == "1" ]] && retain_flag="-r"

echo "$LOG_PREFIX Iniciando simulación bici=$BIKE_ID broker=$BROKER_HOST:$BROKER_PORT intervalo=${INTERVAL}s QoS=$QOS retained=$RETAIN"

# Comprobar conexión básica (opcional: publicar mensaje ping inicial)
PING_PAYLOAD="{\"ping\":true,\"timestamp\":$(date +%s%3N)}"
if ! mosquitto_pub -h "$BROKER_HOST" -p "$BROKER_PORT" -t "bikes/$BIKE_ID/telemetry" -m "$PING_PAYLOAD" -q "$QOS" $retain_flag; then
  echo "$LOG_PREFIX ERROR: No se pudo publicar ping inicial. Verifica broker/puerto." >&2
  exit 2
fi

publish() {
  local ts lat_delta lon_delta local_time
  # Timestamp en epoch millis (siempre UTC internamente)
  ts=$(date +%s%3N)
  # Hora local legible para el log
  local_time=$(TZ="$TIMEZONE" date '+%Y-%m-%d %H:%M:%S')

  # Generar pequeños deltas
  lat_delta=$(awk -v r=$RANDOM -v f=$DRIFT_FACTOR 'BEGIN{printf("%.7f", (r%1000-500)/1000*f)}')
  lon_delta=$(awk -v r=$RANDOM -v f=$DRIFT_FACTOR 'BEGIN{printf("%.7f", (r%1000-500)/1000*f)}')
  # Actualizar posición
  lat=$(awk -v base=$lat -v d=$lat_delta 'BEGIN{printf("%.6f", base + d)}')
  lon=$(awk -v base=$lon -v d=$lon_delta 'BEGIN{printf("%.6f", base + d)}')
  # Descargar batería (0.2% a 0.8% aleatorio)
  battery=$(awk -v b=$battery -v r=$RANDOM 'BEGIN{printf("%.2f", b - ( (r%7)+2 )/100.0)}')

  local payload
  payload=$(printf '{"latitude":%s,"longitude":%s,"battery":%s,"timestamp":%s}' "$lat" "$lon" "$battery" "$ts")
  if mosquitto_pub -h "$BROKER_HOST" -p "$BROKER_PORT" -t "bikes/$BIKE_ID/telemetry" -m "$payload" -q "$QOS" $retain_flag; then
    echo "$LOG_PREFIX [$local_time] Published (#$count) $payload"
  else
    echo "$LOG_PREFIX [$local_time] WARNING: Falló publicación (#$count)" >&2
  fi
}

while true; do
  # Verificar límite de mensajes ANTES de publicar
  if [[ "$MAX_MESSAGES" -gt 0 && "$count" -ge "$MAX_MESSAGES" ]]; then
    echo "$LOG_PREFIX Alcanzado límite de mensajes ($MAX_MESSAGES)."; break
  fi
  # Verificar batería
  battery_ok=$(awk -v b=$battery -v min=$BATTERY_MIN 'BEGIN{print (b > min)?1:0}')
  if [[ "$battery_ok" -ne 1 ]]; then
    echo "$LOG_PREFIX Batería baja ($battery <= $BATTERY_MIN). Fin simulación."; break
  fi

  # Publicar mensaje
  publish
  ((count++))

  # Solo hacer sleep si no es el último mensaje
  if [[ "$MAX_MESSAGES" -eq 0 || "$count" -lt "$MAX_MESSAGES" ]]; then
    sleep "$INTERVAL"
  fi
done

echo "$LOG_PREFIX Simulación terminada."
