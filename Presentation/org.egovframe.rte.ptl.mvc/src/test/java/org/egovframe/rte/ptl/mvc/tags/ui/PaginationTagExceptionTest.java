package org.egovframe.rte.ptl.mvc.tags.ui;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.Tag;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockJspWriter;
import org.springframework.mock.web.MockPageContext;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

class PaginationTagExceptionTest {

    @Test
    void rendersPaginationAndContinuesPage() throws JspException, IOException {
        MockPageContext context = new MockPageContext();
        assertEquals(Tag.EVAL_PAGE, newTag(context).doEndTag());
        assertTrue(context.getContentAsString().contains("<strong>1</strong>"));
        assertTrue(context.getContentAsString().contains("goPage(2)"));
    }

    @Test
    void preservesOutputFailureAsCause() {
        IOException failure = new IOException("Cannot write pagination output");
        JspWriter writer = new MockJspWriter(new StringWriter()) {
            @Override
            public void println(String value) throws IOException {
                throw failure;
            }
        };
        MockPageContext context = new MockPageContext() {
            @Override
            public JspWriter getOut() {
                return writer;
            }
        };

        JspException exception = assertThrows(JspException.class, () -> newTag(context).doEndTag());
        assertSame(failure, exception.getCause());
    }

    private PaginationTag newTag(PageContext context) {
        PaginationInfo info = new PaginationInfo();
        info.setCurrentPageNo(1);
        info.setPageSize(5);
        info.setRecordCountPerPage(10);
        info.setTotalRecordCount(51);
        PaginationTag tag = new PaginationTag();
        tag.setPageContext(context);
        tag.setPaginationInfo(info);
        tag.setJsFunction("goPage");
        return tag;
    }
}
