package com.fn.game;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author fomin
 * @date 2019-12-08
 */
@ComponentScan("com.fn")
@SpringCloudApplication
public class ApiService {

    public static void main(String[] args) {
        SpringApplication.run(ApiService.class, args);
    }

}
