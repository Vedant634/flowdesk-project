package com.flowdesk.flowdesk_backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.time.Duration;

@Configuration
@Slf4j
public class MLServiceConfig {

    @Bean
    public WebClient mlWebClient() {
        TcpClient tcpClient = TcpClient.create()
                .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .doOnConnected(connection ->
                        connection.addHandlerLast(new io.netty.handler.timeout.ReadTimeoutHandler(10))
                );

        return WebClient.builder()
                .baseUrl("http://localhost:5000")
                .clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                .build();
    }
}
