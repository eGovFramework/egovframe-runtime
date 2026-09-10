package org.egovframe.rte.itl.webservice.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MappingInfo.toString() 이 필드 이름을 그대로 라벨로 쓰는지 확인한다.
 *
 * <p>같은 패키지의 Definition 들은 toString() 라벨을 필드 이름과 똑같이 적는다.
 * 라벨이 어긋나면 로그를 필드 이름으로 검색할 수 없다.</p>
 */
public class MappingInfoTest {

    @Test
    public void testToStringUsesFieldNameAsLabel() {
        MappingInfo mappingInfo = new MappingInfo();
        mappingInfo.setArgumentName("sampleArgument");

        String printed = mappingInfo.toString();

        assertTrue(printed.contains("argumentName = "),
                "필드 이름과 같은 라벨이어야 한다 : " + printed);
    }
}
