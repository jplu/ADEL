package fr.eurecom.adel.commons.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Julien Plu on 2019-02-28.
 */
public class OneOfValidator implements ConstraintValidator<OneOf, Object> {
  private String[] values = new String[]{};
  private boolean caseInsensitive;
  private boolean ignoreWhitespace;
  
  @Override
  public final void initialize(final OneOf constraintAnnotation) {
    this.values = constraintAnnotation.value();
    this.caseInsensitive = constraintAnnotation.ignoreCase();
    this.ignoreWhitespace = constraintAnnotation.ignoreWhitespace();
  }
  
  @Override
  public final boolean isValid(final Object t, final ConstraintValidatorContext constraintValidatorContext) {
    if (null == t) {
      return true;
    }
    
    final String v = this.ignoreWhitespace ? t.toString().trim() : t.toString();
    
    if (this.caseInsensitive) {
      for (final String s : this.values) {
        if (s.equalsIgnoreCase(v)) {
          return true;
        }
      }
    } else {
      for (final String s : this.values) {
        if (s.equals(v)) {
          return true;
        }
      }
    }
    
    return false;
  }
}