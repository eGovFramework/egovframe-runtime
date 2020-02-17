package egovframework.rte.psl.data.jpa.repository;

import java.util.List;

import egovframework.rte.psl.data.jpa.domain.Department;

import org.springframework.data.repository.CrudRepository;

public interface DepartmentRepository extends CrudRepository<Department, Long> {

	List<Department> findByDeptNameContaining(String deptname);

}
