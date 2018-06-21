package fr.eurecom.adel.configurations;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.validation.Valid;

import fr.eurecom.adel.annotations.UniqueExtractorName;
import io.dropwizard.validation.OneOf;

/**
 * @author Julien Plu
 */
@UniqueExtractorName
public class ExtractConfiguration {
  @JsonProperty
  @URL
  @NotBlank
  private String tokenize;
  @NotBlank
  @JsonProperty
  private @OneOf({"NEEL", "Stanford", "OKE", "Musicbrainz", "DBpedia"}) String to;
  @JsonProperty
  @Valid
  private Set<NerConfiguration> ner;
  @JsonProperty
  @Valid
  private Set<PosConfiguration> pos;
  @JsonProperty
  @Valid
  private Set<GazConfiguration> gaz;
  @JsonProperty
  @Valid
  private Set<DateConfiguration> date;
  @JsonProperty
  @Valid
  private Set<NumberConfiguration> number;
  @JsonProperty
  @Valid
  private Set<CorefConfiguration> coref;
  private final Set<ExtractorConfiguration> extractors;
  @NotBlank
  @JsonProperty
  private String priority;
  
  public ExtractConfiguration() {
    this.ner = new HashSet<>();
    this.pos = new HashSet<>();
    this.gaz = new HashSet<>();
    this.date = new HashSet<>();
    this.number = new HashSet<>();
    this.coref = new HashSet<>();
    this.extractors = new HashSet<>();
  }
  
  public final String getPriority() {
    return this.priority;
  }
  
  public final void setPriority(final String newPriority) {
    this.priority = newPriority;
  }
  
  public final String getTokenize() {
    return this.tokenize;
  }
  
  public final void setTokenize(final String newTokenize) {
    this.tokenize = newTokenize;
  }
  
  public final String getTo() {
    return this.to;
  }
  
  public final void setTo(final String newTo) {
    this.to = newTo;
  }
  
  public final Set<ExtractorConfiguration> getNer() {
    return Collections.unmodifiableSet(this.ner);
  }
  
  public final void setNer(final Set<NerConfiguration> newNer) {
    this.ner = new HashSet<>(newNer);
    
    this.extractors.addAll(newNer);
  }
  
  public final Set<ExtractorConfiguration> getPos() {
    return Collections.unmodifiableSet(this.pos);
  }
  
  public final void setPos(final Set<PosConfiguration> newPos) {
    this.pos = new HashSet<>(newPos);
    
    this.extractors.addAll(newPos);
  }
  
  public final Set<GazConfiguration> getGaz() {
    return Collections.unmodifiableSet(this.gaz);
  }
  
  public final void setGaz(final Set<GazConfiguration> newGaz) {
    this.gaz = new HashSet<>(newGaz);
    
    this.extractors.addAll(newGaz);
  }
  
  public final Set<DateConfiguration> getDate() {
    return Collections.unmodifiableSet(this.date);
  }
  
  public final void setDate(final Set<DateConfiguration> newDate) {
    this.date = new HashSet<>(newDate);
    
    this.extractors.addAll(newDate);
  }
  
  public final Set<NumberConfiguration> getNumber() {
    return Collections.unmodifiableSet(this.number);
  }
  
  public final void setNumber(final Set<NumberConfiguration> newNumber) {
    this.number = new HashSet<>(newNumber);
    
    this.extractors.addAll(newNumber);
  }
  
  public final Set<CorefConfiguration> getCoref() {
    return Collections.unmodifiableSet(this.coref);
  }
  
  public final void setCoref(final Set<CorefConfiguration> newCoref) {
    this.coref = new HashSet<>(newCoref);
    
    this.extractors.addAll(newCoref);
  }
  
  public final Set<ExtractorConfiguration> getExtractors() {
    return Collections.unmodifiableSet(this.extractors);
  }
  
  @Override
  public final String toString() {
    return "ExtractConfiguration{"
        + "tokenize=" + this.tokenize + '"'
        + ", ner=" + this.ner + '"'
        + ", pos=" + this.pos + '"'
        + ", gaz=" + this.gaz + '"'
        + ", date=" + this.date + '"'
        + ", number=" + this.number + '"'
        + ", to=" + this.to + '"'
        + ", priority=" + this.priority + '"'
        + '}';
  }
}
