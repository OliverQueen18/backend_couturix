package com.couturix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@ConfigurationPropertiesScan
public class CouturiXApplication {

    public static void main(String[] args) {
        SpringApplication.run(CouturiXApplication.class, args);
    }
}
