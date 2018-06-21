package fr.eurecom.adel.outputs;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import fr.eurecom.adel.datatypes.Entity;
import fr.eurecom.adel.utils.StringUtils;

/**
 * @author Julien Plu
 */
public class TsvOutput implements IOutput<Entity> {
  @Override
  public final String write(final Map<String, Pair<String, List<Entity>>> entries,
                            final Map<String, String> indexProperties) {
    final StringBuilder sb = new StringBuilder();
  
    entries.entrySet().forEach(entry -> sb.append(this.writeSingleDocument(entry.getKey(),
        entry.getValue().getRight(), entry.getValue().getLeft())));
  
    return sb.toString();
  }
  
  private String writeSingleDocument(final String uuid, final List<Entity> entries,
                                     final String sentence) {
    final StringBuilder sb = new StringBuilder();
  
    sb.append(uuid);
    sb.append('\t');
  
    Collections.sort(entries);
    
    String tmpSentence = StringUtils.normalizeString(sentence);
    
    for (int i = 0; i < entries.size(); i++) {
      if (i == 0) {
        if (entries.get(i).getStartPosition() == 0) {
          tmpSentence = "[[" + entries.get(i).getPhrase() + "]]" + tmpSentence.substring(
              entries.get(i).getEndPosition());
        } else {
          tmpSentence = tmpSentence.substring(0, entries.get(i).getStartPosition()) + "[["
              + entries.get(i).getPhrase() + "]]" + tmpSentence.substring(
                  entries.get(i).getEndPosition());
        }
      } else {
        tmpSentence = tmpSentence.substring(0,
            entries.get(i).getStartPosition() + i * 4) + "[[" + entries.get(i).getPhrase()
            + "]]" + tmpSentence.substring(entries.get(i).getEndPosition() + i * 4);
      }
    }
    
    sb.append(tmpSentence);
    sb.append(System.lineSeparator());
    
    return sb.toString();
  }
}
