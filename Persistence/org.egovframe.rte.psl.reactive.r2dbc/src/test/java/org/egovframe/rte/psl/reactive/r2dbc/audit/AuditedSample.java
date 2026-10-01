package org.egovframe.rte.psl.reactive.r2dbc.audit;

import org.egovframe.rte.psl.reactive.r2dbc.entity.EgovReactiveBaseEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * 감사 상위 엔티티 검증용 테스트 엔티티.
 */
@Table("audited_sample")
public class AuditedSample extends EgovReactiveBaseEntity {

    @Id
    private Integer id;

    @Column("name")
    private String name;

    public AuditedSample() {
    }

    public AuditedSample(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
