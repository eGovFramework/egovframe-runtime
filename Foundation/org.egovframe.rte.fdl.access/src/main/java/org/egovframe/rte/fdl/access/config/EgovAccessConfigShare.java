/*
 * Copyright 2008-2019 MOIS(Ministry of the Interior and Safety).
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
 * egov-access schema config 공유 변수 클래스
 *
 * <p>Desc.: 설정 간소화 처리에 사용되는 config 공유 변수 클래스s</p>
 *
 * @author Egovframework Center
 * @since 2019.10.01
 * @version 3.9
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				    수정내용
 * ----------------------------------------------
 * 2019.10.01	Egovframework Center	최초 생성
 * </pre>
 */
public class EgovAccessConfigShare {

    public static String DEF_LOGIN_URL = "/uat/uia/egovLoginUsr.do";
    public static String DEF_ACCESS_DENIED_URL = "/uat/uia/egovLoginUsr.do?auth_error=1";
    public static String DEF_REQUEST_MATCH_TYPE = "regex";

}
