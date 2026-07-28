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
package org.egovframe.rte.ptl.reactive.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * EgovServiceException 단위 테스트
 *
 * <p>각 생성자의 getMessage() 반환값(동작 보존)과 Throwable 표준 메시지 전파(개선)를 검증한다.</p>
 */
public class EgovServiceExceptionTest {

    @Test
    public void messageConstructor() {
        EgovServiceException ex = new EgovServiceException("failed");

        assertEquals("failed", ex.getMessage());
        assertSame(EgovErrorCode.INTERNAL_SERVER_ERROR, ex.getEgovErrorCode());
    }

    @Test
    public void errorCodeAndMessageConstructor() {
        EgovServiceException ex = new EgovServiceException(EgovErrorCode.SERVICE_UNAVAILABLE, "failed");

        assertEquals("failed", ex.getMessage());
        assertSame(EgovErrorCode.SERVICE_UNAVAILABLE, ex.getEgovErrorCode());
    }

    @Test
    public void messageIsPropagatedToThrowable() {
        RuntimeException re = new EgovServiceException(EgovErrorCode.SERVICE_UNAVAILABLE, "failed");

        // 수정 전에는 super(message)를 호출하지 않아 null 이었음(개선 입증)
        assertEquals("failed", re.getMessage());
    }

}
