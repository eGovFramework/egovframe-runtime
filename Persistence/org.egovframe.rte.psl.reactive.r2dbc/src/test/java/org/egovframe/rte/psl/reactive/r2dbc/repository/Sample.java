package org.egovframe.rte.psl.reactive.r2dbc.repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="sample")
public class Sample {

    @Id
    private Integer id;
    @Column(value="sample_id")
    private String sampleId;
    @Column(value="name")
    private String name;
    @Column(value="description")
    private String description;
    @Column(value="use_yn")
    private String useYn;
    @Column(value="reg_user")
    private String regUser;

}
