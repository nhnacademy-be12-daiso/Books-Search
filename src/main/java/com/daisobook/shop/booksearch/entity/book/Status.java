package com.daisobook.shop.booksearch.entity.book;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Status {
    ON_SALE,
    SOLD_OUT,
    DISCONTINUE,
    UNPUBLISHED;

    @JsonCreator
    public static Status from(String value) {
        if (value == null) return null;
        switch (value.trim().toUpperCase()) {
            case "AVAILABLE":
            case "ON_SALE":
                return ON_SALE;
            case "SOLD_OUT":
                return SOLD_OUT;
            case "DISCONTINUE":
            case "DISCONTINUED":
                return DISCONTINUE;
            case "UNPUBLISHED":
                return UNPUBLISHED;
            default:
                throw new IllegalArgumentException("Unknown Status: " + value);
        }
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
