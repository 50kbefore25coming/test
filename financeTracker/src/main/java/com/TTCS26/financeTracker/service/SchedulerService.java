package com.TTCS26.financeTracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service chứa các tác vụ chạy theo lịch (Scheduler)
 *
 * Cron expression "0 0 8 * * *":
 * - 0: giây thứ 0
 * - 0: phút thứ 0
 * - 8: giờ thứ 8 (8:00 sáng)
 * - *: mỗi ngày
 * - *: mỗi tháng
 * - *: mỗi thứ
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    private final ExchangeRateService exchangeRateService;

    /**
     * Chạy tự động mỗi ngày lúc 8:00 sáng
     * Fetch tỷ giá các cặp tiền quan trọng từ API và lưu vào H2 database
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void fetchDailyRates() {
        log.info("═══════════════════════════════════════════");
        log.info("⏰ [SCHEDULER] Bắt đầu fetch tỷ giá hàng ngày");
        log.info("═══════════════════════════════════════════");

        try {
            log.info("Fetching USD rates...");
            exchangeRateService.saveRateToDatabase("USD", "VND");
            exchangeRateService.saveRateToDatabase("USD", "EUR");
            exchangeRateService.saveRateToDatabase("USD", "GBP");
            exchangeRateService.saveRateToDatabase("USD", "JPY");

            log.info("Fetching EUR rates...");
            exchangeRateService.saveRateToDatabase("EUR", "VND");
            exchangeRateService.saveRateToDatabase("EUR", "USD");
            exchangeRateService.saveRateToDatabase("EUR", "GBP");

            log.info("Fetching GBP rates...");
            exchangeRateService.saveRateToDatabase("GBP", "VND");
            exchangeRateService.saveRateToDatabase("GBP", "USD");

            log.info("═══════════════════════════════════════════");
            log.info("✅ [SCHEDULER] Hoàn thành fetch tỷ giá hàng ngày");
            log.info("═══════════════════════════════════════════");

        } catch (Exception e) {
            log.error("❌ [SCHEDULER] Lỗi khi fetch tỷ giá: {}", e.getMessage(), e);
        }
    }
}