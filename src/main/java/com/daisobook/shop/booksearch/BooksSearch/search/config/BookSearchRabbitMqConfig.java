package com.daisobook.shop.booksearch.BooksSearch.search.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BookSearchRabbitMqConfig {

    @Value("${rabbitmq.exchange.main}")
    public String BOOKSEARCH_EXCHANGE;

    @Value("${rabbitmq.routing.ai-analysis}")
    public String RK_AI_ANALYSIS;

    @Value("${rabbitmq.routing.book-upsert}")
    public String RK_BOOK_UPSERT;

    @Value("${rabbitmq.routing.book-delete}")
    public String RK_BOOK_DELETE;

    @Bean
    public DirectExchange bookSearchExchange() {
        return new DirectExchange(BOOKSEARCH_EXCHANGE);
    }

    @Bean(name = "bookSearchRabbitTemplate")
    public RabbitTemplate bookSearchRabbitTemplate(ConnectionFactory connectionFactory) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);

        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
