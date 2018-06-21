package fr.eurecom.adel.outputs;

import org.apache.commons.lang3.tuple.Pair;

import fr.eurecom.adel.datatypes.Entity;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Julien Plu
 */
public class Micropost2014Output implements IOutput<Entity> {
  @Override
  public final String write(final Map<String, Pair<String, List<Entity>>> entries,
                            final Map<String, String> indexProperties) {
    final StringBuilder sb = new StringBuilder();
    
    entries.entrySet().forEach(entry -> sb.append(this.writeSingleDocument(entry.getKey(),
        entry.getValue().getRight(), indexProperties)));
    
    return sb.toString();
  }
  
  private String writeSingleDocument(final String uuid, final List<Entity> entries,
                                     final Map<String, String> indexProperties) {
    final StringBuilder result = new StringBuilder();
    
    result.append(uuid);

    Collections.sort(entries);

    for (final Entity entity : entries) {
      if (!"NIL".equals(entity.getBestCandidate().getProperties().get(indexProperties.get(
          "link")).get(0))) {
        result.append('\t');
        result.append(entity.getPhrase());
        result.append('\t');
        result.append(entity.getBestCandidate().getProperties().get(indexProperties.get(
            "link")).get(0));
      }
    }

    result.append(System.lineSeparator());
    
    if (result.toString().split("\t").length == 1) {
      result.delete(0, result.toString().length());
    }
    
    return result.toString();
  }
}
