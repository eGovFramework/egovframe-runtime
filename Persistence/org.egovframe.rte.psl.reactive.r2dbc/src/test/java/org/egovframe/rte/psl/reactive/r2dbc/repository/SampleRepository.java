package org.egovframe.rte.psl.reactive.r2dbc.repository;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;

import static org.springframework.data.r2dbc.query.Criteria.where;

@Repository
public class SampleRepository extends EgovR2dbcRepository<Sample> {

    @Resource(name="connectionFactory")
    private ConnectionFactory connectionFactory;

    public SampleRepository(ConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    public Flux<Sample> selectAllSample() {
        return selectAllData(Query.empty(), Sample.class);
    }

    public Mono<Sample> selectOneSample(int id) {
        return selectOneData(Query.query(where("id").is(id)), Sample.class);
    }

    public Mono<Long> countSample() {
        return countData(Query.empty(), Sample.class);
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
