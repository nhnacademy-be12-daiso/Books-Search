package com.daisobook.shop.booksearch.books_search.config;

import com.daisobook.shop.booksearch.books_search.saga.SagaTopic;
import com.daisobook.shop.booksearch.books_search.saga.todelete.SagaTopic2;
import jakarta.annotation.PostConstruct;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class RabbitMqConfig {

    // Orchestration 용

    @Bean
    public TopicExchange sagaExchange() {
        return new TopicExchange(SagaTopic.ORDER_EXCHANGE);
    }


    @Bean
    public Queue bookQueue() {
        return new Queue(SagaTopic.BOOK_QUEUE);
    }

    @Bean
    public Queue bookRollbackQueue() {
        return new Queue(SagaTopic.BOOK_COMPENSATION_QUEUE);
    }

    @Bean
    public Binding bookBinding(Queue bookQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(bookQueue)
                .to(sagaExchange)
                .with(SagaTopic.BOOK_RK);
    }

    @Bean
    public Binding bookRollbackBinding(Queue bookRollbackQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(bookRollbackQueue)
                .to(sagaExchange)
                .with(SagaTopic.BOOK_COMPENSATION_RK);
    }



    // ==================




    // ---- 초기 설정 ------
    @Value("${spring.profiles.active}")
    private String activeProfile;

    @PostConstruct
    public void init() {
        boolean isDev = "dev".equalsIgnoreCase(activeProfile);
        SagaTopic2.setMode(isDev);
    }
    @Bean("Saga")
    public Map<String, SagaTopic2> sagaTopics() {
        return Arrays.stream(SagaTopic2.values())
                .collect(Collectors.toMap(Enum::name, topic -> topic));
    }

    // 초기 설정 완료되면 이거 필요 없음
    private static final String ORDER_EXCHANGE = "team3.saga.order.exchange";
    @Value("${rabbitmq.queue.book}")
    private String BOOK_QUEUE;

    @Value("${rabbitmq.routing.confirmed}")
    private String ROUTING_KEY_CONFIRMED;

    private static final String BOOK_EXCHANGE = "team3.saga.book.exchange";


    // 발신되는 쪽 Exchange
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE);
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
    public Binding bindingOrderConfirmed(Queue bookInventoryQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(bookInventoryQueue)
                .to(orderExchange)
                .with(ROUTING_KEY_CONFIRMED);
    }

    // Book이 사용할 Exchange
    @Bean
    public DirectExchange bookExchange() {
        return new DirectExchange(BOOK_EXCHANGE);
    }

    // 수신용 컨버터
    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        return converter;
    }

    // 발신용 컨버터
    @Bean
    public MessageConverter simpleMessageConverter() {
        // String, byte[], Serializable 객체를 처리하는 기본 컨버터
        return new SimpleMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(simpleMessageConverter());

        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter()); // 수신은 JSON 컨버터로!
        return factory;
    }

}
