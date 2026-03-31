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
package org.egovframe.rte.fdl.cmmn.trace.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default TraceHandler.
 *
 * @author Judd Cho (horanghi@gmail.com)
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.05.30	Judd Cho			최초 생성
 * 2015.01.31	Vincent Han			코드 품질 개선 및 Logger 적용
 * </pre>
 * @since 2009.06.01
 */
public class DefaultTraceHandler implements TraceHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTraceHandler.class);

    public void todo(Class<?> clazz, String message) {
        LOGGER.debug("DefaultTraceHandler run...............");
    }

}
