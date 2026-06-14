package com.TTCS26.financeTracker.service;

import com.TTCS26.financeTracker.dto.ExchangeRateApiResponse;
import com.TTCS26.financeTracker.model.CurrencyRate;
import com.TTCS26.financeTracker.repository.CurrencyRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service xử lý toàn bộ logic về tỷ giá tiền tệ
 * - Gọi API ngoài lấy tỷ giá realtime
 * - Quy đổi tiền tệ
 * - Lưu tỷ giá vào H2 database
 * - Tính toán biến động 7 ngày
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateService {

    private final RestTemplate restTemplate;
    private final CurrencyRateRepository currencyRateRepository;

    @Value("${exchangerate.api.key}")
    private String apiKey;

    @Value("${exchangerate.api.base-url}")
    private String apiBaseUrl;

    // ===== 1. GỌI API NGOÀI — LẤY TỶ GIÁ REALTIME =====

    public ExchangeRateApiResponse getExchangeRates(String baseCurrency) {
        String url = apiBaseUrl + "/" + apiKey + "/latest/" + baseCurrency.toUpperCase();

        log.info("📡 Gọi API: {}", url);

        try {
            ExchangeRateApiResponse response = restTemplate.getForObject(
                    url,
                    ExchangeRateApiResponse.class
            );

            if (response == null || !"success".equals(response.getResult())) {
                throw new RuntimeException("API trả về lỗi hoặc response null");
            }

            log.info("✅ Lấy tỷ giá thành công cho: {}", baseCurrency);
            return response;

        } catch (Exception e) {
            log.error("❌ Lỗi khi gọi API: {}", e.getMessage());
            throw new RuntimeException("Không thể lấy tỷ giá cho tiền: " + baseCurrency, e);
        }
    }

    // ===== 2. QUY ĐỔI TIỀN TỆ =====

    public double convertCurrency(String fromCurrency, String toCurrency, double amount) {
        fromCurrency = fromCurrency.toUpperCase();
        toCurrency = toCurrency.toUpperCase();

        if (fromCurrency.equals(toCurrency)) {
            log.info("✓ Tiền gốc và tiền đích giống nhau, trả về {}", amount);
            return amount;
        }

        ExchangeRateApiResponse response = getExchangeRates(fromCurrency);

        Map<String, Double> rates = response.getConversionRates();
        if (rates == null || !rates.containsKey(toCurrency)) {
            throw new RuntimeException(
                    "Tiền tệ " + toCurrency + " không được hỗ trợ"
            );
        }

        double rate = rates.get(toCurrency);
        double result = amount * rate;

        log.info("💱 Quy đổi: {} {} = {} {} (rate={})",
                amount, fromCurrency, result, toCurrency, rate);

        return result;
    }

    // ===== 3. LƯU TỶ GIÁ VÀO DATABASE =====

    public void saveRateToDatabase(String baseCurrency, String targetCurrency) {
        baseCurrency = baseCurrency.toUpperCase();
        targetCurrency = targetCurrency.toUpperCase();

        try {
            ExchangeRateApiResponse apiResponse = getExchangeRates(baseCurrency);

            Double rate = apiResponse.getConversionRates().get(targetCurrency);
            if (rate == null) {
                log.warn("⚠️ Tiền {} không tìm thấy", targetCurrency);
                return;
            }

            CurrencyRate currencyRate = CurrencyRate.builder()
                    .baseCurrency(baseCurrency)
                    .targetCurrency(targetCurrency)
                    .rate(rate)
                    .fetchDate(LocalDateTime.now())
                    .build();

            currencyRateRepository.save(currencyRate);
            log.info("💾 Đã lưu vào DB: {} → {} = {}", baseCurrency, targetCurrency, rate);

        } catch (Exception e) {
            log.error("❌ Lỗi khi lưu tỷ giá: {}", e.getMessage());
        }
    }

    // ===== 4. TÍNH BIẾN ĐỘNG 7 NGÀY =====

    public Map<String, Object> getSevenDayTrend(String baseCurrency, String targetCurrency) {
        baseCurrency = baseCurrency.toUpperCase();
        targetCurrency = targetCurrency.toUpperCase();

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        List<CurrencyRate> history = currencyRateRepository
                .findByBaseCurrencyAndTargetCurrencyAndFetchDateAfter(
                        baseCurrency,
                        targetCurrency,
                        sevenDaysAgo
                );

        log.info("📊 Tìm được {} record cho {} → {} trong 7 ngày",
                history.size(), baseCurrency, targetCurrency);

        if (history.isEmpty()) {
            return Map.of(
                    "error", "Không có dữ liệu cho cặp tiền này",
                    "baseCurrency", baseCurrency,
                    "targetCurrency", targetCurrency
            );
        }

        if (history.size() == 1) {
            return Map.of(
                    "message", "Chỉ có 1 record, cần ít nhất 2 để tính trend",
                    "baseCurrency", baseCurrency,
                    "targetCurrency", targetCurrency,
                    "records", history.size()
            );
        }

        history.sort(Comparator.comparing(CurrencyRate::getFetchDate));

        double oldestRate = history.get(0).getRate();
        double newestRate = history.get(history.size() - 1).getRate();
        double changePercent = ((newestRate - oldestRate) / oldestRate) * 100;

        String trend = changePercent > 0 ? "📈 Tăng" : "📉 Giảm";

        return Map.of(
                "baseCurrency", baseCurrency,
                "targetCurrency", targetCurrency,
                "oldestRate", String.format("%.2f", oldestRate),
                "newestRate", String.format("%.2f", newestRate),
                "changePercent", String.format("%.2f%%", changePercent),
                "trend", trend,
                "totalRecords", history.size(),
                "history", history.stream()
                        .map(r -> Map.of(
                                "rate", r.getRate(),
                                "date", r.getFetchDate()
                        ))
                        .collect(Collectors.toList())
        );
    }

    // ===== 5. HỖ TRỢ — LẤY TỶ GIÁ REALTIME NHẤT =====

    public Double getLatestRateFromDb(String baseCurrency, String targetCurrency) {
        return currencyRateRepository
                .findTopByBaseCurrencyAndTargetCurrencyOrderByFetchDateDesc(
                        baseCurrency.toUpperCase(),
                        targetCurrency.toUpperCase()
                )
                .map(CurrencyRate::getRate)
                .orElse(null);
    }
}