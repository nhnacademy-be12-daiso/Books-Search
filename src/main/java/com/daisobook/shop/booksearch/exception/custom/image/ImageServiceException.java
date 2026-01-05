package com.daisobook.shop.booksearch.exception.custom.image;

import com.daisobook.shop.booksearch.exception.custom.BusinessException;
import org.springframework.http.HttpStatus;

public class ImageServiceException extends BusinessException {
    public ImageServiceException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
