package fr.eurecom.adel.commons.validators;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * @author Julien Plu on 2019-02-28.
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = OneOfValidator.class)
public @interface OneOf {
  String message() default "{one.of}";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
  String[] value();
  
  /**
   * Whether or not to ignore case.
   */
  boolean ignoreCase() default false;
  
  /**
   * Whether or not to ignore leading and trailing whitespace.
   */
  boolean ignoreWhitespace() default false;
}
