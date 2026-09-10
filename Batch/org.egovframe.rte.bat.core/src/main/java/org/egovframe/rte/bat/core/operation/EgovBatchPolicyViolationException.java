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
package org.egovframe.rte.bat.core.operation;

/**
 * 배치 실행 정책(동시 실행 수 제한 등) 위반 예외.
 *
 * <p>{@link EgovJobExecutionPolicyLauncher} 가 정책 위반으로 실행을 거부할 때 던진다. Spring Batch 의
 * {@code JobLauncher.run} 시그니처가 허용하는 checked 예외에 해당하지 않는 정책이므로 unchecked 로 둔다.</p>
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
public class EgovBatchPolicyViolationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EgovBatchPolicyViolationException(String message) {
        super(message);
    }

    public EgovBatchPolicyViolationException(String message, Throwable cause) {
        super(message, cause);
    }

}
