package fr.eurecom.adel.tasks.linking;

import java.util.List;

import fr.eurecom.adel.configurations.AdelConfiguration;
import fr.eurecom.adel.datatypes.Entity;

/**
 * @author Julien Plu
 */
public interface Linking {
  void link(final List<Entity> newEntities, final AdelConfiguration conf);
}
