package org.egovframe.rte.psl.data.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "sample")
public class Sample {

    @Id
    private Integer id;

    @Column(name = "sample_id")
    private String sampleId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "use_yn")
    private String useYn;

    @Column(name = "reg_user")
    private String regUser;

}
