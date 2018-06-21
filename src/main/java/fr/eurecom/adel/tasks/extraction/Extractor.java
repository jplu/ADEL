package fr.eurecom.adel.tasks.extraction;

import java.util.List;

import fr.eurecom.adel.configurations.ExtractorConfiguration;
import fr.eurecom.adel.datatypes.Entity;

/**
 * @author Julien Plu
 */
public interface Extractor {
  void extract();
  List<Entity> getEntities();
  void setEntities(final List<Entity> newEntities);
  ExtractorConfiguration getConf();
  void setConf(final ExtractorConfiguration newConf);
}
