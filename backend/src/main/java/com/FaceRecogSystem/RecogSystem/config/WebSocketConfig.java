package com.FaceRecogSystem.RecogSystem.config;

import com.FaceRecogSystem.RecogSystem.websocket.FaceWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private FaceWebSocketHandler faceWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(faceWebSocketHandler, "/ws/face")
                .setAllowedOrigins("*");
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean c = new ServletServerContainerFactoryBean();
        c.setMaxTextMessageBufferSize(1024 * 1024);
        c.setMaxBinaryMessageBufferSize(5 * 1024 * 1024);
        c.setMaxSessionIdleTimeout(300000L);
        return c;
    }
}
