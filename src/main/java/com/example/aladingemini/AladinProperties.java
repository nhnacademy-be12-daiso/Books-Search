package com.example.aladingemini;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ConfigurationProperties(prefix = "aladin")
public class AladinProperties {

    /**
     * 알라딘 TTB Key (환경 변수로 설정 추천)
     */
    private String ttbKey;

    /**
     * 가져올 CategoryId 목록. application.yml 에서는 콤마 구분 문자열로 넣고,
     * 여기서 List<Integer> 로 파싱해서 사용한다.
     */
    private String categoryIds;

    /**
     * 오늘 한 번 실행에서 목표로 하는 최대 도서 수.
     */
    private int targetItemCount = 500;

    /**
     * 카테고리별 최대 페이지 수.
     */
    private int maxPagesPerCategory = 5;

    /**
     * 하루 알라딘 API 호출 상한 (제한 5000보다 살짝 낮게).
     */
    private int maxApiCallsPerDay = 4900;

    public String getTtbKey() {
        return ttbKey;
    }

    public void setTtbKey(String ttbKey) {
        this.ttbKey = ttbKey;
    }

    public String getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(String categoryIds) {
        this.categoryIds = categoryIds;
    }

    public int getTargetItemCount() {
        return targetItemCount;
    }

    public void setTargetItemCount(int targetItemCount) {
        this.targetItemCount = targetItemCount;
    }

    public int getMaxPagesPerCategory() {
        return maxPagesPerCategory;
    }

    public void setMaxPagesPerCategory(int maxPagesPerCategory) {
        this.maxPagesPerCategory = maxPagesPerCategory;
    }

    public int getMaxApiCallsPerDay() {
        return maxApiCallsPerDay;
    }

    public void setMaxApiCallsPerDay(int maxApiCallsPerDay) {
        this.maxApiCallsPerDay = maxApiCallsPerDay;
    }

    public List<Integer> categoryIdList() {
        if (categoryIds == null || categoryIds.isBlank()) {
            return List.of();
        }
        return Arrays.stream(categoryIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
}
