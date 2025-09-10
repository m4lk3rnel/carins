package com.example.carins;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.carins.model.Car;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.service.CarService;

@SpringBootTest
@AutoConfigureMockMvc
class CarInsuranceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

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

    @Test
    void registerClaimShouldReturn201() throws Exception {
        String json = """
            {
                "claimDate": "2025-08-13",
                "description": "Front bumper repair",
                "amount": 1234.56
            }
        """;

        mockMvc.perform(post("/api/cars/{carId}/claim", 1L)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.claimDate").value("2025-08-13"))
        .andExpect(jsonPath("$.description").value("Front bumper repair"))
        .andExpect(jsonPath("$.amount").value(1234.56));
    }

    @Test
    void registerClaimWithoutAmountShouldReturn400() throws Exception {
        String json = """
            {
                "claimDate": "2025-08-13",
                "description": "Front bumper repair"
            }
        """;

        mockMvc.perform(post("/api/cars/{carId}/claim", 1L)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
        .andExpect(status().isBadRequest());
    }

    @Test
    void getCarHistoryReturnsHistoryList() throws Exception {
        mockMvc.perform(get("/api/cars/{carId}/history", 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].eventType").exists())
            .andExpect(jsonPath("$[0].eventDate").exists())
            .andExpect(jsonPath("$[0].description", nullValue())) 
            .andExpect(jsonPath("$[0].amount", nullValue())) 
            .andExpect(jsonPath("$[0].provider").value("Allianz"));
    }

    @Test
    void getCarHistoryForNonExistentCarReturns404() throws Exception {
        mockMvc.perform(get("/api/cars/{carId}/history", 999L))
            .andExpect(status().isNotFound());
    }

    @Test
    void checkInsuranceIsValidForNonExistentCarReturns404() throws Exception {
        mockMvc.perform(get("/api/cars/{carId}/insurance-valid?date=2025-03-13", 999L))
            .andExpect(status().isNotFound());
    }

    @Test 
    void checkInsuranceIsValidInvalidDateFormatReturns400() throws Exception {
        mockMvc.perform(get("/api/cars/{carId}/insurance-valid?date=13-2021-02", 1L))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Invalid date."));
    }

    @Test 
    void checkInsuranceIsValidImpossibleDateReturns400() throws Exception {
        mockMvc.perform(get("/api/cars/{carId}/insurance-valid?date=1897-02-01", 1L))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Date is out of supported range (1900-2100)."));
    }
}
