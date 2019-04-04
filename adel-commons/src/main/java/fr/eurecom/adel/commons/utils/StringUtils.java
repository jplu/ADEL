package fr.eurecom.adel.commons.utils;

import java.text.BreakIterator;

/**
 * @author Julien Plu on 2019-02-26.
 */
public class StringUtils {
  public static int printLength(String s) {
    final BreakIterator it = BreakIterator.getCharacterInstance();
    
    it.setText(s);
    
    int count = 0;
    
    while (it.next() != BreakIterator.DONE) {
      count++;
    }
    
    return count;
  }
}
