/*
 * Copyright 2012-2014 MOSPA(Ministry of Security and Public Administration).
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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.batch.item.file.transform.IncorrectLineLengthException;
import org.springframework.batch.item.file.transform.Range;

/**
 * Reader를 통해서 읽어들인 String을 Byte길이로 잘라서 배열에 추가하는 클래스
 * 
 * @author 배치실행개발팀
 * @since 2012. 08.20
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2012.08.20	배치실행개발팀		최초 생성
 *  </pre>
 */
public class EgovFixedByteLengthTokenizer extends EgovAbstractLineTokenizer {

	// 시스템 Characterset
	public static final String DEFAULT_CHARSET = Charset.defaultCharset().name();

	// 토큰 범위
	private Range[] ranges;

	// 최대 범위
	private int maxRange = 0;

	// 파일 오픈 여부
	boolean open = false;

	// 시스템 Characterset으로 Encoding설정
	private String byteEncoding = DEFAULT_CHARSET;

	/**
	 * 범위값을 세팅
	 * @param ranges
	 */
	public void setColumns(Range[] ranges) {
		this.ranges = Arrays.asList(ranges).toArray(new Range[ranges.length]);
		calculateMaxRange(ranges);
	}

	/**
	 * Encoding 타입을 세팅
	 * @param encoding
	 */
	public void setByteEncoding(String encoding) {
		this.byteEncoding = encoding;
	}

	/**
	 * 주어진 구간값을 계산하여 범위를 산정
	 * @param ranges
	 * @return
	 * @throws Exception
	 */
	private void calculateMaxRange(Range[] ranges) {
		if (ranges == null || ranges.length == 0) {
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
	 * @param line 토큰의 대상이 되는 문자열 (can be <code>null</code>)
	 * @return the resulting tokens (empty if the line is null)
	 * @throws IncorrectLineLengthException if line length is greater than or less than the max range set.
	 */
	protected List<String> doTokenize(String line) throws Exception {
		return doTokenize(line, byteEncoding);
	}

	/**
	 * 주어진 문자열을 Encoding을 적용하여 잘라서 토큰을 생성 <code>line</code>.
	 * @param line the line to be tokenised (can be <code>null</code>)
	 * @return the resulting tokens (empty if the line is null)
	 * @throws IncorrectLineLengthException if line length is greater than or less than the max range set.
	 */
	protected List<String> doTokenize(String line, String encoding) throws Exception {
		List<String> tokens = new ArrayList<String>(ranges.length);
		String token;
		byte[] byteString = line.getBytes(encoding);		
		int lineLength = byteString.length;

		if (lineLength == 0) {
			throw new IncorrectLineLengthException("Line length must be longer than 0", maxRange, lineLength);
		}

		if (lineLength < maxRange) {
			throw new IncorrectLineLengthException("Line is shorter than max range " + maxRange, maxRange, lineLength);
		}

		if (!open && lineLength > maxRange) {
			throw new IncorrectLineLengthException("Line is longer than max range " + maxRange, maxRange, lineLength);
		}

		for (int i = 0; i < ranges.length; i++) {
			int startPos = ranges[i].getMin() - 1;
			int endPos = ranges[i].getMax();
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
