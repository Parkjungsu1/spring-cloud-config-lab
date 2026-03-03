package com.server.springcloudconfiglab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@EnableConfigServer
@SpringBootApplication
public class SpringCloudConfigLabApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudConfigLabApplication.class, args);
    }

}
