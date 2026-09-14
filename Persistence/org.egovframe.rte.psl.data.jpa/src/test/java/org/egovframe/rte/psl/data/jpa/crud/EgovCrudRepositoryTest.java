package org.egovframe.rte.psl.data.jpa.crud;

import org.egovframe.rte.psl.data.jpa.JpaConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EgovBaseEntity 의 감사 필드 자동 기록과 EgovCrudRepository 의 소프트 삭제·복구를 검증한다.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = JpaConfiguration.class)
@Transactional
public class EgovCrudRepositoryTest {

    @Autowired
    private EmployeeRepository repository;

    @Test
    public void testAuditFieldsAreFilledOnSave() {
        Employee employee = repository.saveAndFlush(new Employee("홍길동", "총무과"));

        assertEquals("testUser", employee.getCreatedBy());
        assertNotNull(employee.getCreatedDate());
        assertEquals("testUser", employee.getModifiedBy());
        assertNotNull(employee.getModifiedDate());
        assertNull(employee.getDeletedDate());
        assertFalse(employee.isDeleted());
    }

    @Test
    public void testCreatedDateIsImmutableAndModifiedDateIsRefreshedOnUpdate() {
        Employee employee = repository.saveAndFlush(new Employee("김철수", "기획과"));
        LocalDateTime createdDate = employee.getCreatedDate();

        employee.setDept("인사과");
        Employee updated = repository.saveAndFlush(employee);

        assertEquals(createdDate, updated.getCreatedDate());
        assertNotNull(updated.getModifiedDate());
        assertFalse(updated.getModifiedDate().isBefore(createdDate));
    }

    @Test
    public void testSoftDeleteHidesRowFromActiveQueryAndRestoreBringsItBack() {
        Employee active1 = repository.save(new Employee("EMP-1", "DEV"));
        Employee target = repository.save(new Employee("EMP-2", "DEV"));
        Employee active2 = repository.save(new Employee("EMP-3", "OPS"));
        repository.flush();

        Employee deleted = repository.softDeleteById(target.getId());

        assertTrue(deleted.isDeleted());
        assertNotNull(deleted.getDeletedDate());
        List<Employee> activeRows = repository.findByDeletedDateIsNull();
        assertEquals(2, activeRows.size());
        assertTrue(activeRows.stream().noneMatch(e -> e.getId().equals(target.getId())));
        assertEquals(3, repository.findAll().size());

        repository.restore(deleted);

        assertEquals(3, repository.findByDeletedDateIsNull().size());
        assertNotNull(active1.getId());
        assertNotNull(active2.getId());
    }

    @Test
    public void testSoftDeleteOfMissingIdIsReportedAsInvalidApiUsage() {
        InvalidDataAccessApiUsageException ex = assertThrows(InvalidDataAccessApiUsageException.class,
                () -> repository.softDeleteById(-999));

        assertTrue(ex.getMostSpecificCause().getMessage().contains("-999"), ex.getMostSpecificCause().getMessage());
    }

}
