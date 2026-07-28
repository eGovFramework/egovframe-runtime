/*
 * Copyright 2008-2024 MOIS(Ministry of the Interior and Safety).
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
package org.egovframe.rte.fdl.xml;

import org.egovframe.rte.fdl.xml.error.ErrorChecker;
import org.egovframe.rte.fdl.xml.exception.ValidatorException;
import org.egovframe.rte.fdl.xml.impl.ContentHandlerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.Set;

/**
 * SAXValidator Class
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
public class EgovSAXValidatorService extends AbstractXMLUtility {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovSAXValidatorService.class);

    /**
     * EgovSAXValidatorService 생성자
     */
    public EgovSAXValidatorService() {
        super();
    }

    /**
     * XML Parsing
     *
     * @param isValid - Validation 검사여부
     * @return 파싱결과
     */
    @Override
    public boolean parse(boolean isValid) throws IOException, SAXException, ValidatorException {
        if ((getXML() == null) && (getXMLFile() == null)) {
            String message = null;
            if (isValid) {
                message = "XML Validation을 체크하기 위한 XML이 필요합니다.";
            } else {
                message = "Well-Formed를 체크하기 위한 XML이 필요합니다.";
            }
            LOGGER.debug(message);
        }

        // 파서를 생성한다. 제거된 standalone Xerces 클래스 대신 JAXP SAXParserFactory(JDK 내장 파서)를 사용한다.
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

        // XXE(XML External Entity) 방어 - 외부 엔티티/외부 DTD 로딩을 차단한다.
        // 형제 클래스 EgovDOMValidatorService와 동일한 보안 처리(2026.02.28 KISA 조치)를 적용한다.
        try {
            saxParserFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            saxParserFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            saxParserFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            saxParserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        } catch (ParserConfigurationException | SAXException e) {
            LOGGER.debug("SAX parser does not support one or more XXE-related features: {}", e.getMessage());
        }

        XMLReader parser;
        try {
            parser = saxParserFactory.newSAXParser().getXMLReader();
        } catch (ParserConfigurationException e) {
            throw new SAXException(e);
        }
        parser.setFeature("http://xml.org/sax/features/validation", isValid);
        if (getSCHEMAFile() != null) {
            parser.setFeature("http://apache.org/xml/features/validation/schema", true);
            parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
            parser.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation", getSCHEMAFile());
        }

        ContentHandlerImpl contend = new ContentHandlerImpl();
        parser.setContentHandler(contend);
        //에러 메시지를 저장할 ErrorHandler를 세팅한다.
        ErrorChecker errors = new ErrorChecker();
        //파서에 ErrorHandler를 전달한다.
        parser.setErrorHandler(errors);
        //XML 문서를 파싱한다.
        if (getXMLFile() != null) {
            parser.parse(getXMLFile());
        } else {
            parser.parse(stringToInputSource());
        }

        Set<?> errorReport = errors.getErrorReport();

        //XML 문서 파싱시 발생된 에러가 있다면 XMLValidatorException을 이용해서 에러 메시지를 사용자에게 전달한다.
        if (!errorReport.isEmpty()) {
            makeErrorMessage(errorReport);
            return false;
        } else {
            return true;
        }
    }

}
