package egovframework.rte.psl.data.jpa.repository;

import egovframework.rte.psl.data.jpa.domain.Employee;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface EmployeeRepository extends PagingAndSortingRepository<Employee, Long> {
	
}
