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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * EgovDelimitedLengthTokenizer 클래스
 *
 * @author 표준프레임워크 운영사업단 신용호
 * @since 2017.10.23
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2017.10.23	신용호				최초 생성
 * </pre>
*/
public class EgovEscapableDelimitedLineTokenizer extends EgovAbstractLineTokenizer {

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovEscapableDelimitedLineTokenizer.class);

	// 기본으로 설정 가능한 delimiter. (기본: 콤마)
	public static final String DELIMITER_COMMA = ",";

	// 기본으로 설정 가능한 quotation. (기본: 쌍따옴표)
	public static final String DEFAULT_QUOTE_CHARACTER = "\"";

	// Token 생성시 사용할 delimiter.
	private String delimiter;
	private String regexDelimiter;
	private boolean escape = true;

	// Token 생성시 사용할 quotation.
	private String quoteCharacter = DEFAULT_QUOTE_CHARACTER;

	/**
	 * EgovDelimietedLineTokenizer 기본 생성자
	 * Token 생성시 사용할 delimiter로 콤마(기본 delimiter)를 사용하도록 설정한다.
	 * @see #DELIMITER_COMMA
	 */
	public EgovEscapableDelimitedLineTokenizer() {
		this(DELIMITER_COMMA);
	}

	/**
	 * EgovDelimietedLineTokenizer 생성자
	 * Token 생성시 사용할 delimiter를 인수로 받아 사용하도록 설정한다.
	 * @param delimiter : delimiter로 사용 할 문자열
	 */
	public EgovEscapableDelimitedLineTokenizer(String delimiter) {
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
		// regex 정규식에서 특수문자로 인식하는 것에 대한 처리
		this.regexDelimiter = getRegexDelimiter(delimiter);
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
	 * quoteCharacter로 Escape처리를 할지 설정한다.
	 * @param escape : 이스케이프 처리를 할지 여부 
	 * escape = true : Escape 처리를 한다.
	 * escape = false : Escape 처리를 하지 않고 delimiter문자로 컬럼 구분 처리한다.
	 */
	public void setEscape(boolean escape) {
		this.escape = escape;
	}

	/**
	 * String 형태의 line을 delimiter기준으로 token으로  만든다.
	 * @param line : ItemReader에서 읽어들인 line
	 * @return List String : tokens
	 */
	public List<String> doTokenize(String line) {
		List<String> tokens = new ArrayList<String>();
		int beginIndex = 0;
		int quoteIndex = 0;
		int closeIndex = 0;
		boolean flagEndQuote = true;
		String incompleteToken = "";
		String[] arrLine = line.split(this.regexDelimiter);
		
		for (int ii=0; ii<arrLine.length; ii++) {
			if (this.escape == true) {
				if (flagEndQuote == true) {
					incompleteToken = "";
					quoteIndex = arrLine[ii].indexOf(quoteCharacter, beginIndex);
					if (quoteIndex >= 0) {
						closeIndex = findCloseIndex(arrLine[ii], quoteCharacter, quoteIndex + 1);
						incompleteToken += arrLine[ii];
						if (closeIndex > 0) {
							tokens.add(incompleteToken);
							flagEndQuote = true;
						} else {
							flagEndQuote = false;
						}
					} else {
						tokens.add(arrLine[ii]);
					}
				} else {
					closeIndex = findCloseIndex(arrLine[ii], quoteCharacter, quoteIndex + 1);
					incompleteToken += this.delimiter + arrLine[ii];
					if (closeIndex > 0) {
						tokens.add(incompleteToken);
						flagEndQuote = true;
					} else {
						flagEndQuote = false;
					}
				}
				// 마지막까지 Quote로 닫히지 않는경우 예외처리
				// 토큰에 나머지를 넘긴다.
				// CSV Syntax Error로 컬럼갯수가 맞지 않아 Exception 발생 : Incorrect number
				if (ii + 1 == arrLine.length && flagEndQuote == false) {
					tokens.add(incompleteToken);
				}
			} else {
				tokens.add(arrLine[ii]);
			}
		}

		return tokens;
	}

	/**
	 * Double Quote는 Skip한다.
	 * CSV에서 Double Quote는 " 문자로 취급한다.
	 * @param line
	 * @param quoteCharacter
	 * @param beginIndex
	 * @return
	 */
	private int findCloseIndex(String line, String quoteCharacter, int beginIndex) {
		int singleQuoteIndex = line.indexOf(quoteCharacter, beginIndex);
		int doubleQuoteIndex = line.indexOf(quoteCharacter+quoteCharacter, beginIndex);
		if ( doubleQuoteIndex < 0 && singleQuoteIndex >= 0 ) {
			return singleQuoteIndex;
		}
		if ( doubleQuoteIndex < 0 && singleQuoteIndex < 0 ) return -1;
		if ( doubleQuoteIndex >= 0 ) {
			if ( line.length() > (doubleQuoteIndex+(quoteCharacter.length()*2)) ) {
				return findCloseIndex(line, quoteCharacter, doubleQuoteIndex+(quoteCharacter.length()*2));
			} else {
				return -1;
			}
		}
		return -1;
	}
	
	private String getRegexDelimiter(String delimiter) {
		delimiter = delimiter.replaceAll("\\(", "\\\\(");
		delimiter = delimiter.replaceAll("\\)", "\\\\)");
		delimiter = delimiter.replaceAll("\\{", "\\\\{");
		delimiter = delimiter.replaceAll("\\}", "\\\\}");
		delimiter = delimiter.replaceAll("\\^", "\\\\^");
		delimiter = delimiter.replaceAll("\\[", "\\\\[");
		delimiter = delimiter.replaceAll("\\]", "\\\\]");

		delimiter = delimiter.replaceAll("[*]", "[*]");
		delimiter = delimiter.replaceAll("[+]", "[+]");
		delimiter = delimiter.replaceAll("[$]", "[$]");
		delimiter = delimiter.replaceAll("[|]", "[|]");

		LOGGER.debug("regexDelimiter = "+delimiter);
		return delimiter;
	}

}
