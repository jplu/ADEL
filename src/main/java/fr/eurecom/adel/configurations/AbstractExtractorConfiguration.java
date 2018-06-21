package fr.eurecom.adel.configurations;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.URL;

import fr.eurecom.adel.annotations.MethodName;
import fr.eurecom.adel.utils.StringUtils;
import io.dropwizard.validation.OneOf;

/**
 * @author Julien Plu
 */
abstract class AbstractExtractorConfiguration implements ExtractorConfiguration {
  @JsonProperty
  @NotBlank
  @URL
  private String address;
  @JsonProperty
  @NotEmpty
  @MethodName
  private String method;
  @JsonProperty
  private String tags;
  @JsonProperty
  @NotBlank
  private String profile;
  @JsonProperty
  @NotBlank
  private String name;
  @NotBlank
  @JsonProperty
  private @OneOf({"NEEL", "Stanford", "OKE", "Musicbrainz", "DBpedia"}) String from;
  private String className;
  
  @Override
  public final String getTags() {
    return this.tags;
  }
  
  @Override
  public final void setTags(final String newTags) {
    this.tags = newTags;
  }
  
  @Override
  public final String getAddress() {
    return this.address;
  }
  
  @Override
  public final void setAddress(final String newAddress) {
    this.address = newAddress;
  }
  
  @Override
  public final String getMethod() {
    return this.method;
  }
  
  @Override
  public final void setMethod(final String newMethod) {
    this.method = newMethod;
    this.className = StringUtils.getClassNameFromMethod(newMethod);
  }
  
  @Override
  public final String getClassName() {
    return this.className;
  }
  
  public final void setClassName(final String newClassName) {
    this.className = newClassName;
  }
  
  @Override
  public final String getName() {
    return this.name;
  }
  
  @Override
  public final void setName(final String newName) {
    this.name = newName;
  }
  
  @Override
  public final String getProfile() {
    return this.profile;
  }
  
  @Override
  public final void setProfile(final String newProfile) {
    this.profile = newProfile;
  }
  
  @Override
  public final String getFrom() {
    return this.from;
  }
  
  @Override
  public final void setFrom(final String newFrom) {
    this.from = newFrom;
  }
}
