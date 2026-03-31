package org.egovframe.rte.psl.reactive.mongodb.repository;

import jakarta.annotation.Resource;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class SampleRepositoryReactive extends EgovReactiveMongoDbRepository<Sample> {

    @Resource(name = "reactiveMongoDatabaseFactory")
    private ReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory;

    public SampleRepositoryReactive(ReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory) {
        super(reactiveMongoDatabaseFactory);
        this.reactiveMongoDatabaseFactory = reactiveMongoDatabaseFactory;
    }

    public Flux<Sample> selectAllSample() {
        Query query = new Query();
        return selectAllData(query, Sample.class);
    }

    public Mono<Sample> selectOneSample(int id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(id));
        return selectOneData(query, Sample.class);
    }

    public Mono<Long> countSample(int id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(id));
        return countData(query, Sample.class);
    }

    public Mono<Sample> insertSample(Sample sample) {
        return insertData(sample);
    }

    public Mono<Sample> updateSample(Sample sample) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(sample.getId()));
        Update update = new Update();
        update.set("sample_id", sample.getSampleId())
                .set("name", sample.getName())
                .set("description", sample.getDescription())
                .set("use_yn", sample.getUseYn())
                .set("reg_user", sample.getRegUser());
        return updateData(query, update, Sample.class);
    }

    public Mono<Sample> deleteSample(Sample sample) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(sample.getId()));
        return deleteData(query, Sample.class);
    }

}
