package fr.eurecom.adel.recognition.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import fr.eurecom.adel.recognition.configuration.RecognitionConfig;

/**
 * @author Julien Plu on 2018-12-05.
 */
public class SizePriorityListValidator implements ConstraintValidator<SizePriorityList, RecognitionConfig> {
  @Override
  public final boolean isValid(final RecognitionConfig t, final ConstraintValidatorContext constraintValidatorContext) {
    return t.getAnnotators().size() == t.getTypeoverlapping().getPriority().size();
  }
}
