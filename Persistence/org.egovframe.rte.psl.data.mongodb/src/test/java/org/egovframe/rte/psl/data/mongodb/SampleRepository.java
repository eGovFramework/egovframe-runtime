package org.egovframe.rte.psl.data.mongodb;

import jakarta.annotation.Resource;
import org.egovframe.rte.psl.data.mongodb.repository.EgovMongoDbRepository;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SampleRepository extends EgovMongoDbRepository<Sample> {

    @Resource(name = "mongoDatabaseFactory")
    private MongoDatabaseFactory mongoDatabaseFactory;

    public SampleRepository(MongoDatabaseFactory mongoDatabaseFactory) {
        super(mongoDatabaseFactory);
        this.mongoDatabaseFactory = mongoDatabaseFactory;
    }

    public List<Sample> selectAllSample() {
        Query query = new Query();
        return selectAllData(query, Sample.class);
    }

    public Sample selectOneSample(int id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(id));
        return selectOneData(query, Sample.class);
    }

    public long countSample(int id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(id));
        return countData(query, Sample.class);
    }

    public Sample insertSample(Sample sample) {
        return insertData(sample);
    }

    public Sample updateSample(Sample sample) {
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

    public Sample deleteSample(Sample sample) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(sample.getId()));
        return deleteDate(query, Sample.class);
    }

}
