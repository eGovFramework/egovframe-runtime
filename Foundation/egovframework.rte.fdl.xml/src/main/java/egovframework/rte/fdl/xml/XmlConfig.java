/*
 * Copyright 2009-2014 MOSPA(Ministry of Security and Public Administration).

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
package egovframework.rte.fdl.xml;

/**
 * XML 설정 정보관리 Class
 * 
 * @author 개발프레임웍크 실행환경 개발팀 김종호
 * @since 2009.03.18
 * @version 1.0
 * @see <pre>
 *  == 개정이력(Modification Information) ==
 *
 *   수정일      수정자              수정내용
 *  ---------   ---------   -------------------------------
 * 2009.03.18    김종호        최초생성
 *
 * </pre>
 */
public class XmlConfig {
	/** XML 파일 생성 경로 */
	private String path;

	/**
	 * path 설정
	 * @param path - 패스워드
	 */
	public void setXmlpath(String path) {
		this.path = path;
	}

	/**
	 * path 리턴
	 * @return path - 패스워드
	 */
	public String getXmlpath() {
		return path;
	}
}
