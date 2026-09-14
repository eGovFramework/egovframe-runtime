/*
 * Copyright 2008-2024 MOIS(Ministry of the Interior and Safety).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.egovframe.rte.bat.core.item.file.mapping;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EgovObjectMapperTypeConversionTest {

    @Test
    void mapsPrimitiveAndWrapperFieldsWithTheSameValues() {
        EgovObjectMapper<TypedVO> mapper = mapper("intValue", "integerValue", "longValue", "boxedLongValue",
                "doubleValue", "boxedDoubleValue", "floatValue", "boxedFloatValue",
                "charValue", "characterValue", "booleanValue", "boxedBooleanValue",
                "text", "decimal", "bytes");
        TypedVO result = mapper.mapObject(Arrays.asList("2147483647", "2147483647",
                "-9223372036854775808", "-9223372036854775808", "1.25", "1.25", "2.5", "2.5",
                "한", "한", "TRUE", "TRUE", "sample", "123.45", "abc"));

        assertEquals(Integer.MAX_VALUE, result.intValue);
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), result.integerValue);
        assertEquals(Long.MIN_VALUE, result.longValue);
        assertEquals(Long.valueOf(Long.MIN_VALUE), result.boxedLongValue);
        assertEquals(1.25, result.doubleValue);
        assertEquals(Double.valueOf(1.25), result.boxedDoubleValue);
        assertEquals(2.5f, result.floatValue);
        assertEquals(Float.valueOf(2.5f), result.boxedFloatValue);
        assertEquals('한', result.charValue);
        assertEquals(Character.valueOf('한'), result.characterValue);
        assertEquals(true, result.booleanValue);
        assertEquals(Boolean.TRUE, result.boxedBooleanValue);
        assertEquals("sample", result.text);
        assertEquals(new BigDecimal("123.45"), result.decimal);
        assertArrayEquals("abc".getBytes(), result.bytes);
    }

    @Test
    void rejectsInvalidNumericTokens() {
        String[][] cases = {{"intValue", "2147483648"}, {"integerValue", "2147483648"},
                {"longValue", "invalid"}, {"boxedLongValue", "invalid"},
                {"intValue", ""}, {"integerValue", ""}};
        for (String[] entry : cases) {
            EgovObjectMapper<TypedVO> mapper = mapper(entry[0]);
            assertThrows(NumberFormatException.class,
                    () -> mapper.mapObject(Collections.singletonList(entry[1])), entry[0] + ": " + entry[1]);
        }
    }

    @Test
    void rejectsEmptyCharacterTokens() {
        for (String name : Arrays.asList("charValue", "characterValue")) {
            EgovObjectMapper<TypedVO> mapper = mapper(name);
            assertThrows(StringIndexOutOfBoundsException.class,
                    () -> mapper.mapObject(Collections.singletonList("")), name);
        }
    }

    @Test
    void preservesBooleanParsingForNonTrueTokens() {
        TypedVO result = mapper("booleanValue", "boxedBooleanValue")
                .mapObject(Arrays.asList("invalid", "invalid"));
        assertFalse(result.booleanValue);
        assertEquals(Boolean.FALSE, result.boxedBooleanValue);
    }

    private EgovObjectMapper<TypedVO> mapper(String... names) {
        EgovObjectMapper<TypedVO> mapper = new EgovObjectMapper<>();
        mapper.setType(TypedVO.class);
        mapper.setNames(names);
        mapper.afterPropertiesSet();
        return mapper;
    }

    public static class TypedVO {
        private int intValue;
        private Integer integerValue;
        private long longValue;
        private Long boxedLongValue;
        private double doubleValue;
        private Double boxedDoubleValue;
        private float floatValue;
        private Float boxedFloatValue;
        private char charValue;
        private Character characterValue;
        private boolean booleanValue;
        private Boolean boxedBooleanValue;
        private String text;
        private BigDecimal decimal;
        private byte[] bytes;

        public void setIntValue(int value) { this.intValue = value; }
        public void setIntegerValue(Integer value) { this.integerValue = value; }
        public void setLongValue(long value) { this.longValue = value; }
        public void setBoxedLongValue(Long value) { this.boxedLongValue = value; }
        public void setDoubleValue(double value) { this.doubleValue = value; }
        public void setBoxedDoubleValue(Double value) { this.boxedDoubleValue = value; }
        public void setFloatValue(float value) { this.floatValue = value; }
        public void setBoxedFloatValue(Float value) { this.boxedFloatValue = value; }
        public void setCharValue(char value) { this.charValue = value; }
        public void setCharacterValue(Character value) { this.characterValue = value; }
        public void setBooleanValue(boolean value) { this.booleanValue = value; }
        public void setBoxedBooleanValue(Boolean value) { this.boxedBooleanValue = value; }
        public void setText(String value) { this.text = value; }
        public void setDecimal(BigDecimal value) { this.decimal = value; }
        public void setBytes(byte[] value) { this.bytes = value; }
    }
}
