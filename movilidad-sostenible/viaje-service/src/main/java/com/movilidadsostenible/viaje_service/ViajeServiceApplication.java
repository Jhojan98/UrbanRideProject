package com.movilidadsostenible.viaje_service;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableRabbit
@EnableFeignClients
@SpringBootApplication
@EnableCaching
public class ViajeServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ViajeServiceApplication.class, args);
	}

}
