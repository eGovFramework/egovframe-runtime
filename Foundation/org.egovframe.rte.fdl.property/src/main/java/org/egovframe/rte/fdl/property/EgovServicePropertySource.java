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
package org.egovframe.rte.fdl.property;

import org.springframework.core.env.EnumerablePropertySource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * {@link EgovPropertyService} 의 프로퍼티를 Spring {@code Environment} 에 편입하는 PropertySource.
 *
 * <p>{@code EgovPropertyService} 로 읽는 값은 Spring {@code Environment} 와 연결되어 있지 않아 {@code @Value} 나
 * 플레이스홀더에서 보이지 않는다. 이 클래스를 {@code Environment} 의 PropertySources 에 등록하면 같은 값을
 * {@code @Value("${...}")} 와 {@code resolvePlaceholders} 에서 쓸 수 있다.</p>
 *
 * <pre>{@code
 * // ApplicationContextInitializer 나 초기화 코드에서
 * ConfigurableEnvironment env = context.getEnvironment();
 * env.getPropertySources().addLast(new EgovServicePropertySource("egovPropertyService", egovPropertyService));
 * // 이후 @Value("${Globals.DbType}") 로 EgovPropertyService 값을 쓸 수 있다
 * }</pre>
 *
 * <p>조회는 같은 서비스 인스턴스에 위임하므로 {@code EgovPropertyServiceImpl.refreshPropertyFiles()} 로 다시 읽은 값이
 * 그대로 보인다. 단, {@code @Value} 로 주입된 필드는 주입 시점의 값이 고정되며 갱신 반영은 {@code Environment} 를
 * 직접 조회할 때 기준이다.</p>
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
public class EgovServicePropertySource extends EnumerablePropertySource<EgovPropertyService> {

    /**
     * @param name   PropertySource 이름
     * @param source 값을 제공할 EgovPropertyService
     */
    public EgovServicePropertySource(String name, EgovPropertyService source) {
        super(name, source);
    }

    /**
     * 없는 키는 예외 대신 null 을 돌려줘 다음 PropertySource 로 넘어가게 한다(PropertySource 계약).
     */
    @Override
    public Object getProperty(String name) {
        return getSource().getString(name, null);
    }

    @Override
    public String[] getPropertyNames() {
        List<String> names = new ArrayList<>();
        Iterator<?> keys = getSource().getKeys();
        if (keys != null) {
            while (keys.hasNext()) {
                Object key = keys.next();
                if (key != null) {
                    names.add(String.valueOf(key));
                }
            }
        }
        return names.toArray(new String[0]);
    }

}
