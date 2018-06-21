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
@Constraint(validatedBy = ProperPriorityListValidator.class)
@Documented
public @interface ProperPriorityList {
  String message() default "The number of extractors in a priority list is different from the "
      + "declared extractors. POS and Coref extractors cannot belong to that list.";
  Class<?>[] groups () default {};
  Class<? extends Payload>[] payload() default {};
}
