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

import java.io.ByteArrayInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;

/**
 * egov-security http create
 * 
 *<p>Desc.: egov-security http bean의 xml string 를 생성 Bean</p>
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
public class EgovSecurityHttp {

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovSecurityHttp.class);

	private static EgovSecurityHttp instance;
	
	public EgovSecurityHttp() {}

	public synchronized static EgovSecurityHttp getInstance(){
	    if(instance == null){
	        instance = new EgovSecurityHttp();
	    }
	 return instance;
	}

	
    /**
     * egov-security http bean의 xml string 를 생성
     * @param Sniff			 	: 응답에 대한 브라우저의 MIME 가로채기를 방지 옵션(활성:true, 비활성:false)
     * @param XFrameOptions 	: 프레임셋 동작여부 옵션(DENY:거부, SAMEORIGIN:허용) 
     * @param XssProtection 	: 브라우저가 XSS 공격에 사용될 수 있는 스크립트를 실행하지 않음 옵션(활성:true, 비활성:false)
     * @param Csrf 				: HTTP 요청과 악의적 인 웹 사이트의 요청을 거부 옵션(활성:true, 비활성:false)
     * @return InputStreamResource
     * @throws Exception
     */
	public InputStreamResource getHttp(boolean Sniff, 
							String XFrameOptions, 
							boolean XssProtection, 
							boolean Csrf) throws Exception{
		
		
		String sHttp =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
	        "<beans:beans  xmlns=\"http://www.springframework.org/schema/security\"\n" +
	        "	xmlns:beans=\"http://www.springframework.org/schema/beans\"\n" +
	        "	xmlns:aop=\"http://www.springframework.org/schema/aop\"\n" +
	        "	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
	        "	xmlns:context=\"http://www.springframework.org/schema/context\"\n" +
	        "	xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.0.xsd\n" +
	        "						http://www.springframework.org/schema/security http://www.springframework.org/schema/security/spring-security-4.2.xsd\n" +
	        "						http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-4.0.xsd\n" +
	        "						http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-4.0.xsd\">\n" +
	        
	        "	<http entry-point-ref=\"loginUrlAuthenticationEntryPoint\" request-matcher-ref=\"requestMatcherTypeFactoryBean\"\n" +
	        "			disable-url-rewriting=\"false\" use-expressions=\"false\">\n" +
	        "\n";
	        //Sniff 설정 활성화
	        if(Sniff == true){
				sHttp +=	
				        "		<headers defaults-disabled=\"false\">\n" +
		    	        "			<frame-options policy=\"" + XFrameOptions +"\"/>\n";
				        if(XssProtection == true){
				        sHttp +=  	
		    	        "			<xss-protection enabled=\"true\" block=\"true\"/>\n";
				        }
				sHttp +=	
		    	        "		</headers>\n" +
		    	        "\n";
	       	//Sniff 설정 비활성화
	        }else{
				sHttp +=	
				        "		<headers defaults-disabled=\"true\">\n" +
		    	        "			<frame-options policy=\"" + XFrameOptions +"\"/>\n";
				        if(XssProtection == true){
				        sHttp +=  	
		    	        "			<xss-protection enabled=\"true\" block=\"true\"/>\n";
				        }
				sHttp +=	
		    	        "		</headers>\n" +
		    	        "\n";
	        }
	        
	        //Csrf 설정 활성화
	        if(Csrf == true){
				sHttp +=	
				        "		<csrf disabled=\"false\"/>\n" +
				    	        "\n";	
	        //Csrf 설정 비활성화
	        }else{
				sHttp +=	
				        "		<csrf disabled=\"true\"/>\n" +
				    	        "\n";
	        }
			sHttp +=	
	        "		<form-login login-page=\"/egov_security_login\" username-parameter=\"egov_security_username\" password-parameter=\"egov_security_password\" />\n" +
	        "		<logout logout-url=\"/egov_security_logout\" />\n" +
	        "\n" +
	        "		<anonymous />\n" +
	        "\n" +
	        "		<session-management session-authentication-strategy-ref=\"sas\"/>\n" +
	        "\n" +
	        "		<custom-filter before=\"FILTER_SECURITY_INTERCEPTOR\" ref=\"filterSecurityInterceptor\"/>\n" +
	        "		<custom-filter position=\"CONCURRENT_SESSION_FILTER\" ref=\"concurrencyFilter\" />\n" +
	        "	</http>\n"+
	        "</beans:beans>\n";
			
			
		LOGGER.debug("EgovSecurityHttp START ===");
		LOGGER.debug("EgovSecurityHttp create http string");
		LOGGER.debug(sHttp.toString());
		LOGGER.debug("EgovSecurityHttp END ===");
			
		return new InputStreamResource(new ByteArrayInputStream(sHttp.getBytes("UTF-8")));
	}

}
