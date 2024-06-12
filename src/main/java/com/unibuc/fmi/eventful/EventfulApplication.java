package com.unibuc.fmi.eventful;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class EventfulApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventfulApplication.class, args);
	}

}
