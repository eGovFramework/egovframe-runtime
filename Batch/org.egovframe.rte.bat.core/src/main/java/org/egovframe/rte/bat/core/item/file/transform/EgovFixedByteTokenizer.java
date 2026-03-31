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

import org.egovframe.rte.bat.core.item.file.EgovFlatFileByteReader;
import org.springframework.batch.item.file.transform.IncorrectLineLengthException;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.util.ObjectUtils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Reader를 통해서 읽어들인 String을 Byte길이로 잘라서 배열에 추가하는 클래스
 *
 * @author 배치실행개발팀
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2012.08.20	배치실행개발팀		최초 생성
 * </pre>
 * @since 2012. 08.20
 */
public class EgovFixedByteTokenizer extends EgovAbstractByteLineTokenizer {

    /**
     * 시스템 Characterset
     */
    public static final String DEFAULT_CHARSET = Charset.defaultCharset().name();

    /**
     * 파일 오픈 여부
     */
    boolean open = false;

    /**
     * 토큰 범위
     */
    private Range[] ranges;

    /**
     * 최대 범위
     */
    private int maxRange = 0;

    /**
     * 시스템 Characterset으로 Encoding설정
     */
    private String byteEncoding = DEFAULT_CHARSET;

    /**
     * 범위값을 세팅
     */
    public void setColumns(Range[] ranges) {
        this.ranges = Arrays.asList(ranges).toArray(new Range[ranges.length]);
        calculateMaxRange(ranges);
    }

    /**
     * Encoding 타입을 세팅
     */
    public void setByteEncoding(String encoding) {
        this.byteEncoding = encoding;
    }

    /**
     * 주어진 구간값을 계산하여 범위를 산정
     */
    private void calculateMaxRange(Range[] ranges) {
        if (ObjectUtils.isEmpty(ranges)) {
            maxRange = 0;
            return;
        }

        open = false;
        maxRange = ranges[0].getMin();

        for (int i = 0; i < ranges.length; i++) {
            int upperBound;
            if (ranges[i].hasMaxValue()) {
                upperBound = ranges[i].getMax();
            } else {
                upperBound = ranges[i].getMin();
                if (upperBound > maxRange) {
                    open = true;
                }
            }
            if (upperBound > maxRange) {
                maxRange = upperBound;
            }
        }
    }

    /**
     * 주어진 문자열을 잘라서 토큰을 생성 <code>line</code>.
     *
     * @param line 토큰의 대상이 되는 문자열 (can be <code>null</code>)
     * @return the resulting tokens (empty if the line is null)
     * @throws IncorrectLineLengthException if line length is greater than or less than the max range set.
     */
    protected List<String> doTokenize(byte[] line) throws Exception {
        return doTokenize(line, byteEncoding);
    }

    /**
     * 주어진 문자열을 Encoding을 적용하여 잘라서 토큰을 생성 <code>line</code>.
     *
     * @param byteString the line to be tokenised (can be <code>null</code>)
     * @return the resulting tokens (empty if the line is null)
     * @throws IncorrectLineLengthException if line length is greater than or less than the max range set.
     */
    protected List<String> doTokenize(byte[] byteString, String encoding) throws Exception {
        String token;
        List<String> tokens = new ArrayList<String>(ranges.length);
        int lineLength = byteString.length - EgovFlatFileByteReader.LINE_CRLF;

        if (lineLength == 0) {
            throw new IncorrectLineLengthException("Line length must be longer than 0", maxRange, lineLength);
        }

        if (lineLength < maxRange) {
            throw new IncorrectLineLengthException("Line is shorter than max range " + maxRange, maxRange, lineLength);
        }

        if (!open && lineLength > maxRange) {
            throw new IncorrectLineLengthException("Line is longer than max range " + maxRange, maxRange, lineLength);
        }

        for (Range range : ranges) {
            int startPos = range.getMin() - 1;
            int endPos = range.getMax();
            if (lineLength >= endPos) {
                token = new String(byteString, startPos, endPos - startPos, encoding);
            } else if (lineLength >= startPos) {
                token = new String(byteString, startPos, lineLength - startPos, encoding);
            } else {
                token = "";
            }
            tokens.add(token);
        }

        return tokens;
    }

}
