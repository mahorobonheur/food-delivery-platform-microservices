package com.food.delivery.delivery_service;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;


@SpringBootApplication(exclude = {
		UserDetailsServiceAutoConfiguration.class
})
@EnableFeignClients
public class DeliveryServiceApplication {

	public static void main(String[] args) {

		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
		dotenv.entries().forEach(entry   -> System.setProperty(entry.getKey(), entry.getValue()));
		SpringApplication.run(DeliveryServiceApplication.class, args);
	}

}
