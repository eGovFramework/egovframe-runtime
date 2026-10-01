package org.egovframe.rte.psl.data.jpa.audit;

import org.egovframe.rte.psl.data.jpa.crud.Employee;
import org.egovframe.rte.psl.data.jpa.crud.EmployeeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * MDC 감사자 테스트 — 단위(MDC 유무·공백)와 HSQLDB 통합(저장 시 등록자·수정 시 수정자).
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MdcAuditingConfiguration.class)
@Transactional
class EgovMdcAuditorAwareTest {

	@Autowired
	private EmployeeRepository repository;

	@AfterEach
	void clearMdc() {
		MDC.clear();
	}

	@Test
	void MDC_값을_돌려주고_없거나_비어_있으면_empty_다() {
		EgovMdcAuditorAware auditorAware = new EgovMdcAuditorAware();
		MDC.clear();
		assertEquals(Optional.empty(), auditorAware.getCurrentAuditor());
		MDC.put(EgovMdcAuditorAware.MDC_USER_ID, "  ");
		assertEquals(Optional.empty(), auditorAware.getCurrentAuditor(), "공백은 값이 없는 것으로 본다");
		MDC.put(EgovMdcAuditorAware.MDC_USER_ID, "hong");
		assertEquals(Optional.of("hong"), auditorAware.getCurrentAuditor());
	}

	@Test
	void MDC_에_사용자가_있으면_저장_시_등록자로_기록된다() {
		MDC.put(EgovMdcAuditorAware.MDC_USER_ID, "hong");
		Employee employee = repository.saveAndFlush(new Employee("홍길동", "총무과"));

		assertEquals("hong", employee.getCreatedBy());
		assertEquals("hong", employee.getModifiedBy());
		assertNotNull(employee.getCreatedDate());
		assertNotNull(employee.getModifiedDate());
	}

	@Test
	void MDC_가_비어_있으면_등록자는_비고_일시만_기록된다() {
		MDC.clear();
		Employee employee = repository.saveAndFlush(new Employee("김철수", "기획과"));

		assertNull(employee.getCreatedBy(), "임의 대체값을 만들지 않는다");
		assertNull(employee.getModifiedBy());
		assertNotNull(employee.getCreatedDate());
		assertNotNull(employee.getModifiedDate());
	}

	@Test
	void 수정_시_MDC_의_새_사용자가_수정자로_기록되고_등록자는_그대로다() {
		MDC.put(EgovMdcAuditorAware.MDC_USER_ID, "hong");
		Employee employee = repository.saveAndFlush(new Employee("이영희", "인사과"));

		MDC.put(EgovMdcAuditorAware.MDC_USER_ID, "kim");
		employee.setDept("재무과");
		Employee updated = repository.saveAndFlush(employee);

		assertEquals("hong", updated.getCreatedBy());
		assertEquals("kim", updated.getModifiedBy());
	}
}
