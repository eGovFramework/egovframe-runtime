package org.egovframe.rte.psl.reactive.redis.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RedisConfiguration.class)
@Slf4j
public class RedisTest {

    @Autowired
    private SampleRepository repository;

    @Autowired
    @Qualifier("reactiveRedisConnectionFactory")
    private ReactiveRedisConnectionFactory connectionFactory;

    @Autowired
    @Qualifier("sampleSerializationContext")
    private RedisSerializationContext<String, Sample> serializationContext;

    private EgovRedisRepository<Sample> egovRedisRepository;

    @BeforeEach
    void setUp() {
        egovRedisRepository = new EgovRedisRepository<>(connectionFactory, serializationContext);
    }

    public Sample makeSample() {
        Sample sample = new Sample();
        sample.setId("1");
        sample.setSampleId("SAMPLE-00001");
        sample.setName("Runtime");
        sample.setDescription("Runtime Tool");
        sample.setUseYn("Y");
        sample.setRegUser("eGov");
        return sample;
    }

    public Sample makeSample2() {
        Sample sample = new Sample();
        sample.setId("2");
        sample.setSampleId("SAMPLE-00002");
        sample.setName("Test");
        sample.setDescription("Test Tool");
        sample.setUseYn("Y");
        sample.setRegUser("TestUser");
        return sample;
    }

    @Test
    public void crudTest() {
        Sample sample = makeSample();

        repository.deleteSample(sample).block();

        repository.insertSample(sample)
                .as(StepVerifier::create)
                .expectNextMatches(item -> {
                    log.debug("item >>> {}", item);
                    assertTrue(item);
                    return true;
                })
                .verifyComplete();
    }

    @Test
    public void testSelectData() {
        String testKey = "test:list:select";
        Sample sample1 = makeSample();
        Sample sample2 = makeSample2();

        // 데이터 삭제 후 테스트
        egovRedisRepository.deleteAllData(testKey).block();

        // 데이터 추가
        egovRedisRepository.insertData(testKey, sample1).block();
        egovRedisRepository.insertData(testKey, sample2).block();

        // 데이터 조회 테스트
        egovRedisRepository.selectData(testKey)
                .as(StepVerifier::create)
                .expectNextCount(2)
                .verifyComplete();

        // 정리
        egovRedisRepository.deleteAllData(testKey).block();
    }

    @Test
    public void testFindIndex() {
        String testKey = "test:list:findIndex";
        Sample sample1 = makeSample();
        Sample sample2 = makeSample2();

        // 데이터 삭제 후 테스트
        egovRedisRepository.deleteAllData(testKey).block();

        // 데이터 추가
        egovRedisRepository.insertData(testKey, sample1).block();
        egovRedisRepository.insertData(testKey, sample2).block();

        // 인덱스 찾기 테스트
        egovRedisRepository.findIndex(testKey, sample1)
                .as(StepVerifier::create)
                .expectNextMatches(index -> {
                    log.debug("Found index: {}", index);
                    return index >= 0;
                })
                .verifyComplete();

        // 정리
        egovRedisRepository.deleteAllData(testKey).block();
    }

    @Test
    public void testCountData() {
        String testKey = "test:list:count";
        Sample sample1 = makeSample();
        Sample sample2 = makeSample2();

        // 데이터 삭제 후 테스트
        egovRedisRepository.deleteAllData(testKey).block();

        // 데이터 추가
        egovRedisRepository.insertData(testKey, sample1).block();
        egovRedisRepository.insertData(testKey, sample2).block();

        // 개수 조회 테스트
        egovRedisRepository.countData(testKey)
                .as(StepVerifier::create)
                .expectNextMatches(count -> {
                    log.debug("Count: {}", count);
                    return count == 2;
                })
                .verifyComplete();

        // 정리
        egovRedisRepository.deleteAllData(testKey).block();
    }

    @Test
    public void testInsertData() {
        String testKey = "test:list:insert";
        Sample sample = makeSample();

        // 데이터 삭제 후 테스트
        egovRedisRepository.deleteAllData(testKey).block();

        // 데이터 추가 테스트
        egovRedisRepository.insertData(testKey, sample)
                .as(StepVerifier::create)
                .expectNextMatches(size -> {
                    log.debug("Inserted, new size: {}", size);
                    return size == 1;
                })
                .verifyComplete();

        // 정리
        egovRedisRepository.deleteAllData(testKey).block();
    }

    @Test
    public void testUpdateData() {
        String testKey = "test:list:update";
        Sample sample1 = makeSample();
        Sample sample2 = makeSample2();

        // 데이터 삭제 후 테스트
        egovRedisRepository.deleteAllData(testKey).block();

        // 데이터 추가
        egovRedisRepository.insertData(testKey, sample1).block();

        // 데이터 업데이트 테스트
        egovRedisRepository.updateData(testKey, 0, sample2)
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();

        // 정리
        egovRedisRepository.deleteAllData(testKey).block();
    }

    @Test
    public void testDeleteAllData() {
        String testKey = "test:list:deleteAll";
        Sample sample = makeSample();

        // 데이터 추가
        egovRedisRepository.insertData(testKey, sample).block();

        // 모든 데이터 삭제 테스트
        egovRedisRepository.deleteAllData(testKey)
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    public void testDeleteData() {
        String testKey = "test:list:delete";
        Sample sample1 = makeSample();
        Sample sample2 = makeSample2();

        // 데이터 삭제 후 테스트
        egovRedisRepository.deleteAllData(testKey).block();

        // 데이터 추가
        egovRedisRepository.insertData(testKey, sample1).block();
        egovRedisRepository.insertData(testKey, sample2).block();

        // 특정 데이터 삭제 테스트
        egovRedisRepository.deleteData(testKey, sample1)
                .as(StepVerifier::create)
                .expectNextMatches(count -> {
                    log.debug("Deleted count: {}", count);
                    return count >= 0;
                })
                .verifyComplete();

        // 정리
        egovRedisRepository.deleteAllData(testKey).block();
    }

    // ========== Value Operations Tests ==========

    @Test
    public void testSetValue() {
        String testKey = "test:value:set";
        Sample sample = makeSample();

        // 값 설정 테스트
        egovRedisRepository.setValue(testKey, sample)
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();

        // 정리
        egovRedisRepository.deleteKeyValue(testKey).block();
    }

    @Test
    public void testSetValueWithTimeout() {
        String testKey = "test:value:setTimeout";
        Sample sample = makeSample();
        Duration timeout = Duration.ofSeconds(5);

        // 값 설정 (만료시간 포함) 테스트
        egovRedisRepository.setValue(testKey, sample, timeout)
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();

        // 정리
        egovRedisRepository.deleteKeyValue(testKey).block();
    }

    @Test
    public void testSetValueIfAbsent() {
        String testKey = "test:value:setIfAbsent";
        Sample sample = makeSample();

        // 키가 없을 때 설정 테스트
        egovRedisRepository.setValueIfAbsent(testKey, sample)
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();

        // 이미 키가 있을 때 설정 테스트 (실패해야 함)
        egovRedisRepository.setValueIfAbsent(testKey, sample)
                .as(StepVerifier::create)
                .expectNext(false)
                .verifyComplete();

        // 정리
        egovRedisRepository.deleteKeyValue(testKey).block();
    }

    @Test
    public void testSetValueIfPresent() {
        String testKey = "test:value:setIfPresent";
        Sample sample1 = makeSample();
        Sample sample2 = makeSample2();

        // 키가 없을 때 설정 테스트 (실패해야 함)
        egovRedisRepository.setValueIfPresent(testKey, sample1)
                .as(StepVerifier::create)
                .expectNext(false)
                .verifyComplete();

        // 키 설정
        egovRedisRepository.setValue(testKey, sample1).block();

        // 키가 있을 때 설정 테스트
        egovRedisRepository.setValueIfPresent(testKey, sample2)
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();

        // 정리
        egovRedisRepository.deleteKeyValue(testKey).block();
    }

    @Test
    public void testGetValue() {
        String testKey = "test:value:get";
        Sample sample = makeSample();

        // 값 설정
        egovRedisRepository.setValue(testKey, sample).block();

        // 값 조회 테스트
        egovRedisRepository.getValue(testKey)
                .as(StepVerifier::create)
                .expectNextMatches(retrievedSample -> {
                    log.debug("Retrieved sample: {}", retrievedSample);
                    return sample.getId().equals(retrievedSample.getId());
                })
                .verifyComplete();

        // 정리
        egovRedisRepository.deleteKeyValue(testKey).block();
    }

    @Test
    public void testGetAndSetValue() {
        String testKey = "test:value:getAndSet";
        Sample sample1 = makeSample();
        Sample sample2 = makeSample2();

        // 값 설정
        egovRedisRepository.setValue(testKey, sample1).block();

        // 값 조회 후 새 값 설정 테스트
        egovRedisRepository.getAndSetValue(testKey, sample2)
                .as(StepVerifier::create)
                .expectNextMatches(oldSample -> {
                    log.debug("Old sample: {}", oldSample);
                    return sample1.getId().equals(oldSample.getId());
                })
                .verifyComplete();

        // 정리
        egovRedisRepository.deleteKeyValue(testKey).block();
    }

    @Test
    public void testIncrementValue() {
        String testKey = "test:value:increment";

        // 값 증가 테스트 (기본값 0에서 1로)
        egovRedisRepository.incrementValue(testKey)
                .as(StepVerifier::create)
                .expectNextMatches(value -> {
                    log.debug("Incremented value: {}", value);
                    return value == 1;
                })
                .verifyComplete();

        // 정리
        egovRedisRepository.deleteKeyValue(testKey).block();
    }

    @Test
    public void testIncrementValueWithDelta() {
        String testKey = "test:value:incrementDelta";
        long delta = 5;

        // 값 증가 테스트 (기본값 0에서 5로)
        egovRedisRepository.incrementValue(testKey, delta)
                .as(StepVerifier::create)
                .expectNextMatches(value -> {
                    log.debug("Incremented value: {}", value);
                    return value == delta;
                })
                .verifyComplete();

        // 정리
        egovRedisRepository.deleteKeyValue(testKey).block();
    }

    @Test
    public void testHasKeyValue() {
        String testKey = "test:key:hasKey";
        Sample sample = makeSample();

        // 키가 없을 때 테스트
        egovRedisRepository.hasKeyValue(testKey)
                .as(StepVerifier::create)
                .expectNext(false)
                .verifyComplete();

        // 값 설정
        egovRedisRepository.setValue(testKey, sample).block();

        // 키가 있을 때 테스트
        egovRedisRepository.hasKeyValue(testKey)
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();

        // 정리
        egovRedisRepository.deleteKeyValue(testKey).block();
    }

    @Test
    public void testDeleteKeyValue() {
        String testKey = "test:key:delete";
        Sample sample = makeSample();

        // 값 설정
        egovRedisRepository.setValue(testKey, sample).block();

        // 키 삭제 테스트
        egovRedisRepository.deleteKeyValue(testKey)
                .as(StepVerifier::create)
                .expectNextMatches(count -> {
                    log.debug("Deleted key count: {}", count);
                    return count == 1;
                })
                .verifyComplete();
    }

    @Test
    public void testExpireKeyValue() {
        String testKey = "test:key:expire";
        Sample sample = makeSample();
        Duration timeout = Duration.ofSeconds(10);

        // 값 설정
        egovRedisRepository.setValue(testKey, sample).block();

        // 만료시간 설정 테스트
        egovRedisRepository.expireKeyValue(testKey, timeout)
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();

        // 정리
        egovRedisRepository.deleteKeyValue(testKey).block();
    }

    @Test
    public void testGetExpireValue() {
        String testKey = "test:key:getExpire";
        Sample sample = makeSample();
        Duration timeout = Duration.ofSeconds(10);

        // 값 설정 (만료시간 포함)
        egovRedisRepository.setValue(testKey, sample, timeout).block();

        // 만료시간 조회 테스트
        egovRedisRepository.getExpireValue(testKey)
                .as(StepVerifier::create)
                .expectNextMatches(expire -> {
                    log.debug("Expire time: {}", expire);
                    return expire.getSeconds() > 0;
                })
                .verifyComplete();

        // 정리
        egovRedisRepository.deleteKeyValue(testKey).block();
    }

    @Test
    public void testPersistKeyValue() {
        String testKey = "test:key:persist";
        Sample sample = makeSample();
        Duration timeout = Duration.ofSeconds(10);

        // 값 설정 (만료시간 포함)
        egovRedisRepository.setValue(testKey, sample, timeout).block();

        // 만료시간 제거 테스트
        egovRedisRepository.persistKeyValue(testKey)
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();

        // 정리
        egovRedisRepository.deleteKeyValue(testKey).block();
    }

    // ========== Multi Operations Tests ==========

    @Test
    public void testGetMultiValue() {
        String testKey1 = "test:multi:key1";
        String testKey2 = "test:multi:key2";
        String testKey3 = "test:multi:key3";
        Sample sample1 = makeSample();
        Sample sample2 = makeSample2();

        // 값 설정
        egovRedisRepository.setValue(testKey1, sample1).block();
        egovRedisRepository.setValue(testKey2, sample2).block();
        // testKey3는 설정하지 않음

        // 여러 키의 값 조회 테스트
        egovRedisRepository.getMultiValue(testKey1, testKey2, testKey3)
                .as(StepVerifier::create)
                .expectNextMatches(values -> {
                    log.debug("Multi values: {}", values);
                    return values.size() == 3 && values.get(0) != null && values.get(1) != null && values.get(2) == null;
                })
                .verifyComplete();

        // 정리
        egovRedisRepository.deleteKeyValue(testKey1).block();
        egovRedisRepository.deleteKeyValue(testKey2).block();
    }

    @Test
    public void testSetMultiValue() {
        String testKey1 = "test:multi:set:key1";
        String testKey2 = "test:multi:set:key2";
        Sample sample1 = makeSample();
        Sample sample2 = makeSample2();

        Map<String, Sample> keyValueMap = new HashMap<>();
        keyValueMap.put(testKey1, sample1);
        keyValueMap.put(testKey2, sample2);

        // 여러 키-값 쌍 설정 테스트
        egovRedisRepository.setMultiValue(keyValueMap)
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();

        // 정리
        egovRedisRepository.deleteKeyValue(testKey1).block();
        egovRedisRepository.deleteKeyValue(testKey2).block();
    }

    @Test
    public void testSetMultiValueIfAbsent() {
        String testKey1 = "test:multi:setIfAbsent:key1";
        String testKey2 = "test:multi:setIfAbsent:key2";
        Sample sample1 = makeSample();
        Sample sample2 = makeSample2();

        Map<String, Sample> keyValueMap = new HashMap<>();
        keyValueMap.put(testKey1, sample1);
        keyValueMap.put(testKey2, sample2);

        // 여러 키-값 쌍 설정 테스트 (키가 없을 때만)
        egovRedisRepository.setMultiValueIfAbsent(keyValueMap)
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();

        // 이미 키가 있을 때 다시 설정 테스트 (실패해야 함)
        egovRedisRepository.setMultiValueIfAbsent(keyValueMap)
                .as(StepVerifier::create)
                .expectNext(false)
                .verifyComplete();

        // 정리
        egovRedisRepository.deleteKeyValue(testKey1).block();
        egovRedisRepository.deleteKeyValue(testKey2).block();
    }

}
