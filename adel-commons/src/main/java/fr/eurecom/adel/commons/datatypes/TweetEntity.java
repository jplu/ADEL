package fr.eurecom.adel.commons.datatypes;

import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Julien Plu on 2019-01-05.
 */
@Getter
@Setter
@ToString
@Builder
public class TweetEntity {
  private @NotNull String phrase;
  private String cleanPhrase;
  private @NotNull Integer startOffset;
  private @NotNull Integer endOffset;
}
