package com.TTCS26.financeTracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Exchange_rate_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ExchangeRateHistory {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private int id;


}
