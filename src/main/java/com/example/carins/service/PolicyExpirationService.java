package com.example.carins.service;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.carins.model.InsurancePolicy;
import com.example.carins.repo.InsurancePolicyRepository;

@Service
public class PolicyExpirationService {
    private static final Logger logger = LoggerFactory.getLogger(PolicyExpirationService.class);
    
    @Autowired
    private InsurancePolicyRepository repository;

    @Scheduled(fixedRate=30000)
    public void checkExpiredPolicies() {
        LocalDate today = LocalDate.now();

        List<InsurancePolicy> expiredPolicies = repository.findExpiredPolicies(today);

        for (InsurancePolicy policy : expiredPolicies) {
            logger.info("Policy {} for car {} expired on {}",
            policy.getId(),
            policy.getCar().getId(),
            policy.getEndDate());

            policy.setNotified(true);
            repository.save(policy);
        }
    }
}
