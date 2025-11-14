package com.example.aladingemini;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        AladinProperties.class,
        GeminiProperties.class,
        JobProperties.class
})
public class AladinGeminiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AladinGeminiApplication.class, args);
    }
}
