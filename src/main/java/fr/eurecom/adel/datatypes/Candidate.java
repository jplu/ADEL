package fr.eurecom.adel.datatypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Julien Plu
 */
public class Candidate implements Comparable<Candidate> {
  private Float finalScore;
  private final Map<String, List<String>> properties;
  private String from;
  
  public Candidate() {
    this.properties = new HashMap<>();
    this.finalScore = 0.0f;
    this.from = "";
  }
  
  public final Float getFinalScore() {
    return this.finalScore;
  }
  
  public final void setFinalScore(final Float newFinalScore) {
    this.finalScore = newFinalScore;
  }
  
  public final void putProperty(final String name, final List<Object> value) {
    final List<String> finalValues = new ArrayList<>();
    
    for (final Object item : value) {
      if (!item.toString().isEmpty()) {
        finalValues.add(item.toString());
      }
    }
    
    this.properties.put(name, finalValues);
  }
  
  public final Map<String, List<String>> getProperties() {
    return Collections.unmodifiableMap(this.properties);
  }
  
  public final String getFrom() {
    return this.from;
  }
  
  public final void setFrom(final String newFrom) {
    this.from = newFrom;
  }
  
  @Override
  public final boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    
    if ((obj == null) || (this.getClass() != obj.getClass())) {
      return false;
    }
    
    final Candidate candidate = (Candidate) obj;
    
    return this.properties.equals(candidate.properties);
  }
  
  @Override
  public final int hashCode() {
    int result = this.finalScore.hashCode();
    
    result = 31 * (result + this.properties.hashCode());
    
    return result;
  }
  
  @Override
  public final String toString() {
    return "Candidate{" +
          "link=" + this.properties.get("link") +
//        "properties=" + this.properties +
//        ", finalScore=" + this.finalScore +
//        ", from=" + this.from +
        '}';
  }
  
  @Override
  public final int compareTo(final Candidate o) {
    if (this.finalScore > o.getFinalScore()) {
      return -1;
    } else if (this.finalScore < o.getFinalScore()) {
      return 1;
    }
    
    return 0;
  }
}
