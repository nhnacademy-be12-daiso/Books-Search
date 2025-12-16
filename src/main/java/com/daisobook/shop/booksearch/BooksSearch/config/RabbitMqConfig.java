package com.daisobook.shop.booksearch.BooksSearch.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    private static final String ORDER_EXCHANGE = "team3.order.exchange";
    @Value("${rabbitmq.queue.book}")
    private String BOOK_QUEUE;
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
        return QueueBuilder.durable(BOOK_QUEUE)
                .withArgument("x-dead-letter-exchange", "team3.book.dlx") // 큐에서 문제가 생기면 해당 DLX로 보냄
                .withArgument("x-dead-letter-routing-key", "fail.book")
                .build();
    }

    // exchange랑 queue를 연결함
    @Bean
    public Binding bindingOrderConfirmed(Queue bookInventoryQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(bookInventoryQueue)
                .to(orderExchange)
                .with(ROUTING_KEY_CONFIRMED);
    }

    // Book이 사용할 Exchange
    @Bean
    public TopicExchange bookExchange() {
        return new TopicExchange(BOOK_EXCHANGE);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 3. RabbitTemplate 설정
     * 위에서 만든 JSON 변환기를 템플릿에 끼워줍니다.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());

        return rabbitTemplate;
    }


}
