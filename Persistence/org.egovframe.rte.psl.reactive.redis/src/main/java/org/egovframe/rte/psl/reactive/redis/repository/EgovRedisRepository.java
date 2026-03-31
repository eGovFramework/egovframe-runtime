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
package org.egovframe.rte.psl.reactive.redis.repository;

import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Redis Repository 구현 클래스
 *
 * <p>Desc.: Redis Repository 구현 클래스</p>
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
public class EgovRedisRepository<T> extends ReactiveRedisTemplate<String, T> {

    /**
     * EgovRedisRepository 생성자
     *
     * @param connectionFactory Redis 연결 팩토리
     * @param serializationContext Redis 직렬화 컨텍스트
     */
    public EgovRedisRepository(ReactiveRedisConnectionFactory connectionFactory, RedisSerializationContext<String, T> serializationContext) {
        super(connectionFactory, serializationContext);
    }

    /**
     * Redis List에서 모든 데이터를 조회합니다.
     *
     * @param redisKey Redis 키
     * @return 저장된 모든 데이터 목록
     */
    public Flux<T> selectData(String redisKey) {
        return opsForList().range(redisKey, 0, -1);
    }

    /**
     * Redis List에서 특정 엔티티의 인덱스를 찾습니다.
     *
     * @param redisKey Redis 키
     * @param entity 찾을 엔티티
     * @return 엔티티의 인덱스 (없으면 -1)
     */
    public Mono<Long> findIndex(String redisKey, T entity) {
        return opsForList().indexOf(redisKey, entity);
    }

    /**
     * Redis List의 데이터 개수를 조회합니다.
     *
     * @param redisKey Redis 키
     * @return 데이터 개수
     */
    public Mono<Long> countData(String redisKey) {
        return opsForList().size(redisKey);
    }

    /**
     * Redis List의 왼쪽(앞쪽)에 데이터를 추가합니다.
     *
     * @param redisKey Redis 키
     * @param entity 추가할 엔티티
     * @return 추가 후 List의 크기
     */
    public Mono<Long> insertData(String redisKey, T entity) {
        return opsForList().leftPush(redisKey, entity);
    }

    /**
     * Redis List의 특정 인덱스에 데이터를 업데이트합니다.
     *
     * @param redisKey Redis 키
     * @param idx 업데이트할 인덱스
     * @param entity 새로운 엔티티
     * @return 업데이트 성공 여부
     */
    public Mono<Boolean> updateData(String redisKey, long idx, T entity) {
        return opsForList().set(redisKey, idx, entity);
    }

    /**
     * Redis List의 모든 데이터를 삭제합니다.
     *
     * @param redisKey Redis 키
     * @return 삭제 성공 여부
     */
    public Mono<Boolean> deleteAllData(String redisKey) {
        return opsForList().delete(redisKey);
    }

    /**
     * Redis List에서 특정 엔티티를 삭제합니다.
     *
     * @param redisKey Redis 키
     * @param entity 삭제할 엔티티
     * @return 삭제된 엔티티의 개수
     */
    public Mono<Long> deleteData(String redisKey, T entity) {
        return opsForList().remove(redisKey, 0, entity);
    }

    /**
     * Redis key에 값을 설정합니다.
     *
     * @param redisKey Redis 키
     * @param value 설정할 값
     * @return 설정 성공 여부
     */
    public Mono<Boolean> setValue(String redisKey, T value) {
        return opsForValue().set(redisKey, value);
    }

    /**
     * Redis key에 값을 설정하고 만료 시간을 지정합니다.
     *
     * @param redisKey Redis 키
     * @param value 설정할 값
     * @param timeout 만료 시간
     * @return 설정 성공 여부
     */
    public Mono<Boolean> setValue(String redisKey, T value, Duration timeout) {
        return opsForValue().set(redisKey, value, timeout);
    }

    /**
     * Redis key에 값이 없을 때만 설정합니다.
     *
     * @param redisKey Redis 키
     * @param value 설정할 값
     * @return 설정 성공 여부
     */
    public Mono<Boolean> setValueIfAbsent(String redisKey, T value) {
        return opsForValue().setIfAbsent(redisKey, value);
    }

    /**
     * Redis key에 값이 없을 때만 설정하고 만료 시간을 지정합니다.
     *
     * @param redisKey Redis 키
     * @param value 설정할 값
     * @param timeout 만료 시간
     * @return 설정 성공 여부
     */
    public Mono<Boolean> setValueIfAbsent(String redisKey, T value, Duration timeout) {
        return opsForValue().setIfAbsent(redisKey, value, timeout);
    }

    /**
     * Redis key에 값이 있을 때만 설정합니다.
     *
     * @param redisKey Redis 키
     * @param value 설정할 값
     * @return 설정 성공 여부
     */
    public Mono<Boolean> setValueIfPresent(String redisKey, T value) {
        return opsForValue().setIfPresent(redisKey, value);
    }

    /**
     * Redis key에 값이 있을 때만 설정하고 만료 시간을 지정합니다.
     *
     * @param redisKey Redis 키
     * @param value 설정할 값
     * @param timeout 만료 시간
     * @return 설정 성공 여부
     */
    public Mono<Boolean> setValueIfPresent(String redisKey, T value, Duration timeout) {
        return opsForValue().setIfPresent(redisKey, value, timeout);
    }

    /**
     * Redis key의 값을 가져옵니다.
     *
     * @param redisKey Redis 키
     * @return 저장된 값
     */
    public Mono<T> getValue(String redisKey) {
        return opsForValue().get(redisKey);
    }

    /**
     * Redis key의 값을 가져오고 새로운 값으로 설정합니다.
     *
     * @param redisKey Redis 키
     * @param newValue 새로운 값
     * @return 이전 값
     */
    public Mono<T> getAndSetValue(String redisKey, T newValue) {
        return opsForValue().getAndSet(redisKey, newValue);
    }

    /**
     * Redis key의 값을 1 증가시킵니다.
     *
     * @param redisKey Redis 키
     * @return 증가된 값
     */
    public Mono<Long> incrementValue(String redisKey) {
        return opsForValue().increment(redisKey);
    }

    /**
     * Redis key의 값을 지정된 값만큼 증가시킵니다.
     *
     * @param redisKey Redis 키
     * @param delta 증가시킬 값
     * @return 증가된 값
     */
    public Mono<Long> incrementValue(String redisKey, long delta) {
        return opsForValue().increment(redisKey, delta);
    }

    /**
     * Redis key의 값을 1 감소시킵니다.
     *
     * @param redisKey Redis 키
     * @return 감소된 값
     */
    public Mono<Long> decrementValue(String redisKey) {
        return opsForValue().decrement(redisKey);
    }

    /**
     * Redis key의 값을 지정된 값만큼 감소시킵니다.
     *
     * @param redisKey Redis 키
     * @param delta 감소시킬 값
     * @return 감소된 값
     */
    public Mono<Long> decrementValue(String redisKey, long delta) {
        return opsForValue().decrement(redisKey, delta);
    }

    /**
     * Redis key가 존재하는지 확인합니다.
     *
     * @param redisKey Redis 키
     * @return 존재 여부
     */
    public Mono<Boolean> hasKeyValue(String redisKey) {
        return hasKey(redisKey);
    }

    /**
     * Redis key를 삭제합니다.
     *
     * @param redisKey Redis 키
     * @return 삭제된 키의 개수
     */
    public Mono<Long> deleteKeyValue(String redisKey) {
        return delete(redisKey);
    }

    /**
     * Redis key의 만료 시간을 설정합니다.
     *
     * @param redisKey Redis 키
     * @param timeout 만료 시간
     * @return 설정 성공 여부
     */
    public Mono<Boolean> expireKeyValue(String redisKey, Duration timeout) {
        return expire(redisKey, timeout);
    }

    /**
     * Redis key의 남은 만료 시간을 조회합니다.
     *
     * @param redisKey Redis 키
     * @return 남은 만료 시간 (초)
     */
    public Mono<Duration> getExpireValue(String redisKey) {
        return getExpire(redisKey);
    }

    /**
     * Redis key의 만료 시간을 제거합니다 (영구 저장).
     *
     * @param redisKey Redis 키
     * @return 제거 성공 여부
     */
    public Mono<Boolean> persistKeyValue(String redisKey) {
        return persist(redisKey);
    }

    /**
     * 여러 Redis key의 값을 한 번에 가져옵니다.
     *
     * @param redisKeys Redis 키 목록
     * @return 값 목록
     */
    public Mono<java.util.List<T>> getMultiValue(String... redisKeys) {
        return opsForValue().multiGet(java.util.Arrays.asList(redisKeys));
    }

    /**
     * 여러 Redis key에 값을 한 번에 설정합니다.
     *
     * @param keyValueMap 키-값 쌍
     * @return 설정 성공 여부
     */
    public Mono<Boolean> setMultiValue(java.util.Map<String, T> keyValueMap) {
        return opsForValue().multiSet(keyValueMap);
    }

    /**
     * 여러 Redis key에 값이 없을 때만 한 번에 설정합니다.
     *
     * @param keyValueMap 키-값 쌍
     * @return 설정 성공 여부
     */
    public Mono<Boolean> setMultiValueIfAbsent(java.util.Map<String, T> keyValueMap) {
        return opsForValue().multiSetIfAbsent(keyValueMap);
    }

}
