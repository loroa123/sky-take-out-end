package com.sky;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement // 开启注解方式的事务管理
@Slf4j // Lombok注解，自动生成log对象
@EnableCaching // 开启缓存注解功能
@EnableScheduling // 开启任务调度
public class SkyApplication {
    private static final Logger logger = LoggerFactory.getLogger(SkyApplication.class);
    public static void main(String[] args) {
        SpringApplication.run(SkyApplication.class, args);
        // 无需手动创建Logger对象，直接使用Lombok生成的log对象记录日志
        logger.info("server starte");
    }
}