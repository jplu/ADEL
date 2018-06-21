package fr.eurecom.adel.annotations;


import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import fr.eurecom.adel.utils.StringUtils;

/**
 * @author Julien Plu
 */
public class MethodNameValidator implements ConstraintValidator<MethodName, String> {
  @Override
  public final void initialize(final MethodName newA) {
  }
  
  @Override
  public final boolean isValid(final String newT,
                               final ConstraintValidatorContext newConstraintValidatorContext) {
    return !StringUtils.getClassNameFromMethod(newT).isEmpty();
  }
}
