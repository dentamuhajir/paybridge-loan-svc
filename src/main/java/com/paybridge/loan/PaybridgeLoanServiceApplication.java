package com.paybridge.loan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import reactor.core.publisher.Hooks;

@SpringBootApplication
@EnableScheduling
public class PaybridgeLoanServiceApplication {

	public static void main(String[] args) {
		Hooks.enableAutomaticContextPropagation();
		SpringApplication.run(PaybridgeLoanServiceApplication.class, args);
	}

}
