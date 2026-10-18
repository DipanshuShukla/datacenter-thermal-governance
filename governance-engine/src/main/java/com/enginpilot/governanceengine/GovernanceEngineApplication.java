package com.enginpilot.governanceengine;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GovernanceEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(GovernanceEngineApplication.class, args);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }


}
