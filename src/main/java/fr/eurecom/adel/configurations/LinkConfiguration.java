package fr.eurecom.adel.configurations;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

import fr.eurecom.adel.annotations.Link;
import fr.eurecom.adel.annotations.MethodName;
import fr.eurecom.adel.utils.StringUtils;
import io.dropwizard.validation.OneOf;

/**
 * @author Julien Plu
 */
@Link
public class LinkConfiguration {
  @JsonProperty
  @NotBlank
  @MethodName
  private String method;
  @JsonProperty
  @URL
  private String address;
  @NotBlank
  @JsonProperty
  private @OneOf({"NEEL", "Stanford", "OKE", "Musicbrainz", "DBpedia"}) String to;
  private String className;
  
  public LinkConfiguration() {
  }
  
  public final String getAddress() {
    return this.address;
  }
  
  public final void setAddress(final String newAddress) {
    this.address = newAddress;
  }
  
  public final String getMethod() {
    return this.method;
  }
  
  public final void setMethod(final String newMethod) {
    this.method = newMethod;
    this.className = StringUtils.getClassNameFromMethod(newMethod);
  }
  
  public final String getTo() {
    return this.to;
  }
  
  public final void setTo(final String newTo) {
    this.to = newTo;
  }
  
  public final String getClassName() {
    return this.className;
  }
  
  @Override
  public final String toString() {
    return "LinkConfiguration{"
        + "method='" + this.method + '\''
        + ", address='" + this.address + '\''
        + ", to='" + this.to + '\''
        + ", className='" + this.className + '\''
        + '}';
  }
}
