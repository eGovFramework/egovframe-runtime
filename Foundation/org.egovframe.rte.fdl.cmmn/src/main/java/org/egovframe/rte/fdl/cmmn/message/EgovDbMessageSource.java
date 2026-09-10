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
package org.egovframe.rte.fdl.cmmn.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * DB 테이블의 메시지를 제공하는 MessageSource.
 *
 * <p>초기화 시점에 테이블을 한 번 읽어 불변 스냅숏으로 보관하고, 운영 중 문구가 바뀌면 {@link #refresh()} 로
 * 재기동 없이 다시 읽는다. 조회는 스냅숏에서만 하므로 잠금 없이 스레드 안전하다.
 * 부모 MessageSource({@code setParentMessageSource})와 조합하면 <b>DB 우선, 파일 폴백</b>의 메시지 체계가 된다.</p>
 *
 * <p>기본 조회 쿼리와 테이블 규약은 아래와 같으며 {@code query} 속성으로 바꿀 수 있다. 결과 컬럼 순서는
 * (코드, 언어, 메시지)를 따른다. LANG 은 언어 코드("ko", "en")이며 빈 값이나 NULL 이면 모든 로케일(Locale)의
 * 기본값으로 쓴다.</p>
 * <pre class="code">
 * CREATE TABLE EGOV_MESSAGE_SOURCE (
 *     CODE    VARCHAR(100) NOT NULL,
 *     LANG    VARCHAR(10),
 *     MESSAGE VARCHAR(2000) NOT NULL
 * );
 * </pre>
 *
 * <pre class="code">
 * &lt;bean id="messageSource" class="org.egovframe.rte.fdl.cmmn.message.EgovDbMessageSource"&gt;
 *     &lt;property name="dataSource" ref="dataSource"/&gt;
 *     &lt;property name="parentMessageSource" ref="fileMessageSource"/&gt;  &lt;!-- DB 에 없으면 파일로 폴백 --&gt;
 * &lt;/bean&gt;
 * </pre>
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
public class EgovDbMessageSource extends AbstractMessageSource implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovDbMessageSource.class);

    /** 언어 미지정(기본) 메시지의 내부 키 */
    private static final String DEFAULT_LANG = "";

    private DataSource dataSource;

    private String query = "SELECT CODE, LANG, MESSAGE FROM EGOV_MESSAGE_SOURCE";

    private JdbcTemplate jdbcTemplate;

    /** 언어 → (코드 → 메시지). refresh 시 전체를 불변 스냅숏으로 교체한다. */
    private volatile Map<String, Map<String, String>> messages = Collections.emptyMap();

    /**
     * 메시지 테이블을 읽을 DataSource
     *
     * @param dataSource DataSource
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 메시지 조회 쿼리. 결과 컬럼 순서는 (코드, 언어, 메시지) 규약을 따른다.
     *
     * @param query 조회 쿼리
     */
    public void setQuery(String query) {
        this.query = query;
    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(dataSource, "Property 'dataSource' is required");
        Assert.hasText(query, "Property 'query' is required");
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        refresh();
    }

    /**
     * DB 에서 메시지를 다시 읽는다. 운영 중 문구 변경을 재기동 없이 반영할 때 호출한다.
     * 읽기가 끝나면 스냅숏을 통째로 교체하므로 읽는 도중에도 이전 스냅숏으로 조회가 계속된다.
     */
    public void refresh() {
        Assert.state(jdbcTemplate != null, "EgovDbMessageSource is not initialized - call afterPropertiesSet() first");
        Map<String, Map<String, String>> loaded = new HashMap<>();
        jdbcTemplate.query(query, resultSet -> {
            String code = resultSet.getString(1);
            String lang = resultSet.getString(2);
            String message = resultSet.getString(3);
            String langKey = (lang == null || lang.trim().isEmpty()) ? DEFAULT_LANG : lang.trim().toLowerCase(Locale.ROOT);
            loaded.computeIfAbsent(langKey, key -> new HashMap<>()).put(code, message);
        });
        this.messages = Collections.unmodifiableMap(loaded);
        LOGGER.info("EgovDbMessageSource refreshed: {} language group(s), {} message(s)",
                loaded.size(), loaded.values().stream().mapToInt(Map::size).sum());
    }

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        String message = lookup(code, locale);
        return (message == null) ? null : createMessageFormat(message, locale);
    }

    @Override
    protected String resolveCodeWithoutArguments(String code, Locale locale) {
        return lookup(code, locale);
    }

    private String lookup(String code, Locale locale) {
        Map<String, Map<String, String>> snapshot = messages;
        Map<String, String> byLanguage = snapshot.get(locale.getLanguage().toLowerCase(Locale.ROOT));
        if (byLanguage != null) {
            String message = byLanguage.get(code);
            if (message != null) {
                return message;
            }
        }
        Map<String, String> defaults = snapshot.get(DEFAULT_LANG);
        return (defaults == null) ? null : defaults.get(code);
    }

}
