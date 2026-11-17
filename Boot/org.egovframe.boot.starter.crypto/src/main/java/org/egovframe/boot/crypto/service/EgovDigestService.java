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
package org.egovframe.boot.crypto.service;

/**
 * 메시지 Digest 생성 · 검증 서비스.
 *
 * @author 유지보수
 * @version 5.1
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일       수정자    수정내용
 * ------------------------------------------------------------
 * 2025.06.01   유지보수  최초 생성
 * </pre>
 * @since 2025.06.01
 */
public interface EgovDigestService {

    /** Digest 알고리즘 */
    String getAlgorithm();

    /** Digest 알고리즘 지정 */
    void setAlgorithm(String algorithm);

    /** salt · 반복 없는 단순 Digest 사용 여부 */
    boolean isPlainDigest();

    /** 단순 Digest 사용 여부 지정 */
    void setPlainDigest(boolean isPlainDigest);

    /** Digest 생성 */
    byte[] digest(byte[] data);

    /** 메시지와 Digest 일치 여부 */
    boolean matches(byte[] messageByte, byte[] digestByte);

}
