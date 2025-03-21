package com.devcoop.kiosk.global.config;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

import com.devcoop.kiosk.global.utils.security.PaymentApiKeyGenerator;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {
    
    @Value("${payment.api.url}")
    private String paymentApiUrl;
    
    private final PaymentApiKeyGenerator apiKeyGenerator;
    
    public WebClientConfig(PaymentApiKeyGenerator apiKeyGenerator) {
        this.apiKeyGenerator = apiKeyGenerator;
    }
    
    @Bean
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .responseTimeout(Duration.ofSeconds(30))
            .doOnConnected(conn -> 
                conn.addHandlerLast(new ReadTimeoutHandler(40, TimeUnit.SECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(40, TimeUnit.SECONDS))
            );

        return WebClient.builder()
            .baseUrl(paymentApiUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .filter((request, next) -> {
                ClientRequest filteredRequest = ClientRequest.from(request)
                    .header("X-API-Key", apiKeyGenerator.generateApiKey())
                    .build();
                return next.exchange(filteredRequest);
            })
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }
}