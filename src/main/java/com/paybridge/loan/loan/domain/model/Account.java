package com.paybridge.loan.loan.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record Account(
        @JsonProperty("owner_id")
        UUID ownerId,
        String status
) {}
