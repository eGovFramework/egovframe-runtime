/*
 * Copyright 2008-2009 MOPAS(Ministry of Public Administration and Security).
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
package org.egovframe.rte.fdl.xml.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;

/**
 * 일반적인 문서 이벤트를 처리하는Class로서  문서의 시작, 요소의 시작과 끝, 요소가 포함하는 내용을 만날 때마다 호출
 *
 * @author 개발프레임웍크 실행환경 개발팀 김종호
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.03.18	김종호				최초생성
 * </pre>
 * @since 2009.03.18
 */
public class ContentHandlerImpl implements ContentHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContentHandlerImpl.class);

    /**
     * 문자처리
     *
     * @param ch     - XML 문서의 문자
     * @param start  - 배열내의 개시 위치
     * @param length - 배열로부터 읽어내지는 문자수
     */
    public void characters(char[] ch, int start, int length) {
        String str = new String(ch, start, length);
        if (!str.trim().isEmpty()) {
            LOGGER.debug(str);
        }
    }

    /**
     * 문서종료
     */
    public void endDocument() {
        LOGGER.debug("XML이 종료되었습니다.");
    }

    /**
     * 요소의 종료
     *
     * @param uri       - namespaceURI
     * @param localName - 전치수식자를 포함하지 않는 로컬명. 이름 공간 처리를 하지 않는 경우는 공문자열
     * @param name      - 전치수식자를 가지는 XML 1.0 수식명. 수식명을 사용할 수 없는 경우는 공문자열
     */
    public void endElement(String uri, String localName, String name) {
        LOGGER.debug(name, "{}이 종료하였습니다.");
    }

    /**
     * 전치수식자와 URI 의 매핑의 스코프를 종료
     *
     * @param prefix - 매핑 되고 있던 전치수식자
     */
    public void endPrefixMapping(String prefix) {
    }

    /**
     * 요소 컨텐츠에 포함되는 무시할 수 있는 공백 문자의 통지
     *
     * @param ch     - XML 문서의 문자
     * @param start  - 배열내의 개시 위치
     * @param length - 배열로부터 읽어내지는 문자수
     */
    public void ignorableWhitespace(char[] ch, int start, int length) {
    }

    /**
     * 처리 명령의 통지
     *
     * @param target - 처리 명령의 타겟
     * @param data   - 처리 명령을 받는 데이터. 데이터가 제공되지 않는 경우는 null. 데이터에는, 타겟과 자신을 구별하기 위한 공백 문자는 포함되지 않는다
     */
    public void processingInstruction(String target, String data) {
    }

    /**
     * SAX 문서 이벤트의 발생원을 특정하는 오브젝트 설정
     *
     * @param locator - SAX 문서 이벤트의 위치를 돌려주는 오브젝트
     */
    public void setDocumentLocator(Locator locator) {
    }

    /**
     * 스킵 된 엔티티의 통지 처리
     *
     * @param name - 스킵 된 엔티티의 이름. 파라메이타엔티티의 경우, 이 이름은 '%'로 시작된다. 외부 DTD 부분집합의 경우, 스트링 "[dtd]" 로 시작된다.
     */
    public void skippedEntity(String name) {
    }

    /**
     * 문서의 시작
     */
    public void startDocument() {
        LOGGER.debug("XML이 시작되었습니다.");
    }

    /**
     * 요소의 시작
     *
     * @param uri       - 이름 공간 URI
     * @param localName - 로컬명
     * @param name      - 전치수식자 첨부의 수식명
     */
    public void startElement(String uri, String localName, String name, Attributes atts) {
        LOGGER.debug(name, "{}이 시작되었습니다.");
    }

    /**
     * 전치수식자와 URI 이름 공간 매핑의 스코프 시작
     *
     * @param prefix - 선언되는 이름 공간앞치수식자
     * @param uri    - 전치수식자의 맵 사키나 마에조라간 URI
     */
    public void startPrefixMapping(String prefix, String uri) {
    }

}
