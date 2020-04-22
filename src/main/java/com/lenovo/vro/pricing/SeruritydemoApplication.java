package com.lenovo.vro.pricing;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lenovo.vro.pricing.mapper")
public class SeruritydemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeruritydemoApplication.class, args);
    }

}
