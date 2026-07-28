package org.egovframe.rte.ptl.mvc.tags.ui.pagination;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;
import org.egovframe.rte.ptl.mvc.tags.ui.PaginationTag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockPageContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
public class PaginationTest {

    @Test
    public void defaultPaginationRendererTest() {
        DefaultPaginationRenderer paginationRenderer = new DefaultPaginationRenderer();
        PaginationInfo pageInfo = new PaginationInfo();
        pageInfo.setCurrentPageNo(1);
        pageInfo.setPageSize(5);
        pageInfo.setRecordCountPerPage(10);
        pageInfo.setTotalRecordCount(51);
        String result = paginationRenderer.renderPagination(pageInfo, "");
        assertNotNull(result);
    }

    @Test
    public void defaultPaginationManagerTest() {
        DefaultPaginationManager paginationManager = new DefaultPaginationManager();
        HashMap<String, PaginationRenderer> rendererType = new HashMap<String, PaginationRenderer>();
        rendererType.put("pagination01", new DefaultPaginationRenderer());
        paginationManager.setRendererType(rendererType);
        assertNotNull(paginationManager.getRendererType("pagination01"));
    }

    @Test
    public void paginationTagTest() throws JspException {
        PageContext pageContext = new MockPageContext();

        PaginationTag tag = new PaginationTag();
        tag.setPageContext(pageContext);

        assertEquals(0, tag.doStartTag());
    }

    /**
     * recordCountPerPage 가 미설정(기본값 0)인 경우 getTotalPageCount() 에서
     * '/ by zero' ArithmeticException 이 발생하지 않고 0 을 반환하는지 검증한다.
     */
    @Test
    public void totalPageCountWithZeroRecordCountPerPageTest() {
        PaginationInfo pageInfo = new PaginationInfo();
        pageInfo.setTotalRecordCount(51);
        assertEquals(0, pageInfo.getTotalPageCount());
    }

    /**
     * pageSize 가 미설정(기본값 0)인 경우 getFirstPageNoOnPageList() 에서
     * '/ by zero' ArithmeticException 이 발생하지 않고 1 을 반환하는지 검증한다.
     */
    @Test
    public void firstPageNoOnPageListWithZeroPageSizeTest() {
        PaginationInfo pageInfo = new PaginationInfo();
        pageInfo.setCurrentPageNo(1);
        assertEquals(1, pageInfo.getFirstPageNoOnPageList());
    }

    /**
     * 전체 건수가 0 이면 페이지 수는 0 이어야 한다.
     */
    @Test
    public void totalPageCountWithZeroTotalRecordCountTest() {
        PaginationInfo pageInfo = new PaginationInfo();
        pageInfo.setRecordCountPerPage(10);
        pageInfo.setTotalRecordCount(0);
        assertEquals(0, pageInfo.getTotalPageCount());
    }

    /**
     * 정상 입력에 대한 계산 결과가 수정 전과 동일하게 유지되는지 회귀 검증한다.
     */
    @Test
    public void paginationCalculationRegressionTest() {
        PaginationInfo pageInfo = new PaginationInfo();
        pageInfo.setCurrentPageNo(3);
        pageInfo.setPageSize(5);
        pageInfo.setRecordCountPerPage(10);
        pageInfo.setTotalRecordCount(51);

        assertEquals(6, pageInfo.getTotalPageCount());
        assertEquals(1, pageInfo.getFirstPageNoOnPageList());
        assertEquals(5, pageInfo.getLastPageNoOnPageList());
        assertEquals(20, pageInfo.getFirstRecordIndex());
        assertEquals(30, pageInfo.getLastRecordIndex());
    }

}
