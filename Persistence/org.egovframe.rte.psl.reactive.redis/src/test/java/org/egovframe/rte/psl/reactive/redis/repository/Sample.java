package org.egovframe.rte.psl.reactive.redis.repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RedisHash("sample")
public class Sample {

    @Id
    private String id;
    @Indexed
    private String sampleId;
    @Indexed
    private String name;
    private String description;
    private String useYn;
    private String regUser;

}
