package com.example.carins.web.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ClaimDto(
    @NotNull(message = "claimDate can't be null")
    LocalDate claimDate,
    @NotNull(message = "description can't be null")
    String description, 
    @NotNull(message = "amount can't be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    BigDecimal amount) {}