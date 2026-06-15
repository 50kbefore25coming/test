package com.TTCS26.financeTracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    private final ExchangeRateService exchangeRateService;

    @EventListener(ApplicationReadyEvent.class)
    public void fetchRatesOnStartup() {
        log.info("Cập nhập dữ liệu khởi động");
        fetchDailyRates();
    }

    @Scheduled(cron = "0 */2 * * * *")
    public void fetchDailyRates() {
        log.info("Đang ấy tỉ giá hàng ngày");

        try {
            log.info("Đang lấy tỉ giá USD...");
            exchangeRateService.saveRateToDatabase("USD", "VND");
            exchangeRateService.saveRateToDatabase("USD", "EUR");
            exchangeRateService.saveRateToDatabase("USD", "GBP");
            exchangeRateService.saveRateToDatabase("USD", "JPY");

            log.info("Đang lấy tỷ giá EUR...");
            exchangeRateService.saveRateToDatabase("EUR", "VND");
            exchangeRateService.saveRateToDatabase("EUR", "USD");
            exchangeRateService.saveRateToDatabase("EUR", "GBP");

            log.info("Đang lấy tỉ giá GBP...");
            exchangeRateService.saveRateToDatabase("GBP", "VND");
            exchangeRateService.saveRateToDatabase("GBP", "USD");

            log.info("Hoàn thành lấy tỷ giá hàng ngày");

        } catch (Exception e) {
            log.error("Lỗi khi tìm tỷ giá: {}", e.getMessage(), e);
        }
    }
}