package com.example.aladingemini;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "job")
public class JobProperties {

    /**
     * 결과 JSON 파일이 저장될 디렉터리.
     */
    private String outputDir = "./data";

    /**
     * true 이면 Gemini 로 enrichment 수행, false 이면 알라딘 데이터만 기록.
     */
    private boolean enableEnrichment = true;

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public boolean isEnableEnrichment() {
        return enableEnrichment;
    }

    public void setEnableEnrichment(boolean enableEnrichment) {
        this.enableEnrichment = enableEnrichment;
    }
}
