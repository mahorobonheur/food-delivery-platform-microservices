package com.food.delivery.customer_service;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Path;


@SpringBootApplication
public class CustomerServiceApplication {
	public static void main(String[] args) {
		loadDotEnv();
		SpringApplication.run(CustomerServiceApplication.class, args);
	}

	/**
	 * Loads .env from the module directory or from the nested {@code customer-service/} folder
	 * when IntelliJ uses the parent folder as the working directory.
	 */
	private static void loadDotEnv() {
		Path cwd = Path.of(System.getProperty("user.dir"));
		Path[] candidates = new Path[]{
				cwd.resolve(".env"),
				cwd.resolve("customer-service").resolve(".env")
		};
		for (Path envFile : candidates) {
			if (java.nio.file.Files.isRegularFile(envFile)) {
				Dotenv dotenv = Dotenv.configure()
						.directory(envFile.getParent().toString())
						.ignoreIfMissing()
						.load();
				dotenv.entries().forEach(entry ->
						System.setProperty(entry.getKey(), entry.getValue()));
				return;
			}
		}
		Dotenv.configure().ignoreIfMissing().load().entries().forEach(entry ->
				System.setProperty(entry.getKey(), entry.getValue()));
	}
}
