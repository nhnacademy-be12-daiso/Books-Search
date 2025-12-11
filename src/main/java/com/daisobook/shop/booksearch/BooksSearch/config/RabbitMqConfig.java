package com.daisobook.shop.booksearch.BooksSearch.config;

import ch.qos.logback.classic.pattern.MessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.hibernate.sql.model.ast.builder.ColumnValueBindingBuilder;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    private static final String ORDER_EXCHANGE = "team3.order.exchange";
    private static final String BOOK_QUEUE = "team3.order.confirmed.book.queue";
    private static final String ROUTING_KEY_CONFIRMED = "order.confirmed";

    private static final String BOOK_EXCHANGE = "team3.book.exchange";


    // 발신되는 쪽 Exchange
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    // book이 받아보는 큐
    @Bean
    Queue bookInventoryQueue() {
        return new Queue(BOOK_QUEUE, true); // durable:true ---> 서버 재시작해도 유지
    }

    // exchange랑 queue를 연결함
    @Bean
    public Binding bindingOrderConfirmed(Queue bookInventroyQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(bookInventroyQueue)
                .to(orderExchange)
                .with(ROUTING_KEY_CONFIRMED);
    }

    // Book이 사용할 Exchange
    @Bean
    public TopicExchange bookExchange() {
        return new TopicExchange(BOOK_EXCHANGE);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        // ObjectMapper 인스턴스를 직접 생성
        ObjectMapper objectMapper = new ObjectMapper();

        // 🌟 핵심: Java Time 모듈을 등록하여 Instant, ZonedDateTime 등을 올바르게 처리하도록 설정
        objectMapper.registerModule(new JavaTimeModule());

        // RabbitMQ 컨버터에 설정된 ObjectMapper를 주입
        return new Jackson2JsonMessageConverter(objectMapper);
    }

}
