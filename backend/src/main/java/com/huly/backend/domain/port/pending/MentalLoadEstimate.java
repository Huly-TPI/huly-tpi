package com.huly.backend.domain.port.pending;

import com.huly.backend.domain.model.enums.MentalLoadBucket;

public record MentalLoadEstimate(double score, MentalLoadBucket bucket) {}
