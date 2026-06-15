package com.quantlab.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 基础配置。
 * <p>
 * 当前项目还没有引入 web/json starter，因此这里显式提供 ObjectMapper，
 * 供交易所消息解析组件复用。
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
