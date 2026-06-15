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

    // Gọi API
    public ExchangeRateApiResponse getExchangeRates(String baseCurrency) {
        String url = apiBaseUrl + "/" + apiKey + "/latest/" + baseCurrency.toUpperCase();

        try {
            ExchangeRateApiResponse response = restTemplate.getForObject(
                    url,
                    ExchangeRateApiResponse.class
            );

            if (response == null || !"success".equals(response.getResult())) {
                throw new RuntimeException("API trả về lỗi hoặc response null");
            }

            log.info("Lấy tỷ giá thành công của: {}", baseCurrency);
            return response;

        } catch (Exception e) {
            log.error("Lỗi khi gọi API: {}", e.getMessage());
            throw new RuntimeException("Không thể lấy tỷ giá cho tiền: " + baseCurrency, e);
        }
    }

    // Quy đổi tiền
    public double convertCurrency(String fromCurrency, String toCurrency, double amount) {
        fromCurrency = fromCurrency.toUpperCase();
        toCurrency = toCurrency.toUpperCase();

        if (fromCurrency.equals(toCurrency)) {
            log.info("Tiền gốc và tiền đích giống nhau, trả về {}", amount);
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

        log.info("Quy đổi: {} {} = {} {} (rate={})",
                amount, fromCurrency, result, toCurrency, rate);

        return result;
    }

    // Lưu Database
    public void saveRateToDatabase(String baseCurrency, String targetCurrency) {
        baseCurrency = baseCurrency.toUpperCase();
        targetCurrency = targetCurrency.toUpperCase();

        try {
            ExchangeRateApiResponse apiResponse = getExchangeRates(baseCurrency);

            Double rate = apiResponse.getConversionRates().get(targetCurrency);
            if (rate == null) {
                log.warn("Tiền {} không tìm thấy", targetCurrency);
                return;
            }

            CurrencyRate currencyRate = CurrencyRate.builder()
                    .baseCurrency(baseCurrency)
                    .targetCurrency(targetCurrency)
                    .rate(rate)
                    .fetchDate(LocalDateTime.now())
                    .build();

            currencyRateRepository.save(currencyRate);
            log.info("Đã lưu vào DB: {} → {} = {}", baseCurrency, targetCurrency, rate);

        } catch (Exception e) {
            log.error("Lỗi khi lưu tỷ giá: {}", e.getMessage());
        }
    }

    // Tỉ giá mới nhất
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