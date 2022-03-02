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

import java.util.ArrayList;
import java.util.List;
import org.springframework.util.Assert;

/**
 * EgovDelimitedLengthTokenizer 클래스
 *
 * @author 실행환경 개발팀 이도형
 * @since 2012.07.20
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2012.07.20	이도형				최초 생성
 * </pre>
*/
public class EgovDelimitedLineTokenizer extends EgovAbstractLineTokenizer {

	// 기본으로 설정 가능한 delimiter. (기본: 콤마)
	public static final String DELIMITER_COMMA = ",";

	// 기본으로 설정 가능한 quotation. (기본: 쌍따옴표)
	public static final String DEFAULT_QUOTE_CHARACTER = "\"";

	// Token 생성시 사용할 delimiter.
	private String delimiter;

	// Token 생성시 사용할 quotation.
	private String quoteCharacter = DEFAULT_QUOTE_CHARACTER;

	/**
	 * EgovDelimietedLineTokenizer 기본 생성자
	 * Token 생성시 사용할 delimiter로 콤마(기본 delimiter)를 사용하도록 설정한다.
	 * @see #DELIMITER_COMMA
	 */
	public EgovDelimitedLineTokenizer() {
		this(DELIMITER_COMMA);
	}

	/**
	 * EgovDelimietedLineTokenizer 생성자
	 * Token 생성시 사용할 delimiter를 인수로 받아 사용하도록 설정한다.
	 * @param delimiter : delimiter로 사용 할 문자열
	 */
	public EgovDelimitedLineTokenizer(String delimiter) {
		Assert.state(!delimiter.equals(DEFAULT_QUOTE_CHARACTER), "[" + DEFAULT_QUOTE_CHARACTER + "] is not allowed as delimiter for tokenizers.");
		this.delimiter = delimiter;
		setQuoteCharacter(DEFAULT_QUOTE_CHARACTER);
	}

	/**
	 * Delimiter 문자열을 설정한다.
	 * @param delimiter : delimiter로 사용 할 문자열
	 */
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	/**
	 * Quotation 문자열을 설정한다.
	 * quotation 문자는 CSV 파일에서 여러 개의 delimiter가 하나의 셀(Token)에 들어갈 경우, 
	 * 이를 하나의 Token 으로 묶어주기 위해 사용된다.
	 * @param quoteCharacter : quotation으로 사용 할 문자열
	 */
	public final void setQuoteCharacter(String quoteCharacter) {
		this.quoteCharacter = quoteCharacter;
	}

	/**
	 * String 형태의 line을 delimiter기준으로 token으로  만든다.
	 * @param line : ItemReader에서 읽어들인 line
	 * @return List String : tokens
	 */
	public List<String> doTokenize(String line) {
		List<String> tokens = new ArrayList<String>();
		int beginIndex = 0;
		int delimiterIndex = 0;
		int quoteIndex = line.indexOf(quoteCharacter, beginIndex);
		int endQuoteIndex = 0;
		int lastCut = 0;
		int length = line.length();

		while (lastCut != length && lastCut != -1) {
			delimiterIndex = line.indexOf(delimiter, beginIndex);
			// 문자열에 quotation 문자열을 체크한다
			if (quoteIndex == -1) {   
				lastCut = (delimiterIndex != -1 ? delimiterIndex : length);
				tokens.add(line.substring(beginIndex, lastCut));
				beginIndex = lastCut + 1;
			} else {
				if (quoteIndex < delimiterIndex) {
					// delimiter보다 앞에 있는 quotation을 발견했을 경우, 이는 첫 quotation(여는 quotation)이므로 다음 quotation(닫는 quotation)을 찾는다.
					endQuoteIndex = line.indexOf(quoteCharacter, quoteIndex + 1);
					if (endQuoteIndex != -1) {
						// 닫는 quotation을 발견했으면 하나의 token 으로 처리한다.
						tokens.add(line.substring(quoteIndex + 1, endQuoteIndex));
						// 마지막 token 인지 체크하여 마지막 token 이라면 끝내고, 아니라면 마지막 자른 위치를 닫는 quotation 문자열 다음 식별자로 한다.
						if (line.indexOf(quoteCharacter, endQuoteIndex) != -1) {
							lastCut = line.indexOf(delimiter, endQuoteIndex);
							beginIndex = lastCut + 1;
						}
					} else {
						// 닫는 quotation이 없다면 모든 token 을 하나로 처리한다.
						lastCut = length;
						tokens.add(line.substring(beginIndex, lastCut));
					}
				} else {
					// quotation이 delimiter보다 뒤에 있으면 해당 delimiter를 기준으로 자른다.
					lastCut = (delimiterIndex != -1 ? delimiterIndex : length);
					tokens.add(line.substring(beginIndex, lastCut));
					beginIndex = lastCut + 1;
				}
				quoteIndex = line.indexOf(quoteCharacter, beginIndex);
			}
		}
		
		return tokens;
	}

}
