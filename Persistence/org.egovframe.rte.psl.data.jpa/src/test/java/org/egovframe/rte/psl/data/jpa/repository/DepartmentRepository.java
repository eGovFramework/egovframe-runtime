package org.egovframe.rte.psl.data.jpa.repository;

import org.egovframe.rte.psl.data.jpa.domain.Department;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DepartmentRepository extends CrudRepository<Department, Long> {

	List<Department> findByDeptNameContaining(String deptname);

}
