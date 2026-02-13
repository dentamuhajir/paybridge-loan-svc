package com.paybridge.loan.loan.infrastructure.client.transaction;

import com.paybridge.loan.loan.application.port.transaction.TransactionClient;
import com.paybridge.loan.loan.domain.exception.InvalidLoanApplicationException;
import com.paybridge.loan.loan.domain.exception.InvalidLoanException;
import com.paybridge.loan.loan.domain.model.Account;
import com.paybridge.loan.loan.infrastructure.client.transaction.dto.TransactionApiResponse;
import com.paybridge.loan.shared.exception.DependencyUnavailableException;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Configuration
public class TransactionHttpClient implements TransactionClient {
    private final WebClient webClient;

    public TransactionHttpClient(
            WebClient.Builder builder,
            ObservationRegistry observationRegistry,
            @Value("${transaction.service.base-url}") String baseUrl,
            @Value("${transaction.service.api-key}") String apiKey
    ) {
        this.webClient = builder
                .baseUrl(baseUrl)
                .observationRegistry(observationRegistry)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    @Override
    public Account getAccount(UUID ownerId) {
        log.info("Fetching account for ownerId: {}", ownerId);

        try {
            TransactionApiResponse<Account> response =
                    webClient.get()
                            .uri(TransactionApiPaths.GET_ACCOUNT_BY_OWNER_ID, ownerId)
                            .retrieve()

                            // 400 -> bad loan request
                            .onStatus(
                                    status -> status.value() == 400,
                                    res -> res.bodyToMono(String.class)
                                            .map(body ->
                                                    new InvalidLoanApplicationException(
                                                            "Invalid loan application"
                                                    )
                                            )
                            )

                            // 404 / 409 -> loan precondition failed
                            .onStatus(
                                    status -> status.value() == 404 || status.value() == 409,
                                    res -> res.bodyToMono(String.class)
                                            .map(body ->
                                                    new InvalidLoanException(
                                                            "Loan precondition failed"
                                                    )
                                            )
                            )

                            // 5xx -> dependency unavailable
                            .onStatus(
                                    status -> status.is5xxServerError(),
                                    res -> res.bodyToMono(String.class)
                                            .map(body ->
                                                    new DependencyUnavailableException(
                                                            "Transaction service unavailable"
                                                    )
                                            )
                            )

                            .bodyToMono(new ParameterizedTypeReference<
                                                                TransactionApiResponse<Account>>() {})
                            .retryWhen(
                                    Retry.backoff(3, Duration.ofMillis(500))  // 3 retries
                                            .maxBackoff(Duration.ofSeconds(2))
                                            .filter(this::isRetryableError)
                                            .onRetryExhaustedThrow((spec, signal) ->
                                                    new DependencyUnavailableException("Transaction service retry exhausted")
                                            )
                            )

                            .block(Duration.ofSeconds(5));

            if (response == null || !response.success()) {
                throw new DependencyUnavailableException("Invalid response from transaction service");
            }

            Account data = response.data();

            log.info("Successfully retrieved account data: {}", data);

            return new Account(
                    data.ownerId(),
                    data.status()
            );

        } catch (Exception e) {
            // LOG THE EXCEPTION HERE
            log.error("Failed to call Transaction Service for ownerId: {}. Error: {}", ownerId, e.getMessage());

            // Re-throw if it's already our domain exception, otherwise wrap it
            if (e instanceof InvalidLoanApplicationException || e instanceof InvalidLoanException || e instanceof DependencyUnavailableException) {
                throw e;
            }
            throw new DependencyUnavailableException("Transaction service call failed: " + e.getMessage());
        }
    }

    private boolean isRetryableError(Throwable throwable) {
        if (throwable instanceof InvalidLoanApplicationException) return false;
        if (throwable instanceof InvalidLoanException) return false;
        if (throwable instanceof DependencyUnavailableException) return true;
        if (throwable instanceof java.util.concurrent.TimeoutException) return true;
        if (throwable instanceof org.springframework.web.reactive.function.client.WebClientRequestException) return true;
        return false;
    }
}
