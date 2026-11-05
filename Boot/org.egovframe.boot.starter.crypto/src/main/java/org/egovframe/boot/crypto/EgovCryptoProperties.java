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
package org.egovframe.boot.crypto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 암호화 설정 프로퍼티({@code org.egovframe.crypto.*}).
 *
 * @author 유지보수
 * @version 5.1
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일       수정자    수정내용
 * ------------------------------------------------------------
 * 2025.06.01   유지보수  최초 생성
 * 2026.10.23   유지보수  cryptoPropertyLocation 추가
 * </pre>
 * @since 2025.06.01
 */
@ConfigurationProperties(prefix = "org.egovframe.crypto")
@Getter
@Setter
public class EgovCryptoProperties {

    private boolean initial = true;
    private boolean crypto = true;
    private String algorithm;
    private String algorithmKey;
    private String algorithmKeyHash;
    private int cryptoBlockSize = 1024;
    private boolean plainDigest = false;
    /** 암호화된 DB 접속 항목(Globals.*) 프로퍼티 파일 위치. 미지정 시 Spring Environment 에서 조회 */
    private String cryptoPropertyLocation;

}
