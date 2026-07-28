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
 * EgovException 단위 테스트
 *
 * <p>각 생성자의 getMessage() 반환값(동작 보존)과 Throwable 표준 메시지 전파(개선)를 검증한다.</p>
 */
public class EgovExceptionTest {

    @Test
    public void messageConstructor() {
        EgovException ex = new EgovException("invalid");

        assertEquals("invalid", ex.getMessage());
        assertSame(EgovErrorCode.INVALID_INPUT_VALUE, ex.getEgovErrorCode());
    }

    @Test
    public void errorCodeAndMessageConstructor() {
        EgovException ex = new EgovException(EgovErrorCode.NOT_FOUND, "custom");

        assertEquals("custom", ex.getMessage());
        assertSame(EgovErrorCode.NOT_FOUND, ex.getEgovErrorCode());
    }

    @Test
    public void errorCodeConstructorUsesErrorCodeMessage() {
        EgovException ex = new EgovException(EgovErrorCode.NOT_FOUND);

        assertEquals(EgovErrorCode.NOT_FOUND.getMessage(), ex.getMessage());
        assertSame(EgovErrorCode.NOT_FOUND, ex.getEgovErrorCode());
    }

    @Test
    public void messageIsPropagatedToThrowable() {
        RuntimeException re = new EgovException(EgovErrorCode.NOT_FOUND, "custom");

        // 수정 전에는 super(message)를 호출하지 않아 null 이었음(개선 입증)
        assertEquals("custom", re.getMessage());
    }

}
