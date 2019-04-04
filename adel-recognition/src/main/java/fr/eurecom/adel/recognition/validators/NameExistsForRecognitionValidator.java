package fr.eurecom.adel.recognition.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import fr.eurecom.adel.recognition.configuration.AnnotatorConfig;
import fr.eurecom.adel.recognition.configuration.RecognitionConfig;
import fr.eurecom.adel.commons.utils.ReflectionUtils;

/**
 * @author Julien Plu on 2019-02-09.
 */
public class NameExistsForRecognitionValidator implements ConstraintValidator<NameExistsForRecognition, RecognitionConfig> {
  @Override
  public boolean isValid(final RecognitionConfig t, final ConstraintValidatorContext constraintValidatorContext) {
    for (final AnnotatorConfig annotatorConfig : t.getAnnotators()) {
      if (ReflectionUtils.getClassNameFromMethod(annotatorConfig.getAnnotator(), "annotator").isEmpty()) {
        return false;
      }
    }
    
    if (ReflectionUtils.getClassNameFromMethod(t.getTypeoverlapping().getMethod(), "typeoverlapresolution").isEmpty()) {
      return false;
    }
    
    if (ReflectionUtils.getClassNameFromMethod(t.getMentionoverlapping(), "mentionoverlapresolution").isEmpty()) {
      return false;
    }
    
    if (ReflectionUtils.getClassNameFromMethod(t.getTweetnormalization().getUsermention(), "usermentiondereferencing").isEmpty()) {
      return false;
    }
    
    return !ReflectionUtils.getClassNameFromMethod(t.getTweetnormalization().getHashtag(), "hashtagsegmentation").isEmpty();
  }
}
