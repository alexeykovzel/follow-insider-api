package com.followinsider.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.followinsider.client.EdgarClient;
import com.followinsider.data.forms.refs.FormRefLoader;
import com.followinsider.data.forms.f345.OwnershipDocLoader;
import com.followinsider.data.forms.refs.FormType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AppBeanConfig {

    @Value("${trading.edgar_user_agent}")
    private String userAgent;

    @Bean
    public EdgarClient edgarClient() {
        return new EdgarClient(userAgent);
    }

    @Bean
    public FormRefLoader formRefLoader(EdgarClient edgarClient) {
        return new FormRefLoader(edgarClient, FormType.F4);
    }

    @Bean
    public OwnershipDocLoader ownershipDocLoader(EdgarClient edgarClient) {
        return new OwnershipDocLoader(edgarClient);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}