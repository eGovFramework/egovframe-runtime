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
package egovframework.rte.fdl.access.interceptor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.util.AntPathMatcher;

/**
 * Ant / Regular Expression Style Path Check
 *
 * <p>Desc.: Ant / Regular Expression Style Path Check</p>
 *
 * @author Egovframework Center
 * @since 2019.10.01
 * @version 3.9
 * @see <pre>
 * == 개정이력(Modification Information) ==
 *
 * 수정일			수정자					수정내용
 * ---------------------------------------------------------------------------------
 * 2019.10.01	Egovframework Center	최초 생성
 *
 * </pre>
 */
public class EgovAccessUtil {

    public static boolean antMatcher(String pattern, String inputString) {
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        return antPathMatcher.match(pattern, inputString);
    }

    public static boolean regexMatcher(String pattern, String inputString) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(inputString);
        return m.find();
    }

}
