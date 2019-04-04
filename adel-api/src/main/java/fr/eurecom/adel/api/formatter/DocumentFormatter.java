package fr.eurecom.adel.api.formatter;

import java.util.List;
import java.util.stream.Collectors;

import fr.eurecom.adel.commons.datatypes.Document;
import fr.eurecom.adel.commons.datatypes.Entity;
import fr.eurecom.adel.commons.datatypes.Token;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Julien Plu on 2019-02-09.
 */
@Getter
@Setter
public class DocumentFormatter {
  private List<Entity> entities;
  
  public DocumentFormatter(final Document newDocument) {
    this.entities = newDocument.getEntities();
  }
}
