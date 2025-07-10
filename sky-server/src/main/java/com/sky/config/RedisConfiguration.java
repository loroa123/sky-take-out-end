package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfiguration {

    //由bean来注入RedisConnectionFactory
    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){
        log.info("开始创建redis模板对象...");
        RedisTemplate redisTemplate = new RedisTemplate();
        //设置redis的连接工厂对象，template关联上模版
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //设置redis key的序列化器。在里面显示的时候就能够数据库显示避免乱码。其实不用key就难以显示
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        return redisTemplate;
    }
}
