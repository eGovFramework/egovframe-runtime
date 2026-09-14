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
package org.egovframe.rte.bat.core.operation.lock;

/**
 * 배치 자원 락을 대기 시간 안에 얻지 못했을 때 던지는 예외.
 *
 * <p>{@link EgovResourceLockStepListener} 가 Step 시작 전에 던져 Step 을 실패시킨다. Spring Batch 리스너 계약이
 * checked 예외를 허용하지 않으므로 unchecked 로 둔다.</p>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2026.09.10	실행환경 개발팀		최초 생성
 * </pre>
 */
public class EgovResourceLockException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EgovResourceLockException(String message) {
        super(message);
    }

    public EgovResourceLockException(String message, Throwable cause) {
        super(message, cause);
    }

}
