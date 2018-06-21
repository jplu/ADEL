package fr.eurecom.adel.configurations;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.validator.constraints.NotBlank;

import fr.eurecom.adel.annotations.UriPriorityList;
import io.dropwizard.validation.OneOf;

/**
 * @author Julien Plu
 */
@UriPriorityList
public class NerdConfiguration {
  @NotBlank
  @JsonProperty
  private @OneOf({"NEEL", "Stanford", "OKE", "Musicbrainz", "DBpedia"}) String to;
  @NotBlank
  @JsonProperty
  private String priority;
  
  public NerdConfiguration() {
  }
  
  public final String getTo() {
    return this.to;
  }
  
  public final void setTo(final String newTo) {
    this.to = newTo;
  }
  
  public final String getPriority() {
    return this.priority;
  }
  
  public final void setPriority(final String newPriority) {
    this.priority = newPriority;
  }
  
  @Override
  public final String toString() {
    return "NerdConfiguration{"
        + "to=" + this.to + '\''
        + ", priority='" + this.priority + '\''
        + '}';
  }
}
