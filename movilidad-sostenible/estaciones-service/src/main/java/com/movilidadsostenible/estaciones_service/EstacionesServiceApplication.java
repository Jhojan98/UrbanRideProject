package com.movilidadsostenible.estaciones_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class EstacionesServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(EstacionesServiceApplication.class, args);
	}

}
