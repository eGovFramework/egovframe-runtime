package org.egovframe.rte.itl.integration.message.typed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.egovframe.rte.itl.integration.type.ListType;
import org.egovframe.rte.itl.integration.type.PrimitiveType;
import org.egovframe.rte.itl.integration.type.RecordType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link TypedList}·{@link TypedMap}이 저장할 때와 같은 기준으로 조회·삭제하는지 검증한다.
 *
 * <p>두 클래스는 값을 담을 때 element/field type으로 변환한다. 예를 들어 element type이
 * <code>long</code>이면 {@link PrimitiveType#LONG}이 <code>Integer</code>를 <code>Long</code>으로
 * 바꿔 담는다. 조회 인자를 변환하지 않으면 방금 넣은 값을 그대로 물어도 찾지 못한다.</p>
 */
class TypedLookupConversionTest {

    private final ListType longListType = new ListType("longList", "longList", PrimitiveType.LONG);

    private TypedList longList() {
        return new TypedList(longListType, Arrays.asList(1, 2, 3));
    }

    @Test
    @DisplayName("생성자에 넘긴 값은 저장 시 element type으로 변환된다")
    void constructorConvertsElements() {
        assertEquals(Long.class, longList().get(0).getClass());
    }

    @Test
    @DisplayName("넣은 값 그대로 contains·indexOf로 찾을 수 있다")
    void containsAndIndexOfFindOriginalValue() {
        TypedList list = longList();

        assertTrue(list.contains(Integer.valueOf(1)));
        assertEquals(0, list.indexOf(Integer.valueOf(1)));
        assertEquals(2, list.lastIndexOf(Integer.valueOf(3)));
        // 변환 후 형태로 물어도 결과는 같다
        assertTrue(list.contains(Long.valueOf(1L)));
    }

    @Test
    @DisplayName("넣은 값 그대로 remove로 지울 수 있다")
    void removeAcceptsOriginalValue() {
        TypedList list = longList();

        assertTrue(list.remove(Integer.valueOf(1)));
        assertEquals(2, list.size());
        assertFalse(list.contains(Integer.valueOf(1)));
    }

    @Test
    @DisplayName("Collection 인자를 받는 조회·삭제도 같은 기준으로 동작한다")
    void collectionArgumentsUseTheSameConversion() {
        assertTrue(longList().containsAll(Arrays.asList(1, 2)));

        TypedList removeTarget = longList();
        assertTrue(removeTarget.removeAll(Arrays.asList(1, 2)));
        assertEquals(1, removeTarget.size());

        TypedList retainTarget = longList();
        assertTrue(retainTarget.retainAll(Arrays.asList(1)));
        assertEquals(1, retainTarget.size());
        assertEquals(Long.valueOf(1L), retainTarget.get(0));
    }

    @Test
    @DisplayName("element type에 담을 수 없는 인자는 예외 없이 없는 값으로 처리한다")
    void foreignArgumentIsTreatedAsAbsent() {
        TypedList list = longList();

        assertFalse(list.contains("문자열"));
        assertEquals(-1, list.indexOf("문자열"));
        assertFalse(list.remove("문자열"));
        assertFalse(list.containsAll(Arrays.asList("문자열")));
        assertEquals(3, list.size());
    }

    @Test
    @DisplayName("null 인자도 예외 없이 처리한다")
    void nullArgumentIsHandled() {
        TypedList list = longList();

        assertFalse(list.contains(null));
        assertFalse(list.remove(null));
        assertEquals(3, list.size());
    }

    @Test
    @DisplayName("TypedMap도 넣은 값 그대로 containsValue로 찾을 수 있다")
    void typedMapContainsValueFindsOriginalValue() {
        Map<String, org.egovframe.rte.itl.integration.type.Type> fieldTypes = new LinkedHashMap<>();
        fieldTypes.put("count", PrimitiveType.LONG);
        fieldTypes.put("name", PrimitiveType.STRING);
        RecordType recordType = new RecordType("record", "record", fieldTypes);

        TypedMap map = new TypedMap(recordType);
        map.put("count", Integer.valueOf(7));
        map.put("name", "홍길동");

        assertEquals(Long.class, map.get("count").getClass());
        assertTrue(map.containsValue(Integer.valueOf(7)));
        assertTrue(map.containsValue(Long.valueOf(7L)));
        assertTrue(map.containsValue("홍길동"));
        assertFalse(map.containsValue(Integer.valueOf(8)));
    }
}
