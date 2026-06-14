package com.TTCS26.financeTracker.repository;

import com.TTCS26.financeTracker.model.CurrencyRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository xử lý tất cả thao tác với bảng currency_rate
 * JpaRepository tự sinh SQL từ tên method
 */
@Repository
public interface CurrencyRateRepository extends JpaRepository<CurrencyRate, Long> {

    /**
     * Tìm tỷ giá mới nhất của cặp tiền
     * Spring tự sinh SQL: SELECT * FROM currency_rate WHERE ... ORDER BY fetch_date DESC LIMIT 1
     */
    Optional<CurrencyRate> findTopByBaseCurrencyAndTargetCurrencyOrderByFetchDateDesc(
            String baseCurrency,
            String targetCurrency
    );

    /**
     * Tìm lịch sử tỷ giá trong khoảng thời gian (dùng cho tính năng 7 ngày)
     */
    List<CurrencyRate> findByBaseCurrencyAndTargetCurrencyAndFetchDateAfter(
            String baseCurrency,
            String targetCurrency,
            LocalDateTime fromDate
    );

    /**
     * Tìm tất cả tỷ giá của 1 cặp tiền (debugging)
     */
    List<CurrencyRate> findByBaseCurrencyAndTargetCurrency(
            String baseCurrency,
            String targetCurrency
    );
}