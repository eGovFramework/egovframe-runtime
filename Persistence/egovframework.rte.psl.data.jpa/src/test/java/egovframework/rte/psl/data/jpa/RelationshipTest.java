package egovframework.rte.psl.data.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import egovframework.rte.psl.data.jpa.domain.Department;
import egovframework.rte.psl.data.jpa.domain.Employee;
import egovframework.rte.psl.data.jpa.repository.DepartmentRepository;
import egovframework.rte.psl.data.jpa.repository.EmployeeRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:META-INF/spring/context-*.xml")
@Transactional
public class RelationshipTest {

	@Autowired
	DepartmentRepository repository;
	
	@Autowired
	EmployeeRepository employeeRepository;

	@Before
	public void setUp() {
		// no-op
	}

	@Test
	public void testSelectList() {
		
		Employee manager = getManager();
		
		manager = employeeRepository.save(manager);
		
		Department[] departments = getDepartmentList();
		
		for (int i = 0; i < departments.length; i++) {
			departments[i].setManager(manager);
			
			departments[i] = repository.save(departments[i]);
		}

		assertEquals(departments.length, repository.count());
		
		List<Department> list = (List<Department>) repository.findByDeptNameContaining("Department");
		
		for (Department department : departments) {
			assertTrue(list.contains(department));
		}
		
		for (Department department : list) {
			assertEquals(manager, department.getManager());
		}
	}

	private Employee getManager() {
		Employee manager = new Employee();
		
		manager.setEmployeeName("Manager");
		manager.setHireDate(new Date());
		manager.setJob("Manager");
		
		return manager;
	}
	
	private Department[] getDepartmentList() {
		
		List<Department> departments = new ArrayList<Department>();
		
		Department department = null;
		for (int i = 0; i < 10; i++) {
			department = new Department();
			
			department.setDeptName("Department " + (i+1));
			
			departments.add(department);
		}
		
		return departments.toArray(new Department[0]);
	}
	
}
