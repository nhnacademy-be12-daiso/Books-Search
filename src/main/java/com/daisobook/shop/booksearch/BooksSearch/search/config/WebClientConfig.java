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
        // 1. 커넥션 풀 축소 (학교 서버 보호용)
        // 학교 서버가 동시에 처리할 수 있는 양이 적으므로, 우리 쪽에서 미리 줄을 세워서 보냄
        ConnectionProvider provider = ConnectionProvider.builder("custom-provider")
                .maxConnections(20)       // 500 -> 20 (동시 요청 수를 현실적으로 줄임)
                .pendingAcquireMaxCount(50) // 대기열도 줄임
                .pendingAcquireTimeout(Duration.ofSeconds(45)) // 대기 시간
                .maxIdleTime(Duration.ofSeconds(20)) // 유휴 커넥션 빨리 정리
                .lifo() // 후입선출 (최근에 쓴 커넥션 재사용이 성능에 유리)
                .build();

        // 2. 타임아웃 현실화 (30초)
        HttpClient httpClient = HttpClient.create(provider)
                // 연결 타임아웃 (서버가 꺼져있을 때 빨리 알기 위함)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(30)) // 응답 30초 넘으면 에러 (5분은 너무 김)
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(30, TimeUnit.SECONDS))
                );

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }
}