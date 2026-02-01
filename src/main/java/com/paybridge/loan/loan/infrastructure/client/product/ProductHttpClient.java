package com.paybridge.loan.loan.infrastructure.client.product;

import com.paybridge.loan.loan.application.port.product.ProductClient;
import com.paybridge.loan.loan.domain.enums.InterestType;
import com.paybridge.loan.loan.domain.model.ProductTenor;
import com.paybridge.loan.loan.infrastructure.client.product.dto.ProductApiResponse;
import com.paybridge.loan.loan.infrastructure.client.product.dto.ProductTenorResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.UUID;

@Configuration
public class ProductHttpClient implements ProductClient {
    private final WebClient webClient;

    public ProductHttpClient(
            WebClient.Builder builder,
            @Value("${product.service.base-url}") String baseUrl,
            @Value("${product.service.api-key}") String apiKey
    ) {
        this.webClient = builder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    @Override
    public ProductTenor getLoanTenor(UUID tenorId){
        System.out.println("pass to client ");
        ProductApiResponse<ProductTenorResponse> response =
                webClient.get()
                        .uri(ProductApiPaths.LOAN_TENOR_BY_ID, tenorId)
                        .retrieve()
                        .bodyToMono(
                                new org.springframework.core.ParameterizedTypeReference<
                                        ProductApiResponse<ProductTenorResponse>>() {})
                        .block(Duration.ofSeconds(5));

        if (response == null || !response.success()) {
            throw new IllegalStateException("Failed to fetch loan tenor");
        }

        ProductTenorResponse data = response.data();

        return new ProductTenor(
                data.tenorId(),
                data.tenorMonths(),
                data.interestRate(),
                data.adminFee(),
                InterestType.fromExternal(data.interestType())
        );
    }
}
