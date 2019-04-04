package fr.eurecom.adel.commons.validators;

import java.nio.file.Paths;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Julien Plu on 2019-02-12.
 */
public class FileValidator implements ConstraintValidator<File, String> {
  @Override
  public boolean isValid(final String t, final ConstraintValidatorContext constraintValidatorContext) {
    if (t.isEmpty()) {
      return true;
    }
    
    return Paths.get(t).toFile().isFile();
  }
}
