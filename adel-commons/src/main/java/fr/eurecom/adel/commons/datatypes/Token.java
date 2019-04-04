package fr.eurecom.adel.commons.datatypes;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Julien Plu on 2019-02-09.
 */
@Getter
@Setter
@Builder
public class Token {
  private String value;
  private Integer begin;
  private Integer end;
}
