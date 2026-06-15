package com.TTCS26.financeTracker.controller;

import com.TTCS26.financeTracker.dto.ExchangeRateApiResponse;
import com.TTCS26.financeTracker.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/rates")
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    // Lấy tỉ giá
    @GetMapping("/{currency}")
    public ResponseEntity<?> getExchangeRates(@PathVariable String currency) {
        log.info("Lấy tỷ giá của {}", currency);

        try {
            ExchangeRateApiResponse response = exchangeRateService.getExchangeRates(currency);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi không tìm được tỉ giá: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    // Quy đổi
    @GetMapping("/convert")
    public ResponseEntity<?> convertCurrency(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(defaultValue = "1") double amount
    ) {
        log.info("Quy đổi {} {} sang {}", amount, from, to);

        try {
            double result = exchangeRateService.convertCurrency(from, to, amount);

            ExchangeRateApiResponse apiResponse = exchangeRateService.getExchangeRates(from);
            double rate = apiResponse.getConversionRates().get(to.toUpperCase());

            return ResponseEntity.ok(Map.of(
                    "from", from.toUpperCase(),
                    "to", to.toUpperCase(),
                    "amount", amount,
                    "result", String.format("%.2f", result),
                    "rate", String.format("%.4f", rate)
            ));

        } catch (Exception e) {
            log.error("Lỗi quy đổi: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    // Kiểm tra tỉ giá mới nhất
    @GetMapping("/db/latest")
    public ResponseEntity<?> getLatestFromDb(
            @RequestParam String from,
            @RequestParam String to
    ) {
        log.info("Lấy từ DB {} → {}", from, to);

        Double rate = exchangeRateService.getLatestRateFromDb(from, to);

        if (rate == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of(
                "from", from.toUpperCase(),
                "to", to.toUpperCase(),
                "latestRate", rate
        ));
    }
}