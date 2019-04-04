package fr.eurecom.adel.commons.datatypes;

import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Julien Plu on 17/11/2018.
 */
@Getter
@Setter
@ToString
@Builder
public final class Entity {
  private @NotNull String phrase;
  private @NotNull String cleanPhrase;
  private @NotNull String type;
  private @NotNull Integer startOffset;
  private @NotNull Integer endOffset;
}
