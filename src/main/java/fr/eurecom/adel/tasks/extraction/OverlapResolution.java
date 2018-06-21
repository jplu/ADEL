package fr.eurecom.adel.tasks.extraction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import fr.eurecom.adel.configurations.AdelConfiguration;
import fr.eurecom.adel.datatypes.Entity;
import fr.eurecom.adel.utils.StringUtils;

/**
 * @author Julien Plu
 */
public final class OverlapResolution {
  private OverlapResolution() {
  }
  
  public static List<Entity> resolve(final Iterable<Entity> oldEntities,
                                     final AdelConfiguration adelConf, final String to,
                                     final List<String> extractorPriority) {
    final List<Entity> newEntities = new ArrayList<>();
    
    for (final Entity old : oldEntities) {
      final List<Integer> overlapIndexes = OverlapResolution.overlappingIndex(newEntities, old);
      
      if (!overlapIndexes.isEmpty()) {
        Entity newEntity = OverlapResolution.entityResolution(newEntities.get(
            overlapIndexes.get(0)), old, adelConf, to, extractorPriority);
        
        for (int i = 1;i < overlapIndexes.size();i++) {
          newEntity = OverlapResolution.entityResolution(newEntities.get(
              overlapIndexes.get(i)), newEntity, adelConf, to, extractorPriority);
        }
        
        final Collection<Entity> entitiesToRemove = new ArrayList<>();
  
        for (final Integer overlapIndexe : overlapIndexes) {
          entitiesToRemove.add(newEntities.get(overlapIndexe));
        }
        
        newEntities.removeAll(entitiesToRemove);
        newEntities.add(newEntity);
      } else {
        if (!old.getType().isEmpty()) {
          old.setType(OverlapResolution.resolveTypeMapping(old.getFrom(), to, old.getType(),
              adelConf) + "--" + extractorPriority.indexOf(old.getExtractor()));
        }
              
        old.setFrom(to);
        
        newEntities.add(old);
      }
    }
    
    return newEntities;
  }
  
  public static void majorityVote(final Iterable<Entity> newEntities,
                                  final AdelConfiguration adelConf, final String to) {
    for (final Entity entity : newEntities) {
      if (entity.getType().isEmpty()) {
        entity.setType(OverlapResolution.resolveDefaultTypeMapping(adelConf, to));
      } else {
        final Map<String, Long> map = Arrays.stream(entity.getType().split("\\|\\|"))
            .collect(Collectors.groupingBy(s -> s.split("--")[0], Collectors.counting()));
        
        if (Collections.max(map.values()).equals(Collections.min(map.values()))) {
          final List<String> types = Arrays.asList(entity.getType().split("\\|\\|"));
          
          types.sort(Comparator.comparing(newS -> Integer.valueOf(newS.split("--")[1])));
          
          entity.setType(types.get(0).split("--")[0]);
        } else {
          entity.setType(map.entrySet().stream().max((entry1, entry2) -> entry1.getValue() >
              entry2.getValue() ? 1 : -1).get().getKey());
        }
      }
    }
  }
  
  private static List<Integer> overlappingIndex(final List<Entity> newEntities, final Entity old) {
    final List<Integer> indexes = new ArrayList<>();
    
    for (final Entity entity : newEntities) {
      if (OverlapResolution.isOverlap(entity, old)) {
        indexes.add(newEntities.indexOf(entity));
      }
    }
    
    return indexes;
  }
  
  private static Entity entityResolution(final Entity m1, final Entity m2,
                                         final AdelConfiguration adelConf, final String to,
                                         final List<String> extractorPriority) {
    final Entity finalEntity = new Entity();
    
    if (m1.getStartPosition() <= m2.getStartPosition()) {
      finalEntity.setStartPosition(m1.getStartPosition());
    } else {
      finalEntity.setStartPosition(m2.getStartPosition());
    }
    
    if (m1.getEndPosition() >= m2.getEndPosition()) {
      finalEntity.setEndPosition(m1.getEndPosition());
    } else {
      finalEntity.setEndPosition(m2.getEndPosition());
    }
    
    if (m1.getStartPosition() < m2.getStartPosition()) {
      finalEntity.setPhrase(StringUtils.concat(m1.getPhrase(), m2.getPhrase()));
      finalEntity.setCleanPhrase(StringUtils.concat(m1.getPhrase(), m2.getPhrase()));
    } else {
      finalEntity.setPhrase(StringUtils.concat(m2.getPhrase(), m1.getPhrase()));
      finalEntity.setCleanPhrase(StringUtils.concat(m2.getCleanPhrase(), m1.getCleanPhrase()));
    }
    
    finalEntity.setFrom(to);
    
    if (m1.getType().isEmpty() && m2.getType().isEmpty()) {
      finalEntity.setType("");
    } else if (m2.getType().isEmpty() && !m1.getType().isEmpty()) {
      finalEntity.setType(m1.getType());
    } else if (!m2.getType().isEmpty() && m1.getType().isEmpty()) {
      finalEntity.setType(OverlapResolution.resolveTypeMapping(m2.getFrom(), to, m2.getType(),
          adelConf) + "--" + extractorPriority.indexOf(m2.getExtractor()));
    } else {
        finalEntity.setType(m1.getType() + "||" + OverlapResolution.resolveTypeMapping(m2.getFrom(),
            to, m2.getType(), adelConf) + "--" + extractorPriority.indexOf(m2.getExtractor()));
    }
    
    if (m1.getExtractor().equals(m2.getExtractor())) {
      finalEntity.setExtractor(m1.getExtractor());
    } else {
      finalEntity.setExtractor(m1.getExtractor() + '+' + m2.getExtractor());
    }
    
    return finalEntity;
  }
  
  private static boolean isOverlap(final Entity e1, final Entity e2) {
    if (e1 == null || e2 == null) {
      throw new WebApplicationException("An unexpected error occured with one of the extractor. " +
          "Try your query once again.", Response.Status.PRECONDITION_FAILED);
    }
    
    boolean res = (e1.getStartPosition() > e2.getStartPosition())
        && (e1.getStartPosition() < e2.getEndPosition());
    
    if ((e1.getStartPosition().intValue() == e2.getStartPosition().intValue())
        || (e1.getEndPosition().intValue() == e2.getEndPosition().intValue())) {
      res = true;
    }
    
    if ((e2.getStartPosition() > e1.getStartPosition())
        && (e2.getStartPosition() < e1.getEndPosition())) {
      res = true;
    }
    
    return res;
  }
  
  public static void resolveTypesMappingLink(final Collection<Entity> newEntities,
                                             final AdelConfiguration adelConf) {
    newEntities.parallelStream().forEach(entity -> {
      if (entity.getBestCandidate().getProperties().containsKey(adelConf.getIndexProperties().get(
          "types")) && !entity.getBestCandidate().getProperties().get(
              adelConf.getIndexProperties().get("types")).isEmpty() &&
          !entity.getBestCandidate().getFrom().equals(adelConf.getLink().getTo())) {
        boolean notFound = true;
        
        Collections.reverse(entity.getBestCandidate().getProperties().get(
            adelConf.getIndexProperties().get("types")));
        
        for (final String type : entity.getBestCandidate().getProperties().get(
            adelConf.getIndexProperties().get("types"))) {
          if (adelConf.getTypesMapping().get(entity.getBestCandidate().getFrom() + '2'
              + adelConf.getLink().getTo()).containsKey(type)) {
            entity.setType(OverlapResolution.resolveTypeMapping(entity.getBestCandidate().getFrom(),
                adelConf.getLink().getTo(), type, adelConf));
  
            notFound = false;
            
            break;
          }
        }
        
        if (notFound) {
          throw new WebApplicationException("The mapping for the types " +
              entity.getBestCandidate().getProperties().get(adelConf.getIndexProperties().get(
                  "types")) + " does not exists in the file " + entity.getBestCandidate().getFrom()
              + '2' + adelConf.getLink().getTo(), Response.Status.PRECONDITION_FAILED);
        }
      } else if (entity.getType().isEmpty()) {
        entity.setType(OverlapResolution.resolveDefaultTypeMapping(adelConf,
            adelConf.getLink().getTo()));
      }
  
      entity.setFrom(entity.getBestCandidate().getFrom());
    });
  }
  
  public static void resolveTypesMappingNerd(final Collection<Entity> newEntities,
                                             final AdelConfiguration adelConf) {
    newEntities.parallelStream().forEach(entity -> {
      if (entity.getBestCandidate().getProperties().containsKey(adelConf.getIndexProperties().get(
          "types")) && !entity.getBestCandidate().getFrom().equals(adelConf.getLink().getTo())) {
        final int size = entity.getBestCandidate().getProperties().get(
            adelConf.getIndexProperties().get("types")).size();
    
        if (size != 0 && !entity.getType().isEmpty()) {
          boolean notFound = true;
  
          Collections.reverse(entity.getBestCandidate().getProperties().get(
              adelConf.getIndexProperties().get("types")));
          for (final String type : entity.getBestCandidate().getProperties().get(
              adelConf.getIndexProperties().get("types"))) {
            if (adelConf.getTypesMapping().get(entity.getBestCandidate().getFrom() + '2'
                + adelConf.getLink().getTo()).containsKey(type)) {
              entity.setType(entity.getType() + "||" + OverlapResolution.resolveTypeMapping(
                  entity.getBestCandidate().getFrom(), adelConf.getNerd().getTo(), type, adelConf)
                  + "--" + Arrays.asList(adelConf.getNerd().getPriority().split(
                  ",")).indexOf("uri"));
  
              notFound = false;
  
              break;
            }
          }
  
          if (notFound) {
            throw new WebApplicationException("The mapping for the types " +
                entity.getBestCandidate().getProperties().get(adelConf.getIndexProperties().get(
                    "types")) + " does not exists in the file "
                + entity.getBestCandidate().getFrom() + '2' + adelConf.getLink().getTo(),
                Response.Status.PRECONDITION_FAILED);
          }
        } else if (size != 0 && entity.getType().isEmpty()) {
          boolean notFound = true;
  
          Collections.reverse(entity.getBestCandidate().getProperties().get(
              adelConf.getIndexProperties().get("types")));
  
          for (final String type : entity.getBestCandidate().getProperties().get(
              adelConf.getIndexProperties().get("types"))) {
            if (adelConf.getTypesMapping().get(entity.getBestCandidate().getFrom() + '2'
                + adelConf.getLink().getTo()).containsKey(type)) {
              entity.setType(OverlapResolution.resolveTypeMapping(
                  entity.getBestCandidate().getFrom(), adelConf.getLink().getTo(), type, adelConf));
      
              notFound = false;
      
              break;
            }
          }
  
          if (notFound) {
            throw new WebApplicationException("The mapping for the types " +
                entity.getBestCandidate().getProperties().get(adelConf.getIndexProperties().get(
                    "types")) + " does not exists in the file "
                + entity.getBestCandidate().getFrom() + '2' + adelConf.getLink().getTo(),
                Response.Status.PRECONDITION_FAILED);
          }
        }
      }
    });
  }
  
  private static String resolveDefaultTypeMapping(final AdelConfiguration adelConf,
                                                  final String to) {
    if (!adelConf.getTypesMapping().containsKey("default")) {
      throw new WebApplicationException("The default mapping file does not exists",
          Response.Status.PRECONDITION_FAILED);
    }
  
    if (!adelConf.getTypesMapping().get("default").containsKey(to)) {
      throw new WebApplicationException("The default mapping for " + to + " does not exists",
          Response.Status.PRECONDITION_FAILED);
    }
    
    return adelConf.getTypesMapping().get("default").get(to);
  }
  
  private static String resolveTypeMapping(final String from, final String to, final String type,
                                           final AdelConfiguration adelConf) {
    final String newType;
    
    if (!from.equals(to)) {
      if (!adelConf.getTypesMapping().containsKey(from + '2' + to)) {
        throw new WebApplicationException("The mapping file " + from + '2' + to + " does not " +
            "exists", Response.Status.PRECONDITION_FAILED);
      }
  
      if (!adelConf.getTypesMapping().get(from + '2' + to).containsKey(type)) {
        throw new WebApplicationException("The mapping for the type " + type + " does not exists " +
            "in the file " + from + '2' + to, Response.Status.PRECONDITION_FAILED);
      }
  
      newType = adelConf.getTypesMapping().get(from + '2' + to).get(type);
    } else {
      newType = type;
    }
    
    return newType;
  }
}
