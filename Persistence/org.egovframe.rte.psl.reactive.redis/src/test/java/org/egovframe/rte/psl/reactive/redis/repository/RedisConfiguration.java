package org.egovframe.rte.psl.reactive.redis.repository;

import org.egovframe.rte.psl.reactive.redis.connect.EgovRedisConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.reactive.config.EnableWebFlux;

@Configuration
@EnableWebFlux
@ComponentScan(basePackages="org.egovframe.rte.psl.reactive.redis.repository")
public class RedisConfiguration {

    private String host = "localhost";
    private int port = 6379;

    @Bean(name="reactiveRedisConnectionFactory")
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
        EgovRedisConfiguration egovRedisConfiguration = new EgovRedisConfiguration(this.host, this.port);
        return egovRedisConfiguration.reactiveRedisConnectionFactory();
    }

    @Bean(name="sampleSerializationContext")
    public RedisSerializationContext<String, Sample> sampleSerializationContext() {
        Jackson2JsonRedisSerializer<Sample> serializer = new Jackson2JsonRedisSerializer<>(Sample.class);
        RedisSerializationContext.RedisSerializationContextBuilder<String, Sample> builder =
                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());
        return builder.value(serializer).hashValue(serializer).hashKey(serializer).build();
    }

}
