package com.daisobook.shop.booksearch.search.component;

import com.daisobook.shop.booksearch.search.domain.RabbitBook;
import com.daisobook.shop.booksearch.books_search.search.message.*;
import com.daisobook.shop.booksearch.search.message.BookDeleteMessage;
import com.daisobook.shop.booksearch.search.message.BookUpsertMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class BookSearchTaskPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String rkUpsert;
    private final String rkDelete;

    public BookSearchTaskPublisher(
            @Qualifier("bookSearchRabbitTemplate") RabbitTemplate rabbitTemplate,
            @Value("${rabbitmq.exchange.main}") String exchange,
            @Value("${rabbitmq.routing.book-upsert}") String rkUpsert,
            @Value("${rabbitmq.routing.book-delete}") String rkDelete
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.rkUpsert = rkUpsert;
        this.rkDelete = rkDelete;
    }

    // 단일 도서 ES 업서트 발행 (등록/수정)
    public void publishBookUpsert(RabbitBook book, String reason) {
        log.info("Sending to Exchange: {}, RoutingKey: {}", exchange, rkUpsert);
        rabbitTemplate.convertAndSend(
                exchange,
                rkUpsert,
                new BookUpsertMessage(
                        UUID.randomUUID().toString(),
                        new BookUpsertMessage.BookPayload(
                                book.getIsbn(),
                                book.getId(),
                                book.getTitle(),
                                book.getAuthor(),
                                book.getPublisher(),
                                book.getDescription(),
                                book.getPubDate(),
                                book.getPrice(),
                                book.getCategories(),
                                book.getImageUrl(),
                                book.getPublisherId(),
                                book.getCategoryId()
                        ),
                        System.currentTimeMillis(),
                        reason
                )
        );
    }

    // 단일 도서 ES 삭제 발행
    public void publishBookDelete(String isbn, String reason) {
        rabbitTemplate.convertAndSend(
                exchange,
                rkDelete,
                new BookDeleteMessage(UUID.randomUUID().toString(), isbn, System.currentTimeMillis(), reason)
        );
    }

}
