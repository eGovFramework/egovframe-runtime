package org.egovframe.rte.ptl.mvc.tags.ui.pagination;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MessageSourcePaginationRenderer 의 로케일별 라벨 조회·폴백과 AbstractPaginationRenderer 의 라벨 setter 를 검증한다.
 */
public class MessageSourcePaginationRendererTest {

    @AfterEach
    public void resetLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    private PaginationInfo paginationInfo() {
        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setCurrentPageNo(15);
        paginationInfo.setRecordCountPerPage(10);
        paginationInfo.setPageSize(10);
        paginationInfo.setTotalRecordCount(500);
        return paginationInfo;
    }

    private static StaticMessageSource englishMessages() {
        StaticMessageSource messageSource = new StaticMessageSource();
        messageSource.addMessage(MessageSourcePaginationRenderer.KEY_FIRST, Locale.ENGLISH, "First");
        messageSource.addMessage(MessageSourcePaginationRenderer.KEY_PREVIOUS, Locale.ENGLISH, "Prev");
        messageSource.addMessage(MessageSourcePaginationRenderer.KEY_NEXT, Locale.ENGLISH, "Next");
        messageSource.addMessage(MessageSourcePaginationRenderer.KEY_LAST, Locale.ENGLISH, "Last");
        return messageSource;
    }

    @Test
    public void testLabelsAreResolvedFromMessageSourceForTheRequestLocale() {
        MessageSourcePaginationRenderer renderer = new MessageSourcePaginationRenderer();
        renderer.setMessageSource(englishMessages());
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        String rendered = renderer.renderPagination(paginationInfo(), "goPage");

        assertTrue(rendered.contains("[First]"), rendered);
        assertTrue(rendered.contains("[Prev]"), rendered);
        assertTrue(rendered.contains("[Next]"), rendered);
        assertTrue(rendered.contains("[Last]"), rendered);
        assertFalse(rendered.contains("[처음]"), rendered);
        assertTrue(rendered.contains("goPage(1)"), rendered);
        assertTrue(rendered.contains("<strong>15</strong>"), rendered);
    }

    @Test
    public void testRenderingIsIdenticalToDefaultRendererForKoreanLocale() {
        MessageSourcePaginationRenderer renderer = new MessageSourcePaginationRenderer();
        renderer.setMessageSource(englishMessages());
        LocaleContextHolder.setLocale(Locale.KOREAN);

        assertEquals(new DefaultPaginationRenderer().renderPagination(paginationInfo(), "goPage"),
                renderer.renderPagination(paginationInfo(), "goPage"));
    }

    @Test
    public void testFallsBackToKoreanDefaultsWhenMessageIsMissing() {
        MessageSourcePaginationRenderer renderer = new MessageSourcePaginationRenderer();
        renderer.setMessageSource(new StaticMessageSource());
        LocaleContextHolder.setLocale(Locale.FRENCH);

        String rendered = renderer.renderPagination(paginationInfo(), "goPage");

        assertTrue(rendered.contains("[처음]"), rendered);
        assertTrue(rendered.contains("[마지막]"), rendered);
    }

    @Test
    public void testWorksWithoutMessageSource() {
        MessageSourcePaginationRenderer renderer = new MessageSourcePaginationRenderer();

        String rendered = renderer.renderPagination(paginationInfo(), "goPage");

        assertTrue(rendered.contains("[처음]"), rendered);
        assertTrue(rendered.contains("[다음]"), rendered);
    }

    @Test
    public void testLabelTextWithMessageFormatSpecialCharactersIsRenderedLiterally() {
        StaticMessageSource messageSource = new StaticMessageSource();
        messageSource.addMessage(MessageSourcePaginationRenderer.KEY_NEXT, Locale.FRENCH, "Suivant {1} d'accord");
        MessageSourcePaginationRenderer renderer = new MessageSourcePaginationRenderer();
        renderer.setMessageSource(messageSource);
        LocaleContextHolder.setLocale(Locale.FRENCH);

        String rendered = renderer.renderPagination(paginationInfo(), "goPage");

        assertTrue(rendered.contains("[Suivant {1} d'accord]"), rendered);
    }

    @Test
    public void testLabelSettersAllowCustomizationOfExistingRenderers() {
        DefaultPaginationRenderer renderer = new DefaultPaginationRenderer();
        renderer.setFirstPageLabel("<a href=\"#\" onclick=\"{0}({1}); return false;\">&lt;&lt;</a>");

        String rendered = renderer.renderPagination(paginationInfo(), "goPage");

        assertTrue(rendered.contains("&lt;&lt;"), rendered);
        assertFalse(rendered.contains("[처음]"), rendered);
    }

}
