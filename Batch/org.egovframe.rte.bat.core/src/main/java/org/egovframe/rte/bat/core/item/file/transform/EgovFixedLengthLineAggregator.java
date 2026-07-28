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
package org.egovframe.rte.bat.core.item.file.transform;

import org.springframework.batch.item.file.transform.ExtractorLineAggregator;
import org.springframework.util.Assert;

/**
 * Object 배열로 구성된 item 정보들을 Write 하기위해 fixedLength 방식으로 String화 하는 클래스
 *
 * @author 배치실행개발팀
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2012.07.20	배치실행개발팀		최초 생성
 * </pre>
 * @since 2012.07.20
 */
public class EgovFixedLengthLineAggregator<T> extends ExtractorLineAggregator<T> {

    /**
     * 각 field가 차지 할 length 배열
     */
    private int[] fieldRanges;

    /**
     * Padding Pattern
     */
    private char padding = ' ';

    /**
     * 사용할 padding의 패턴 set
     *
     * @param padding 사용할 Padding Pattern
     */
    public void setPadding(char padding) {
        this.padding = padding;
    }

    /**
     * 각 field가 차지 할 length set
     *
     * @param fieldRanges 각 field가 차지 할 length 배열
     */
    public void setFieldRanges(int[] fieldRanges) {
        this.fieldRanges = new int[fieldRanges.length];
        System.arraycopy(fieldRanges, 0, this.fieldRanges, 0, fieldRanges.length);
    }

    /**
     * 실제 VO fields의 Value를 String화
     *
     * @param fields Aggregate할 실제 VO fields의 Value
     */
    @Override
    protected String doAggregate(Object[] fields) {
        Assert.notNull(fieldRanges, "This argument is required : It must not be null");
        return aggregateFixedLength(obtainFieldValueLength(fields), fields);
    }

    /**
     * Object 배열 형태로 담긴 정보를 fixedLength 범위에 맞춰 String화 한다.
     *
     * @param fieldValueLength item에 담긴 정보 각각의 length
     * @param fields           field 정보 배열
     * @return String화 된 정보
     */
    private String aggregateFixedLength(int[] fieldValueLength, Object[] fields) {
        int fieldsLength = fields.length;

        //1. XML에서 지정한 field 범위 길이 갯수(ranges)와 field 갯수(fields)가 일치하는 지 검사.
        if (fieldsLength != fieldRanges.length) {
            Assert.state(fieldsLength == fieldRanges.length, "The number of field's ranges: " + fieldRanges.length + " is must match the number of field: " + fieldsLength);
        }

        StringBuilder value = new StringBuilder();

        for (int k = 0; k < fieldsLength; k++) {
            if (fieldRanges[k] >= fieldValueLength[k]) {
                value.append(fields[k].toString());
                if (fieldRanges[k] > fieldValueLength[k]) {
                    // 부족한 길이만큼 padding 문자로 채운다. (무상태 생성 → thread-safe)
                    int needPaddingSize = fieldRanges[k] - fieldValueLength[k];
                    value.append(String.valueOf(padding).repeat(needPaddingSize));
                }
            } else {
                //2. VO의 field 길이가 XML에서 지정한 field 범위 길이를 벗어나면 예외 발생.
                Assert.state(fieldRanges[k] >= fieldValueLength[k], "Supplied text: " + fields[k] + " is longer than defined length: " + fieldRanges[k]);
            }
        }

        return value.toString();
    }

    /**
     * 정보 각각의 length를 구한다.
     *
     * @param fields field 정보 배열
     * @return 정보 각각의 length 배열
     */
    private int[] obtainFieldValueLength(Object[] fields) {
        int[] fieldValueLength = new int[fields.length];
        for (int i = 0; i < fields.length; i++) {
            fieldValueLength[i] = fields[i].toString().length();
        }
        return fieldValueLength;
    }

}
