package org.egovframe.rte.psl.data.jpa;

import org.egovframe.rte.psl.data.jpa.domain.Employee;
import org.egovframe.rte.psl.data.jpa.repository.EmployeeRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:META-INF/spring/context-*.xml")
@Transactional
public class PagingAndSortingTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(PagingAndSortingTest.class);

	@Autowired
	EmployeeRepository repository;

	private static final String[] NAME = {"Alice", "Bob", "Charles", "Dennis", "Emily", "Frank ", "George", "Henry"};

	@Test
	public void testSelectList() {
		Employee[] employees = getEmployeeList();
		for (int i = 0; i < employees.length; i++) {
			employees[i] = repository.save(employees[i]);
		}

		Page<Employee> list;
		int size = 3;
		int totalIndex = 0;
		for (int i = 0; i < (NAME.length + (size - 1)) / size ; i++) {	// 올림 처리
			list = repository.findAll(PageRequest.of(i, size, Sort.by(Sort.Direction.ASC, "employeeName")));
			LOGGER.debug("Number of current page's element :  {}", list.getNumberOfElements());
			for (Employee employee : list) {
				LOGGER.debug("Selected employee name : {}", employee.getEmployeeName());
				assertEquals(NAME[totalIndex++], employee.getEmployeeName());
			}
			assertEquals((i == NAME.length / size ? NAME.length % size : size), list.getNumberOfElements());
		}
	}

	private Employee[] getEmployeeList() {
		Employee employee;
		List<Employee> employees = new ArrayList<>();
		for (String s : NAME) {
			employee = new Employee();
			employee.setEmployeeName(s);
			employee.setHireDate(new Date());
			employees.add(employee);
		}
		return employees.toArray(new Employee[0]);
	}

}
