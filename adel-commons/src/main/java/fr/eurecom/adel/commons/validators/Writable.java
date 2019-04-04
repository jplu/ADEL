package fr.eurecom.adel.commons.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = WritableValidator.class)
@Documented
public @interface Writable {
  String message() default "{writable}";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}
