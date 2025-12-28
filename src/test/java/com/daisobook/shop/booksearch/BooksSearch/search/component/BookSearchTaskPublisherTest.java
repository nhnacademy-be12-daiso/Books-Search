package com.daisobook.shop.booksearch.BooksSearch.search.component;

import com.daisobook.shop.booksearch.BooksSearch.search.domain.RabbitBook;
import com.daisobook.shop.booksearch.BooksSearch.search.message.BookDeleteMessage;
import com.daisobook.shop.booksearch.BooksSearch.search.message.BookUpsertMessage;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@SpringJUnitConfig(classes = BookSearchTaskPublisherTest.TestConfig.class)
@TestPropertySource(properties = {
        "rabbitmq.exchange.main=main-ex",
        "rabbitmq.routing.book-upsert=rk.book.upsert",
        "rabbitmq.routing.book-delete=rk.book.delete"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookSearchTaskPublisherTest {

    @Configuration
    static class TestConfig {
        @Bean
        BookSearchTaskPublisher bookSearchTaskPublisher(RabbitTemplate bookSearchRabbitTemplate) {
            return new BookSearchTaskPublisher(
                    bookSearchRabbitTemplate,
                    "main-ex",
                    "rk.book.upsert",
                    "rk.book.delete"
            );
        }
    }

    @MockitoBean(name = "bookSearchRabbitTemplate")
    RabbitTemplate rabbitTemplate;

    @Resource
    BookSearchTaskPublisher publisher;

    @Nested
    @DisplayName("Book Upsert 메시지 발행 테스트")
    class Upsert {

        @Test
        @DisplayName("publishBookUpsert: 예상된 Exchange와 RoutingKey로, 올바른 Payload가 전송된다")
        void publishBookUpsert_shouldSendToExpectedExchangeAndRoutingKey_withCorrectPayload() {
            // given
            RabbitBook rb = new RabbitBook();
            rb.setId(10L);
            rb.setIsbn("978-1-2345-6789-0");
            rb.setTitle("Title");
            rb.setAuthor("Author A, Author B");
            rb.setPublisher("Pub");
            rb.setDescription("Desc");
            rb.setPubDate(LocalDate.of(2025, 12, 28));
            rb.setPrice(15000);
            rb.setCategories(List.of("C1", "C2"));
            rb.setImageUrl("https://img/1.jpg");
            rb.setPublisherId(77L);
            rb.setCategoryId(99L);

            ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);

            // when
            publisher.publishBookUpsert(rb, "UNIT_TEST");

            // then
            verify(rabbitTemplate).convertAndSend(
                    org.mockito.ArgumentMatchers.eq("main-ex"),
                    org.mockito.ArgumentMatchers.eq("rk.book.upsert"),
                    payloadCaptor.capture()
            );

            Object payload = payloadCaptor.getValue();
            assertThat(payload)
                    .as("RabbitTemplate에 전달된 메시지 타입이 BookUpsertMessage여야 함")
                    .isInstanceOf(BookUpsertMessage.class);

            BookUpsertMessage msg = (BookUpsertMessage) payload;

            assertThat(msg.reason()).as("reason이 정확히 전달돼야 함").isEqualTo("UNIT_TEST");
            assertThat(msg.ts()).as("timestamp는 0보다 커야 함").isPositive();
            assertThat(msg.requestId()).as("requestId는 비어있으면 안 됨").isNotBlank();

            BookUpsertMessage.BookPayload b = msg.book();
            assertThat(b.isbn()).as("isbn 매핑").isEqualTo("978-1-2345-6789-0");
            assertThat(b.id()).as("id 매핑").isEqualTo(10L);
            assertThat(b.title()).as("title 매핑").isEqualTo("Title");
            assertThat(b.author()).as("author 매핑").isEqualTo("Author A, Author B");
            assertThat(b.publisher()).as("publisher 매핑").isEqualTo("Pub");
            assertThat(b.description()).as("description 매핑").isEqualTo("Desc");
            assertThat(b.pubDate()).as("pubDate 매핑").isEqualTo(LocalDate.of(2025, 12, 28));
            assertThat(b.price()).as("price 매핑").isEqualTo(15000);
            assertThat(b.categories()).as("categories 매핑").containsExactly("C1", "C2");
            assertThat(b.imageUrl()).as("imageUrl 매핑").isEqualTo("https://img/1.jpg");
            assertThat(b.publisherId()).as("publisherId 매핑").isEqualTo(77L);
            assertThat(b.categoryId()).as("categoryId 매핑").isEqualTo(99L);
        }
    }

    @Nested
    @DisplayName("Book Delete 메시지 발행 테스트")
    class Delete {
        @Test
        @DisplayName("publishBookDelete: 예상된 Exchange와 RoutingKey로, 올바른 Payload가 전송된다")
        void publishBookDelete_shouldSendToExpectedExchangeAndRoutingKey_withCorrectPayload() {
            // given
            ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);

            // when
            publisher.publishBookDelete("978-0-0000-0000-0", "DELETE_TEST");

            // then
            verify(rabbitTemplate).convertAndSend(
                    org.mockito.ArgumentMatchers.eq("main-ex"),
                    org.mockito.ArgumentMatchers.eq("rk.book.delete"),
                    payloadCaptor.capture()
            );

            Object payload = payloadCaptor.getValue();
            assertThat(payload)
                    .as("RabbitTemplate에 전달된 메시지 타입이 BookDeleteMessage여야 함")
                    .isInstanceOf(BookDeleteMessage.class);

            BookDeleteMessage msg = (BookDeleteMessage) payload;
            assertThat(msg.isbn()).as("isbn 전달").isEqualTo("978-0-0000-0000-0");
            assertThat(msg.reason()).as("reason 전달").isEqualTo("DELETE_TEST");
            assertThat(msg.ts()).as("timestamp는 0보다 커야 함").isPositive();
            assertThat(msg.requestId()).as("requestId는 비어있으면 안 됨").isNotBlank();
        }
    }
}
