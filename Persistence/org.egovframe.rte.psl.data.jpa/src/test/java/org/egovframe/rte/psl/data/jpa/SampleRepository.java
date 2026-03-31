package org.egovframe.rte.psl.data.jpa;

import org.egovframe.rte.psl.data.jpa.repository.EgovJpaRepository;
import org.springframework.stereotype.Repository;

@Repository("sampleRepository")
public interface SampleRepository extends EgovJpaRepository<Sample, Integer> {
}
