package com.example.aladingemini.model;

import java.time.LocalDate;
import java.util.List;

/**
 * AI 및 내부 로직까지 포함된 완성 도서 정보.
 */
public class BookEnriched {

    private String isbn13;

    private String title;
    private String author;
    private String publisher;
    private String publishedAt;

    private Integer listPrice;

    private String description; // 최종 상세 설명
    private List<String> toc;   // 목차

    private String categoryLevel1;
    private String categoryLevel2;
    private String categoryLevel3;

    private List<String> tags;

    private boolean packable;

    private int stock;
    private SaleStatus saleStatus;

    private boolean aiGeneratedDescription;
    private boolean aiGeneratedToc;
    private boolean aiGeneratedCategory;
    private boolean aiGeneratedTags;

    public String getIsbn13() {
        return isbn13;
    }

    public void setIsbn13(String isbn13) {
        this.isbn13 = isbn13;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Integer getListPrice() {
        return listPrice;
    }

    public void setListPrice(Integer listPrice) {
        this.listPrice = listPrice;
    }

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

    public String getCategoryLevel1() {
        return categoryLevel1;
    }

    public void setCategoryLevel1(String categoryLevel1) {
        this.categoryLevel1 = categoryLevel1;
    }

    public String getCategoryLevel2() {
        return categoryLevel2;
    }

    public void setCategoryLevel2(String categoryLevel2) {
        this.categoryLevel2 = categoryLevel2;
    }

    public String getCategoryLevel3() {
        return categoryLevel3;
    }

    public void setCategoryLevel3(String categoryLevel3) {
        this.categoryLevel3 = categoryLevel3;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public boolean isPackable() {
        return packable;
    }

    public void setPackable(boolean packable) {
        this.packable = packable;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public SaleStatus getSaleStatus() {
        return saleStatus;
    }

    public void setSaleStatus(SaleStatus saleStatus) {
        this.saleStatus = saleStatus;
    }

    public boolean isAiGeneratedDescription() {
        return aiGeneratedDescription;
    }

    public void setAiGeneratedDescription(boolean aiGeneratedDescription) {
        this.aiGeneratedDescription = aiGeneratedDescription;
    }

    public boolean isAiGeneratedToc() {
        return aiGeneratedToc;
    }

    public void setAiGeneratedToc(boolean aiGeneratedToc) {
        this.aiGeneratedToc = aiGeneratedToc;
    }

    public boolean isAiGeneratedCategory() {
        return aiGeneratedCategory;
    }

    public void setAiGeneratedCategory(boolean aiGeneratedCategory) {
        this.aiGeneratedCategory = aiGeneratedCategory;
    }

    public boolean isAiGeneratedTags() {
        return aiGeneratedTags;
    }

    public void setAiGeneratedTags(boolean aiGeneratedTags) {
        this.aiGeneratedTags = aiGeneratedTags;
    }
}
