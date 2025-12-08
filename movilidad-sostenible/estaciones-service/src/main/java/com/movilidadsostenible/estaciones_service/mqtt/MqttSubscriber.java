package com.movilidadsostenible.estaciones_service.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movilidadsostenible.estaciones_service.model.dto.StationTelemetryDTOAdmin;
import com.movilidadsostenible.estaciones_service.model.dto.StationTelemetryDTOUser;
import com.movilidadsostenible.estaciones_service.model.entity.Station;
import com.movilidadsostenible.estaciones_service.services.StationsService;
import com.movilidadsostenible.estaciones_service.services.websocket.TelemetryWebSocketPublisher;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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
    @Value("${mqtt.input-topic.user}")
    private String inputTopicUser;
    @Value("${mqtt.input-topic.admin}")
    private String inputTopicAdmin;
    @Value("${mqtt.qos:1}")
    private int qos;

    @Autowired
    private StationsService repository;

    @Autowired
    private TelemetryWebSocketPublisher webSocketPublisher;

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

                client.subscribe(inputTopicUser, qos);
                client.subscribe(inputTopicAdmin, qos);
                log.info("Suscrito a topic {} con QoS {}", inputTopicUser, qos);
                log.info("Suscrito a topic {} con QoS {}", inputTopicAdmin, qos);
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
            if (client != null && client.isConnected() ) {
                client.subscribe(inputTopicUser, qos);
                client.subscribe(inputTopicAdmin, qos);
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
        String payloadStr = new String(message.getPayload(), StandardCharsets.UTF_8);
        boolean isUserTopic = MqttTopic.isMatched(inputTopicUser, topic);
        boolean isAdminTopic = MqttTopic.isMatched(inputTopicAdmin, topic);
      System.out.println(isUserTopic);
      System.out.println(isAdminTopic);

        if (isUserTopic) {
            try {
              Integer idFromTopic = extractStationId(topic);
              StationTelemetryDTOUser telemetry = objectMapper.readValue(payloadStr, StationTelemetryDTOUser.class);
              if (telemetry.getIdStation() == null) {
                telemetry.setIdStation(idFromTopic);
              }
              if (telemetry.getTimestamp() == null) {
                telemetry.setTimestamp(System.currentTimeMillis());
              }
              if (telemetry.getIdStation() == null) {
                log.warn("Mensaje MQTT sin id de estacion. topic={}, payload={}", topic, payloadStr);
                return;
              }
              Optional<Station> opt = repository.findById(telemetry.getIdStation());
              if (opt.isEmpty()) {
                log.warn("Estacion {} no existe en BD. Ignorando telemetría.", telemetry.getIdStation());
                return;
              }
              webSocketPublisher.sendTelemetryUser(telemetry);

            } catch (Exception e) {
              log.error("Error procesando mensaje MQTT", e);
            }
        } else if (isAdminTopic) {
          try {
            Integer idFromTopic = extractStationId(topic);
            StationTelemetryDTOAdmin telemetry = objectMapper.readValue(payloadStr, StationTelemetryDTOAdmin.class);
            if (telemetry.getIdStation() == null) {
              telemetry.setIdStation(idFromTopic);
            }
            if (telemetry.getTimestamp() == null) {
              telemetry.setTimestamp(System.currentTimeMillis());
            }
            if (telemetry.getIdStation() == null) {
              log.warn("Mensaje MQTT sin id de estacion. topic={}, payload={}", topic, payloadStr);
              return;
            }
            Optional<Station> opt = repository.findById(telemetry.getIdStation());
            if (opt.isEmpty()) {
              log.warn("Estacion {} no existe en BD. Ignorando telemetría.", telemetry.getIdStation());
              return;
            }
            Station station = opt.get();
            station.setCctvStatus(telemetry.isCctvStatus());
            repository.save(station);

            webSocketPublisher.sendTelemetryAdmin(telemetry);

            log.info("Actualizada estacion {} cctvStatus={}", station.getIdStation(), station.getCctvStatus());
          } catch (Exception e) {
            log.error("Error procesando mensaje MQTT", e);
          }
        } else {
            // Mensaje en tópico no esperado
            log.debug("Mensaje MQTT en tópico no coincidente con filtros. filterUser={}, filterAdmin={}, topic={}", inputTopicUser, inputTopicAdmin, topic);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // no publicamos desde este componente
    }

    private Integer extractStationId(String topic) {
        // Espera: bikes/{id}/telemetry
        if (topic == null) return null;
        String[] parts = topic.split("/");
        if (parts.length >= 4 && "station".equals(parts[0]) && "telemetry".equals(parts[2])) {
            try { return Integer.parseInt(parts[1]); } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
