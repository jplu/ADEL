package fr.eurecom.adel.recognition.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import fr.eurecom.adel.recognition.configuration.AnnotatorConfig;
import fr.eurecom.adel.recognition.configuration.RecognitionConfig;

/**
 * @author Julien Plu on 2018-12-06.
 */
public class ContentPriorityListValidator implements ConstraintValidator<ContentPriorityList, RecognitionConfig> {
  @Override
  public boolean isValid(final RecognitionConfig t, final ConstraintValidatorContext constraintValidatorContext) {
    for (final AnnotatorConfig config : t.getAnnotators()) {
      if (!t.getTypeoverlapping().getPriority().contains(config.getName())) {
        return false;
      }
    }
    
    return true;
  }
}
