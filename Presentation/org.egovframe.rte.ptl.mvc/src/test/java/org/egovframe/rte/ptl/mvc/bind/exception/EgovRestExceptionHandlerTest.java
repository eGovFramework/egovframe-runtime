package org.egovframe.rte.ptl.mvc.bind.exception;

import org.egovframe.rte.fdl.cmmn.exception.BaseException;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.egovframe.rte.fdl.cmmn.exception.FdlException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * EgovRestExceptionHandler — RFC 9457(ProblemDetail) 매핑 기본 구현 검증.
 */
public class EgovRestExceptionHandlerTest {

    private final EgovRestExceptionHandler handler = new EgovRestExceptionHandler();

    @Test
    public void 업무예외는_400과_메시지를_그대로_전달한다() {
        ResponseEntity<ProblemDetail> response =
                handler.handleEgovBizException(new EgovBizException("잔액이 부족합니다"));

        assertEquals(400, response.getStatusCode().value());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Business Error", problem.getTitle());
        assertEquals("잔액이 부족합니다", problem.getDetail());
        assertNotNull(problem.getProperties().get("timestamp"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void 제약위반은_400과_필드오류_목록으로_응답하고_거부된_값은_싣지_않는다() {
        // 종전에는 Exception 폴백으로 500 이 되던 정합성 결함 — 400 계약의 회귀 고정
        org.springframework.validation.BindException bindException =
                new org.springframework.validation.BindException(new Object(), "memberDto");
        bindException.addError(new org.springframework.validation.FieldError("memberDto", "email",
                "secret-input@internal", false, new String[] {"EgovEmailCheck.memberDto.email"}, null,
                "이메일 형식이 아닙니다"));
        bindException.addError(new org.springframework.validation.FieldError("memberDto", "name",
                null, false, null, null, "이름은 필수입니다"));

        ResponseEntity<ProblemDetail> response = handler.handleBindException(bindException);

        assertEquals(400, response.getStatusCode().value());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Validation Failed", problem.getTitle());
        java.util.List<java.util.Map<String, String>> errors =
                (java.util.List<java.util.Map<String, String>>) problem.getProperties().get("errors");
        assertEquals(2, errors.size(), "위반 필드 전부가 목록으로 담겨야 한다");
        assertEquals("email", errors.get(0).get("field"));
        assertEquals("이메일 형식이 아닙니다", errors.get(0).get("message"));
        assertFalse(errors.toString().contains("secret-input"),
                "거부된 입력 값은 어떤 형태로도 응답에 실리면 안 된다(입력 반사·민감값 노출 방지)");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void 파라미터_제약위반도_동일_형식의_400으로_정규화된다() {
        jakarta.validation.ConstraintViolationException violationException =
                new jakarta.validation.ConstraintViolationException(
                        java.util.Set.of(violation("selectMember.userId", "사용자 ID 형식이 아닙니다")));

        ResponseEntity<ProblemDetail> response = handler.handleConstraintViolation(violationException);

        assertEquals(400, response.getStatusCode().value());
        java.util.List<java.util.Map<String, String>> errors =
                (java.util.List<java.util.Map<String, String>>) response.getBody().getProperties().get("errors");
        assertEquals("userId", errors.get(0).get("field"), "property path 말단이 필드명으로 정규화되어야 한다");
        assertEquals("사용자 ID 형식이 아닙니다", errors.get(0).get("message"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void 메시지소스_연동_시_코드_해석이_기본_메시지보다_우선한다() {
        org.springframework.context.support.StaticMessageSource messages =
                new org.springframework.context.support.StaticMessageSource();
        messages.addMessage("EgovEmailCheck.memberDto.email",
                org.springframework.context.i18n.LocaleContextHolder.getLocale(), "해석된 이메일 오류 메시지");
        EgovRestExceptionHandler resolvingHandler = new EgovRestExceptionHandler();
        resolvingHandler.setMessageSource(messages);
        org.springframework.validation.BindException bindException =
                new org.springframework.validation.BindException(new Object(), "memberDto");
        bindException.addError(new org.springframework.validation.FieldError("memberDto", "email",
                null, false, new String[] {"EgovEmailCheck.memberDto.email"}, null, "기본 메시지"));

        java.util.List<java.util.Map<String, String>> errors = (java.util.List<java.util.Map<String, String>>)
                resolvingHandler.handleBindException(bindException).getBody().getProperties().get("errors");

        assertEquals("해석된 이메일 오류 메시지", errors.get(0).get("message"), "코드 해석 우선");
        // 미연동(기본 핸들러)은 기본 메시지로 완결 — 위 제약위반 테스트가 함께 증명한다
    }

    /** ConstraintViolation 테스트 스텁 — 핸들러가 쓰는 propertyPath(toString)·message 만 실값 */
    private static jakarta.validation.ConstraintViolation<Object> violation(String path, String message) {
        return new jakarta.validation.ConstraintViolation<>() {
            @Override public String getMessage() { return message; }
            @Override public String getMessageTemplate() { return message; }
            @Override public Object getRootBean() { return null; }
            @Override public Class<Object> getRootBeanClass() { return Object.class; }
            @Override public Object getLeafBean() { return null; }
            @Override public Object[] getExecutableParameters() { return new Object[0]; }
            @Override public Object getExecutableReturnValue() { return null; }
            @Override public jakarta.validation.Path getPropertyPath() {
                return new jakarta.validation.Path() {
                    @Override public java.util.Iterator<Node> iterator() { return java.util.Collections.emptyIterator(); }
                    @Override public String toString() { return path; }
                };
            }
            @Override public Object getInvalidValue() { return null; }
            @Override public jakarta.validation.metadata.ConstraintDescriptor<?> getConstraintDescriptor() { return null; }
            @Override public <U> U unwrap(Class<U> type) { throw new UnsupportedOperationException(); }
        };
    }

    @Test
    public void 내부예외는_500과_일반화_메시지로_응답한다_CWE209() {
        ResponseEntity<ProblemDetail> fdl = handler.handleFdlException(
                new FdlException("jdbc:oracle:thin:@10.0.0.1 connection failed"));
        ResponseEntity<ProblemDetail> base = handler.handleBaseException(
                new BaseException("internal table EGOV_SECRET missing"));
        ResponseEntity<ProblemDetail> plain = handler.handleException(
                new IllegalStateException("stacktrace details"));

        for (ResponseEntity<ProblemDetail> response : java.util.List.of(fdl, base, plain)) {
            assertEquals(500, response.getStatusCode().value());
            ProblemDetail problem = response.getBody();
            assertNotNull(problem);
            assertEquals("Internal Server Error", problem.getTitle());
            // 내부 정보(접속 문자열·테이블명 등)가 응답에 노출되면 안 된다
            assertFalse(problem.getDetail().contains("oracle"));
            assertFalse(problem.getDetail().contains("EGOV_SECRET"));
        }
    }

}
