package org.egovframe.rte.psl.reactive.cassandra.repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("sample")
public class Sample {

    @PrimaryKeyColumn(name="id", ordinal=0, type=PrimaryKeyType.PARTITIONED)
    private String id;
    @PrimaryKeyColumn(name="sample_id", ordinal=1, type=PrimaryKeyType.CLUSTERED)
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
