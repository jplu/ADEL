package fr.eurecom.adel.recognition.validators;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * @author Julien Plu on 2018-12-06.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ContentPriorityListValidator.class)
@Documented
public @interface ContentPriorityList {
  String message() default "{propertylist.content}";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}
