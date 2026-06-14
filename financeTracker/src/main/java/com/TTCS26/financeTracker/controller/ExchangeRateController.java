package com.TTCS26.financeTracker.controller;

import com.TTCS26.financeTracker.dto.ExchangeRateApiResponse;
import com.TTCS26.financeTracker.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller xử lý HTTP request từ client
 *
 * Cách test:
 * 1. GET /rates/USD
 * 2. GET /rates/convert?from=USD&to=VND&amount=100
 * 3. GET /rates/trend?from=USD&to=VND
 */
@RestController
@RequestMapping("/rates")
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    /**
     * Endpoint: GET /rates/{currency}
     * Lấy tỷ giá realtime của 1 loại tiền
     *
     * Ví dụ: GET /rates/USD
     */
    @GetMapping("/{currency}")
    public ResponseEntity<?> getExchangeRates(@PathVariable String currency) {
        log.info("📥 GET /rates/{} - Lấy tỷ giá của {}", currency, currency);

        try {
            ExchangeRateApiResponse response = exchangeRateService.getExchangeRates(currency);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Lỗi: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * Endpoint: GET /rates/convert
     * Quy đổi tiền tệ
     *
     * Ví dụ: GET /rates/convert?from=USD&to=VND&amount=100
     * → 100 USD = ? VND
     */
    @GetMapping("/convert")
    public ResponseEntity<?> convertCurrency(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(defaultValue = "1") double amount
    ) {
        log.info("📥 GET /rates/convert - Quy đổi {} {} sang {}", amount, from, to);

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
            log.error("❌ Lỗi: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * Endpoint: GET /rates/trend
     * Xem biến động tỷ giá trong 7 ngày
     *
     * Ví dụ: GET /rates/trend?from=USD&to=VND
     * → Biến động USD→VND trong 7 ngày: tăng hay giảm bao nhiêu %?
     */
    @GetMapping("/trend")
    public ResponseEntity<?> getSevenDayTrend(
            @RequestParam String from,
            @RequestParam String to
    ) {
        log.info("📥 GET /rates/trend - Biến động 7 ngày {} → {}", from, to);

        try {
            Map<String, Object> trend = exchangeRateService.getSevenDayTrend(from, to);
            return ResponseEntity.ok(trend);

        } catch (Exception e) {
            log.error("❌ Lỗi: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * Endpoint helper: GET /rates/db/latest
     * Kiểm tra tỷ giá mới nhất trong DB (debugging)
     */
    @GetMapping("/db/latest")
    public ResponseEntity<?> getLatestFromDb(
            @RequestParam String from,
            @RequestParam String to
    ) {
        log.info("📥 GET /rates/db/latest - Lấy từ DB {} → {}", from, to);

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