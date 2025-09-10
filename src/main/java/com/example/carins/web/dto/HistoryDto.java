package com.example.carins.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HistoryDto(LocalDate eventDate, String eventType, String description, BigDecimal amount, String provider) {}
