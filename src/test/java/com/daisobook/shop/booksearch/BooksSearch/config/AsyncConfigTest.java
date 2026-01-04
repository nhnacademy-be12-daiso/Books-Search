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
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

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

        latch.await(2, TimeUnit.SECONDS);

        // Then
        assertThat(threadName[0]).startsWith("CustomTaskExecutor-");
    }

    @Test
    @DisplayName("스레드 풀 용량을 초과하는 부하 상황에서 CallerRunsPolicy에 의해 메인 스레드가 작업을 수행하는지 확인")
    void callerRunsPolicy_Test() throws InterruptedException {
        // 1. Thread.sleep() 대신 작업 지연을 위한 비즈니스 로직 시뮬레이션 (CountDownLatch 활용)
        int totalTasks = 30;
        CountDownLatch taskLatch = new CountDownLatch(totalTasks);
        CountDownLatch blockLatch = new CountDownLatch(1); // 큐를 꽉 채우기 위해 스레드를 붙잡아두는 용도

        String mainThreadName = Thread.currentThread().getName();
        final AtomicBoolean mainThreadRanTask = new AtomicBoolean(false);

        // 2. 여러 작업을 실행하여 스레드 풀과 큐를 가득 채움
        for (int i = 0; i < totalTasks; i++) {
            executor.execute(() -> {
                try {
                    // 현재 실행 중인 스레드가 메인 스레드인지 확인
                    if (Thread.currentThread().getName().equals(mainThreadName)) {
                        mainThreadRanTask.set(true);
                    } else {
                        // 메인 스레드가 아닌 풀 스레드들은 blockLatch가 열릴 때까지 대기하여 큐를 채움
                        blockLatch.await(500, TimeUnit.MILLISECONDS);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    taskLatch.countDown();
                }
            });
        }

        // 3. 작업이 처리되도록 대기 (blockLatch를 열어 묶여있던 스레드들 해제)
        blockLatch.countDown();

        // 4. Awaitility를 사용하여 메인 스레드 실행 여부 검증 (Thread.sleep 제거)
        await().atMost(5, TimeUnit.SECONDS)
                .untilTrue(mainThreadRanTask);

        // 5. 최종 상태 검증
        assertThat(mainThreadRanTask.get()).as("메인 스레드가 작업을 직접 수행해야 함").isTrue();
    }
}