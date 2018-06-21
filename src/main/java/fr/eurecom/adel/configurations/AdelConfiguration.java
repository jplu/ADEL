package fr.eurecom.adel.configurations;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import fr.eurecom.adel.annotations.ExtractorExistsPriorityList;
import fr.eurecom.adel.annotations.ProperPriorityList;

/**
 * @author Julien Plu
 */
@ProperPriorityList
@ExtractorExistsPriorityList
public class AdelConfiguration {
  @JsonProperty
  @NotNull
  @Valid
  private LinkConfiguration link;
  @JsonProperty
  @NotNull
  @Valid
  private ExtractConfiguration extract;
  @JsonProperty
  @NotNull
  @Valid
  private IndexConfiguration index;
  @JsonProperty
  @NotNull
  @Valid
  private NerdConfiguration nerd;
  private final Map<String, String> indexProperties;
  private String text;
  private final Map<String, Map<String, String>> typesMapping;
  
  public AdelConfiguration() {
    this.indexProperties = new HashMap<>();
    this.typesMapping = new HashMap<>();
  }
  
  public final LinkConfiguration getLink() {
    return this.link;
  }
  
  public final void setLink(final LinkConfiguration newLink) {
    this.link = newLink;
  }
  
  public final ExtractConfiguration getExtract() {
    return this.extract;
  }
  
  public final void setExtract(final ExtractConfiguration newExtract) {
    this.extract = newExtract;
  }
  
  public final IndexConfiguration getIndex() {
    return this.index;
  }
  
  public final void setIndex(final IndexConfiguration newIndex) {
    this.index = newIndex;
  }
  
  public final Map<String, String> getIndexProperties() {
    return Collections.unmodifiableMap(this.indexProperties);
  }
  
  public final Map<String, Map<String, String>> getTypesMapping() {
    return Collections.unmodifiableMap(this.typesMapping);
  }
  
  public final String getText() {
    return this.text;
  }
  
  public final void setText(final String newText) {
    this.text = newText;
  }
  
  public final NerdConfiguration getNerd() {
    return this.nerd;
  }
  
  public final void setNerd(final NerdConfiguration newNerd) {
    this.nerd = newNerd;
  }
  
  public final void prepareIndexProperties() {
    try {
      final List<String> lines = Files.readAllLines(Paths.get("mappings" +
              FileSystems.getDefault().getSeparator() + "index" +
              FileSystems.getDefault().getSeparator() + this.index.getName() + ".map"),
          Charset.forName("UTF-8"));
      final String[] header = lines.get(0).split("\t");
      
      int count = 0;
      
      for (int i = 0; i < header.length; i++) {
        if (header[i].equalsIgnoreCase(this.index.getIndexType())) {
          count = i;
        }
      }
      
      if (count == 0) {
        throw new WebApplicationException("Impossible to find the mapping for " +
            this.index.getIndexType() + " in " + "mappings" +
            FileSystems.getDefault().getSeparator() + "index" +
            FileSystems.getDefault().getSeparator() + this.index.getName() + ".map",
            Response.Status.PRECONDITION_FAILED);
      }
      
      lines.remove(0);
      
      for (final String line : lines) {
        this.indexProperties.put(line.split("\t")[0], line.split("\t")[count]);
      }
    } catch (final IOException ex) {
      throw new WebApplicationException("Issue to open " + "mappings" +
          FileSystems.getDefault().getSeparator() + "index" +
          FileSystems.getDefault().getSeparator() + this.index.getName() + ".map", ex,
          Response.Status.PRECONDITION_FAILED);
    }
  }
  
  public final void prepareTypesMapping() {
    try {
      Files.list(Paths.get("mappings"+ FileSystems.getDefault().getSeparator() +
          "types")).forEach(file -> {
            try {
              final List<String> lines = Files.readAllLines(file, Charset.forName("UTF-8"));
              final String name = file.getFileName().toString().split("\\.")[0];
              final Map<String, String> mapping = new HashMap<>();
              
              for (final String line : lines) {
                final String left = line.split("\t")[0];
                final String right = line.split("\t")[1];
                
                for (final String type : left.split(",")) {
                  mapping.put(type, right);
                }
              }
      
              this.typesMapping.put(name, mapping);
            } catch (final IOException ex) {
              throw new WebApplicationException("Issue to open " + file, ex,
                  Response.Status.PRECONDITION_FAILED);
            }
      });
    } catch (final IOException ex) {
      throw new WebApplicationException("Issue to list the directory mappings" +
          FileSystems.getDefault().getSeparator() + "types", ex,
          Response.Status.PRECONDITION_FAILED);
    }
  }
  
  @Override
  public final String toString() {
    return "PipelineConfiguration{"
        + "link=" + this.link
        + ", extract=" + this.extract
        + ", index=" + this.index
        + '}';
  }
}
