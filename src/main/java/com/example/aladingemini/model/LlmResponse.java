package com.example.aladingemini.model;

import java.util.List;

/**
 * Gemini가 반환하는 JSON을 파싱해서 담는 DTO.
 */
public class LlmResponse {

    private String description;
    private List<String> toc;
    private String categoryPath;
    private List<String> tags;
    private Boolean packable;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getToc() {
        return toc;
    }

    public void setToc(List<String> toc) {
        this.toc = toc;
    }

    public String getCategoryPath() {
        return categoryPath;
    }

    public void setCategoryPath(String categoryPath) {
        this.categoryPath = categoryPath;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Boolean getPackable() {
        return packable;
    }

    public void setPackable(Boolean packable) {
        this.packable = packable;
    }
}
