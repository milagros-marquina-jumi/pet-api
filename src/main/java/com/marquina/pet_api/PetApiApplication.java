package com.marquina.pet_api;

import com.marquina.pet_api.config.PetStoreProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(PetStoreProperties.class)
public class PetApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(PetApiApplication.class, args);
	}

}
