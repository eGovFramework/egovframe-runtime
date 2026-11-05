package org.egovframe.rte.fdl.idgnr.impl;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * 테이블 ID 생성기의 SQL 식별자 검증과 시각 기반 UUID 의 형식을 검증한다.
 */
@SuppressWarnings("deprecation")
public class EgovIdGnrSecurityTest {

    @Test
    public void SQL_식별자에_구문을_섞으면_거부한다() {
        EgovTableIdGnrServiceImpl service = new EgovTableIdGnrServiceImpl();
        for (String bad : new String[]{"ids; DROP TABLE users", "ids--", "ids /*x*/", "ids'", "", "1ids"}) {
            assertThrows(bad, IllegalArgumentException.class, () -> service.setTable(bad));
            assertThrows(bad, IllegalArgumentException.class, () -> service.setTableNameFieldName(bad));
            assertThrows(bad, IllegalArgumentException.class, () -> service.setNextIdFieldName(bad));
        }
        assertThrows(IllegalArgumentException.class, () -> new EgovTableIdGnrService().setTable("ids where 1=1"));
    }

    @Test
    public void 정상_식별자와_스키마_한정_테이블명은_허용한다() {
        EgovTableIdGnrServiceImpl service = new EgovTableIdGnrServiceImpl();
        service.setTable("COMTECOPSEQ");
        service.setTable("egov.ids");
        service.setTableNameFieldName("table_name");
        service.setNextIdFieldName("NEXT_ID");
        // 구분값(tableName)은 바인드 변수로 전달되므로 식별자 형식 제한을 받지 않는다
        service.setTableName("BBS-ID'");
    }

    @Test
    public void 시각_기반_UUID_는_RFC_4122_형식이다() {
        UUID first = TimeBasedUUIDGenerator.generateId(0x0000AABBCCDDEEFFL);
        UUID second = TimeBasedUUIDGenerator.generateId(0x0000AABBCCDDEEFFL);
        assertEquals(1, first.version());
        assertEquals(2, first.variant());
        assertTrue(first.timestamp() < second.timestamp());
        // clockSequence 는 기동 시 무작위로 한 번 정해지고 같은 JVM 안에서는 유지된다
        assertEquals(first.clockSequence(), second.clockSequence());
        assertEquals(0x0000AABBCCDDEEFFL, first.node());
    }

}
