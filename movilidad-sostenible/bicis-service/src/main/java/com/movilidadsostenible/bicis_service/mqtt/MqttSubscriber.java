package com.movilidadsostenible.bicis_service.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movilidadsostenible.bicis_service.model.dto.BicycleTelemetryDTO;
import com.movilidadsostenible.bicis_service.model.entity.Bicycle;
import com.movilidadsostenible.bicis_service.rabbit.publisher.BicisPublisher;
import com.movilidadsostenible.bicis_service.repositories.BicycleRepository;
import com.movilidadsostenible.bicis_service.services.websocket.TelemetryWebSocketPublisher;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class MqttSubscriber implements MqttCallbackExtended {

    private static final Logger log = LoggerFactory.getLogger(MqttSubscriber.class);

    @Value("${mqtt.broker-url}")
    private String brokerUrl;
    @Value("${mqtt.client-id}")
    private String clientId;
    @Value("${mqtt.username:}")
    private String username;
    @Value("${mqtt.password:}")
    private String password;
    @Value("${mqtt.input-topic}")
    private String inputTopic;
    @Value("${mqtt.qos:1}")
    private int qos;

    @Autowired
    private BicycleRepository repository;

    @Autowired
    private TelemetryWebSocketPublisher webSocketPublisher;

    @Autowired
    private BicisPublisher bicisPublisher;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MqttClient client;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void start() {
        scheduler.execute(this::connectAndSubscribeWithRetry);
    }

    private void connectAndSubscribeWithRetry() {
        while (true) {
            try {
                if (client == null) {
                    client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
                    client.setCallback(this);
                }
                MqttConnectOptions opts = new MqttConnectOptions();
                opts.setAutomaticReconnect(true);
                opts.setCleanSession(false); // mantener suscripciones
                if (username != null && !username.isBlank()) {
                    opts.setUserName(username);
                }
                if (password != null && !password.isBlank()) {
                    opts.setPassword(password.toCharArray());
                }
                if (!client.isConnected()) {
                    log.info("Conectando a MQTT {} como {}...", brokerUrl, clientId);
                    client.connect(opts);
                    log.info("Conectado a MQTT");
                }
                client.subscribe(inputTopic, qos);
                log.info("Suscrito a topic {} con QoS {}", inputTopic, qos);
                return; // éxito
            } catch (Exception e) {
                log.warn("Fallo conectando/suscribiendo a MQTT. Reintentando en 5s: {}", e.getMessage());
                sleep(5);
            }
        }
    }

    private void sleep(int seconds) {
        try { TimeUnit.SECONDS.sleep(seconds); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }

    @PreDestroy
    public void stop() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
            }
        } catch (Exception e) {
            log.debug("Error al desconectar MQTT", e);
        }
        scheduler.shutdownNow();
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        log.info("Conexión MQTT completa. reconnect={}, uri={}", reconnect, serverURI);
        try {
            if (client != null && client.isConnected()) {
                client.subscribe(inputTopic, qos);
            }
        } catch (MqttException e) {
            log.error("Error re-suscribiendo tras reconexión", e);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("Conexión MQTT perdida: {}", cause != null ? cause.getMessage() : "");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        try {
            String payloadStr = new String(message.getPayload(), StandardCharsets.UTF_8);
            String idFromTopic = extractBikeId(topic);
            BicycleTelemetryDTO telemetry = objectMapper.readValue(payloadStr, BicycleTelemetryDTO.class);
            if (telemetry.getIdBicycle() == null) {
                telemetry.setIdBicycle(idFromTopic);
            }
            if (telemetry.getTimestamp() == null) {
                telemetry.setTimestamp(System.currentTimeMillis());
            }
            if (telemetry.getIdBicycle() == null) {
                log.warn("Mensaje MQTT sin id de bicicleta. topic={}, payload={}", topic, payloadStr);
                return;
            }
            Optional<Bicycle> opt = repository.findById(telemetry.getIdBicycle());
            if (opt.isEmpty()) {
                log.warn("Bicicleta {} no existe en BD. Ignorando telemetría.", telemetry.getIdBicycle());
                return;
            }
            Bicycle bike = opt.get();
            if (telemetry.getLatitude() != null) bike.setLatitude(telemetry.getLatitude());
            if (telemetry.getLongitude() != null) bike.setLength(telemetry.getLongitude());
            if (telemetry.getBattery() != null) bike.setBattery(telemetry.getBattery());
            if ("ERROR".equals(telemetry.getPadlockStatus())) {
                bike.setPadlockStatus("ERROR");
                bicisPublisher.sendJsonMaintenanceMessage(telemetry);
            } else if ("LOCKED".equals(telemetry.getPadlockStatus())) {
                bike.setPadlockStatus("LOCKED");
            } else if ("UNLOCKED".equals(telemetry.getPadlockStatus())) {
                bike.setPadlockStatus("UNLOCKED");
            }
            bike.setLastUpdate(new Timestamp(telemetry.getTimestamp()));
            repository.save(bike);
            webSocketPublisher.sendTelemetry(telemetry);
            log.info("Actualizada bici {} lat={}, lon={}, bat={}", bike.getIdBicycle(), bike.getLatitude(), bike.getLength(), bike.getBattery());
        } catch (Exception e) {
            log.error("Error procesando mensaje MQTT", e);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // no publicamos desde este componente
    }

    private String extractBikeId(String topic) {
        // Espera: bikes/{id}/telemetry
        if (topic == null) return null;
        String[] parts = topic.split("/");
        if (parts.length >= 3 && "bikes".equals(parts[0]) && "telemetry".equals(parts[2])) {
            try { return parts[1];} catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
