package com.daisobook.shop.booksearch.books_search.config;

import com.daisobook.shop.booksearch.books_search.client.UserApiClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = FeignConfig.class)
@ActiveProfiles("test")
// 핵심: 테스트 시 데이터베이스 관련 자동 설정을 제외하여 DDL 에러를 방지합니다.
@ImportAutoConfiguration(exclude = {
        FeignAutoConfiguration.class,
        DataSourceAutoConfiguration.class,// LoadBalancer 관련
        HibernateJpaAutoConfiguration.class// LoadBalancer 핵심
})
// 테스트 시 RabbitMQ 자동 설정을 완전히 제외합니다.
@EnableAutoConfiguration(
        exclude = {RabbitAutoConfiguration.class}
)
class FeignConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("EnableFeignClients 설정에 의해 UserApiClient 인터페이스가 빈으로 등록되어야 한다")
    void feignClients_Scanning_Test() {
        // 빈이 존재하는지 확인
        String[] beanNames = applicationContext.getBeanNamesForType(UserApiClient.class);

        assertThat(beanNames).isNotEmpty();
    }
}