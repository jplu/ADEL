package fr.eurecom.adel.annotations;

import java.nio.file.FileSystems;
import java.nio.file.Paths;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Julien Plu
 */
public class QueryValidator implements ConstraintValidator<Query, String> {
  @Override
  public final void initialize(final Query newA) {
  }
  
  @Override
  public final boolean isValid(final String newT,
                               final ConstraintValidatorContext newConstraintValidatorContext) {
    return Paths.get("queries" + FileSystems.getDefault().getSeparator() + "elasticsearch" +
        FileSystems.getDefault().getSeparator() + newT + ".qry").toFile().exists();
  }
}
