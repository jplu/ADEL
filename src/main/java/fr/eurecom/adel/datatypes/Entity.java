package fr.eurecom.adel.datatypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.eurecom.adel.utils.EntityComparator;
import fr.eurecom.adel.utils.HashtagSegmentation;
import fr.eurecom.adel.utils.StringUtils;

/**
 * @author jplu
 */
public class Entity implements Comparable<Entity> {
  private String phrase;
  private String cleanPhrase;
  private List<Candidate> candidates;
  private Candidate bestCandidate;
  private Integer startPosition;
  private Integer endPosition;
  private String extractor;
  private String type;
  private String from;
  
  public Entity() {
    this.phrase = "";
    this.cleanPhrase = "";
    this.candidates = new ArrayList<>();
    this.startPosition = 0;
    this.endPosition = 0;
    this.extractor = "";
    this.type = "";
    this.from = "";
    this.bestCandidate = new Candidate();
  }
  
  public Entity(final String newPhrase, final List<Candidate> newCandidates) {
    this.phrase = newPhrase;
    this.candidates = new ArrayList<>(newCandidates);
    this.startPosition = 0;
    this.endPosition = 0;
    this.extractor = "";
    this.from = "";
    this.type = "";
    this.bestCandidate = new Candidate();
    
    this.retrieveCleanPhrase();
  }
  
  public Entity(final String newPhrase, final List<Candidate> newCandidates,
                final Integer newStartPosition, final Integer newEndPosition,
                final String newExtractor) {
    this.phrase = newPhrase;
    this.candidates = new ArrayList<>(newCandidates);
    this.startPosition = newStartPosition;
    this.endPosition = newEndPosition;
    this.extractor = newExtractor;
    this.type = "";
    this.from = "";
    this.bestCandidate = new Candidate();
    
    this.retrieveCleanPhrase();
  }
  
  public Entity(final String newPhrase, final Candidate newBestCandidate,
                final Integer newStartPosition, final Integer newEndPosition,
                final String newExtractor) {
    this.phrase = newPhrase;
    this.candidates = new ArrayList<>();
    this.startPosition = newStartPosition;
    this.endPosition = newEndPosition;
    this.extractor = newExtractor;
    this.type = "";
    this.from = "";
    this.bestCandidate = newBestCandidate;
    
    this.retrieveCleanPhrase();
  }
  
  public Entity(final String newPhrase, final String newCleanPhrase,
                final Candidate newBestCandidate, final Integer newStartPosition,
                final Integer newEndPosition, final String newExtractor) {
    this.phrase = newPhrase;
    this.candidates = new ArrayList<>();
    this.startPosition = newStartPosition;
    this.endPosition = newEndPosition;
    this.extractor = newExtractor;
    this.type = "";
    this.from = "";
    this.bestCandidate = newBestCandidate;
    this.cleanPhrase = newCleanPhrase;
  }
  
  public Entity(final String newPhrase, final List<Candidate> newCandidates,
                final Integer newStartPosition, final Integer newEndPosition,
                final String newExtractor, final String newType) {
    this.phrase = newPhrase;
    this.candidates = new ArrayList<>(newCandidates);
    this.startPosition = newStartPosition;
    this.endPosition = newEndPosition;
    this.extractor = newExtractor;
    this.type = newType;
    this.from = "";
    this.bestCandidate = new Candidate();
    
    this.retrieveCleanPhrase();
  }
  
  public Entity(final String newPhrase, final Candidate newBestCandidate,
                final Integer newStartPosition, final Integer newEndPosition,
                final String newExtractor, final String newType) {
    this.phrase = newPhrase;
    this.candidates = new ArrayList<>();
    this.startPosition = newStartPosition;
    this.endPosition = newEndPosition;
    this.extractor = newExtractor;
    this.type = newType;
    this.from = "";
    this.bestCandidate = newBestCandidate;
    
    this.retrieveCleanPhrase();
  }
  
  public Entity(final String newPhrase, final String newCleanPhrase,
                final Candidate newBestCandidate, final Integer newStartPosition,
                final Integer newEndPosition, final String newExtractor, final String newType) {
    this.phrase = newPhrase;
    this.candidates = new ArrayList<>();
    this.startPosition = newStartPosition;
    this.endPosition = newEndPosition;
    this.extractor = newExtractor;
    this.type = newType;
    this.from = "";
    this.bestCandidate = newBestCandidate;
    this.cleanPhrase = newCleanPhrase;
  }
  
  public Entity(final String newPhrase, final List<Candidate> newCandidates,
                final Integer newStartPosition, final Integer newEndPosition,
                final String newExtractor, final String newType, final String newFrom) {
    this.phrase = newPhrase;
    this.candidates = new ArrayList<>(newCandidates);
    this.startPosition = newStartPosition;
    this.endPosition = newEndPosition;
    this.extractor = newExtractor;
    this.type = newType;
    this.from = newFrom;
    this.bestCandidate = new Candidate();
    
    this.retrieveCleanPhrase();
  }
  
  public Entity(final String newPhrase, final String newCleanPhrase,
                final List<Candidate> newCandidates, final Integer newStartPosition,
                final Integer newEndPosition, final String newExtractor, final String newFrom) {
    this.phrase = newPhrase;
    this.candidates = new ArrayList<>(newCandidates);
    this.startPosition = newStartPosition;
    this.endPosition = newEndPosition;
    this.extractor = newExtractor;
    this.type = "";
    this.from = newFrom;
    this.bestCandidate = new Candidate();
    this.cleanPhrase = newCleanPhrase;
  }
  
  public final String getFrom() {
    return this.from;
  }
  
  public final void setFrom(final String newFrom) {
    this.from = newFrom;
  }
  
  public final String getExtractor() {
    return this.extractor;
  }
  
  public final void setExtractor(final String newExtractor) {
    this.extractor = newExtractor;
  }
  
  public final String getCleanPhrase() {
    return this.cleanPhrase;
  }
  
  public final void setCleanPhrase(final String newCleanPhrase) {
    this.cleanPhrase = newCleanPhrase;
  }
  
  public final String getPhrase() {
    return this.phrase;
  }
  
  public final void setPhrase(final String newPhrase) {
    this.phrase = newPhrase;
  }
  
  public final List<Candidate> getCandidates() {
    return Collections.unmodifiableList(this.candidates);
  }
  
  public final void setCandidates(final List<Candidate> newCandidates) {
    this.candidates = new ArrayList<>(newCandidates);
  }
  
  public final void addCandidate(final Candidate newCandidate) {
    this.candidates.add(newCandidate);
  }
  
  public final Integer getStartPosition() {
    return this.startPosition;
  }
  
  public final void setStartPosition(final Integer newStartPosition) {
    this.startPosition = newStartPosition;
  }
  
  public final Integer getEndPosition() {
    return this.endPosition;
  }
  
  public final void setEndPosition(final Integer newEndPosition) {
    this.endPosition = newEndPosition;
  }
  
  public final String getType() {
    return this.type;
  }
  
  public final void setType(final String newType) {
    this.type = newType;
  }
  
  public final Candidate getBestCandidate() {
    return this.bestCandidate;
  }
  
  public final void setBestCandidate(final Candidate newBestCandidate) {
    this.bestCandidate = newBestCandidate;
    this.candidates.clear();
  }
  
  private void retrieveCleanPhrase() {
    this.cleanPhrase = "";
    
    if ("@".equals(Character.toString(this.phrase.charAt(0))) && this.phrase.length() > 1) {
      this.startPosition++;
    }
    
    if ("#".equals(Character.toString(this.phrase.charAt(0))) && this.phrase.length() > 1) {
      this.startPosition++;
    }
    
    if (this.cleanPhrase.isEmpty()) {
      this.cleanPhrase = this.phrase;
    }
  }
  
  public void sortCandidates(final EntityComparator... comparators) {
    this.candidates.sort(EntityComparator.decending(EntityComparator.getComparator(comparators)));
  }
  
  @Override
  public final boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    
    if ((obj == null) || (this.getClass() != obj.getClass())) {
      return false;
    }
    
    final Entity entity = (Entity) obj;
    
    if (!this.phrase.equals(entity.phrase)) {
      return false;
    }
    
    if (!this.cleanPhrase.equals(entity.cleanPhrase)) {
      return false;
    }
    
    if (!this.startPosition.equals(entity.startPosition)) {
      return false;
    }
  
    if (!this.extractor.equals(entity.extractor)) {
      return false;
    }
    
    return this.endPosition.equals(entity.endPosition);
  }
  
  @Override
  public final int hashCode() {
    int result = this.phrase.hashCode();
    
    result = 31 * (result + this.cleanPhrase.hashCode());
    result = 31 * (result + this.candidates.hashCode());
    result = 31 * (result + this.startPosition.hashCode());
    result = 31 * (result + this.endPosition.hashCode());
    result = 31 * (result + this.type.hashCode());
    
    return result;
  }
  
  @Override
  public final String toString() {
    return "Entity{"
        + "phrase='" + this.phrase + '\''
        + ", cleanPhrase='" + this.cleanPhrase + '\''
        + ", candidates=" + this.candidates
        + ", startPosition=" + this.startPosition
        + ", endPosition=" + this.endPosition
        + ", type='" + this.type + '\''
        + ", extractor='" + this.extractor + '\''
        + ", from='" + this.from + '\''
        + ", bestCandidate='" + this.bestCandidate + '\''
        + '}';
  }
  
  @Override
  public final int compareTo(final Entity o) {
    if (this.startPosition < o.startPosition) {
      return -1;
    }
    
    if (this.startPosition > o.startPosition) {
      return 1;
    }
  
    return 0;
  }
}
