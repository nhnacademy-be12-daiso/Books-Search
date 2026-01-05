package com.daisobook.shop.booksearch.books_search.search.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// BookSearchRabbitMqConfig.java 수정

import com.fasterxml.jackson.databind.SerializationFeature; // 추가
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // 추가

@Configuration
public class BookSearchRabbitMqConfig {

    @Value("${rabbitmq.exchange.main}")
    private String BOOKSEARCH_EXCHANGE;

    @Bean
    public DirectExchange bookSearchExchange() {
        return new DirectExchange(BOOKSEARCH_EXCHANGE);
    }

    @Bean(name = "bookSearchRabbitTemplate")
    public RabbitTemplate bookSearchRabbitTemplate(
            ConnectionFactory connectionFactory
            // ObjectMapper 주입 제거하고 직접 생성하거나, 주입받은 것에 모듈 추가
    ) {
        // 1. ObjectMapper를 직접 설정하여 날짜 모듈 추가
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        converter.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);

        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);

        // 2. Mandatory 설정 (라우팅 실패 시 리턴)
        template.setMandatory(true);
        template.setReturnsCallback(returned -> {
            System.err.println("[RabbitMQ RETURN] 반환됨 (라우팅 키 오류?)");
            System.err.println("ReplyCode: " + returned.getReplyCode());
            System.err.println("RoutingKey: " + returned.getRoutingKey());
        });

        return template;
    }
}
