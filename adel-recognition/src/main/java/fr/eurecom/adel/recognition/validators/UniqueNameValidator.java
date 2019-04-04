package fr.eurecom.adel.recognition.validators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import fr.eurecom.adel.recognition.configuration.AnnotatorConfig;

/**
 * @author Julien Plu on 2019-02-13.
 */
public class UniqueNameValidator implements ConstraintValidator<UniqueName, List<AnnotatorConfig>> {
  @Override
  public boolean isValid(final List<AnnotatorConfig> t, final ConstraintValidatorContext constraintValidatorContext) {
    final Collection<String> names = new ArrayList<>();
  
    for (final AnnotatorConfig extractor : t) {
      if (names.contains(extractor.getName())) {
        return false;
      } else {
        names.add(extractor.getName());
      }
    }
  
    return true;
  }
}