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
@Constraint(validatedBy = IndexValidator.class)
@Documented
public @interface Index {
  String message() default "Only one index must be set";
  Class<?>[] groups () default {};
  Class<? extends Payload>[] payload() default {};
}
