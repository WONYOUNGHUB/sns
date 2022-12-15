package com.study.sns.configuration;

import com.study.sns.model.User;
import io.lettuce.core.RedisURI;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableRedisRepositories
@RequiredArgsConstructor
public class RedisConfiguration {

    private final RedisProperties redisProperties;

    @Bean
    public RedisConnectionFactory redisConnectionFactory(){
        RedisURI redisURI  = RedisURI.create(redisProperties.getUrl());
        //연결방법은 jedis lettuce(최신)
        org.springframework.data.redis.connection.RedisConfiguration configuration = LettuceConnectionFactory.createRedisConfiguration(redisURI);
        LettuceConnectionFactory factory = new LettuceConnectionFactory(configuration);
        factory.afterPropertiesSet();
        return factory;

    }

     @Bean
    public RedisTemplate<String, User>userRedisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String,User>redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);//레디스 서버와연결에 필요한 정보를 날릴수있는 설정(connection factory)
        redisTemplate.setKeySerializer(new StringRedisSerializer());//키 시리얼라이즈
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<User>(User.class));

        return redisTemplate;
    }

}
