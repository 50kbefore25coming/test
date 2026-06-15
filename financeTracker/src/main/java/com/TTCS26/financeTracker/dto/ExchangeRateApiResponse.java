package com.TTCS26.financeTracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

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