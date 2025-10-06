package com.charityplatform.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@EntityScan("com.charityplatform.backend.model")
@EnableJpaRepositories("com.charityplatform.backend.repository")
@SpringBootApplication(scanBasePackages = "com.charityplatform.backend")
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
