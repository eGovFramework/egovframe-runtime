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
package org.egovframe.rte.ptl.mvc.tags.ui.pagination;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

/**
 * MessageSource 로 라벨을 가져오는 다국어 페이징 렌더러.
 *
 * <p>기존 렌더러는 [처음]/[이전]/[다음]/[마지막] 라벨이 한국어로 고정되어 다국어 화면에서 바꿀 수 없다. 이 렌더러는 요청
 * 로케일(Locale)({@link LocaleContextHolder}) 기준으로 {@link MessageSource} 에서 라벨 문구를 가져오고, 메시지가 없으면
 * 기존 한국어 라벨로 돌아간다. 라벨은 렌더링 시점에 지역 변수로만 다루므로 스레드 안전하다.</p>
 *
 * <p>메시지 키: {@value #KEY_FIRST}, {@value #KEY_PREVIOUS}, {@value #KEY_NEXT}, {@value #KEY_LAST}</p>
 *
 * <pre>
 * &lt;bean id="imageRenderer" class="org.egovframe.rte.ptl.mvc.tags.ui.pagination.MessageSourcePaginationRenderer"/&gt;
 * &lt;!-- messages_en.properties: pagination.link.first=First ... --&gt;
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
public class MessageSourcePaginationRenderer extends AbstractPaginationRenderer implements MessageSourceAware {

    /** 처음 페이지 라벨 메시지 키 */
    public static final String KEY_FIRST = "pagination.link.first";
    /** 이전 페이지 라벨 메시지 키 */
    public static final String KEY_PREVIOUS = "pagination.link.previous";
    /** 다음 페이지 라벨 메시지 키 */
    public static final String KEY_NEXT = "pagination.link.next";
    /** 마지막 페이지 라벨 메시지 키 */
    public static final String KEY_LAST = "pagination.link.last";

    private static final String DEFAULT_FIRST = "처음";
    private static final String DEFAULT_PREVIOUS = "이전";
    private static final String DEFAULT_NEXT = "다음";
    private static final String DEFAULT_LAST = "마지막";

    private MessageSource messageSource;

    public MessageSourcePaginationRenderer() {
        firstPageLabel = linkTemplate(DEFAULT_FIRST);
        previousPageLabel = linkTemplate(DEFAULT_PREVIOUS);
        currentPageLabel = "<strong>{0}</strong>&#160;";
        otherPageLabel = "<a href=\"#\" onclick=\"{0}({1}); return false;\">{2}</a>&#160;";
        nextPageLabel = linkTemplate(DEFAULT_NEXT);
        lastPageLabel = linkTemplate(DEFAULT_LAST);
    }

    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public String renderPagination(PaginationInfo paginationInfo, String jsFunction) {
        if (messageSource == null) {
            return super.renderPagination(paginationInfo, jsFunction);
        }
        Locale locale = LocaleContextHolder.getLocale();
        String first = messageSource.getMessage(KEY_FIRST, null, DEFAULT_FIRST, locale);
        String previous = messageSource.getMessage(KEY_PREVIOUS, null, DEFAULT_PREVIOUS, locale);
        String next = messageSource.getMessage(KEY_NEXT, null, DEFAULT_NEXT, locale);
        String last = messageSource.getMessage(KEY_LAST, null, DEFAULT_LAST, locale);
        return doRender(paginationInfo, jsFunction,
                linkTemplate(first), linkTemplate(previous), currentPageLabel, otherPageLabel,
                linkTemplate(next), linkTemplate(last));
    }

    private static String linkTemplate(String labelText) {
        return "<a href=\"#\" onclick=\"{0}({1}); return false;\">[" + escapeForMessageFormat(labelText) + "]</a>&#160;";
    }

    /**
     * 라벨 문구를 MessageFormat 템플릿에 안전하게 넣는다(작은따옴표·중괄호 이스케이프).
     */
    private static String escapeForMessageFormat(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("'", "''").replace("{", "'{'").replace("}", "'}'");
    }

}
