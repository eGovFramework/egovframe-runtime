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
package org.egovframe.rte.ptl.reactive.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 발생한 오류를 처리하고 동일한 형식으로 응답을 보내기 위한 클래스
 *
 * <p>Desc.: 발생한 오류를 처리하고 동일한 형식으로 응답을 보내기 위한 클래스</p>
 *
 * <p>응답 본문은 기본적으로 {@code timestamp}/{@code status}/{@code code}/{@code message} 형식이다.
 * {@link #setProblemDetailEnabled(boolean)} 로 RFC 9457(Problem Details, {@code application/problem+json})
 * 형식을 선택할 수 있다.</p>
 *
 * <p>자기 HTTP 상태를 가진 Spring 표준 예외({@link ErrorResponse} — 405·415·404·필수 파라미터 누락·깨진 본문·
 * 본문 크기 초과 등)는 그 상태와 헤더(405 의 허용 메서드 등)로 응답한다. 종전에는 미분류로 보아 500 이었다
 * (정합성 결함 수정 — 5.0.x 대비 동작 변경). 기본 형식의 {@code code} 는 상태에 대응하는 오류 코드이고, 대응이
 * 없으면 4xx 는 E001·5xx 는 E021 을 쓰되 {@code status} 는 실제 값이다. 설명은 일반화 문구이며 요청 경로·값을
 * 되비추지 않는다. 입력 검증 실패({@link WebExchangeBindException}·프레임워크 메서드 파라미터 검증)는 400 과
 * 필드 오류 목록 {@code errors}(필드·메시지, 거부 값 없음)로, 형 불일치는 E002 로 응답한다 — 서블릿 MVC 오류
 * 응답과 같은 모양이다. {@link #setMessageSource(MessageSource)} 를 연동하면 필드 메시지는 오류 코드 해석이
 * 우선이다. 이미 전송이 시작된 응답은 건드리지 않고 오류를 그대로 전파한다.</p>
 *
 * @author 유지보수
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2023.08.31   유지보수            최초 생성
 * 2026.09.10   실행환경 개발팀      json-simple 수기 직렬화를 Jackson 으로 교체, RFC 9457 problem+json 선택 옵션 추가
 * 2026.09.30   실행환경 개발팀      표준 예외 상태 보존·입력 검증 400·커밋된 응답 불간섭
 * </pre>
 * @since 2023.08.31
 */
@Order(-3)
public class EgovExceptionHandler implements WebExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovExceptionHandler.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final MediaType PROBLEM_JSON_UTF8 = new MediaType("application", "problem+json", StandardCharsets.UTF_8);

    /** 입력 검증 실패 응답의 제목·메시지 — 서블릿 MVC 오류 응답과 같은 문구(두 스택 의미 일치) */
    private static final String VALIDATION_TITLE = "Validation Failed";
    private static final String VALIDATION_MESSAGE = "Request validation failed.";

    /** 형 변환 실패의 일반화 필드 메시지 — 기본 메시지는 거부된 입력값·자바 타입명을 담는다 */
    private static final String BINDING_FAILURE_MESSAGE = "Invalid value.";

    /** 대응 오류 코드가 없는 4xx 표준 예외의 일반화 메시지 */
    private static final String CLIENT_ERROR_MESSAGE = "The request could not be processed.";

    /** 요청 쪽 이름을 가진 파라미터 애너테이션 — 필드 오류의 이름으로 쓴다 */
    private static final Set<Class<? extends Annotation>> NAMED_VALUE_ANNOTATIONS = Set.of(
            RequestParam.class, PathVariable.class, RequestHeader.class,
            CookieValue.class, MatrixVariable.class, RequestPart.class);

    /**
     * RFC 9457(Problem Details) 응답 형식 사용 여부. 기본값 false 는 기존 형식을 유지한다.
     */
    private boolean problemDetailEnabled = false;

    /**
     * RFC 9457({@code application/problem+json}) 응답 형식을 선택한다.
     * false(기본)면 기존 {@code timestamp}/{@code status}/{@code code}/{@code message} 형식을 유지한다.
     *
     * @param problemDetailEnabled true 면 Problem Details 형식으로 응답
     */
    public void setProblemDetailEnabled(boolean problemDetailEnabled) {
        this.problemDetailEnabled = problemDetailEnabled;
    }

    /**
     * RFC 9457 응답 형식 사용 여부
     *
     * @return true 면 Problem Details 형식
     */
    public boolean isProblemDetailEnabled() {
        return problemDetailEnabled;
    }

    /**
     * 필드 오류 메시지 해석용 메시지 소스. 기본 null(미연동)이다.
     */
    private MessageSource messageSource;

    /**
     * 필드 오류 메시지 해석용 {@link MessageSource} 를 연동한다. 연동하면 오류 코드 해석이 우선하고,
     * 형 변환 실패는 코드만 해석한다(입력값을 담은 기본 메시지로 폴백하지 않는다). 미연동이면 제약의 기본 메시지다.
     *
     * @param messageSource 메시지 소스(null 이면 연동 해제)
     * @since 5.1
     */
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            // 이미 전송이 시작된 응답(스트리밍·SSE 도중 등)은 상태·본문을 다시 쓸 수 없다 —
            // 오류를 그대로 흘려 서버의 기본 처리(연결 종료)에 맡긴다
            LOGGER.debug("Response already committed, propagating error: {}", ex.toString());
            return Mono.error(ex);
        }
        if (ex instanceof EgovException) {
            EgovException egovException = (EgovException) ex;
            EgovErrorCode egovErrorCode = egovException.getEgovErrorCode();
            String message = resolveResponseMessage(egovErrorCode, egovException.getMessage(), ex);
            return getExceptionResponse(response, egovErrorCode, message);
        } else if (ex instanceof EgovServiceException) {
            EgovServiceException egovServiceException = (EgovServiceException) ex;
            EgovErrorCode egovErrorCode = egovServiceException.getEgovErrorCode();
            String message = resolveResponseMessage(egovErrorCode, egovServiceException.getMessage(), ex);
            return getExceptionResponse(response, egovErrorCode, message);
        } else if (ex instanceof WebExchangeBindException bindException) {
            return validationResponse(response, bindingErrors(bindException, locale(exchange)));
        } else if (ex instanceof HandlerMethodValidationException methodValidation
                && !methodValidation.getStatusCode().is5xxServerError()) {
            return validationResponse(response, methodValidationErrors(methodValidation, locale(exchange)));
        } else if (ex instanceof ServerWebInputException inputException
                && inputException.getCause() instanceof TypeMismatchException typeMismatch) {
            return typeMismatchResponse(response, inputException, typeMismatch, locale(exchange));
        } else if (ex instanceof ErrorResponse errorResponse) {
            // 반환값 검증 실패(프레임워크가 500 으로 분류)도 여기서 5xx 일반화로 처리된다
            return standardResponse(response, errorResponse, ex);
        } else {
            EgovErrorCode egovErrorCode = EgovErrorCode.INTERNAL_SERVER_ERROR;
            LOGGER.error("Unhandled exception while processing request", ex);
            return getExceptionResponse(response, egovErrorCode, egovErrorCode.getMessage());
        }
    }

    /**
     * INTERNAL_SERVER_ERROR로 분류된 예외는 하위 계층(DB 등) 원본 메시지를 그대로 감싼 것일 수 있어
     * (CWE-209 오류 메시지를 통한 정보노출) 클라이언트 응답에는 일반화된 메시지만 노출하고, 원본
     * 메시지·스택트레이스는 서버 로그로만 남긴다. 그 외 명시적 오류코드(입력값 검증 등 개발자가
     * 의도적으로 설정한 사용자 대상 메시지)는 기존처럼 그대로 전달한다.
     */
    private String resolveResponseMessage(EgovErrorCode egovErrorCode, String originalMessage, Throwable ex) {
        if (egovErrorCode == EgovErrorCode.INTERNAL_SERVER_ERROR) {
            LOGGER.error("Internal error while processing request: {}", originalMessage, ex);
            return egovErrorCode.getMessage();
        }
        return originalMessage;
    }

    private Mono<Void> getExceptionResponse(ServerHttpResponse response, EgovErrorCode egovErrorCode, String message) {
        return handlerResponse(response, EgovExceptionResponse.of(egovErrorCode, message), null, null);
    }

    /**
     * 응답 본문을 쓴다. {@code title} 이 null 이면 상태의 표준 사유 문구를 쓰고, {@code errors} 가 있으면
     * 기존 필드 뒤에 필드 오류 목록을 덧붙인다(기존 파서 호환).
     */
    private Mono<Void> handlerResponse(ServerHttpResponse response, EgovExceptionResponse egovResponse,
                                       String title, List<Map<String, String>> errors) {
        String timestamp = egovResponse.getTimestamp();
        int status = egovResponse.getStatus();
        String code = egovResponse.getCode();
        String message = egovResponse.getMessage();
        // 표준 예외는 HttpStatus 에 없는 상태를 가질 수 있어 HttpStatusCode 로 설정한다
        response.setStatusCode(HttpStatusCode.valueOf(status));
        Map<String, Object> body = new LinkedHashMap<>();
        if (problemDetailEnabled) {
            // RFC 9457 Problem Details — 표준 필드 뒤에 실행환경 확장 필드(code, timestamp)를 둔다
            response.getHeaders().setContentType(PROBLEM_JSON_UTF8);
            body.put("type", "about:blank");
            body.put("title", (title != null) ? title : reasonPhrase(status));
            body.put("status", status);
            body.put("detail", message);
            body.put("code", code);
            body.put("timestamp", timestamp);
        } else {
            response.getHeaders().setContentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8));
            body.put("timestamp", timestamp);
            body.put("status", status);
            body.put("code", code);
            body.put("message", message);
        }
        if (errors != null) {
            // 입력 검증 실패의 필드 오류 목록(필드·메시지)
            body.put("errors", errors);
        }
        byte[] payload;
        try {
            payload = OBJECT_MAPPER.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            // 문자열·정수와 문자열 맵 목록만 담는 맵이라 실제로는 일어나지 않지만, 응답 없이 끝나지 않도록 최소 본문으로 대체한다
            LOGGER.error("Failed to serialize error response body", e);
            payload = ("{\"status\":" + status + "}").getBytes(StandardCharsets.UTF_8);
        }
        DataBuffer dataBuffer = response.bufferFactory().wrap(payload);
        return response.writeWith(Mono.just(dataBuffer));
    }

    /**
     * 자기 HTTP 상태를 가진 프레임워크 표준 예외 — 그 상태와 헤더(405 의 허용 메서드 등)를 보존한다.
     * 기본 형식의 code 는 상태에 대응하는 오류 코드이고, 대응이 없으면 4xx 는 E001·5xx 는 E021 이되 status 는
     * 실제 값이다. 설명은 일반화 문구이고 요청 경로·값을 되비추지 않는다. 5xx 는 서버 쪽 원인이라 ERROR,
     * 4xx 는 클라이언트 오류라 debug 로 남긴다.
     */
    private Mono<Void> standardResponse(ServerHttpResponse response, ErrorResponse errorResponse, Throwable ex) {
        int status = errorResponse.getStatusCode().value();
        EgovErrorCode egovErrorCode = errorCodeOf(status);
        String message;
        if (egovErrorCode != null) {
            message = egovErrorCode.getMessage();
        } else if (status >= 500) {
            egovErrorCode = EgovErrorCode.INTERNAL_SERVER_ERROR;
            message = egovErrorCode.getMessage();
        } else {
            egovErrorCode = EgovErrorCode.INVALID_INPUT_VALUE;
            message = CLIENT_ERROR_MESSAGE;
        }
        if (status >= 500) {
            LOGGER.error("Standard exception mapped to status {}", status, ex);
        } else {
            LOGGER.debug("Standard exception mapped to status {}: {}", status, ex.getMessage());
        }
        errorResponse.getHeaders().forEach((name, values) -> response.getHeaders().put(name, values));
        return handlerResponse(response, EgovExceptionResponse.of(status, egovErrorCode, message), null, null);
    }

    /**
     * 상태 → 실행환경 오류 코드. 412(E010 은 "등록된 사용자 없음"이라는 업무 의미)처럼 뜻이 다른 코드는
     * 대응시키지 않는다. 대응이 없으면 null.
     */
    private static EgovErrorCode errorCodeOf(int status) {
        return switch (status) {
            case 400 -> EgovErrorCode.INVALID_INPUT_VALUE;
            case 401 -> EgovErrorCode.UNAUTHORIZED;
            case 403 -> EgovErrorCode.ACCESS_DENIED;
            case 404 -> EgovErrorCode.NOT_FOUND;
            case 405 -> EgovErrorCode.METHOD_NOT_ALLOWED;
            case 406 -> EgovErrorCode.NOT_ACCEPTABLE;
            case 413 -> EgovErrorCode.PAYLOAD_TOO_LARGE;
            case 415 -> EgovErrorCode.UNSUPPORTED_MEDIA_TYPE;
            case 422 -> EgovErrorCode.UNPROCESSABLE_ENTITY;
            case 500 -> EgovErrorCode.INTERNAL_SERVER_ERROR;
            case 503 -> EgovErrorCode.SERVICE_UNAVAILABLE;
            default -> null;
        };
    }

    /** 입력 검증 실패 공통 응답 — 400 · E001 · 필드 오류 목록. */
    private Mono<Void> validationResponse(ServerHttpResponse response, List<Map<String, String>> errors) {
        return handlerResponse(response,
                EgovExceptionResponse.of(EgovErrorCode.INVALID_INPUT_VALUE, VALIDATION_MESSAGE),
                VALIDATION_TITLE, errors);
    }

    /**
     * 단일 값 형 변환 실패({@code @PathVariable}·{@code @RequestParam} 등) — 400 · E002 · 필드 오류 1건.
     * 거부된 입력값·자바 타입명은 싣지 않고 원본은 debug 로그로만 남긴다.
     */
    private Mono<Void> typeMismatchResponse(ServerHttpResponse response, ServerWebInputException ex,
                                            TypeMismatchException typeMismatch, Locale locale) {
        MethodParameter parameter = ex.getMethodParameter();
        String name = (parameter != null) ? parameterName(parameter) : typeMismatch.getPropertyName();
        if (name == null) {
            name = "value";
        }
        LOGGER.debug("Type mismatch handled for '{}': {}", name, typeMismatch.getMessage());
        List<Map<String, String>> errors = new ArrayList<>(1);
        errors.add(errorEntry(name, resolveTypeMismatchMessage(name, typeMismatch.getRequiredType(), locale)));
        EgovErrorCode egovErrorCode = EgovErrorCode.INVALID_TYPE_VALUE;
        return handlerResponse(response, EgovExceptionResponse.of(egovErrorCode, egovErrorCode.getMessage()),
                VALIDATION_TITLE, errors);
    }

    /** {@code @Valid} 본문·바인딩 검증 실패의 필드 오류 목록 — 거부된 값은 싣지 않는다. */
    private List<Map<String, String>> bindingErrors(WebExchangeBindException ex, Locale locale) {
        LOGGER.debug("Validation failure handled: {} error(s)", ex.getErrorCount());
        List<Map<String, String>> errors = new ArrayList<>();
        for (FieldError fieldError : ex.getFieldErrors()) {
            if (fieldError.isBindingFailure()) {
                // 원본(입력값·타입명 포함)은 서버 로그로만 남긴다
                LOGGER.debug("Binding failure on field '{}': {}", fieldError.getField(), fieldError.getDefaultMessage());
            }
            errors.add(errorEntry(fieldError.getField(), resolveMessage(fieldError, locale)));
        }
        for (ObjectError globalError : ex.getGlobalErrors()) {
            errors.add(errorEntry(globalError.getObjectName(), resolveMessage(globalError, locale)));
        }
        return errors;
    }

    /** 프레임워크 내장 메서드 파라미터 검증 실패의 필드 오류 목록 — 파라미터 이름·메시지, 거부 값 없음. */
    private List<Map<String, String>> methodValidationErrors(HandlerMethodValidationException ex, Locale locale) {
        List<Map<String, String>> errors = new ArrayList<>();
        for (ParameterValidationResult result : ex.getParameterValidationResults()) {
            if (result instanceof ParameterErrors parameterErrors) {
                for (FieldError fieldError : parameterErrors.getFieldErrors()) {
                    errors.add(errorEntry(fieldError.getField(), resolveMessage(fieldError, locale)));
                }
                for (ObjectError globalError : parameterErrors.getGlobalErrors()) {
                    errors.add(errorEntry(globalError.getObjectName(), resolveMessage(globalError, locale)));
                }
            } else {
                String name = parameterName(result.getMethodParameter());
                for (MessageSourceResolvable error : result.getResolvableErrors()) {
                    errors.add(errorEntry(name, resolveResolvable(error, locale)));
                }
            }
        }
        for (MessageSourceResolvable error : ex.getCrossParameterValidationResults()) {
            errors.add(errorEntry(ex.getMethod().getName(), resolveResolvable(error, locale)));
        }
        return errors;
    }

    /**
     * 위반 메시지 해석 — 연동된 {@link MessageSource} 의 오류 코드 해석이 우선이고, 미연동이면 제약의 기본 메시지다.
     * 형 변환 실패는 코드만 해석하고, 해석되지 않으면 일반화 메시지다(기본 메시지는 입력값·타입명을 담는다).
     */
    private String resolveMessage(ObjectError error, Locale locale) {
        boolean bindingFailure = (error instanceof FieldError) && ((FieldError) error).isBindingFailure();
        if (messageSource != null) {
            try {
                MessageSourceResolvable resolvable = bindingFailure
                        ? new DefaultMessageSourceResolvable(error.getCodes(), error.getArguments())
                        : error;
                return messageSource.getMessage(resolvable, locale);
            } catch (NoSuchMessageException noMessage) {
                // 코드·기본 메시지 모두 없음 — 아래 기본 메시지 경로로 폴백
            }
        }
        return bindingFailure ? BINDING_FAILURE_MESSAGE : error.getDefaultMessage();
    }

    private String resolveResolvable(MessageSourceResolvable error, Locale locale) {
        if (messageSource != null) {
            try {
                return messageSource.getMessage(error, locale);
            } catch (NoSuchMessageException noMessage) {
                // 코드·기본 메시지 모두 없음 — 아래 기본 메시지로 폴백
            }
        }
        return error.getDefaultMessage();
    }

    /**
     * 단일 값 형 변환 실패 메시지 해석 — 서블릿 MVC 오류 응답과 같은 코드 단계
     * ({@code typeMismatch.이름} → {@code typeMismatch.타입} → {@code typeMismatch})와 이름 인자를 쓴다.
     * 해석되지 않으면 일반화 메시지다.
     */
    private String resolveTypeMismatchMessage(String name, Class<?> requiredType, Locale locale) {
        if (messageSource == null) {
            return BINDING_FAILURE_MESSAGE;
        }
        List<String> codes = new ArrayList<>(3);
        codes.add("typeMismatch." + name);
        if (requiredType != null) {
            codes.add("typeMismatch." + requiredType.getName());
        }
        codes.add("typeMismatch");
        MessageSourceResolvable resolvable = new DefaultMessageSourceResolvable(
                codes.toArray(new String[0]),
                new Object[]{new DefaultMessageSourceResolvable(new String[]{name}, name)},
                BINDING_FAILURE_MESSAGE);
        return messageSource.getMessage(resolvable, locale);
    }

    /**
     * 요청 쪽 이름 — 이름 있는 값 애너테이션({@code @RequestParam}·{@code @PathVariable} 등)의 이름이 우선이고,
     * 없으면 파라미터 이름({@code -parameters} 컴파일), 그것도 없으면 {@code arg}+인덱스다.
     */
    private static String parameterName(MethodParameter parameter) {
        for (Annotation annotation : parameter.getParameterAnnotations()) {
            if (NAMED_VALUE_ANNOTATIONS.contains(annotation.annotationType())) {
                String name = namedValue(annotation);
                if (name != null && !name.isEmpty()) {
                    return name;
                }
            }
        }
        String name = parameter.getParameterName();
        return (name != null) ? name : "arg" + parameter.getParameterIndex();
    }

    /** 애너테이션의 name 속성, 비어 있으면 value 속성(두 속성은 별칭이라 한쪽만 채워진다). */
    private static String namedValue(Annotation annotation) {
        Object name = AnnotationUtils.getValue(annotation, "name");
        if (name instanceof String text && !text.isEmpty()) {
            return text;
        }
        Object value = AnnotationUtils.getValue(annotation);
        return (value instanceof String text) ? text : null;
    }

    private static Locale locale(ServerWebExchange exchange) {
        Locale locale = exchange.getLocaleContext().getLocale();
        return (locale != null) ? locale : Locale.getDefault();
    }

    private static String reasonPhrase(int status) {
        HttpStatus resolved = HttpStatus.resolve(status);
        return (resolved != null) ? resolved.getReasonPhrase() : "Error";
    }

    private static Map<String, String> errorEntry(String field, String message) {
        Map<String, String> entry = new LinkedHashMap<>(2);
        entry.put("field", field);
        entry.put("message", message);
        return entry;
    }

}
