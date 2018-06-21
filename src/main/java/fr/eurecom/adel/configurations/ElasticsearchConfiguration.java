package fr.eurecom.adel.configurations;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.Pattern;

import fr.eurecom.adel.annotations.Query;

/**
 * @author Julien Plu
 */
public class ElasticsearchConfiguration {
  @JsonProperty
  @NotBlank
  @Query
  private String query;
  @JsonProperty
  @Pattern(regexp="true|false")
  @NotBlank
  private String strict;
  @JsonProperty
  @NotBlank
  @URL
  private String address;
  
  public ElasticsearchConfiguration() {}
  
  public final String getStrict() {
    return this.strict;
  }
  
  public final void setStrict(final String newStrict) {
    this.strict = newStrict;
  }
  
  public final String getQuery() {
    return this.query;
  }
  
  public final void setQuery(final String newQuery) {
    this.query = newQuery;
  }
  
  public String getAddress() {
    return this.address;
  }
  
  public void setAddress(final String newAddress) {
    this.address = newAddress;
  }
  
  @Override
  public final String toString() {
    return "ElasticsearchConfiguration{"
        + "query='" + this.query + '\''
        + ", strict='" + this.strict + '\''
        + ", address='" + this.address + '\''
        + '}';
  }
}
