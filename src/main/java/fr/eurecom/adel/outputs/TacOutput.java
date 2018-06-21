package fr.eurecom.adel.outputs;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import fr.eurecom.adel.datatypes.Entity;

/**
 * @author Julien Plu
 * @author Giuseppe Rizzo <giuse.rizzo@gmail.com>
 */
public class TacOutput implements IOutput<Entity> {
  @Override
  public final String write(final Map<String, Pair<String, List<Entity>>> entries,
                            final Map<String, String> indexProperties) {
    final StringBuilder sb = new StringBuilder();
    
    entries.forEach((key, value) -> {
      sb.append(this.writeSingleDocument(key, value.getRight(), indexProperties));
    });
    
    return sb.toString();
  }
  
  private String writeSingleDocument(final String uuid, final Iterable<Entity> entries,
                                     final Map<String, String> indexProperties) {
    final StringBuilder sb = new StringBuilder();
    
    try {
      for (final Entity entity : entries) {
        sb.append(uuid);
        sb.append('\t');
        sb.append(entity.getStartPosition());
        sb.append('\t');
        sb.append(entity.getEndPosition());
        sb.append('\t');
        
        if ("NIL".equals(entity.getBestCandidate().getProperties().get(indexProperties.get(
            "link")).get(0))) {
          sb.append(entity.getBestCandidate().getProperties().get(indexProperties.get("link")).get(
              0) + uuid);
        } else {
          sb.append(entity.getBestCandidate().getProperties().get(indexProperties.get("link")).get(
              0));
        }
        
        sb.append('\t');
        sb.append(Float.toString(entity.getBestCandidate().getFinalScore()));
        
        if (!entity.getType().isEmpty()) {
          sb.append('\t');
          sb.append(entity.getType());
        }
        
        sb.append('\t');
        
        sb.deleteCharAt(sb.length() - 1);
        
        sb.append(System.lineSeparator());
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    
    return sb.toString();
  }
}
