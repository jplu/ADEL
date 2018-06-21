package fr.eurecom.adel.outputs;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

import fr.eurecom.adel.datatypes.Entity;
import fr.eurecom.adel.utils.RdfTools;

/**
 * @author Julien Plu
 */
public class BratOutput implements IOutput<Entity> {
  @Override
  public final String write(final Map<String, Pair<String, List<Entity>>> entries,
                            final Map<String, String> indexProperties) {
    final StringBuilder sb = new StringBuilder();
  
    entries.entrySet().forEach(entry -> sb.append(this.writeSingleDocument(
        entry.getValue().getRight(), indexProperties)));
    
    return sb.toString();
  }
  
  private String writeSingleDocument(final Iterable<Entity> entries,
                                     final Map<String, String> indexProperties) {
    final StringBuilder sb = new StringBuilder();
    int count = 1;
  
    for (final Entity entity : entries) {
      sb.append('T');
      sb.append(count);
      sb.append('\t');
      sb.append(entity.getType());
      sb.append(' ');
      sb.append(entity.getStartPosition());
      sb.append(' ');
      sb.append(entity.getEndPosition());
      sb.append('\t');
      sb.append(entity.getPhrase());
      sb.append(System.lineSeparator());
  
      if (!entity.getBestCandidate().getFrom().isEmpty()) {
        sb.append('#');
        sb.append(count);
        sb.append('\t');
        sb.append("AnnotatorNotes T");
        sb.append(count);
        sb.append('\t');
        sb.append(entity.getBestCandidate().getProperties().get(indexProperties.get("link")).get(
            0));
        sb.append(System.lineSeparator());
      }
      
      count++;
    }
    
    return sb.toString();
  }
}
