package fr.eurecom.adel.recognition.domain.repositories;

import java.util.List;
import java.util.Map;

import fr.eurecom.adel.commons.datatypes.Entity;
import fr.eurecom.adel.recognition.configuration.AnnotatorConfig;

/**
 * @author Julien Plu on 2018-12-17.
 */
@FunctionalInterface
public interface MentionOverlapResolutionRepository {
  List<Entity> resolveMentionOverlapping(Map<AnnotatorConfig, List<Entity>> documents);
}
