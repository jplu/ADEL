package fr.eurecom.adel.recognition.usecases;

import java.util.List;
import java.util.Map;

import fr.eurecom.adel.commons.datatypes.Entity;
import fr.eurecom.adel.recognition.domain.repositories.MentionOverlapResolutionRepository;
import fr.eurecom.adel.recognition.domain.repositories.TypeOverlapResolutionRepository;
import fr.eurecom.adel.recognition.configuration.AnnotatorConfig;
import fr.eurecom.adel.recognition.configuration.TypeOverlappingConfig;
import fr.eurecom.adel.recognition.exceptions.MappingNotExistsException;
import fr.eurecom.adel.recognition.exceptions.TypeNotExistsException;

/**
 * @author Julien Plu on 2018-11-26.
 */
public class OverlapResolution {
  private final MentionOverlapResolutionRepository mentionOverlapResolutionRepository;
  private final TypeOverlapResolutionRepository typeOverlapResolutionRepository;
  private final TypeOverlappingConfig config;
  
  public OverlapResolution(final TypeOverlapResolutionRepository newTypeOverlapResolutionRepository, final TypeOverlappingConfig newConfig, final MentionOverlapResolutionRepository newMentionOverlapResolutionRepository) {
    this.mentionOverlapResolutionRepository = newMentionOverlapResolutionRepository;
    this.typeOverlapResolutionRepository = newTypeOverlapResolutionRepository;
    this.config = newConfig;
  }
  
  final List<Entity> resolveOverlap(final Map<AnnotatorConfig, List<Entity>> documents) throws MappingNotExistsException, TypeNotExistsException {
    final List<Entity> newEntities = this.mentionOverlapResolutionRepository.resolveMentionOverlapping(documents);
    
    this.typeOverlapResolutionRepository.resolveTypeOverlapping(this.config, newEntities);
    
    return newEntities;
  }
}
