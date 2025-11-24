package com.movilidadsostenible.bicis_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.util.TimeZone;

@SpringBootApplication
@EnableFeignClients
public class BicisServiceApplication {

	@PostConstruct
	public void init() {
		// Establecer zona horaria por defecto para toda la aplicación
		TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
	}

	public static void main(String[] args) {
		SpringApplication.run(BicisServiceApplication.class, args);
	}

}
