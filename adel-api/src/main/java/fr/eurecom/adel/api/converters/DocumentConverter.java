package fr.eurecom.adel.api.converters;

import javax.validation.constraints.NotNull;

import fr.eurecom.adel.commons.datatypes.Document;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Julien Plu on 17/11/2018.
 */
@Getter
@Setter
public class DocumentConverter {
  private @NotNull String text;
  
  public final Document toDocument() {
    return Document.builder().text(this.text).build();
  }
}
