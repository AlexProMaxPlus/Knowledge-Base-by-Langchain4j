package com.zyx.consultant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.zyx.consultant.knowledge")
public class ConsultantApplication {

    /**
     * 应用启动入口：创建 Spring 容器并启动内置 Web 服务器。
     */
    public static void main(String[] args) {
        SpringApplication.run(ConsultantApplication.class, args);
    }
}
