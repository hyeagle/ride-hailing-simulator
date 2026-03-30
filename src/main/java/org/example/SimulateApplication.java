package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 应用启动类
 * 
 * @author example
 */
@SpringBootApplication
public class SimulateApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimulateApplication.class, args);
        System.out.println("====================================");
        System.out.println("高德地图路线规划系统启动成功！");
        System.out.println("访问地址: http://localhost:8080");
        System.out.println("====================================");
    }
}
