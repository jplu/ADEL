package fr.eurecom.adel.commons.validators;

import java.nio.file.Files;
import java.nio.file.Paths;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Julien Plu on 2019-03-06.
 */
public class AlreadyExistsValidator implements ConstraintValidator<AlreadyExists, String> {
  @Override
  public final boolean isValid(final String t, final ConstraintValidatorContext constraintValidatorContext) {
    if (t.isEmpty()) {
      return true;
    }
    
    return !Files.exists(Paths.get(t));
  }
}