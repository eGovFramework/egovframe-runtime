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

}
