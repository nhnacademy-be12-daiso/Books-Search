package com.daisobook.shop.booksearch.BooksSearch.search.config;

import com.daisobook.shop.booksearch.BooksSearch.search.message.BookUpsertMessage;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
@Import(BookSearchRabbitMqConfig.class)
@TestPropertySource(properties = {
        "rabbitmq.exchange.main=main-ex"
})
class BookSearchRabbitMqConfigTest {

    @MockitoBean
    ConnectionFactory connectionFactory;

    @jakarta.annotation.Resource
    DirectExchange bookSearchExchange;

    @jakarta.annotation.Resource(name = "bookSearchRabbitTemplate")
    RabbitTemplate bookSearchRabbitTemplate;

    @Test
    void bookSearchExchange_shouldUseConfiguredName() {
        assertThat(bookSearchExchange.getName())
                .as("@Value로 주입된 exchange 이름이 그대로 사용되어야 함")
                .isEqualTo("main-ex");
    }

    @Test
    void rabbitTemplate_shouldUseJacksonConverter_withJavaTimeModuleConfigured() {
        assertThat(bookSearchRabbitTemplate.getMessageConverter())
                .as("RabbitTemplate의 messageConverter는 Jackson2JsonMessageConverter여야 함")
                .isInstanceOf(Jackson2JsonMessageConverter.class);

        Jackson2JsonMessageConverter converter =
                (Jackson2JsonMessageConverter) bookSearchRabbitTemplate.getMessageConverter();

        BookUpsertMessage msg = new BookUpsertMessage(
                "req-1",
                new BookUpsertMessage.BookPayload(
                        "ISBN",
                        1L,
                        "TITLE",
                        "AUTHOR",
                        "PUB",
                        "DESC",
                        LocalDate.of(2025, 12, 28),
                        1000,
                        List.of("C1"),
                        "IMG",
                        10L,
                        20L
                ),
                123L,
                "REASON"
        );

        Message amqpMsg = converter.toMessage(msg, new MessageProperties());
        String json = new String(amqpMsg.getBody(), StandardCharsets.UTF_8);

        assertThat(json)
                .as("LocalDate는 타임스탬프가 아니라 yyyy-MM-dd 문자열로 직렬화되어야 함")
                .contains("\"pubDate\":\"2025-12-28\"");
    }
}
