package fr.eurecom.adel.recognition.implementation.repositories.mentionoverlapresolution.merge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import fr.eurecom.adel.recognition.configuration.AnnotatorConfig;
import fr.eurecom.adel.commons.datatypes.Entity;
import fr.eurecom.adel.recognition.domain.repositories.MentionOverlapResolutionRepository;
import fr.eurecom.adel.commons.validators.Name;

/**
 * @author Julien Plu on 2018-12-17.
 */
@Name(name = "Merge")
public class MergeMentionOverlapResolution implements MentionOverlapResolutionRepository {
  @Override
  public final List<Entity> resolveMentionOverlapping(
      final Map<AnnotatorConfig, List<Entity>> documents) {
    final List<Entity> finalEntities = new ArrayList<>();

    for (final Map.Entry<AnnotatorConfig, List<Entity>> document : documents.entrySet()) {
      for (final Entity old : document.getValue()) {
        final List<Integer> overlapIndexes = this.overlappingIndex(finalEntities, old);
        
        if (overlapIndexes.isEmpty()) {
          if (!old.getType().isEmpty()) {
            old.setType(
                old.getType()
                    + "from"
                    + document.getKey().getFrom()
                    + "--"
                    + document.getKey().getName());
          }
          
          finalEntities.add(old);
        } else {
          final Collection<Entity> newEntities = new ArrayList<>();
          Entity newEntity = this.entityResolution(finalEntities.get(overlapIndexes.get(0)), old,
              document.getKey());
          
          newEntities.add(newEntity);
  
          for (int i = 1;i < overlapIndexes.size();i++) {
            newEntity = this.entityResolution(finalEntities.get(overlapIndexes.get(i)), old,
                document.getKey());
            newEntities.add(newEntity);
          }

          if (newEntities.size() > 1) {
            final StringBuilder newType = new StringBuilder();
            
            for (final Entity entity : newEntities) {
              final String[] splitTypes = entity.getType().split("\\|\\|");

              for (final String splitType : splitTypes) {
                if (!newType.toString().contains(splitType)) {
                  newType.append(splitType);
                  newType.append("||");
                }
              }
            }
  
            newEntity.setType(newType.substring(0, newType.length() - 2));
          }
  
          final Collection<Entity> entitiesToRemove = new ArrayList<>();
          
          for (final Integer overlapIndexe : overlapIndexes) {
            entitiesToRemove.add(finalEntities.get(overlapIndexe));
          }
  
          finalEntities.removeAll(entitiesToRemove);
          finalEntities.add(newEntity);
        }
      }
    }
  
    return finalEntities;
  }
  
  private List<Integer> overlappingIndex(final List<Entity> finalEntities, final Entity old) {
    final List<Integer> indexes = new ArrayList<>();
    
    for (final Entity entity : finalEntities) {
      if (this.isOverlap(entity, old)) {
        indexes.add(finalEntities.indexOf(entity));
      }
    }
    
    return indexes;
  }
  
  private boolean isOverlap(final Entity e1, final Entity e2) {
    boolean res = (e1.getStartOffset() > e2.getStartOffset()) &&
        (e1.getStartOffset() < e2.getEndOffset());
    
    if ((e1.getStartOffset().intValue() == e2.getStartOffset().intValue())
        || (e1.getEndOffset().intValue() == e2.getEndOffset().intValue())) {
      res = true;
    }
    
    if ((e2.getStartOffset() > e1.getStartOffset()) && (e2.getStartOffset() < e1.getEndOffset())) {
      res = true;
    }
    
    return res;
  }
  
  private Entity entityResolution(final Entity e1, final Entity e2, final AnnotatorConfig config) {
    final Entity finalEntity = Entity.builder().build();
    
    if (e1.getStartOffset() <= e2.getStartOffset()) {
      finalEntity.setStartOffset(e1.getStartOffset());
    } else {
      finalEntity.setStartOffset(e2.getStartOffset());
    }
    
    if (e1.getEndOffset() >= e2.getEndOffset()) {
      finalEntity.setEndOffset(e1.getEndOffset());
    } else {
      finalEntity.setEndOffset(e2.getEndOffset());
    }
    
    if (e1.getStartOffset() < e2.getStartOffset()) {
      finalEntity.setPhrase(this.concat(e1.getPhrase(), e2.getPhrase()));
      finalEntity.setCleanPhrase(this.concat(e1.getPhrase(), e2.getPhrase()));
    } else {
      finalEntity.setPhrase(this.concat(e2.getPhrase(), e1.getPhrase()));
      finalEntity.setCleanPhrase(this.concat(e2.getCleanPhrase(), e1.getCleanPhrase()));
    }
    
    if (e1.getType().isEmpty() && e2.getType().isEmpty()) {
      finalEntity.setType("");
    } else if (e2.getType().isEmpty() && !e1.getType().isEmpty()) {
      finalEntity.setType(e1.getType());
    } else if (!e2.getType().isEmpty() && e1.getType().isEmpty()) {
      finalEntity.setType(e2.getType() + "from" + config.getFrom() + "--" + config.getName());
    } else {
      finalEntity.setType(e1.getType() + "||" + e2.getType() + "from" + config.getFrom() + "--" + config.getName());
    }
    
    return finalEntity;
  }
  
  private String concat(final String s1, final String s2) {
    if (s2.toLowerCase(Locale.getDefault()).contains(s1.toLowerCase(Locale.getDefault()))) {
      return s2;
    }
    
    if (s1.toLowerCase(Locale.getDefault()).contains(s2.toLowerCase(Locale.getDefault()))) {
      return s1;
    }
    
    final int len = Math.min(s1.length(), s2.length());
    int index = -1;
    
    for (int i = len; i > 0; i--) {
      final String substring = s2.substring(0, i);
      
      if (s1.toLowerCase(Locale.getDefault()).endsWith(substring.toLowerCase(Locale.getDefault()))) {
        index = i;
        break;
      }
    }
    
    final StringBuilder sb = new StringBuilder(s1);
    
    if (index <= s2.length()) {
      sb.append(s2.substring(index));
    }
    
    return sb.toString();
  }
}
