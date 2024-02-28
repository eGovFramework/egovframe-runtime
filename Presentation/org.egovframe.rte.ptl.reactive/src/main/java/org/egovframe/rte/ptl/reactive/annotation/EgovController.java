package org.egovframe.rte.ptl.reactive.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Controller;

import java.lang.annotation.*;

/**
 * Indicates that an annotated class is a Controller
 *
 * <p>Desc.: Indicates that an annotated class is a Controller</p>
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
public @interface EgovController {

    @AliasFor(annotation = Controller.class)
    String value() default "";

}
