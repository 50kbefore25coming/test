package com.TTCS26.financeTracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point của ứng dụng Finance Tracker API
 *
 * @SpringBootApplication = Auto-config Spring Boot
 * @EnableScheduling = Kích hoạt @Scheduled tasks chạy nền
 */
@SpringBootApplication
@EnableScheduling
public class FinanceTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanceTrackerApplication.class, args);
    }

}