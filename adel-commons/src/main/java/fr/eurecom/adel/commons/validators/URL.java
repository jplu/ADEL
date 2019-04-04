package fr.eurecom.adel.commons.validators;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;


/**
 * @author Julien Plu on 2019-02-09.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Constraint(validatedBy = URLValidator.class)
@Documented
public @interface URL {
  String message() default "{URL}";
  Class<?>[] groups() default { };
  Class<? extends Payload>[] payload() default { };
}
