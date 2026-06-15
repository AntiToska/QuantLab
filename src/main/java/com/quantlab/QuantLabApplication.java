package com.quantlab;

import com.quantlab.common.config.QuantLabProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(QuantLabProperties.class)
public class QuantLabApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuantLabApplication.class, args);
    }
}
