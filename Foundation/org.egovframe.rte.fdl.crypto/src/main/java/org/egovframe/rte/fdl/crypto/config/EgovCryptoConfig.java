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
package org.egovframe.rte.fdl.crypto.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * egov-crypto 설정 정보를 담당하는 통합 설정 클래스
 *
 * <p>Desc.: properties 파일 기반 설정을 모두 지원하는 통합 설정 클래스</p>
 *
 * @author 장동한
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2018.08.09	장동한			최초 생성
 * 2024.12.19   유지보수			properties 설정 지원 및 통합
 * </pre>
 * @since 2018.08.09
 */
public class EgovCryptoConfig {

    @JsonProperty("id")
    private String id;

    @JsonProperty("initial")
    private boolean initial;

    @JsonProperty("crypto")
    private boolean crypto;

    @JsonProperty("algorithm")
    private String algorithm;

    @JsonProperty("algorithmKey")
    private String algorithmKey;

    @JsonProperty("algorithmKeyHash")
    private String algorithmKeyHash;

    @JsonProperty("cryptoBlockSize")
    private int cryptoBlockSize;

    @JsonProperty("cryptoPropertyLocation")
    private String cryptoPropertyLocation;

    @JsonProperty("plainDigest")
    private boolean plainDigest;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isInitial() {
        return initial;
    }

    public void setInitial(boolean initial) {
        this.initial = initial;
    }

    public boolean isCrypto() {
        return crypto;
    }

    public void setCrypto(boolean crypto) {
        this.crypto = crypto;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getAlgorithmKey() {
        return algorithmKey;
    }

    public void setAlgorithmKey(String algorithmKey) {
        this.algorithmKey = algorithmKey;
    }

    public String getAlgorithmKeyHash() {
        return algorithmKeyHash;
    }

    public void setAlgorithmKeyHash(String algorithmKeyHash) {
        this.algorithmKeyHash = algorithmKeyHash;
    }

    public int getCryptoBlockSize() {
        return cryptoBlockSize;
    }

    public void setCryptoBlockSize(int cryptoBlockSize) {
        this.cryptoBlockSize = cryptoBlockSize;
    }

    public String getCryptoPropertyLocation() {
        return cryptoPropertyLocation;
    }

    public void setCryptoPropertyLocation(String cryptoPropertyLocation) {
        this.cryptoPropertyLocation = cryptoPropertyLocation;
    }

    public boolean isPlainDigest() {
        return plainDigest;
    }

    public void setPlainDigest(boolean plainDigest) {
        this.plainDigest = plainDigest;
    }

}
