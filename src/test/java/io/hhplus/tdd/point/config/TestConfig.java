package io.hhplus.tdd.point.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;


@TestConfiguration
public class TestConfig {

    // * RestClient 빈을 생성합니다.
    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }


}
