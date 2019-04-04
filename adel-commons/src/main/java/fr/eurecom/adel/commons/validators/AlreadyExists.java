package fr.eurecom.adel.commons.validators;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * @author Julien Plu on 2019-03-06.
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AlreadyExistsValidator.class)
@Documented
public @interface AlreadyExists {
  String message() default "{already.exists}";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}
