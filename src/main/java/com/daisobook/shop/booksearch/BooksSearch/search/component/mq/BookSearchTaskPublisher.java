package com.daisobook.shop.booksearch.BooksSearch.search.component.mq;

import com.daisobook.shop.booksearch.BooksSearch.search.message.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class BookSearchTaskPublisher {

    private final RabbitTemplate rabbitTemplate;

    public BookSearchTaskPublisher(@Qualifier("bookSearchRabbitTemplate") RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // AI 분석 작업 배치 발행
    public void publishAiAnalysisBatch(List<String> isbns) {
        if (isbns == null || isbns.isEmpty()) return;

        rabbitTemplate.convertAndSend(
                BookSearchMqConstants.EXCHANGE,
                BookSearchMqConstants.RK_AI_ANALYSIS,
                new AiAnalysisRequestMessage(
                        UUID.randomUUID().toString(),
                        isbns,
                        System.currentTimeMillis()
                )
        );
    }

    // 단일 도서 ES 업서트 발행 (등록/수정)
    public void publishBookUpsert(com.daisobook.shop.booksearch.BooksSearch.search.domain.Book book, String reason) {
        rabbitTemplate.convertAndSend(
                BookSearchMqConstants.EXCHANGE,
                BookSearchMqConstants.RK_BOOK_UPSERT,
                new BookUpsertMessage(
                        UUID.randomUUID().toString(),
                        new BookUpsertMessage.BookPayload(
                                book.getIsbn(),
                                book.getTitle(),
                                book.getAuthor(),
                                book.getPublisher(),
                                book.getDescription(),
                                book.getPubDate(),
                                book.getPrice(),
                                book.getImageUrl()
                        ),
                        System.currentTimeMillis(),
                        reason
                )
        );
    }

    // 단일 도서 ES 삭제 발행
    public void publishBookDelete(String isbn, String reason) {
        rabbitTemplate.convertAndSend(
                BookSearchMqConstants.EXCHANGE,
                BookSearchMqConstants.RK_BOOK_DELETE,
                new BookDeleteMessage(UUID.randomUUID().toString(), isbn, System.currentTimeMillis(), reason)
        );
    }

}
