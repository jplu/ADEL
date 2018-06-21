package fr.eurecom.adel.utils;

import java.util.Comparator;

import fr.eurecom.adel.datatypes.Candidate;

/**
 * @author Julien Plu
 */
public enum EntityComparator implements Comparator<Candidate> {
  SCORE_SORT {
    public int compare(final Candidate e1, final Candidate e2) {
      return e1.getFinalScore().compareTo(e2.getFinalScore());
    }
  };
  
  public static Comparator<Candidate> decending(final Comparator<Candidate> other) {
    return (e1, e2) -> -1 * other.compare(e1, e2);
  }
  
  public static Comparator<Candidate> getComparator(final EntityComparator... multipleOptions) {
    return (e1, e2) -> {
      for (final EntityComparator option : multipleOptions) {
        final int result = option.compare(e1, e2);
        
        if (result != 0) {
          return result;
        }
      }
      return 0;
    };
  }
}
