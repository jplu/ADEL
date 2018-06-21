package fr.eurecom.adel.configurations;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Min;

import fr.eurecom.adel.annotations.FolderExists;

/**
 * @author Julien Plu
 */
public class LuceneConfiguration {
  @JsonProperty
  @NotBlank
  private String fields;
  @JsonProperty
  @Min(1)
  private Integer size;
  @JsonProperty
  @NotBlank
  @FolderExists
  private String folder;
  
  public LuceneConfiguration() {}
  
  public final String getFields() {
    return this.fields;
  }
  
  public final void setFields(final String newFields) {
    this.fields = newFields;
  }
  
  public final Integer getSize() {
    return this.size;
  }
  
  public final void setSize(final Integer newSize) {
    this.size = newSize;
  }
  
  public String getFolder() {
    return this.folder;
  }
  
  public void setFolder(final String newFolder) {
    this.folder = newFolder;
  }
  
  @Override
  public final String toString() {
    return "LuceneConfiguration{"
        + "fields='" + this.fields + '\''
        + ", size='" + this.size + '\''
        + ", folder='" + this.folder + '\''
        + '}';
  }
}
