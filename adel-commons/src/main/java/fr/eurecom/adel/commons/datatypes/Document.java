package fr.eurecom.adel.commons.datatypes;

import java.util.List;

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
public final class Document {
  private final @NotNull String text;
  private List<Entity> entities;
  private List<List<Token>> tokens;
}
