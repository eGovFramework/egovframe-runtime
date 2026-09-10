package org.egovframe.rte.psl.data.jpa.crud;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.egovframe.rte.psl.data.jpa.entity.EgovSoftDeletableEntity;

/**
 * 감사 필드 + 소프트 삭제 공통 엔티티 검증용 테스트 엔티티.
 */
@Entity
@Table(name = "crud_employee")
public class Employee extends EgovSoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "dept", length = 50)
    private String dept;

    protected Employee() {
    }

    public Employee(String name, String dept) {
        this.name = name;
        this.dept = dept;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

}
