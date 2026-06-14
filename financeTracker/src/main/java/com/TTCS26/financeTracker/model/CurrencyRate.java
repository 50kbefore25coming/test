package com.TTCS26.financeTracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entity lưu lịch sử tỷ giá tiền tệ trong H2 Database
 * Mỗi bản ghi = 1 lần fetch tỷ giá từ API tại 1 thời điểm
 */
@Entity
@Table(name = "currency_rate")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrencyRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 3)
    private String baseCurrency;

    @Column(nullable = false, length = 3)
    private String targetCurrency;

    @Column(nullable = false)
    private Double rate;

    @Column(nullable = false)
    private LocalDateTime fetchDate;
}