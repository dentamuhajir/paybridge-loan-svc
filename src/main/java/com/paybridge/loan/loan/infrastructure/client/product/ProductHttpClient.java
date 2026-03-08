package com.paybridge.loan.loan.infrastructure.client.product;

import com.paybridge.loan.loan.application.port.product.ProductClient;
import com.paybridge.loan.loan.domain.enums.InterestType;
import com.paybridge.loan.loan.domain.model.ProductTenor;
import com.paybridge.loan.loan.infrastructure.client.product.dto.ProductApiResponse;
import com.paybridge.loan.loan.infrastructure.client.product.dto.ProductTenorResponse;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Configuration
public class ProductHttpClient implements ProductClient {
    private final WebClient webClient;

    public ProductHttpClient(
            WebClient.Builder builder,
            ObservationRegistry observationRegistry,
            @Value("${product.service.base-url}") String baseUrl,
            @Value("${product.service.api-key}") String apiKey
    ) {
        this.webClient = builder
                .baseUrl(baseUrl)
                .observationRegistry(observationRegistry)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    @Override
    public ProductTenor getLoanTenor(UUID tenorId){
        log.info("Get loan tenor ID {}", tenorId);
        ProductApiResponse<ProductTenorResponse> response =
                webClient.get()
                        .uri(ProductApiPaths.LOAN_TENOR_BY_ID, tenorId)
                        .retrieve()
                        .bodyToMono(
                                new org.springframework.core.ParameterizedTypeReference<
                                        ProductApiResponse<ProductTenorResponse>>() {})
                        .block(Duration.ofSeconds(5));

        if (response == null || !response.success()) {
            log.info("Failed to fetch loan tenor {}", tenorId);
            throw new IllegalStateException("Failed to fetch loan tenor");
        }

        ProductTenorResponse data = response.data();

        log.info("Product Tenor Response {}", data);

        return new ProductTenor(
                data.tenorId(),
                data.tenorMonths(),
                data.interestRate(),
                data.adminFee(),
                InterestType.fromExternal(data.interestType())
        );
    }
}
