package com.springtest.cookapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class CookApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CookApiApplication.class, args);
    }

}
