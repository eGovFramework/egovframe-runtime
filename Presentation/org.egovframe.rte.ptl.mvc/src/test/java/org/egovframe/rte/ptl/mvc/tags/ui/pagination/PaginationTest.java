package org.egovframe.rte.ptl.mvc.tags.ui.pagination;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;
import org.egovframe.rte.ptl.mvc.tags.ui.PaginationTag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockPageContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Modifier;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    /**
     * AbstractKrdsPaginationRenderer는 이름·Javadoc·형제 클래스(AbstractPaginationRenderer)와
     * 동일하게 하위 클래스가 라벨 필드를 채워야 하는 계약이므로, 직접 인스턴스화를 막는
     * abstract 클래스여야 한다.
     */
    @Test
    public void abstractKrdsPaginationRendererIsAbstractTest() {
        assertTrue(Modifier.isAbstract(AbstractKrdsPaginationRenderer.class.getModifiers()));
    }

    /**
     * 라벨 필드를 채운 하위 클래스에서는 renderPagination()이 정상 동작해야 한다
     * (abstract 전환이 정상적인 서브클래싱 사용까지 막지 않는지 검증).
     */
    @Test
    public void abstractKrdsPaginationRendererRendersWhenSubclassedTest() {
        AbstractKrdsPaginationRenderer renderer = new AbstractKrdsPaginationRenderer() {
            {
                firstPageLabel = "[first]";
                previousPageLabel = "[prev]";
                previousPageDisabledLabel = "[prev-disabled]";
                currentPageLabel = "{0}";
                otherPageLabel = "{2}";
                nextPageLabel = "[next]";
                nextPageDisabledLabel = "[next-disabled]";
                lastPageLabel = "[last]";
                dotPageLabel = "...";
            }
        };

        PaginationInfo pageInfo = new PaginationInfo();
        pageInfo.setCurrentPageNo(1);
        pageInfo.setPageSize(5);
        pageInfo.setRecordCountPerPage(10);
        pageInfo.setTotalRecordCount(51);

        String result = renderer.renderPagination(pageInfo, "");
        assertNotNull(result);
    }

}
