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
package org.egovframe.rte.fdl.access.config;

/**
 * egov-access 모듈의 공유 설정 상수를 관리하는 클래스
 *
 * <p>Desc.: 런타임에 설정값을 공유하기 위한 정적 상수 클래스</p>
 *
 * @author 유지보수
 * @version 6.0
 * @since 2025.06.01
 */
public class EgovAccessConfigShare {

    /**
     * 기본 로그인 URL
     */
    public static String DEF_LOGIN_URL = "/uat/uia/egovLoginUsr.do";

    /**
     * 기본 접근 거부 URL
     */
    public static String DEF_ACCESS_DENIED_URL = "/uat/uia/egovLoginUsr.do?auth_error=1";

    /**
     * 기본 요청 매칭 타입
     */
    public static String DEF_REQUEST_MATCH_TYPE = "regex";

    /**
     * Private constructor to prevent instantiation
     */
    private EgovAccessConfigShare() {
    }

}
