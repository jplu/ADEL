package fr.eurecom.adel.annotations;

import java.util.Arrays;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import fr.eurecom.adel.configurations.NerdConfiguration;

/**
 * @author Julien Plu
 */
public class UriPriorityListValidator implements ConstraintValidator<UriPriorityList,
    NerdConfiguration> {
  @Override
  public final void initialize(final UriPriorityList newA) {}
  
  @Override
  public final boolean isValid(final NerdConfiguration newT,
                               final ConstraintValidatorContext newConstraintValidatorContext) {
    return Arrays.asList(newT.getPriority().split(",")).contains("uri");
  }
}