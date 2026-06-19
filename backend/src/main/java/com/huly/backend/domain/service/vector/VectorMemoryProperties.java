package com.huly.backend.domain.service.vector;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.vector-memory")
public class VectorMemoryProperties {

    private Integer minContentLength = 12;
    private Integer guidedCloudsMinContentLength = 3;
    private Integer maxContentLength = 4_000;
    private Integer defaultLimit = 5;
    private Integer maxLimit = 20;
    private Double similarityThreshold = 0.65;
    private Double recallSimilarityThreshold = 0.35;

    public Integer getMinContentLength() {
        return minContentLength;
    }

    public void setMinContentLength(Integer minContentLength) {
        this.minContentLength = minContentLength;
    }

    public Integer getGuidedCloudsMinContentLength() {
        return guidedCloudsMinContentLength;
    }

    public void setGuidedCloudsMinContentLength(Integer guidedCloudsMinContentLength) {
        this.guidedCloudsMinContentLength = guidedCloudsMinContentLength;
    }

    public Integer getMaxContentLength() {
        return maxContentLength;
    }

    public void setMaxContentLength(Integer maxContentLength) {
        this.maxContentLength = maxContentLength;
    }

    public Integer getDefaultLimit() {
        return defaultLimit;
    }

    public void setDefaultLimit(Integer defaultLimit) {
        this.defaultLimit = defaultLimit;
    }

    public Integer getMaxLimit() {
        return maxLimit;
    }

    public void setMaxLimit(Integer maxLimit) {
        this.maxLimit = maxLimit;
    }

    public Double getSimilarityThreshold() {
        return similarityThreshold;
    }

    public void setSimilarityThreshold(Double similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }

    public Double getRecallSimilarityThreshold() {
        return recallSimilarityThreshold;
    }

    public void setRecallSimilarityThreshold(Double recallSimilarityThreshold) {
        this.recallSimilarityThreshold = recallSimilarityThreshold;
    }
}
