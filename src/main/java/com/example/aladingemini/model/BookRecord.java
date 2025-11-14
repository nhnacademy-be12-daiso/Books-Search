package com.example.aladingemini.model;

/**
 * 알라딘에서 직접 가져오는 원본 + 내부에서 바로 쓸 필드들.
 * (AI enrichment 전 단계)
 */
public class BookRecord {

    private String isbn13;
    private String title;
    private String author;
    private String publisher;
    private String description;   // 알라딘 설명
    private String pubDate;       // 문자열 그대로 (yyyy-MM-dd 등)
    private Integer priceStandard;
    private String aladinCategoryName;

    private int stock;            // 1~10 랜덤
    private SaleStatus saleStatus; // 기본 ON_SALE

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public Integer getPriceStandard() {
        return priceStandard;
    }

    public void setPriceStandard(Integer priceStandard) {
        this.priceStandard = priceStandard;
    }

    public String getAladinCategoryName() {
        return aladinCategoryName;
    }

    public void setAladinCategoryName(String aladinCategoryName) {
        this.aladinCategoryName = aladinCategoryName;
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
}
