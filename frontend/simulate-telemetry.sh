#!/bin/bash

# Script para simular telemetría de estaciones y bicicletas vía MQTT
# Uso: ./simulate-telemetry.sh

MOSQUITTO_HOST="${MOSQUITTO_HOST:-34.9.26.232}"
MOSQUITTO_PORT="${MOSQUITTO_PORT:-1883}"

echo "🚀 Iniciando simulación de telemetría MQTT"
echo "📡 Conectando a: $MOSQUITTO_HOST:$MOSQUITTO_PORT"
echo ""

# Función para generar datos aleatorios de estación
generate_station_telemetry() {
    local station_id=$1
    local available_mechanic=$((RANDOM % 10))
    local available_electric=$((RANDOM % 8))
    local timestamp=$(date +%s%3N)
    
    echo "{
  \"idStation\": $station_id,
  \"availableMechanicBikes\": $available_mechanic,
  \"availableElectricBikes\": $available_electric,
  \"timestamp\": $timestamp
}"
}

# Función para generar datos aleatorios de bicicleta
generate_bike_telemetry() {
    local bike_id=$1
    local battery=$((RANDOM % 100))
    local latitude=$(echo "scale=6; 4.6 + ($RANDOM / 32767.0) * 0.2" | bc)
    local longitude=$(echo "scale=6; -74.1 + ($RANDOM / 32767.0) * 0.2" | bc)
    
    echo "{
  \"idBicycle\": $bike_id,
  \"battery\": $battery,
  \"latitude\": $latitude,
  \"longitude\": $longitude,
  \"timestamp\": $(date +%s%3N)
}"
}

# Verificar si mosquitto_pub está instalado
if ! command -v mosquitto_pub &> /dev/null; then
    echo "❌ Error: mosquitto_pub no está instalado"
    echo "Instalar con: sudo apt-get install mosquitto-clients"
    exit 1
fi

echo "✅ mosquitto_pub encontrado"
echo ""

# Simulación continua
echo "🔄 Iniciando simulación (Ctrl+C para detener)..."
echo ""

counter=0
while true; do
    counter=$((counter + 1))
    
    # Simular 5 estaciones
    for station_id in 1 2 3 4 5; do
        telemetry=$(generate_station_telemetry $station_id)
        topic="station/$station_id/telemetry"
        
        echo "📤 [$counter] Publicando en $topic"
        echo "$telemetry" | mosquitto_pub -h "$MOSQUITTO_HOST" -p "$MOSQUITTO_PORT" -t "$topic" -s
        
        if [ $? -eq 0 ]; then
            echo "✅ Enviado: Estación $station_id"
        else
            echo "❌ Error al enviar a estación $station_id"
        fi
    done
    
    echo ""
    
    # Simular 3 bicicletas
    for bike_id in 101 102 103; do
        telemetry=$(generate_bike_telemetry $bike_id)
        topic="bike/$bike_id/telemetry"
        
        echo "📤 [$counter] Publicando en $topic"
        echo "$telemetry" | mosquitto_pub -h "$MOSQUITTO_HOST" -p "$MOSQUITTO_PORT" -t "$topic" -s
        
        if [ $? -eq 0 ]; then
            echo "✅ Enviado: Bicicleta $bike_id"
        else
            echo "❌ Error al enviar a bicicleta $bike_id"
        fi
    done
    
    echo ""
    echo "⏱️  Esperando 10 segundos..."
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    sleep 10
done
