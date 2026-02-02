package com.bank.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.bank.batch.client")
public class BankingEodSettlementBatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankingEodSettlementBatchApplication.class, args);
		System.out.println("Application Started Successfully");
	}
}