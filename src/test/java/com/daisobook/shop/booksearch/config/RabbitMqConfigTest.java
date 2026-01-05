package com.daisobook.shop.booksearch.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = RabbitMqConfig.class)
@ActiveProfiles("test")
class RabbitMqConfigTest {

    @Autowired
    private ApplicationContext context;

    @MockitoBean
    private ConnectionFactory connectionFactory; // 브로커 연결 차단용 모킹

    @Test
    @DisplayName("1. Orchestration용 Saga 빈 등록 검증")
    void saga_Orchestration_Beans_Test() {
        // Exchange
        TopicExchange sagaExchange = context.getBean("sagaExchange", TopicExchange.class);
        assertThat(sagaExchange.getName()).isNotNull();

        // Queues
        assertThat(context.getBean("bookQueue", Queue.class)).isNotNull();
        assertThat(context.getBean("bookRollbackQueue", Queue.class)).isNotNull();

        // Bindings
        assertThat(context.getBean("bookBinding", Binding.class).getDestination()).isEqualTo(context.getBean("bookQueue", Queue.class).getName());
        assertThat(context.getBean("bookRollbackBinding", Binding.class).getRoutingKey()).isNotNull();
    }

    @Test
    @DisplayName("2. 초기 설정(@PostConstruct) 및 SagaTopic2 맵 검증")
    void init_And_SagaTopics_Map_Test() {
        // @Bean("Saga") Map 검증
        Map<String, Object> sagaTopics = (Map<String, Object>) context.getBean("Saga");
        assertThat(sagaTopics).isNotEmpty();
        System.out.println("Registered SagaTopics Count: " + sagaTopics.size());
    }

    @Test
    @DisplayName("3. 발신용 Direct Exchanges 검증")
    void direct_Exchanges_Test() {
        DirectExchange orderExchange = context.getBean("orderExchange", DirectExchange.class);
        DirectExchange bookExchange = context.getBean("bookExchange", DirectExchange.class);

        assertThat(orderExchange.getName()).isEqualTo("team3.saga.order.exchange");
        assertThat(bookExchange.getName()).isEqualTo("team3.saga.book.exchange");
    }

    @Test
    @DisplayName("4. DLX 및 Durable 설정이 포함된 수신 큐 검증")
    void bookInventoryQueue_Detail_Test() {
        Queue inventoryQueue = context.getBean("bookInventoryQueue", Queue.class);
        
        assertThat(inventoryQueue.isDurable()).isTrue();
        assertThat(inventoryQueue.getArguments()).containsEntry("x-dead-letter-exchange", "team3.book.dlx");
        assertThat(inventoryQueue.getArguments()).containsEntry("x-dead-letter-routing-key", "fail.book");
    }

    @Test
    @DisplayName("5. RabbitTemplate 및 컨버터 설정 검증")
    void rabbit_Infrastructure_Test() {
        // RabbitTemplate & 발신 컨버터 (Simple)
        RabbitTemplate rabbitTemplate = context.getBean(RabbitTemplate.class);
        assertThat(rabbitTemplate.getMessageConverter()).isInstanceOf(SimpleMessageConverter.class);

        // 리스너 팩토리의 컨버터를 확인하기 위해 필드 접근이 필요할 수 있으나, 빈 존재 여부로 우선 확인
        MessageConverter jsonConverter = context.getBean("jsonMessageConverter", MessageConverter.class);
        assertThat(jsonConverter).isInstanceOf(Jackson2JsonMessageConverter.class);
    }

    @Test
    @DisplayName("6. 주문 확인 바인딩(Binding) 검증")
    void order_Confirmed_Binding_Test() {
        Binding binding = context.getBean("bindingOrderConfirmed", Binding.class);
        assertThat(binding.getExchange()).isEqualTo("team3.saga.order.exchange");
        assertThat(binding.isDestinationQueue()).isTrue();
    }
}