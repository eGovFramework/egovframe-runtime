/*
 * Copyright 2008-2024 MOIS(Ministry of the Interior and Safety).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.egovframe.rte.psl.reactive.redis.connect;

import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.time.Duration;

/**
 * Redis 데이터베이스 연동 클래스
 *
 * <p>Desc.: Redis 데이터베이스 연동 클래스</p>
 *
 * @author 유지보수
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2023.08.31   유지보수            최초 생성
 * </pre>
 * @since 2023.08.31
 */
public class EgovRedisConfiguration {

    private final String host;
    private final int port;
    private final String password;
    private final boolean useSsl;
    private final Duration connectTimeout;
    private final Duration commandTimeout;
    private final int maxActive;
    private final int maxIdle;
    private final int minIdle;

    /**
     * EgovRedisConfiguration 생성자 (기본 설정)
     *
     * @param host Redis 서버 호스트
     * @param port Redis 서버 포트
     * @param password Redis 서버 비밀번호
     */
    public EgovRedisConfiguration(String host, int port, String password) {
        this(host, port, password, false, Duration.ofSeconds(10), Duration.ofSeconds(5), 8, 8, 0);
    }

    /**
     * EgovRedisConfiguration 생성자 (전체 설정)
     *
     * @param host Redis 서버 호스트
     * @param port Redis 서버 포트
     * @param password Redis 서버 비밀번호
     * @param useSsl SSL 사용 여부
     * @param connectTimeout 연결 타임아웃
     * @param commandTimeout 명령 타임아웃
     * @param maxActive 최대 활성 연결 수
     * @param maxIdle 최대 유휴 연결 수
     * @param minIdle 최소 유휴 연결 수
     */
    public EgovRedisConfiguration(String host, int port, String password, boolean useSsl, 
                                 Duration connectTimeout, Duration commandTimeout,
                                 int maxActive, int maxIdle, int minIdle) {
        if (host == null || host.trim().isEmpty()) {
            throw new IllegalArgumentException("Host cannot be null or empty");
        }
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
        if (connectTimeout == null || connectTimeout.isNegative()) {
            throw new IllegalArgumentException("Connect timeout cannot be null or negative");
        }
        if (commandTimeout == null || commandTimeout.isNegative()) {
            throw new IllegalArgumentException("Command timeout cannot be null or negative");
        }
        if (maxActive <= 0) {
            throw new IllegalArgumentException("Max active connections must be positive");
        }
        if (maxIdle < 0) {
            throw new IllegalArgumentException("Max idle connections cannot be negative");
        }
        if (minIdle < 0) {
            throw new IllegalArgumentException("Min idle connections cannot be negative");
        }
        
        this.host = host.trim();
        this.port = port;
        this.password = password;
        this.useSsl = useSsl;
        this.connectTimeout = connectTimeout;
        this.commandTimeout = commandTimeout;
        this.maxActive = maxActive;
        this.maxIdle = maxIdle;
        this.minIdle = minIdle;
    }

    /**
     * ReactiveRedisConnectionFactory를 생성합니다.
     *
     * @return ReactiveRedisConnectionFactory 인스턴스
     */
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(host);
        redisStandaloneConfiguration.setPort(port);
        if (password != null && !password.trim().isEmpty()) {
            redisStandaloneConfiguration.setPassword(password);
        }
        
        // Lettuce 클라이언트 설정
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(commandTimeout)
                .shutdownTimeout(Duration.ofSeconds(2))
                .build();
        
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(redisStandaloneConfiguration, clientConfig);
        connectionFactory.setValidateConnection(true);
        connectionFactory.setShareNativeConnection(false);
        
        return connectionFactory;
    }

    /**
     * SSL을 사용하는 ReactiveRedisConnectionFactory를 생성합니다.
     *
     * @return SSL이 활성화된 ReactiveRedisConnectionFactory 인스턴스
     */
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactoryWithSsl() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(host);
        redisStandaloneConfiguration.setPort(port);
        if (password != null && !password.trim().isEmpty()) {
            redisStandaloneConfiguration.setPassword(password);
        }
        
        // Lettuce 클라이언트 설정 (SSL 포함)
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(commandTimeout)
                .shutdownTimeout(Duration.ofSeconds(2))
                .useSsl()
                .build();
        
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(redisStandaloneConfiguration, clientConfig);
        connectionFactory.setValidateConnection(true);
        connectionFactory.setShareNativeConnection(false);
        
        return connectionFactory;
    }

    /**
     * 현재 설정 정보를 반환합니다.
     *
     * @return 설정 정보 문자열
     */
    public String getConfigurationInfo() {
        return String.format("Redis Configuration - Host: %s, Port: %d, SSL: %s, MaxActive: %d, MaxIdle: %d, MinIdle: %d",
                host, port, useSsl, maxActive, maxIdle, minIdle);
    }

    // Getter 메서드들
    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean isUseSsl() {
        return useSsl;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public Duration getCommandTimeout() {
        return commandTimeout;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public int getMinIdle() {
        return minIdle;
    }

}
