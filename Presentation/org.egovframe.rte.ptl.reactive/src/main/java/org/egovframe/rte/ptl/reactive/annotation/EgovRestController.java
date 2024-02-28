package org.egovframe.rte.ptl.reactive.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.annotation.*;

/**
 * A convenience annotation that is itself annotated with @Controller and @ResponseBody.
 *
 * <p>Desc.: A convenience annotation that is itself annotated with @Controller and @ResponseBody.</p>
 *
 * @author ESFC
 * @since 2023.08.31
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2023.08.31   ESFC            최초 생성
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Controller
@ResponseBody
public @interface EgovRestController {

    @AliasFor(annotation = Controller.class)
    String value() default "";

}
