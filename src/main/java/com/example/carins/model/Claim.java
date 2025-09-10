package com.example.carins.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;

@Entity
@Table(name = "claim")
public class Claim {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JsonIgnore
    private Car car;

    @NotNull(message="claimDate can't be null")
    private LocalDate claimDate;

    @NotNull(message="description can't be null")
    private String description;

    @NotNull(message="amount can't be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;

    public Long getId() { return id; }

    public Car getCar() { return car; }
    public void setCar(@NotNull(message="car can't be null") Car car) {this.car = car;}
    public LocalDate getClaimDate() { return claimDate; }
    public void setClaimDate(@NotNull(message="claimDate can't be null") LocalDate claimDate) { this.claimDate = claimDate; }
    public String getDescription() { return description; }
    public void setDescription(@NotNull(message="description can't be null") String description) { this.description = description; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(
        @NotNull(message="amount can't be null")
        @DecimalMin(value = "0.0",
                inclusive = false,
                message = "Amount must be greater than 0")
        BigDecimal amount
    ) 
    { this.amount = amount; }
    
}

