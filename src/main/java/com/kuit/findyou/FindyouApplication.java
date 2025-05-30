package com.kuit.findyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FindyouApplication {

	public static void main(String[] args) {
		SpringApplication.run(FindyouApplication.class, args);
	}

}
