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
package org.egovframe.rte.fdl.cmmn.exception.manager;

import org.egovframe.rte.fdl.cmmn.exception.handler.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DefaultExceptionHandleManager.
 *
 * <p>디폴트 ExceptionHandleManager
 * 사용자에 의해 구현시 참고하여 구현해주면 된다.</p>
 *
 * @author Judd Cho (horanghi@gmail.com)
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.05.30	Judd Cho			최초 생성
 * 2015.01.31	Vincent Han			코드 품질 개선
 * 2026.10.25	실행환경 개발팀		run(Exception, String) — 싱글톤 상태 변경 없이 발생 위치 전달
 * </pre>
 * @since 2009.06.01
 */
public class DefaultExceptionHandleManager extends AbstractExceptionHandleManager implements ExceptionHandlerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExceptionHandleManager.class);

    @Override
    public boolean run(Exception exception) throws Exception {
        LOGGER.debug(" DefaultExceptionHandleManager.run() ");
        // 매칭조건이 false 인 경우
        if (!enableMatcher()) {
            return false;
        }
        for (String pattern : patterns) {
            LOGGER.debug("pattern = {}, thisPackageName = {}", pattern, thisPackageName);
            LOGGER.debug("pm.match(pattern, thisPackageName) = {}", pm.match(pattern, thisPackageName));
            if (pm.match(pattern, thisPackageName)) {
                for (ExceptionHandler eh : handlers) {
                    eh.occur(exception, getPackageName());
                }
                break;
            }
        }
        return true;
    }

    /**
     * 발생 위치를 인자로 받아 상태(thisPackageName) 변경 없이 처리한다.
     * 하위 클래스가 run(Exception) 을 재정의한 경우에는 기존 동작을 위해 기본 구현(동기화)을 쓴다.
     */
    @Override
    public boolean run(Exception exception, String packageName) throws Exception {
        if (getClass() != DefaultExceptionHandleManager.class) {
            return ExceptionHandlerService.super.run(exception, packageName);
        }
        LOGGER.debug(" DefaultExceptionHandleManager.run() ");
        // 매칭조건이 false 인 경우
        if (!enableMatcher()) {
            return false;
        }
        for (String pattern : patterns) {
            LOGGER.debug("pattern = {}, packageName = {}", pattern, packageName);
            if (pm.match(pattern, packageName)) {
                for (ExceptionHandler eh : handlers) {
                    eh.occur(exception, packageName);
                }
                break;
            }
        }
        return true;
    }

}
