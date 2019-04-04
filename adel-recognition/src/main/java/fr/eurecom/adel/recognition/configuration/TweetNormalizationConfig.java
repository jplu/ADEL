package fr.eurecom.adel.recognition.configuration;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Julien Plu on 2018-12-09.
 */
@Getter
@Setter
public class TweetNormalizationConfig {
  private @NotNull String usermention;
  private @NotNull String hashtag;
  private @NotNull Boolean activate;
}
