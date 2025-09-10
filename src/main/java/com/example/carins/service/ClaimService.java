package com.example.carins.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.carins.model.Car;
import com.example.carins.model.Claim;
import com.example.carins.repo.*;
import com.example.carins.web.dto.ClaimDto;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final CarRepository carRepository;

    public ClaimService(ClaimRepository claimRepository,
                        CarRepository carRepository) {
        this.claimRepository = claimRepository;
        this.carRepository = carRepository;
    }

    public Claim registerClaim(Long carId, ClaimDto claimDto) {
        Car car = carRepository.findById(carId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found with id " + carId));

        Claim claim = new Claim();
        claim.setCar(car);
        claim.setClaimDate(claimDto.claimDate());
        claim.setDescription(claimDto.description());
        claim.setAmount(claimDto.amount());

        return claimRepository.save(claim);
    }
}
