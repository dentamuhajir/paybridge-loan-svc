package com.paybridge.loan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PaybridgeLoanServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaybridgeLoanServiceApplication.class, args);
	}

}
