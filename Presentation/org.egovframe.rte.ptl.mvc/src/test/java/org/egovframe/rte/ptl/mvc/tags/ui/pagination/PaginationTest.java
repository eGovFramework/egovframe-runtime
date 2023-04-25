package org.egovframe.rte.ptl.mvc.tags.ui.pagination;

import org.egovframe.rte.ptl.mvc.tags.ui.PaginationTag;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockPageContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:META-INF/spring/pagination/test-servlet.xml")
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
