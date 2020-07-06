package com.gblfy.websocket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * 开启websocket的支持
 *
 * @Author gblfy
 * @Email gbly02@gmail.com
 * @Date 2019/11/20 PM 23:50
 */
@Configuration  
public class WebSocketConfig {  
    @Bean  
    public ServerEndpointExporter serverEndpointExporter(){  
        return new ServerEndpointExporter();  
    }  
}  
