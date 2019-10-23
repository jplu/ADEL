package fr.eurecom.adel.commons.validators;

import java.nio.file.Files;
import java.nio.file.Paths;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Julien Plu on 2019-02-12.
 */
public class WritableValidator implements ConstraintValidator<Writable, String> {
  @Override
  public final boolean isValid(final String t, final ConstraintValidatorContext constraintValidatorContext) {
    if (t.isEmpty()) {
      return true;
    }
    
    if ("/".equals(t)) {
      return false;
    }
    
    if (!t.startsWith(".") && !t.startsWith("/") && !t.startsWith("~")) {
      return Files.isWritable(Paths.get("./" + t).getParent());
    }
    
    return Files.isWritable(Paths.get(t).getParent());
  }
}