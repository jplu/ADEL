package fr.eurecom.adel.tasks.indexing;

import fr.eurecom.adel.datatypes.Entity;

/**
 * @author Julien Plu
 */
public interface Index {
  void searchCandidates(final Entity newEntity);
}
