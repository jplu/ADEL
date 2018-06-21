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
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = QueryValidator.class)
@Documented
public @interface Query {
  String message() default "The Elasticsearch query file does not exists in the folder " +
      "queries/elasticsearch";
  Class<?>[] groups () default {};
  Class<? extends Payload>[] payload() default {};
}
