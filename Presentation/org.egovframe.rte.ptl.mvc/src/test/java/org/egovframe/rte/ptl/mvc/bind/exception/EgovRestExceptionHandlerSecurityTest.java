package org.egovframe.rte.ptl.mvc.bind.exception;

import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.junit.Test;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * REST 예외 응답이 내부 정보·거부된 입력값을 노출하지 않는지 검증한다(CWE-209).
 */
public class EgovRestExceptionHandlerSecurityTest {

    private final EgovRestExceptionHandler handler = new EgovRestExceptionHandler();

    @Test
    public void 미분류_예외는_일반화_메시지로_500() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleException(new IllegalStateException("ORA-00942: table SECRET_TBL does not exist"));
        assertEquals(500, response.getStatusCodeValue());
        assertFalse(String.valueOf(response.getBody()), String.valueOf(response.getBody()).contains("SECRET_TBL"));
        assertEquals("application/problem+json", response.getHeaders().getContentType().toString());
    }

    @Test
    public void 바인딩_오류는_거부된_값을_싣지_않는다() {
        Bean target = new Bean();
        BindException e = new BindException(new BeanPropertyBindingResult(target, "bean"));
        e.rejectValue("name", "Size", "<script>alert(1)</script> is too long");
        e.addError(new org.springframework.validation.FieldError("bean", "age", "abc<svg>", true,
                new String[]{"typeMismatch"}, null, "Failed to convert value 'abc<svg>' to java.lang.Integer"));
        ResponseEntity<Map<String, Object>> response = handler.handleBindException(e);
        assertEquals(400, response.getStatusCodeValue());
        String body = String.valueOf(response.getBody());
        assertFalse(body, body.contains("abc<svg>"));
        assertFalse(body, body.contains("java.lang.Integer"));
        assertTrue(body, body.contains("Invalid value."));
    }

    @Test
    public void 형_변환_실패와_깨진_본문은_400_일반화_메시지() {
        ResponseEntity<Map<String, Object>> mismatch =
                handler.handleTypeMismatch(new TypeMismatchException("1' OR '1'='1", Integer.class));
        assertEquals(400, mismatch.getStatusCodeValue());
        assertFalse(String.valueOf(mismatch.getBody()).contains("OR '1'"));

        ResponseEntity<Map<String, Object>> unreadable = handler.handleNotReadable(new HttpMessageNotReadableException(
                "JSON parse error: Unexpected character at [Source: com.example.SecretDto]",
                new MockHttpInputMessage(new byte[0])));
        assertEquals(400, unreadable.getStatusCodeValue());
        assertFalse(String.valueOf(unreadable.getBody()).contains("SecretDto"));
    }

    @Test
    public void 표준_예외는_상태를_보존하고_요청_경로를_되비추지_않는다() {
        ResponseEntity<Map<String, Object>> notAllowed = handler.handleException(
                new HttpRequestMethodNotSupportedException("DELETE", Arrays.asList("GET", "POST")));
        assertEquals(405, notAllowed.getStatusCodeValue());
        assertTrue(notAllowed.getHeaders().getAllow().contains(org.springframework.http.HttpMethod.GET));

        ResponseEntity<Map<String, Object>> notFound = handler.handleException(
                new NoHandlerFoundException("GET", "/<script>alert(1)</script>", new org.springframework.http.HttpHeaders()));
        assertEquals(HttpStatus.NOT_FOUND.value(), notFound.getStatusCodeValue());
        assertFalse(String.valueOf(notFound.getBody()).contains("<script>"));
    }

    @Test
    public void 업무_예외는_사용자_메시지를_전달한다() {
        ResponseEntity<Map<String, Object>> response = handler.handleEgovBizException(new EgovBizException("잔액이 부족합니다."));
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("잔액이 부족합니다.", response.getBody().get("detail"));
    }

    @Test
    public void 인증_인가_예외는_보안_계층으로_다시_던진다() {
        // ptl.mvc 는 Spring Security 에 의존하지 않으므로 테스트 전용 동명 클래스로 이름 판별을 검증한다
        RuntimeException denied = new org.springframework.security.access.AccessDeniedException("denied");
        assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> handler.handleException(denied));
        // 같은 단순 이름의 다른 패키지 예외는 보안 예외로 보지 않는다
        assertEquals(500, handler.handleException(new AccessDeniedException("fake")).getStatusCodeValue());
    }

    @SuppressWarnings("unused")
    static class Bean {
        private String name;
        private Integer age;

        public String getName() {
            return name;
        }

        public Integer getAge() {
            return age;
        }
    }

    static class AccessDeniedException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        AccessDeniedException(String message) {
            super(message);
        }
    }

}
