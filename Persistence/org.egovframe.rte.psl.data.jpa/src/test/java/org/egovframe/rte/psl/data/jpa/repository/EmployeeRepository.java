package org.egovframe.rte.psl.data.jpa.repository;

import org.egovframe.rte.psl.data.jpa.domain.Employee;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface EmployeeRepository extends PagingAndSortingRepository<Employee, Long> {
	
}
