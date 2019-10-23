package fr.eurecom.adel.commons.validators;

import java.nio.file.Files;
import java.nio.file.Paths;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/** @author Julien Plu on 2019-02-23. */
public class MustExistsValidator implements ConstraintValidator<MustExists, String> {
  @Override
  public final boolean isValid(final String t, final ConstraintValidatorContext constraintValidatorContext) {
    if (t.isEmpty()) {
      return true;
    }

    return Files.exists(Paths.get(t));
  }
}