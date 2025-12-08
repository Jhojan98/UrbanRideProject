package com.movilidadsostenible.notification_realtime_service;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRabbit
public class NotificationRealtimeServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationRealtimeServiceApplication.class, args);
	}

}
