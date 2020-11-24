/*
 * Copyright 2008-2009 MOPAS(Ministry of Public Administration and Security).
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
package org.egovframe.rte.fdl.security.securedobject.impl;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class SelfRegexRequestMatcher implements RequestMatcher {

	private String pattern = null;
	private String httpMethod = null;
	private RegexRequestMatcher requestMatcher = null;

	public SelfRegexRequestMatcher(String pattern, String httpMethod) {
		this(pattern, httpMethod, false);
	}

	public SelfRegexRequestMatcher(String pattern, String httpMethod, boolean caseInsensitive) {
		this.requestMatcher = new RegexRequestMatcher(pattern, httpMethod, caseInsensitive);
		this.pattern = pattern;
		this.httpMethod = httpMethod;
	}

	@Override
	public boolean matches(HttpServletRequest request) {
		return requestMatcher.matches(request);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SelfRegexRequestMatcher)) {
			return false;
		}

		SelfRegexRequestMatcher key = (SelfRegexRequestMatcher) obj;
		if (!pattern.equals(key.pattern)) {
			return false;
		}

		if (httpMethod == null) {
			return key.httpMethod == null;
		}

		return httpMethod.equals(key.httpMethod);
	}
	
	@Override
	public int hashCode() {
		int code = 31 ^ pattern.hashCode();

		if (httpMethod != null) {
            code ^= httpMethod.hashCode();
        }
        
        return code;
    }

}
