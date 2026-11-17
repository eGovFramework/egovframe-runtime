/*
 * Copyright 2009-2026 MOIS(Ministry of the Interior and Safety).
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
package org.egovframe.rte.fdl.xml.error;

import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashSet;
import java.util.Set;

/**
 * DefaultHandler 상속 클래스 : XML 파싱 중 발생한 에러 메시지를 Set에 저장
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
public class ErrorChecker extends DefaultHandler {

    /**
     * 에러 메시지를 저장하는 Set
     **/
    private final Set<String> errorReport = new HashSet<>();

    /**
     * DefaultHandler.error 오버라이드 : 파싱 중 error 레벨 에러 메시지 수신
     *
     * @param e - SAXParseException
     */
    public void error(SAXParseException e) {
        errorReport.add("Parsing error:  " + e.getMessage());
    }

    /**
     * DefaultHandler.warning 오버라이드 : 파싱 중 warning 레벨 에러 메시지 수신
     *
     * @param e - SAXParseException
     */
    public void warning(SAXParseException e) {
        errorReport.add("Parsing problem:  " + e.getMessage());
    }

    /**
     * DefaultHandler.fatalError 오버라이드 : 파싱 중 fatalError 레벨 에러 메시지 수신
     *
     * @param e - SAXParseException
     */
    public void fatalError(SAXParseException e) {
        errorReport.add("Parsing Fatal error:  " + e.getMessage());
    }

    /**
     * XML 문서 파싱시 발생한 에러를 저장하고 있는 에러 메시지들을 반환.
     *
     * @param
     * @return Set
     */
    public Set<String> getErrorReport() {
        return errorReport;
    }

}
