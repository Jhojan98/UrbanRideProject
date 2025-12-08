package com.movilidadsostenible.notification_realtime_service.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/sse")
public class SseController {

  // Mapa UID -> lista de emitters (permite múltiples sesiones por usuario)
  private final ConcurrentMap<String, CopyOnWriteArrayList<SseEmitter>> clientsByUid = new ConcurrentHashMap<>();
  private final ObjectMapper objectMapper = new ObjectMapper();

  @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter connect(@RequestParam(name = "uid", required = true) String uid) {
    if (uid == null || uid.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "uid is required");
    }

    SseEmitter emitter = new SseEmitter(0L); // sin timeout
    clientsByUid.computeIfAbsent(uid, k -> new CopyOnWriteArrayList<>()).add(emitter);

    emitter.onCompletion(() -> removeEmitter(uid, emitter));
    emitter.onTimeout(() -> removeEmitter(uid, emitter));
    emitter.onError(t -> removeEmitter(uid, emitter));

    // Enviar un evento inicial para abrir el stream y confirmar conexión
    try {
      emitter.send(SseEmitter.event().name("connected").id(uid).data("OK"));
    } catch (IOException e) {
      removeEmitter(uid, emitter);
      emitter.complete();
    }

    return emitter;
  }

  private void removeEmitter(String uid, SseEmitter emitter) {
    List<SseEmitter> list = clientsByUid.get(uid);
    if (list != null) {
      list.remove(emitter);
      if (list.isEmpty()) {
        clientsByUid.remove(uid);
      }
    }
    try {
      emitter.complete();
    } catch (Exception ignored) {}
  }

  // Enviar solo a un usuario específico (String plano legacy)
  public void sendToUser(String uid, String message) {
    // Reutiliza el método JSON enviando un objeto con la clave "message"
    sendToUser(uid, java.util.Map.of("message", message));
  }

  // Enviar solo a un usuario específico como JSON
  public void sendToUser(String uid, Object payload) {
    List<SseEmitter> list = clientsByUid.get(uid);
    if (list == null) return;
    for (SseEmitter emitter : list) {
      try {
        String json = objectMapper.writeValueAsString(payload);
        emitter.send(SseEmitter.event()
            .name("mensaje")
            .data(json, MediaType.APPLICATION_JSON));
      } catch (Exception e) {
        removeEmitter(uid, emitter);
      }
    }
  }

  // Broadcast a todos los usuarios conectados
  public void sendToAll(String message) {
    for (String uid : clientsByUid.keySet()) {
      sendToUser(uid, message);
    }
  }

  @PostMapping("/test")
  public void test(@RequestParam String uid, @RequestParam String msg) {
    if ("all".equalsIgnoreCase(uid)) {
      sendToAll(msg);
    } else {
      sendToUser(uid, java.util.Map.of("message", msg));
    }
  }

}
