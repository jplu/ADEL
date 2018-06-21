package fr.eurecom.adel.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import fr.eurecom.adel.configurations.LinkConfiguration;

/**
 * @author Julien Plu
 */
public class LinkValidator implements ConstraintValidator<Link, LinkConfiguration> {
  @Override
  public final void initialize(final Link newA) {
  }
  
  @Override
  public final boolean isValid(final LinkConfiguration newT,
                               final ConstraintValidatorContext newConstraintValidatorContext) {
    boolean res = true;
    
    if ("fastText".equals(newT.getMethod()) && (newT.getAddress() == null ||
        newT.getAddress().isEmpty())) {
      res = false;
    }
    
    return res;
  }
}
