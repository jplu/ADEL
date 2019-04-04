package fr.eurecom.adel.recognition.validators;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * @author Julien Plu on 2018-12-05.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SizePriorityListValidator.class)
@Documented
public @interface SizePriorityList {
  String message() default "{propertylist.size}";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}
