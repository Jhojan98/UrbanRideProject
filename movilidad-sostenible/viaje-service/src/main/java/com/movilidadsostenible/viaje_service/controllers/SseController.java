package com.movilidadsostenible.viaje_service.controllers;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/sse")
public class SseController {

  private final List<SseEmitter> clients = new CopyOnWriteArrayList<>();

  @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter connect() {
    SseEmitter emitter = new SseEmitter(0L); // sin timeout
    clients.add(emitter);

    emitter.onCompletion(() -> clients.remove(emitter));
    emitter.onTimeout(() -> clients.remove(emitter));
    emitter.onError(t -> clients.remove(emitter));

    // Enviar un evento inicial para abrir el stream y confirmar conexión
    try {
      emitter.send(SseEmitter.event().name("connected").data("OK"));
    } catch (IOException e) {
      clients.remove(emitter);
      emitter.complete();
    }

    return emitter;
  }

  // ESTE MÉTODO ENVÍA INFO DEL BACK AL FRONT
  public void sendToAll(String message) {
    for (SseEmitter emitter : clients) {
      try {
        emitter.send(SseEmitter.event()
          .name("mensaje")
          .data(message));
      } catch (Exception e) {
        clients.remove(emitter);
        emitter.complete();
      }
    }
  }

  @PostMapping("/test")
  public void test(@RequestParam String msg) {
    sendToAll(msg);
  }

}
