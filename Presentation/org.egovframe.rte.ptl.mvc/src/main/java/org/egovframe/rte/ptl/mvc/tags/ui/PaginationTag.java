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
package org.egovframe.rte.ptl.mvc.tags.ui;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.TagSupport;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.DefaultPaginationManager;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationManager;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationRenderer;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.io.IOException;

/**
 * PaginationTag.java
 * <p/><b>NOTE:</b><pre> 페이징을 위한 Tag class .
 * 실제 페이징을 위한 작업은 PaginationRenderer에게 위임한다.
 * 어떤 PaginationRenderer를 사용할지는 PaginationManager에게 위임하는데, PaginationManager는 빈설정 파일의 정보와
 * 태그의 type 속성값을 비교하여 PaginationRenderer을 결정한다.
 * </pre>
 *
 * @author 실행환경 개발팀 함철
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.06.01	함철				최초 생성
 * 2025.09.07   chanseok2323    PaginationTag 상수 처리
 * </pre>
 * @since 2009.06.01
 */
public class PaginationTag extends TagSupport {

    private static final String PAGINATION_MANAGER_BEAN_NAME = "paginationManager";

    private PaginationInfo paginationInfo;
    private String type;
    private String jsFunction;

    public int doEndTag() throws JspException {

        try {
            JspWriter out = pageContext.getOut();
            PaginationManager paginationManager;
            WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(pageContext.getServletContext());
            WebApplicationContext ctxd = RequestContextUtils.findWebApplicationContext((HttpServletRequest) pageContext.getRequest(), pageContext.getServletContext());

            if (!ObjectUtils.isEmpty(ctx) && ctx.containsBean(PAGINATION_MANAGER_BEAN_NAME)) {
                paginationManager = (PaginationManager) ctx.getBean(PAGINATION_MANAGER_BEAN_NAME);
            } else if (!ObjectUtils.isEmpty(ctxd) && ctxd.containsBean(PAGINATION_MANAGER_BEAN_NAME)) {
                paginationManager = (PaginationManager) ctxd.getBean(PAGINATION_MANAGER_BEAN_NAME);
            } else {
                //bean 정의가 없다면 DefaultPaginationManager를 사용. 빈설정이 없으면 기본 적인 페이징 리스트라도 보여주기 위함.
                paginationManager = new DefaultPaginationManager();
            }

            PaginationRenderer paginationRenderer = paginationManager.getRendererType(type);
            String contents = paginationRenderer.renderPagination(paginationInfo, jsFunction);
            out.println(contents);
            return EVAL_PAGE;
        } catch (IOException e) {
            throw new JspException();
        }
    }

    public void setJsFunction(String jsFunction) {
        this.jsFunction = jsFunction;
    }

    public void setPaginationInfo(PaginationInfo paginationInfo) {
        this.paginationInfo = paginationInfo;
    }

    public void setType(String type) {
        this.type = type;
    }

}
