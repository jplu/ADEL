package fr.eurecom.adel.recognition.validators;

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
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = HasTokenizerValidator.class)
@Documented
public @interface HasTokenizer {
  String message() default "{has.tokenizer}";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}
