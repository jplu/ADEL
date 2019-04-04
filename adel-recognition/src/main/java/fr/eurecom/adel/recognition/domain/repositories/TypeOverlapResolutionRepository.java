package fr.eurecom.adel.recognition.domain.repositories;

import java.util.List;

import fr.eurecom.adel.commons.datatypes.Entity;
import fr.eurecom.adel.recognition.configuration.TypeOverlappingConfig;
import fr.eurecom.adel.recognition.exceptions.MappingNotExistsException;
import fr.eurecom.adel.recognition.exceptions.TypeNotExistsException;

/**
 * @author Julien Plu on 2018-11-26.
 */
@FunctionalInterface
public interface TypeOverlapResolutionRepository {
  void resolveTypeOverlapping(TypeOverlappingConfig config, List<Entity> entities) throws MappingNotExistsException, TypeNotExistsException;
}
