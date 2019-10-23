package fr.eurecom.adel.recognition.validators;

import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import fr.eurecom.adel.recognition.configuration.AnnotatorConfig;

/**
 * @author Julien Plu on 2019-02-09.
 */
public class HasTokenizerValidator implements ConstraintValidator<HasTokenizer, List<AnnotatorConfig>> {
  @Override
  public final boolean isValid(final List<AnnotatorConfig> t, final ConstraintValidatorContext constraintValidatorContext) {
    for (final AnnotatorConfig config : t) {
      if (config.getTokenizer()) {
        return true;
      }
    }
    
    return false;
  }
}
