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
@Constraint(validatedBy = LinkValidator.class)
@Documented
public @interface Link {
  String message() default "When using the fasttext linking method the address property must be " +
      "given";
  Class<?>[] groups () default {};
  Class<? extends Payload>[] payload() default {};
}
