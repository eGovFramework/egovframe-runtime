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

import java.util.List;

/**
 * Byte단위로 아이템을 잘라서 처리하는 상위 클래스
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
public abstract class EgovAbstractByteLineTokenizer implements EgovByteLineTokenizer<Object> {
	
	/**
	 * doTokenize를 호출하여 Token목록을 생성한다. 
	 * 
	 * @param line 
	 * @return List String: token 목록 
	 */
	public List<String> tokenize(byte[] line) throws Exception {
		
		List<String> tokens = doTokenize(line);
			
		return tokens;
	}
	
	/**
	 * doTokenize를 호출하여 Encoding Type에 따라 Token목록을 생성한다. 
	 * @param line
	 * @param encoding
	 * @return List String: token 목록 
	 */
	public List<String> tokenize(byte[] line, String encoding) throws Exception {
		
		List<String> tokens = doTokenize(line, encoding);
			
		return tokens;
	}
	
	/**
	 * Token 목록을 생성한다. 
	 * 실제 구현은 Token을 만드는 방식에 따라 하위 클래스에서 이루어진다.
	 * 
	 * @param line
	 * @return List String: token 목록
	 */
	protected abstract List<String> doTokenize(byte[] line) throws Exception;
	
	/**
	 * Encoding Type에 따른 Token 목록을 생성한다. 
	 * 실제 구현은 Token을 만드는 방식에 따라 하위 클래스에서 이루어진다.
	 * @param line
	 * @return List String : token 목록
	 */
	protected  List<String> doTokenize(byte[] line, String encoding) throws Exception {
		return null;
	}
}
