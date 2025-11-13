package com.kuit.findyou.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ThreadPoolConfig {

    @Bean
    public ExecutorService statisticsExecutor() {
        // 스레드를 필요한 만큼만 지정
        return Executors.newFixedThreadPool(10);
    }
}