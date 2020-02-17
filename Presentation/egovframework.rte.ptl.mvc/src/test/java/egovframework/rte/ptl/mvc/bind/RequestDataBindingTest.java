package egovframework.rte.ptl.mvc.bind;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.ServletRequestDataBinder;

/**
 *
 * 시스템명 : 실행환경시스템
 * 서브시스템명 : 화면처리
 * 요구사항ID : REQ-RTE-111
 * 요구사항명 : 데이터 바인딩
 * 설명 : Http Request 데이터가 Object에 바인딩되는 기능을 테스트한다.
 *
 * @author Ham Cheol
 */

public class RequestDataBindingTest {

	@Test
	public void testDataBinding() throws ParseException {

		Employee employee = new Employee();
		ServletRequestDataBinder binder = new ServletRequestDataBinder(employee, "employee");
		MockHttpServletRequest request = new MockHttpServletRequest();

		request.addParameter("empId", "AA1001");
		request.addParameter("empName", "Hong Kil Dong");
		request.addParameter("empAge", "8");
		request.addParameter("birthDate", "2000/10/31");

		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
		CustomDateEditor dateEditor = new CustomDateEditor(format, true);
		Date expected = format.parse("2000/10/31");

		binder.registerCustomEditor(Date.class, dateEditor);

		binder.bind(request);

		assertNotNull(employee);

		assertEquals("AA1001", employee.getEmpId());
		assertEquals("Hong Kil Dong", employee.getEmpName());
		assertEquals(8, employee.getEmpAge());
		assertEquals(expected, employee.getBirthDate());

	}

	private class Employee {

		private String empId;
		private String empName;
		private int empAge;
		private Date birthDate;

		public String getEmpId() {
			return empId;
		}

		@SuppressWarnings("unused")
		public void setEmpId(String empId) {
			this.empId = empId;
		}

		public String getEmpName() {
			return empName;
		}

		@SuppressWarnings("unused")
		public void setEmpName(String empName) {
			this.empName = empName;
		}

		public int getEmpAge() {
			return empAge;
		}

		@SuppressWarnings("unused")
		public void setEmpAge(int empAge) {
			this.empAge = empAge;
		}

		public Date getBirthDate() {
			return birthDate;
		}

		@SuppressWarnings("unused")
		public void setBirthDate(Date birthDate) {
			this.birthDate = birthDate;
		}
	}

}
