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

package egovframework.rte.bat.core.item.file.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.batch.item.file.transform.IncorrectLineLengthException;
import org.springframework.batch.item.file.transform.Range;

/**
 * EgovFixedLengthTokenizer 클래스
 * @author 실행환경 개발팀 이도형
 * @since 2012.07.20
 * @version 1.0
 * @see 
 * <pre>
 *      개정이력(Modification Information)
 *   
 *   수정일      수정자           수정내용
 *  ------- -------- ---------------------------
 *  2012.07.20  이도형     최초 생성
 * </pre>
*/
public class EgovFixedLengthTokenizer extends EgovAbstractLineTokenizer {
	
	// 필드 범위
	private Range[] ranges;
	
	// 필드 범위의 최대값
	private int maxRange = 0;
	
	// 최대값 도달 여부
	boolean open = false;

	/**
	 * 필드의 범위값을 설정한다. 
	 * ex) "1,4,7" or "1-3,4-6,7", "1-2,4-5,7-10". 
	 * 
	 * @param ranges
	 */
	public void setColumns(Range[] ranges) {
		this.ranges = Arrays.asList(ranges).toArray(new Range[ranges.length]);
		calculateMaxRange(ranges);
	}

	/**
	 * 필드 범위의 최대값을 계산한다.
	 * 
	 * @param ranges
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
	 * String 형태의 line을 필드 범위 값(ranges)을 기준으로 token으로  자른다.
	 * 
	 * @param line : ItemReader에서 읽어들인 line
	 * @return Listz : tokens
	 */
	protected List<String> doTokenize(String line) {
		List<String> tokens = new ArrayList<String>(ranges.length);
		int lineLength;
		String token;
		
		lineLength = line.length();
		
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
				token = line.substring(startPos, endPos);
			} else if (lineLength >= startPos) {
				token = line.substring(startPos);
			} else {
				token = "";
			}

			tokens.add(token);
		}

		return tokens;
	}
}
