package com.daisobook.shop.booksearch.dto.test;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TestReq {
        private String metadata;
//        @JsonProperty("image0")
        private MultipartFile image0;
        private MultipartFile image1;
        private MultipartFile image2;
        private MultipartFile image3;
        private MultipartFile image4;
}
