package com.movilidadsostenible.viaje_service.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movilidadsostenible.viaje_service.clients.SlotsClient;
import com.movilidadsostenible.viaje_service.clients.UserClientRest;
import com.movilidadsostenible.viaje_service.models.dto.EndTravelDTO;
import com.movilidadsostenible.viaje_service.models.dto.TravelEndDTO;
import com.movilidadsostenible.viaje_service.models.dto.TravelStartDTO;
import com.movilidadsostenible.viaje_service.models.entity.Travel;
import com.movilidadsostenible.viaje_service.rabbit.publisher.TravelPublisher;
import com.movilidadsostenible.viaje_service.services.ReservationTempService;
import com.movilidadsostenible.viaje_service.services.TravelService;
import com.movilidadsostenible.viaje_service.services.ExchangeRateService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
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
import java.time.Instant;
import java.time.ZoneOffset;

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
    private TravelService repository;
    @Autowired
    private TravelPublisher travelPublisher;
    @Autowired
    private ReservationTempService reservationTempService;
    @Autowired
    private SlotsClient slotsClient;
    @Autowired
    private ExchangeRateService exchangeRateService;
    @Autowired
    private UserClientRest userClientRest;

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
          String slotIdIdFromTopic = extractSlotId(topic);
          String bicyIdFromTopic = extractBikeId(topic);
          EndTravelDTO telemetry = objectMapper.readValue(payloadStr, EndTravelDTO.class);
          if (telemetry.getSlotId() == null) {
              telemetry.setSlotId(slotIdIdFromTopic);
          }
          if (telemetry.getIdBicycle() == null) {
            telemetry.setIdBicycle(bicyIdFromTopic);
          }
          if (telemetry.getEndTravelTimestamp() == null) {
              telemetry.setEndTravelTimestamp(System.currentTimeMillis());
          }
          if (telemetry.getSlotId() == null) {
            log.warn("Mensaje MQTT sin id de slot. topic={}, payload={}", topic, payloadStr);
            return;
          }
          if (telemetry.getIdBicycle() == null) {
            log.warn("Mensaje MQTT sin id de bicicleta. topic={}, payload={}", topic, payloadStr);
            return;
          }

          Optional<Travel> opt = repository.findFirstByIdBicycleAndStatus(telemetry.getIdBicycle(), "IN_PROGRESS");

          if (opt.isEmpty()) {
              log.warn("Bicicleta {} no existe en BD. Ignorando telemetría.", telemetry.getIdBicycle());
              return;
          }

          Travel travel = opt.get();
          travel.setStatus("COMPLETED");

          // Establecer la fecha de finalización desde telemetría (UTC)
          long endMillis = telemetry.getEndTravelTimestamp();
          travel.setEndedAt(Instant.ofEpochMilli(endMillis).atZone(ZoneOffset.UTC).toLocalDateTime());

          // Convertir el LocalDateTime de inicio a epoch millis (UTC)
          long startMillis = travel.getStartedAt().atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
          long diffMillis = endMillis - startMillis;
          long durationMinutes = diffMillis > 0 ? (diffMillis / 60000) : 0;

          // Tasa de cambio dinámica desde API: 1 UDT = COP_PER_USD COP
          final double COP_PER_UDT = exchangeRateService.getCopPerUsd();

          double costUdT = 0.0;
          int extraMinutes = 0;

          if (("LAST MILE").equalsIgnoreCase(travel.getTravelType())) {
              // Última milla: 45 min máximo a 17,500 COP; minuto adicional 250 COP
              int includedMinutes = 45;
              extraMinutes = (int) Math.max(0, durationMinutes - includedMinutes);
              double baseCop = 17500.0;
              double extraCop = extraMinutes * 250.0;
              costUdT = (baseCop + extraCop) / COP_PER_UDT;
          } else if (("LONG TRAVEL").equalsIgnoreCase(travel.getTravelType())) {
              // Recorrido largo: 75 min máximo a 25,000 COP; minuto adicional 1,000 COP
              int includedMinutes = 75;
              extraMinutes = (int) Math.max(0, durationMinutes - includedMinutes);
              double baseCop = 25000.0;
              double extraCop = extraMinutes * 1000.0;
              costUdT = (baseCop + extraCop) / COP_PER_UDT;
          }

          // Usar el setter correcto del costo en la entidad Travel
          travel.setTravelCost(costUdT);

          try {
            userClientRest.chargeTrip(travel.getUid(), costUdT, extraMinutes);
          }
          catch (Exception e) {
            log.error("Error cobrando viaje al usuario {}: {}", travel.getUid(), e.getMessage());
          }

          repository.save(travel);

          slotsClient.lockSlotById(slotIdIdFromTopic, bicyIdFromTopic);

          TravelEndDTO travelEndDTO = new TravelEndDTO();
          travelEndDTO.setUserId(travel.getUid());
          travelEndDTO.setTravelId(travel.getIdTravel());
          travelEndDTO.setTravelType(travel.getTravelType());
          travelEndDTO.setStationStartId(travel.getFromIdStation());
          travelEndDTO.setStationEndId(travel.getToIdStation());

          travelPublisher.sendJsonTravelEndMessage(travelEndDTO);


        } catch (Exception e) {
            log.error("Error procesando mensaje MQTT", e);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // no publicamos desde este componente
    }

    private String extractSlotId(String topic) {
      // Espera: bikes/{id}/telemetry
      if (topic == null) return null;
      String[] parts = topic.split("/");
      if (parts.length >= 4 && "travel".equals(parts[0]) && "end".equals(parts[3])) {
        try { return parts[1];} catch (NumberFormatException ignored) {}
      }
      return null;
    }

    private String extractBikeId(String topic) {
        // Espera: bikes/{id}/telemetry
        if (topic == null) return null;
        String[] parts = topic.split("/");
        if (parts.length >= 4 && "travel".equals(parts[0]) && "end".equals(parts[3])) {
            try { return parts[2];} catch (NumberFormatException ignored) {}
        }
        return null;
    }


}
