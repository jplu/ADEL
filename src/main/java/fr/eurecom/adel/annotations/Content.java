package fr.eurecom.adel.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * @author Julien Plu
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ContentValidator.class)
@Documented
public @interface Content {
  String message() default "The properties content and URL cannot be empty or filled in same time";
  Class<?>[] groups () default {};
  Class<? extends Payload>[] payload() default {};
}
