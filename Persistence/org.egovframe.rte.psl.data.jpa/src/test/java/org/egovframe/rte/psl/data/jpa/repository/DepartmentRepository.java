package org.egovframe.rte.psl.data.jpa.repository;

import java.util.List;

import org.egovframe.rte.psl.data.jpa.domain.Department;

import org.springframework.data.repository.CrudRepository;

public interface DepartmentRepository extends CrudRepository<Department, Long> {

	List<Department> findByDeptNameContaining(String deptname);

}
