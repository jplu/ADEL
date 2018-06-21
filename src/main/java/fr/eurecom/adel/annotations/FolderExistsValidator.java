package fr.eurecom.adel.annotations;

import java.nio.file.Paths;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Julien Plu
 */
public class FolderExistsValidator implements ConstraintValidator<FolderExists, String> {
  @Override
  public final void initialize(final FolderExists newA) {}
  
  @Override
  public final boolean isValid(final String newT,
                         final ConstraintValidatorContext newConstraintValidatorContext) {
    return (newT == null) || Paths.get(newT).toFile().exists();
  }
}
