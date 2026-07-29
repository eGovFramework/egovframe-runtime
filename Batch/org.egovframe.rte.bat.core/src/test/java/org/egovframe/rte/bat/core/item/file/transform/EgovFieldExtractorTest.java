package org.egovframe.rte.bat.core.item.file.transform;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * EgovFieldExtractor JUnit Test 클래스
 *
 * <p>extract(item)이 리플렉션 getter 호출로 VO 필드를 추출하여 Object 배열로
 * 반환하는지 검증한다. names 순서에 따른 추출 순서와 null 필드 처리를 확인한다.</p>
 *
 * @version 1.0
 * <p>
 * == 개정이력(Modification Information) ==
 * <p>
 * 수정일        수정자           수정내용
 * -------      -------------  ----------------------
 * 2026.07.28  eGovFrame        최초 생성
 * @since 2026.07.28
 */
public class EgovFieldExtractorTest {

    /**
     * 리플렉션 getter 추출 검증을 위한 단순 VO 픽스처.
     */
    public static class SampleVO {
        private String id;
        private String name;
        private Integer amount;

        public SampleVO(String id, String name, Integer amount) {
            this.id = id;
            this.name = name;
            this.amount = amount;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Integer getAmount() {
            return amount;
        }
    }

    private EgovFieldExtractor<SampleVO> newExtractor(String... names) {
        EgovFieldExtractor<SampleVO> extractor = new EgovFieldExtractor<>();
        extractor.setNames(names);
        extractor.afterPropertiesSet();
        return extractor;
    }

    @Test
    public void testExtractReturnsFieldsInNamesOrder() {
        EgovFieldExtractor<SampleVO> extractor = newExtractor("id", "name", "amount");

        Object[] actual = extractor.extract(new SampleVO("A001", "홍길동", 1000));

        assertArrayEquals(new Object[]{"A001", "홍길동", 1000}, actual);
    }

    @Test
    public void testExtractRespectsCustomNamesOrder() {
        // names 배열 순서대로 추출되어야 한다.
        EgovFieldExtractor<SampleVO> extractor = newExtractor("amount", "id");

        Object[] actual = extractor.extract(new SampleVO("A001", "홍길동", 1000));

        assertArrayEquals(new Object[]{1000, "A001"}, actual);
    }

    @Test
    public void testExtractHandlesNullField() {
        // getter 가 null 을 반환하는 필드는 결과 배열에도 null 로 담긴다.
        EgovFieldExtractor<SampleVO> extractor = newExtractor("id", "name", "amount");

        Object[] actual = extractor.extract(new SampleVO("A001", null, null));

        assertArrayEquals(new Object[]{"A001", null, null}, actual);
    }

}
