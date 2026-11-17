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
 * <p>{@code Environment} 의 PropertySources 에 등록 시 {@code @Value("${...}")}·{@code resolvePlaceholders} 에서 사용 가능
 * (미등록 시 {@code EgovPropertyService} 값은 플레이스홀더에서 보이지 않음)</p>
 *
 * <pre>{@code
 * // ApplicationContextInitializer 나 초기화 코드에서
 * ConfigurableEnvironment env = context.getEnvironment();
 * env.getPropertySources().addLast(new EgovServicePropertySource("egovPropertyService", egovPropertyService));
 * // 이후 @Value("${Globals.DbType}") 로 EgovPropertyService 값을 쓸 수 있다
 * }</pre>
 *
 * <p>조회는 같은 서비스 인스턴스에 위임 ({@code refreshPropertyFiles()} 결과 즉시 반영).
 * 단, {@code @Value} 주입 필드는 주입 시점 값 고정 (갱신 반영은 {@code Environment} 직접 조회 시)</p>
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
     * 없는 키는 null 반환 (다음 PropertySource 로 위임, PropertySource 계약).
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
