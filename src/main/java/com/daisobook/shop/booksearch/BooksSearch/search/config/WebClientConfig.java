package com.daisobook.shop.booksearch.BooksSearch.search.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        ConnectionProvider provider = ConnectionProvider.builder("custom-provider")
                .maxConnections(20) // 학교 서버 보호를 위해 '동시 진입'은 제한 (이건 유지해야 함!)
                .pendingAcquireMaxCount(1000) // 대기열을 아주 넉넉하게 (많이 줄 서도 됨)
                .pendingAcquireTimeout(Duration.ofMinutes(3)) // 대기 시간 3분 (줄 서다 포기하지 않게)
                .maxIdleTime(Duration.ofSeconds(30)) // 유휴 커넥션 정리
                .lifo()
                .build();

        HttpClient httpClient = HttpClient.create(provider)
                // 연결 시도 타임아웃 (서버가 꺼진 경우엔 10초면 충분)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)

                // [핵심 변경] 응답 타임아웃: 5분 (300초)
                // 리랭킹이 1~2분 걸려도 절대 끊지 않고 기다려줍니다.
                .responseTimeout(Duration.ofMinutes(5))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(300, TimeUnit.SECONDS)) // 읽기 5분
                                .addHandlerLast(new WriteTimeoutHandler(300, TimeUnit.SECONDS)) // 쓰기 5분
                );

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(20 * 1024 * 1024)) // 메모리도 넉넉히 (20MB)
                .build();
    }
}