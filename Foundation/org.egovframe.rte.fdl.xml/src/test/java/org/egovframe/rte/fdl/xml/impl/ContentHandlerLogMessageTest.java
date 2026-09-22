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
package org.egovframe.rte.fdl.xml.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.egovframe.rte.fdl.xml.EgovSAXValidatorService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ContentHandlerImpl 이 남기는 로그 메시지 검증
 */
public class ContentHandlerLogMessageTest {

    /**
     * SAX 파싱 중 ContentHandlerImpl 이 남긴 메시지를 모으는 Appender
     */
    private static class CapturingAppender extends AbstractAppender {

        private final List<String> messages = new ArrayList<>();

        CapturingAppender() {
            super("contentHandlerCapture", null, null, true, Property.EMPTY_ARRAY);
        }

        @Override
        public void append(LogEvent event) {
            messages.add(event.getMessage().getFormattedMessage());
        }
    }

    /**
     * 요소의 시작과 종료 로그에 요소명과 안내 문구가 모두 남아야 한다.
     */
    @Test
    public void elementLogKeepsBothNameAndText() throws Exception {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Logger logger = context.getLogger(ContentHandlerImpl.class.getName());
        CapturingAppender appender = new CapturingAppender();
        appender.start();
        logger.addAppender(appender);

        try {
            EgovSAXValidatorService saxValidator = new EgovSAXValidatorService();
            saxValidator.setXML("<person/>");
            saxValidator.parse(false);
        } finally {
            logger.removeAppender(appender);
            appender.stop();
        }

        assertEquals(
                List.of("XML이 시작되었습니다.", "person이 시작되었습니다.", "person이 종료하였습니다.", "XML이 종료되었습니다."),
                appender.messages);
    }

}
