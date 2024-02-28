package org.egovframe.rte.psl.reactive.redis.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class SampleRepository extends EgovRedisRepository<Sample> {

    public SampleRepository(
            @Qualifier("reactiveRedisConnectionFactory") ReactiveRedisConnectionFactory connectionFactory,
            @Qualifier("sampleSerializationContext") RedisSerializationContext serializationContext)
    {
        super(connectionFactory, serializationContext);
    }

    public Flux<Sample> selectAllSample() {
        return selectData("sample");
    }

    public Mono<Sample> selectOneSample(String id) {
        return this.selectAllSample().filter(v->v.getId().equals(id)).last();
    }

    public Mono<Long> countSample() {
        return countData("sample");
    }

    public Mono<Boolean> insertSample(Sample sample) {
        return insertData("sample", sample).hasElement();
    }

    public Mono<Sample> updateSample(Sample sample) {
        return findIndex("sample", sample).flatMap(idx->updateData("sample", idx, sample));
    }

    public Mono<Boolean> deleteAllSample() {
        return deleteAllData("sample").hasElement();
    }

    public Mono<Boolean> deleteSample(Sample sample) {
        return deleteData("sample", sample).hasElement();
    }

}
