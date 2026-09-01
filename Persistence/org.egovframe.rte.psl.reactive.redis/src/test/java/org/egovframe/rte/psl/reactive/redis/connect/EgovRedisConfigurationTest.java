package org.egovframe.rte.psl.reactive.redis.connect;

import io.lettuce.core.SocketOptions;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EgovRedisConfigurationTest {

    @Test
    public void reactiveRedisConnectionFactory_appliesConnectTimeout() {
        Duration customConnectTimeout = Duration.ofMillis(1234);
        EgovRedisConfiguration config = new EgovRedisConfiguration(
                "localhost", 6379, null, false,
                customConnectTimeout, Duration.ofSeconds(5), 8, 8, 0);

        LettuceConnectionFactory factory = (LettuceConnectionFactory) config.reactiveRedisConnectionFactory();
        LettuceClientConfiguration clientConfiguration = factory.getClientConfiguration();
        SocketOptions socketOptions = clientConfiguration.getClientOptions()
                .orElseThrow(() -> new AssertionError("ClientOptions must be set"))
                .getSocketOptions();

        assertEquals(customConnectTimeout, socketOptions.getConnectTimeout());
    }

    @Test
    public void reactiveRedisConnectionFactory_appliesPoolSettings() {
        EgovRedisConfiguration config = new EgovRedisConfiguration(
                "localhost", 6379, null, false,
                Duration.ofSeconds(10), Duration.ofSeconds(5), 20, 15, 5);

        LettuceConnectionFactory factory = (LettuceConnectionFactory) config.reactiveRedisConnectionFactory();
        LettuceClientConfiguration clientConfiguration = factory.getClientConfiguration();

        assertInstanceOf(LettucePoolingClientConfiguration.class, clientConfiguration);
        GenericObjectPoolConfig<?> poolConfig = ((LettucePoolingClientConfiguration) clientConfiguration).getPoolConfig();
        assertEquals(20, poolConfig.getMaxTotal());
        assertEquals(15, poolConfig.getMaxIdle());
        assertEquals(5, poolConfig.getMinIdle());
    }

    @Test
    public void reactiveRedisConnectionFactory_appliesUseSsl() {
        EgovRedisConfiguration config = new EgovRedisConfiguration(
                "localhost", 6379, null, true,
                Duration.ofSeconds(10), Duration.ofSeconds(5), 8, 8, 0);

        LettuceConnectionFactory factory = (LettuceConnectionFactory) config.reactiveRedisConnectionFactory();

        assertTrue(config.isUseSsl());
        assertTrue(factory.isUseSsl());
    }

    @Test
    public void reactiveRedisConnectionFactory_doesNotApplySslWhenUseSslIsFalse() {
        EgovRedisConfiguration config = new EgovRedisConfiguration(
                "localhost", 6379, null, false,
                Duration.ofSeconds(10), Duration.ofSeconds(5), 8, 8, 0);

        LettuceConnectionFactory factory = (LettuceConnectionFactory) config.reactiveRedisConnectionFactory();

        assertFalse(factory.isUseSsl());
    }

}
