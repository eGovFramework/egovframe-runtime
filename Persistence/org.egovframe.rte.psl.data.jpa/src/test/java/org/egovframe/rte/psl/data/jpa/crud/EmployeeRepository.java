package org.egovframe.rte.psl.data.jpa.crud;

import org.egovframe.rte.psl.data.jpa.repository.EgovCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 공통 CRUD 리포지토리 검증용 — 상속 선언과 활성 행 파생 쿼리만으로 완성된다.
 */
@Repository
public interface EmployeeRepository extends EgovCrudRepository<Employee, Integer> {

    /** 활성(미삭제) 행 목록 */
    List<Employee> findByDeletedDateIsNull();

}
