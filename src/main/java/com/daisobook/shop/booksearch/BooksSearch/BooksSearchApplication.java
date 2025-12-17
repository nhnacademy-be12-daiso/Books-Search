package com.daisobook.shop.booksearch.BooksSearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class BooksSearchApplication {

	public static void main(String[] args) {
		SpringApplication.run(BooksSearchApplication.class, args);
	}

}
