package com.paybridge.loan.loan.infrastructure.client.transaction;

import com.paybridge.loan.loan.application.port.transaction.TransactionClient;
import com.paybridge.loan.loan.domain.model.Account;
import com.paybridge.loan.loan.infrastructure.client.transaction.dto.TransactionApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.UUID;

@Configuration
public class TransactionHttpClient implements TransactionClient {
    private final WebClient webClient;

    public TransactionHttpClient(
            WebClient.Builder builder,
            @Value("${transaction.service.base-url}") String baseUrl,
            @Value("${transaction.service.api-key}") String apiKey
    ) {
        this.webClient = builder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    @Override
    public Account getAccount (UUID ownerId){
        System.out.println("Client Account pass");
        TransactionApiResponse<Account> response =
                webClient.get()
                        .uri(TransactionApiPaths.GET_ACCOUNT_BY_OWNER_ID, ownerId)
                        .retrieve()
                        .bodyToMono(
                                new org.springframework.core.ParameterizedTypeReference<
                                        TransactionApiResponse<Account>>() {})
                        .block(Duration.ofSeconds(5));

        if (response == null || !response.success()) {
            throw new IllegalStateException("Failed to fetch account");
        }

        System.out.println(response.data());

        Account data = response.data();

        return new Account(
                data.ownerId(),
                data.status()
        );
    }

}
