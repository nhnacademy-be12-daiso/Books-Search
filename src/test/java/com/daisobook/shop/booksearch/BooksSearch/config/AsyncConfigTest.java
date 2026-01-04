package com.daisobook.shop.booksearch.BooksSearch.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = AsyncConfig.class) // 설정 파일만 로드하여 테스트 최적화
class AsyncConfigTest {

    @Autowired
    @Qualifier("CustomTaskExecutor")
    private Executor executor;

    @Test
    @DisplayName("CustomTaskExecutor 빈이 올바른 설정값으로 생성되는지 확인한다")
    void customTaskExecutor_Settings_Test() {
        // Given & When
        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;

        // Then
        assertThat(taskExecutor.getCorePoolSize()).isEqualTo(2);
        assertThat(taskExecutor.getMaxPoolSize()).isEqualTo(4);
        assertThat(taskExecutor.getThreadNamePrefix()).isEqualTo("CustomTaskExecutor-");
        
        // RejectedExecutionHandler가 CallerRunsPolicy인지 확인
        assertThat(taskExecutor.getThreadPoolExecutor().getRejectedExecutionHandler())
                .isInstanceOf(ThreadPoolExecutor.CallerRunsPolicy.class);
    }

    @Test
    @DisplayName("비동기 작업 실행 시 설정된 스레드 이름 접두사를 사용하는지 확인한다")
    void threadNamePrefix_Test() throws InterruptedException {
        // Given
        CountDownLatch latch = new CountDownLatch(1);
        final String[] threadName = new String[1];

        // When
        executor.execute(() -> {
            threadName[0] = Thread.currentThread().getName();
            latch.countDown();
        });

        boolean await = latch.await(2, TimeUnit.SECONDS);

        // Then
        assertThat(threadName[0]).startsWith("CustomTaskExecutor-");
    }

    @Test
    @DisplayName("스레드 풀 용량을 초과하는 부하 상황에서 CallerRunsPolicy에 의해 메인 스레드가 작업을 수행하는지 확인")
    void callerRunsPolicy_Test() throws InterruptedException {
        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;
        
        // 큐를 꽉 채우고 스레드 풀도 다 쓰게 만들기 위해 대기 작업 제출
        // Core(2) + Queue(20) + Max(2) = 총 24개 작업 이후 25번째부터는 Caller가 실행
        int totalTasks = 30; 
        CountDownLatch latch = new CountDownLatch(totalTasks);
        String mainThreadName = Thread.currentThread().getName();
        final boolean[] mainThreadRanTask = {false};

        for (int i = 0; i < totalTasks; i++) {
            executor.execute(() -> {
                try {
                    // 메인 스레드가 이 로직을 실행하는지 체크
                    if (Thread.currentThread().getName().equals(mainThreadName)) {
                        mainThreadRanTask[0] = true;
                    }
                    Thread.sleep(100); // 작업을 지연시켜 큐를 채움
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean await = latch.await(5, TimeUnit.SECONDS);

        // Then: 작업량이 많아 스레드가 거절당했을 때 메인 스레드가 대신 실행했어야 함
        assertThat(mainThreadRanTask[0]).isTrue();
    }
}