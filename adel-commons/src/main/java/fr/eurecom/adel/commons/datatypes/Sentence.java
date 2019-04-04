package fr.eurecom.adel.commons.datatypes;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Julien Plu on 2019-02-15.
 */
@Getter
@Setter
public class Sentence {
  private List<Token> tokens;
}
