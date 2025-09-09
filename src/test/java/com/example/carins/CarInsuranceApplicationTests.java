package com.example.carins;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.carins.model.*;
import com.example.carins.service.CarService;
import com.example.carins.repo.*;

@SpringBootTest
class CarInsuranceApplicationTests {

    @Autowired
    CarRepository carRepository;

    @Autowired
    InsurancePolicyRepository insuranceRepository;

    @Autowired
    CarService service;

    @Test
    void insuranceValidityBasic() {
        assertTrue(service.isInsuranceValid(1L, LocalDate.parse("2024-06-01")));
        assertTrue(service.isInsuranceValid(1L, LocalDate.parse("2025-06-01")));
        assertFalse(service.isInsuranceValid(2L, LocalDate.parse("2025-02-01")));
    }

    @Test
    void createPolicyWithoutEndDate() {
        InsurancePolicy insurancePolicy = new InsurancePolicy();
        Car car = carRepository.findById(2L).orElseThrow();
        // insurancePolicy.setCar(carRepository.getCar(2L));
        insurancePolicy.setCar(car);
        insurancePolicy.setProvider("Test provider");
        insurancePolicy.setStartDate(LocalDate.now());
        insurancePolicy.setEndDate(null); // intentionally null

        // https://stackoverflow.com/questions/40268446/junit-5-how-to-assert-an-exception-is-thrown
        Exception exception = assertThrows(Exception.class,
                    () -> insuranceRepository.saveAndFlush(insurancePolicy));

        String exceptionMessage = exception.getMessage();
        assertTrue(exceptionMessage.contains("endDate"));
    }
}
