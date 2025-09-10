package com.example.carins.web;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.carins.model.Car;
import com.example.carins.model.Claim;
import com.example.carins.service.CarService;
import com.example.carins.service.ClaimService;
import com.example.carins.service.HistoryService;
import com.example.carins.web.dto.CarDto;
import com.example.carins.web.dto.ClaimDto;
import com.example.carins.web.dto.HistoryDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class CarController {

    private final CarService carService;
    private final ClaimService claimService;
    private final HistoryService historyService;

    public CarController(CarService carService, ClaimService claimService, HistoryService historyService) {
        this.carService = carService;
        this.claimService = claimService;
        this.historyService = historyService;
    }

    @GetMapping("/cars")
    public List<CarDto> getCars() {
        return carService.listCars().stream().map(this::toDto).toList();
    }

    @GetMapping("/cars/{carId}/insurance-valid")
    public ResponseEntity<?> isInsuranceValid(@PathVariable Long carId, @RequestParam String date) {
        Optional<Car> car = carService.getCar(carId);
        if(car.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // TODO: validate date format and handle errors consistently
        try {
            LocalDate d = LocalDate.parse(date);

            if (d.isBefore(LocalDate.of(1900, 1, 1)) ||
                d.isAfter(LocalDate.of(2100, 12, 31))) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Date is out of supported range (1900-2100)."));
            }

            boolean valid = carService.isInsuranceValid(carId, d);

            if (!valid) return ResponseEntity.notFound().build();

            return ResponseEntity.ok(new InsuranceValidityResponse(carId, d.toString(), valid));
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid date."));
        }
    }

    @GetMapping("/cars/{carId}/history")
    public ResponseEntity<?> getHistory(@PathVariable Long carId) {

        Optional<Car> car = carService.getCar(carId);
        if(car.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<HistoryDto> history = historyService.getCarHistory(carId);

        if (history.isEmpty()) return ResponseEntity.ok(Collections.emptyList());
        return ResponseEntity.ok(history);
    }

    @PostMapping("/cars/{carId}/claim")
    public ResponseEntity<?> registerClaim(@PathVariable Long carId, @Valid @RequestBody ClaimDto claimDto) {

        Optional<Car> car = carService.getCar(carId);
        if(car.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Claim claim = claimService.registerClaim(carId, claimDto);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(claim.getId())
                    .toUri();

        return ResponseEntity.created(location).body(claim);
    }

    private CarDto toDto(Car c) {
        var o = c.getOwner();
        return new CarDto(c.getId(), c.getVin(), c.getMake(), c.getModel(), c.getYearOfManufacture(),
                o != null ? o.getId() : null,
                o != null ? o.getName() : null,
                o != null ? o.getEmail() : null);
    }

    public record InsuranceValidityResponse(Long carId, String date, boolean valid) {}
}
