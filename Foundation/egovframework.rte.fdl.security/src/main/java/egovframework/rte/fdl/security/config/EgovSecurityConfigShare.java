/*
 * Copyright 2014 MOSPA(Ministry of Security and Public Administration).
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
package egovframework.rte.fdl.security.config;

/**
 * config와 initializer간의 공유 변수 클래스
 * 
 *<p>Desc.: config와 initializer간의 공유 변수 </p>
 *
 * @author 장동한
 * @since 2016.07.03
 * @version 1.0
 * @see <pre>
 *  == 개정이력(Modification Information) ==
 *   
 *   수정일				수정자		수정내용
 *  ---------------------------------------------------------------------------------
 *   2014.03.12	장동한			SpringSecurity 4.x 업그레이드 추가
 * 
 * </pre>
 */

public class EgovSecurityConfigShare {

	public static boolean alwaysUseDefaultTargetUrl = true;

	public static boolean sniff = true;

	public static String xFrameOptions = "SAMEORIGIN";

	public static boolean xssProtection = true;

	public static boolean cacheControl = false;

	public static boolean csrf = false;

}
