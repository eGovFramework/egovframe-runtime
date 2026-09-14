package org.egovframe.rte.ptl.mvc.bind.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.metadata.ConstraintDescriptor;
import org.egovframe.rte.fdl.cmmn.exception.BaseException;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.egovframe.rte.fdl.cmmn.exception.FdlException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.validation.BindException;
import org.springframework.validation.DataBinder;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EgovRestExceptionHandler 의 <b>정보 노출·입력 반사</b> 회귀 검증(CWE-209, 반사형 CWE-79 의 서버 측 경로).
 *
 * <p>응답 본문은 클라이언트에게 그대로 전달된다. 어떤 예외에서도 <b>원본 예외 메시지·자바 타입명·
 * 거부된 입력값·파서 상세</b>가 실리지 않아야 하고, 클라이언트가 유발한 오류는 5xx 가 아니라 4xx 여야
 * 한다(5xx 는 스택과 함께 ERROR 로 기록되므로 익명 요청으로 오류 로그를 채울 수 있다).</p>
 *
 * <p>기능 테스트({@link EgovRestExceptionHandlerTest})와 별도로 공격자가 유발할 수 있는 예외를 모아
 * 두고 한 건이라도 새면 실패한다. 예외는 핸들러 메서드를 직접 부르지 않고 {@code @ExceptionHandler}
 * 해석기를 거쳐 보낸다 — 실제 요청 처리에서 어느 메서드로 가는지까지 검증하기 위해서다.</p>
 */
public class EgovRestExceptionHandlerSecurityTest {

    private final EgovRestExceptionHandler handler = new EgovRestExceptionHandler();

    /** 바인딩 대상 — 숫자 필드에 문자열을 넣어 형 변환 실패(typeMismatch)를 만든다. */
    public static class Form {
        private int age;
        private String name;

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    public void 내부_예외의_원본_메시지는_어떤_계층에서도_응답에_실리지_않는다() throws Exception {
        List<Exception> internal = List.of(
                new SQLException("ORA-00942: table SECRET_USERS does not exist"),
                new RuntimeException("/etc/app/config.yml: Permission denied"),
                new FdlException("fdl-internal-secret"),
                new BaseException("base-internal-secret"),
                new IllegalStateException("db password is hunter2"));
        for (Exception e : internal) {
            ResponseEntity<ProblemDetail> response = dispatch(e);
            assertEquals(500, response.getStatusCode().value(), e.getClass().getSimpleName());
            String body = flatten(response);
            assertFalse(body.contains("SECRET") || body.contains("hunter2") || body.contains("Permission denied")
                    || body.contains("internal-secret"), "원본 메시지 노출: " + body);
            assertFalse(body.contains("Exception") || body.contains("java."), "예외 클래스명·스택 흔적: " + body);
        }
    }

    @Test
    public void 형_변환_실패의_거부된_값과_자바_타입명은_응답에_실리지_않는다() throws Exception {
        // Spring 의 typeMismatch 기본 메시지는 "Failed to convert ... 'java.lang.String' ... For input string: \"...\"" 이다
        ResponseEntity<ProblemDetail> response = dispatch(typeMismatch("<script>alert(1)</script>"));

        assertEquals(400, response.getStatusCode().value());
        String body = flatten(response);
        assertTrue(body.contains("age"), "위반 필드명은 남는다: " + body);
        assertFalse(body.contains("<script>") || body.contains("java.lang") || body.contains("For input string"),
                "거부된 입력값·자바 타입명이 반사됨: " + body);
    }

    @Test
    public void 메시지소스에_typeMismatch_코드가_없어도_기본_메시지로_새지_않는다() throws Exception {
        StaticMessageSource messageSource = new StaticMessageSource();
        handler.setMessageSource(messageSource);
        try {
            String body = flatten(dispatch(typeMismatch("<script>alert(1)</script>")));
            assertFalse(body.contains("<script>") || body.contains("java.lang"),
                    "코드 미정의 시 기본 메시지로 폴백하며 샘: " + body);

            messageSource.addMessage("typeMismatch", LocaleContextHolder.getLocale(), "값 형식이 올바르지 않습니다");
            body = flatten(dispatch(typeMismatch("<script>alert(1)</script>")));
            assertTrue(body.contains("값 형식이 올바르지 않습니다"), "코드가 있으면 그 메시지를 쓴다: " + body);
            assertFalse(body.contains("<script>"), body);
        } finally {
            handler.setMessageSource(null);
        }
    }

    @Test
    public void 제약_위반의_invalidValue는_응답에_실리지_않는다() throws Exception {
        Set<ConstraintViolation<?>> violations = new LinkedHashSet<>();
        violations.add(violation("selectOne.userId", "must match pattern", "SECRET-VALUE"));

        ResponseEntity<ProblemDetail> response = dispatch(new ConstraintViolationException(violations));

        assertEquals(400, response.getStatusCode().value());
        String body = flatten(response);
        assertTrue(body.contains("userId") && body.contains("must match pattern"), body);
        assertFalse(body.contains("SECRET-VALUE"), "invalidValue 노출: " + body);
    }

    @Test
    public void 단일_값_형_변환_실패는_400이고_입력값을_반사하지_않는다() throws Exception {
        MethodArgumentTypeMismatchException e = new MethodArgumentTypeMismatchException(
                "<script>alert(1)</script>", int.class, "id", null,
                new NumberFormatException("For input string: \"<script>alert(1)</script>\""));

        ResponseEntity<ProblemDetail> response = dispatch(e);

        assertEquals(400, response.getStatusCode().value(), "클라이언트 입력 오류는 400 이다(500 이면 스택이 ERROR 로 남는다)");
        String body = flatten(response);
        assertTrue(body.contains("id"), body);
        assertFalse(body.contains("<script>") || body.contains("java.lang") || body.contains("For input string"), body);
    }

    @Test
    public void 읽을_수_없는_본문은_400이고_파서_상세를_싣지_않는다() throws Exception {
        HttpMessageNotReadableException e = new HttpMessageNotReadableException(
                "JSON parse error: Unexpected character; nested exception is com.fasterxml.jackson.core.JsonParseException"
                        + " at [Source: (PushbackInputStream); line: 1, column: 2]",
                new MockHttpInputMessage(new byte[0]));

        ResponseEntity<ProblemDetail> response = dispatch(e);

        assertEquals(400, response.getStatusCode().value());
        String body = flatten(response);
        assertFalse(body.contains("jackson") || body.contains("JSON parse error") || body.contains("PushbackInputStream"),
                "파서 상세 노출: " + body);
    }

    @Test
    public void 확장_필드는_timestamp_errors_messageKey_뿐이다() throws Exception {
        List<Exception> samples = List.of(
                new SQLException("x"),
                typeMismatch("v"),
                new EgovBizException("잔액이 부족합니다"),
                new MethodArgumentTypeMismatchException("v", int.class, "id", null, null));
        for (Exception e : samples) {
            Map<String, Object> properties = dispatch(e).getBody().getProperties();
            assertNotNull(properties);
            assertTrue(Set.of("timestamp", "errors", "messageKey").containsAll(properties.keySet()),
                    "예상 밖 확장 필드(스택·예외명 등): " + properties.keySet());
        }
    }

    // ------------------------------------------------------------ helpers

    /** 실제 요청 처리와 같은 규칙({@code @ExceptionHandler} 해석)으로 핸들러 메서드를 고른 뒤 호출한다. */
    @SuppressWarnings("unchecked")
    private ResponseEntity<ProblemDetail> dispatch(Exception e) throws Exception {
        Method method = new ExceptionHandlerMethodResolver(EgovRestExceptionHandler.class).resolveMethod(e);
        assertNotNull(method, "매핑되는 @ExceptionHandler 가 있어야 한다: " + e.getClass().getName());
        return (ResponseEntity<ProblemDetail>) method.invoke(handler, e);
    }

    private static String flatten(ResponseEntity<ProblemDetail> response) {
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        return problem.getTitle() + "|" + problem.getDetail() + "|" + problem.getProperties();
    }

    private static BindException typeMismatch(String rawValue) {
        DataBinder binder = new DataBinder(new Form(), "form");
        MutablePropertyValues values = new MutablePropertyValues();
        values.add("age", rawValue);
        values.add("name", "ok");
        binder.bind(values);
        return new BindException(binder.getBindingResult());
    }

    /** 핸들러는 getMessage()·getPropertyPath() 만 읽는다 — 검증 구현체 없이 스텁으로 충분하다. */
    private static ConstraintViolation<Object> violation(String path, String message, Object invalidValue) {
        return new ConstraintViolation<Object>() {
            @Override
            public String getMessage() {
                return message;
            }

            @Override
            public String getMessageTemplate() {
                return message;
            }

            @Override
            public Object getRootBean() {
                return null;
            }

            @Override
            public Class<Object> getRootBeanClass() {
                return Object.class;
            }

            @Override
            public Object getLeafBean() {
                return null;
            }

            @Override
            public Object[] getExecutableParameters() {
                return new Object[0];
            }

            @Override
            public Object getExecutableReturnValue() {
                return null;
            }

            @Override
            public Path getPropertyPath() {
                return new Path() {
                    @Override
                    public Iterator<Node> iterator() {
                        return Collections.emptyIterator();
                    }

                    @Override
                    public String toString() {
                        return path;
                    }
                };
            }

            @Override
            public Object getInvalidValue() {
                return invalidValue;
            }

            @Override
            public ConstraintDescriptor<?> getConstraintDescriptor() {
                return null;
            }

            @Override
            public <U> U unwrap(Class<U> type) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
