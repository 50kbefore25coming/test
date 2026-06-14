package com.TTCS26.financeTracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO ánh xạ cấu trúc JSON response từ ExchangeRate API
 *
 * Response từ API có dạng:
 * {
 *   "result": "success",
 *   "base_code": "USD",
 *   "conversion_rates": {
 *     "VND": 25300.5,
 *     "EUR": 0.92,
 *     ...
 *   },
 *   "time_last_update_unix": 1718592345
 * }
 */
@Data
@NoArgsConstructor
public class ExchangeRateApiResponse {

    private String result;

    @JsonProperty("base_code")
    private String baseCode;

    @JsonProperty("conversion_rates")
    private Map<String, Double> conversionRates;

    @JsonProperty("time_last_update_unix")
    private Long timeLastUpdateUnix;
}