package com.TTCS26.financeTracker.repository;

import com.TTCS26.financeTracker.model.CurrencyRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyRateRepository extends JpaRepository<CurrencyRate, Long> {

    //tỉ giá mới nhất
    Optional<CurrencyRate> findTopByBaseCurrencyAndTargetCurrencyOrderByFetchDateDesc(
            String baseCurrency,
            String targetCurrency
    );
    // tìm tỉ giá trong khoảng thời gian
    List<CurrencyRate> findByBaseCurrencyAndTargetCurrencyAndFetchDateAfter(
            String baseCurrency,
            String targetCurrency,
            LocalDateTime fromDate
    );

    // lịch sử tỉ giá của 1 cặp tiền
    List<CurrencyRate> findByBaseCurrencyAndTargetCurrency(
            String baseCurrency,
            String targetCurrency
    );
}