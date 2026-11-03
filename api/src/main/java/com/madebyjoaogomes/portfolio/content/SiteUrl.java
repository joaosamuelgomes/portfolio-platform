package com.madebyjoaogomes.portfolio.content;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.RECORD_COMPONENT;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Pattern;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * An image or link the site renders: a path on the site ({@code /cover.png}) or an
 * {@code https://} URL. Rejects other schemes such as {@code javascript:} and {@code data:},
 * so stolen admin access cannot plant script in the published pages. Null is valid.
 */
@Documented
@Constraint(validatedBy = {})
@Pattern(regexp = "^(https://|/(?!/))\\S*$")
@ReportAsSingleViolation
@Target({FIELD, PARAMETER, RECORD_COMPONENT, TYPE_USE})
@Retention(RUNTIME)
public @interface SiteUrl {

    String message() default "use um caminho do site (/imagem.png) ou uma URL https://";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
