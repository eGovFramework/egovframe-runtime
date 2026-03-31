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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Set;

/**
 * DOMValidator Class
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
public class EgovDOMValidatorService extends AbstractXMLUtility {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovDOMValidatorService.class);

    /**
     * EgovDOMValidatorService 생성자
     */
    public EgovDOMValidatorService() {
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
        if (ObjectUtils.isEmpty(getXML()) && ObjectUtils.isEmpty(getXMLFile())) {
            LOGGER.debug(isValid ? "XML Validation을 체크하기 위한 XML이 필요합니다." : "Well-Formed를 체크하기 위한 XML이 필요합니다.");
            return false;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // 2026.02.28 KISA 보안취약점 조치 - XML 외부개체 참조(XXE) 방지
            try {
                factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            } catch (ParserConfigurationException e) {
                LOGGER.debug("DocumentBuilderFactory does not support secure processing feature: {}", e.getMessage());
            }
            try {
                factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
                factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            } catch (IllegalArgumentException e) {
                LOGGER.debug("DocumentBuilderFactory does not support external access restriction attributes: {}", e.getMessage());
            }
            try {
                factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
                factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            } catch (ParserConfigurationException e) {
                LOGGER.debug("DocumentBuilderFactory does not support one or more XXE-related features: {}", e.getMessage());
            }

            factory.setNamespaceAware(true);
            factory.setValidating(isValid);

            if (!ObjectUtils.isEmpty(getXMLFile())) {
                factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
                factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", getSCHEMAFile());
            }

            DocumentBuilder builder = factory.newDocumentBuilder();

            ErrorChecker errors = new ErrorChecker();
            builder.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException exception) {
                    errors.warning(exception);
                }

                @Override
                public void error(SAXParseException exception) {
                    errors.error(exception);
                }

                @Override
                public void fatalError(SAXParseException exception) {
                    errors.fatalError(exception);
                }
            });

            if (!ObjectUtils.isEmpty(getXMLFile())) {
                builder.parse(getXMLFile());
            } else {
                builder.parse(stringToInputSource());
            }

            Set<?> errorReport = errors.getErrorReport();
            if (!errorReport.isEmpty()) {
                makeErrorMessage(errorReport);
                return false;
            }

            return true;

        } catch (ParserConfigurationException e) {
            LOGGER.debug("[{}] EgovDOMValidatorService Parser() : {}", e.getClass().getName(), e.getMessage());
            throw new ValidatorException("Parser configuration error");
        }
    }
}
