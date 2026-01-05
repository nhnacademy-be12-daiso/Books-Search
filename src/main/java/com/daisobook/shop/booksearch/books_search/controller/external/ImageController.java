package com.daisobook.shop.booksearch.books_search.controller.external;

import com.daisobook.shop.booksearch.books_search.controller.docs.ImageControllerDocs;
import com.daisobook.shop.booksearch.books_search.service.MinIOService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
public class ImageController implements ImageControllerDocs {

    private final MinIOService minIOService;

    @PostMapping(value = "/api/books/images/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public String upload(@RequestPart("image") MultipartFile image){
        return "/proxy/image?url=%s".formatted(minIOService.uploadImageFromFile(image, 1L));
    }
}
