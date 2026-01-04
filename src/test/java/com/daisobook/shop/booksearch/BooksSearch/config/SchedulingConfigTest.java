package com.daisobook.shop.booksearch.BooksSearch.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = SchedulingConfig.class)
class SchedulingConfigTest {

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("기본 스케줄러 또는 TaskScheduler 빈이 컨텍스트에 존재해야 한다")
    void taskScheduler_Detection_Test() {
        // 스프링 부트는 기본적으로 스케줄링을 위한 TaskScheduler 설정을 자동으로 시도합니다.
        // 특정 빈이 생성되었는지 확인하거나, 스케줄링 기능 자체가 활성화되었는지 체크합니다.
        String[] beanNames = context.getBeanNamesForType(ScheduledAnnotationBeanPostProcessor.class);

        assertThat(beanNames).isNotEmpty();
    }
}