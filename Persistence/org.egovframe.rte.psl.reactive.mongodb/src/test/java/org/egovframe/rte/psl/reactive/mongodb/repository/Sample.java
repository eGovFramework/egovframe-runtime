package org.egovframe.rte.psl.reactive.mongodb.repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection="sample")
public class Sample {

    @Id
    private Integer id;
    @Field("sample_id")
    private String sampleId;
    @Field("name")
    private String name;
    @Field("description")
    private String description;
    @Field("use_yn")
    private String useYn;
    @Field("reg_user")
    private String regUser;

}
