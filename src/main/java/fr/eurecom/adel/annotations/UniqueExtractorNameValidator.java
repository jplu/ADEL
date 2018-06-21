package fr.eurecom.adel.annotations;

import java.util.ArrayList;
import java.util.Collection;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import fr.eurecom.adel.configurations.ExtractConfiguration;
import fr.eurecom.adel.configurations.ExtractorConfiguration;

/**
 * @author Julien Plu
 */
public class UniqueExtractorNameValidator implements ConstraintValidator<UniqueExtractorName,
    ExtractConfiguration> {
  @Override
  public final void initialize(final UniqueExtractorName newA) {
  }
  
  @Override
  public final boolean isValid(final ExtractConfiguration newT,
                               final ConstraintValidatorContext newConstraintValidatorContext) {
    final Collection<String> names = new ArrayList<>();
    boolean res = true;
    
    for (final ExtractorConfiguration extractor : newT.getExtractors()) {
      if (names.contains(extractor.getName())) {
        res = false;
      } else {
        names.add(extractor.getName());
      }
    }
    
    return res;
  }
}