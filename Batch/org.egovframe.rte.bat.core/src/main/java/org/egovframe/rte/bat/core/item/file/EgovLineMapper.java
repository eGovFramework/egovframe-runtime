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
package org.egovframe.rte.bat.core.item.file;

/**
 * Byte처리용 EgovLineMapper 인터페이스
 * 
 * @author 배치실행개발팀
 * @since 2012.07.30
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2012.10.20	배치실행개발팀		최초 생성
 * 
 * </pre>
 */
public interface EgovLineMapper<T> {

	/**
	 * Byte단위로 읽어들인 한라인을 Mapping
	 * @param line to be mapped
	 * @param lineNumber of the current line
	 * @return mapped object of type T
	 * @throws Exception if error occured while parsing.
	 */
	T mapLine(byte[] line, int lineNumber) throws Exception;

}
