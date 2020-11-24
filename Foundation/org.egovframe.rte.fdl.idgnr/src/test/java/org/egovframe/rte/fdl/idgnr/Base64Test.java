package org.egovframe.rte.fdl.idgnr;

import static org.junit.Assert.assertEquals;

import org.egovframe.rte.fdl.idgnr.impl.Base64;

import org.junit.Test;

/**
 * Base64Test 클래스
 * @author 실행환경 개발팀 김태호
 * @since 2009.02.01
 * @version 1.0
 * @see <pre>
 *  == 개정이력(Modification Information) ==
 *   
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2009.02.01  김태호          최초 생성
 * 
 * </pre>
 */
public class Base64Test {
    /**
     * 길이와 위치를 지정한 encoding 테스트
     */
    @Test
    public void testEncodeWithLengthOffset() {
        // 1. encode with length and offset
        byte[] data = new byte[] {100, 122, 21, 127 };
        int len = 3;
        int off = 1;
        String encodedData1 = Base64.encode(data, off, len);
        assertEquals("ehV/", encodedData1);

        // 2. encode with length and offset
        len = 2;
        off = 2;
        String encodedData2 = Base64.encode(data, off, len);
        assertEquals("FX8=", encodedData2);

        // 3. encode with length and offset
        len = 1;
        off = 3;
        String encodedData3 = Base64.encode(data, off, len);
        assertEquals("fw==", encodedData3);
    }

    /**
     * Byte Array 의 인코딩 테스트
     */
    @Test
    public void testDefaultEncode() {
        // 1. encode
        byte[] data = new byte[] {100, 122, 21, 127 };
        String encodedData = Base64.encode(data);
        assertEquals("ZHoVfw==", encodedData);
    }
}
