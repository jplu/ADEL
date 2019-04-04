package fr.eurecom.adel.api.formatter;

import fr.eurecom.adel.recognition.configuration.RecognitionConfig;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Julien Plu on 2019-02-12.
 */
@Getter
@Setter
public class ProfileFormatter {
  private final RecognitionProfile recognition;
  
  public ProfileFormatter(final RecognitionConfig newRecognitionProfile) {
    this.recognition = new RecognitionProfile(newRecognitionProfile);
  }
}
