package com.movilidadsostenible.slots_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SlotsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SlotsServiceApplication.class, args);
	}

}
