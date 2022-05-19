package org.freaknet.elibus.dnstls.proxy.configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ExecutorServiceConfig {


    @Value("${dnstls.udp.threads:8}")
    private Integer threads;
    
    /**
     * Executor service to service UDP requests from a fixed thread pool.
     * 
     * @return a ExecutorService.
     */
    @Bean
    @Primary
    public ExecutorService fixedThreadPool() {
        return Executors.newFixedThreadPool(this.threads);
    }
}