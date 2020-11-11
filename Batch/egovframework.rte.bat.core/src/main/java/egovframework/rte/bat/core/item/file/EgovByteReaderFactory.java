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

package egovframework.rte.bat.core.item.file;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.springframework.core.io.Resource;

/**
 * Resource와 Encoding을 입력받아 EgovReader를 Factory Pattern으로 생성함
 * 
 * @author 배치실행개발팀
 * @since 2012. 07.30
 * @version 1.0
 * @see
 *      <pre>
 * == 개정이력(Modification Information) ==
 *   
 *   수정일               수정자               수정내용
 *  -------      --------     ---------------------------
 *  2012. 10.20  배치실행개발팀    최초 생성
 * 
 * </pre>
 */
public class EgovByteReaderFactory {

	/**
	 * Resource와 encoding을 입력받아 Reader를 생성하는 예제
	 * @param resource
	 * @param encoding
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public EgovByteReader create(Resource resource, String encoding) throws UnsupportedEncodingException, IOException {
		return new EgovByteReader(resource.getInputStream(), encoding);
	}
	
}