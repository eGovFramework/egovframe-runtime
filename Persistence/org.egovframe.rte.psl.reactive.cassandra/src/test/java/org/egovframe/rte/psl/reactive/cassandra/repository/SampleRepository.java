package org.egovframe.rte.psl.reactive.cassandra.repository;

import org.springframework.data.cassandra.ReactiveSession;
import org.springframework.data.cassandra.core.query.Criteria;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;

@Repository
public class SampleRepository extends EgovCassandraRepository<Sample> {

    @Resource(name="reactiveSession")
    private ReactiveSession reactiveSession;

    public SampleRepository(ReactiveSession reactiveSession) {
        super(reactiveSession);
        this.reactiveSession = reactiveSession;
    }

    private final String PARTITION_KEY_NAME = "part01";

    public Flux<Sample> selectAllSample() {
        Query query = Query.empty();
        return selectAllData(query, Sample.class);
    }

    public Mono<Sample> selectOneSample(String sampleId) {
        return selectOneData(Query.query(
                Criteria.where("id").is(PARTITION_KEY_NAME),
                Criteria.where("sample_id").is(sampleId)
        ), Sample.class);
    }

    public Mono<Long> countSample(String sampleId) {
        return countData(Query.query(
                Criteria.where("id").is(PARTITION_KEY_NAME),
                Criteria.where("sample_id").is(sampleId)
        ), Sample.class);
    }

    public Mono<Sample> insertSample(Sample sample) {
        return insertData(sample);
    }

    public Mono<Sample> updateSample(Sample sample) {
        return updateData(sample);
    }

    public Mono<Sample> deleteSample(Sample sample) {
        return deleteData(sample);
    }

}
