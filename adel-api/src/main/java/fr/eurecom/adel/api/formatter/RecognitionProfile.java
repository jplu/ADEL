package fr.eurecom.adel.api.formatter;

import java.util.List;

import fr.eurecom.adel.recognition.configuration.AnnotatorConfig;
import fr.eurecom.adel.recognition.configuration.RecognitionConfig;
import fr.eurecom.adel.recognition.configuration.TweetNormalizationConfig;
import fr.eurecom.adel.recognition.configuration.TypeOverlappingConfig;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Julien Plu on 2019-02-12.
 */
@Getter
@Setter
public class RecognitionProfile {
  private final List<AnnotatorConfig> annotators;
  private final TypeOverlappingConfig typeOverlapping;
  private final TweetNormalizationConfig tweetNormalization;
  private final String mentionOverlapping;
  
  public RecognitionProfile(final RecognitionConfig newRecognitionConfig) {
    this.annotators = newRecognitionConfig.getAnnotators();
    this.typeOverlapping = newRecognitionConfig.getTypeoverlapping();
    this.tweetNormalization = newRecognitionConfig.getTweetnormalization();
    this.mentionOverlapping = newRecognitionConfig.getMentionoverlapping();
  }
}
