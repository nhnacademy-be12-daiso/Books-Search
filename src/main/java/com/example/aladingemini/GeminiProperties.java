package com.example.aladingemini;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gemini")
public class GeminiProperties {

    /**
     * Gemini API Key (Google AI Studio에서 발급)
     */
    private String apiKey;

    /**
     * 사용할 모델 이름 (예: gemini-2.5-flash)
     */
    private String model = "gemini-2.5-flash";

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
