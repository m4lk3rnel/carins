package com.example.carins.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.carins.repo.*;
import com.example.carins.web.dto.HistoryDto;

@Service
public class HistoryService {

    private final ClaimRepository claimRepository;
    private final InsurancePolicyRepository insuranceRepository;

    public HistoryService(ClaimRepository claimRepository, InsurancePolicyRepository insuranceRepository) {
        this.claimRepository = claimRepository;
        this.insuranceRepository = insuranceRepository;
    }

    public List<HistoryDto> getCarHistory(Long carId) { 
        List<HistoryDto> history = new ArrayList<>();

        insuranceRepository.findByCarId(carId)
            .forEach(policy -> history.add(new HistoryDto(
                policy.getStartDate(),
                "POLICY",
                null,
                null,
                policy.getProvider()
        )));

        claimRepository.findByCarId(carId)
            .forEach(claim -> history.add(new HistoryDto(
                claim.getClaimDate(),
                "CLAIM",
                claim.getDescription(),
                claim.getAmount(),
                null
        )));

        history.sort(Comparator.comparing(HistoryDto::eventDate));

        return history;
    }
}
