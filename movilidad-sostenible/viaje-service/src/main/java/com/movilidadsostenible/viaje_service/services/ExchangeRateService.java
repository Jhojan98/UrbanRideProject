package com.movilidadsostenible.viaje_service.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Service
public class ExchangeRateService {
    private static final Logger log = LoggerFactory.getLogger(ExchangeRateService.class);

    private static final String API_URL = "https://api.exchangerate-api.com/v4/latest/USD";
    // Fallback por si falla la API
    private static final double DEFAULT_COP_PER_USD = 3798.0;

    private final RestTemplate restTemplate = new RestTemplate();

    private Double cachedCopPerUsd = null;
    private Instant lastFetch = null;
    private final Duration cacheTtl = Duration.ofMinutes(30);

    public double getCopPerUsd() {
        if (cachedCopPerUsd != null && lastFetch != null && Instant.now().isBefore(lastFetch.plus(cacheTtl))) {
            return cachedCopPerUsd;
        }
        try {
            ResponseEntity<Map> resp = restTemplate.getForEntity(API_URL, Map.class);
            Map<String, Object> body = resp.getBody();
            if (body == null) {
                log.warn("Respuesta vacía de ExchangeRate API, usando fallback");
                return useFallback();
            }
            Object ratesObj = body.get("rates");
            if (!(ratesObj instanceof Map)) {
                log.warn("Formato de rates inesperado, usando fallback");
                return useFallback();
            }
            Map<?, ?> rates = (Map<?, ?>) ratesObj;
            Object copObj = rates.get("COP");
            if (copObj instanceof Number) {
                double copPerUsd = ((Number) copObj).doubleValue();
                cachedCopPerUsd = copPerUsd;
                lastFetch = Instant.now();
                return copPerUsd;
            } else {
                log.warn("No se encontró tasa COP en respuesta, usando fallback");
                return useFallback();
            }
        } catch (Exception e) {
            log.warn("Fallo consultando ExchangeRate API: {}. Usando fallback.", e.getMessage());
            return useFallback();
        }
    }

    private double useFallback() {
        cachedCopPerUsd = DEFAULT_COP_PER_USD;
        lastFetch = Instant.now();
        return DEFAULT_COP_PER_USD;
    }
}

