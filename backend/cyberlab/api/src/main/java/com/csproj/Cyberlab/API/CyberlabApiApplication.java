package com.csproj.Cyberlab.API;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class CyberlabApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CyberlabApiApplication.class, args);
	}

}
