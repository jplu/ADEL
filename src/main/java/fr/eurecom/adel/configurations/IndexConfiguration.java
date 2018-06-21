package fr.eurecom.adel.configurations;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;

import fr.eurecom.adel.annotations.Index;
import io.dropwizard.validation.OneOf;

/**
 * @author Julien Plu
 */
@Index
public class IndexConfiguration {
  @JsonProperty
  @Valid
  private ElasticsearchConfiguration elasticsearch;
  @JsonProperty
  @Valid
  private LuceneConfiguration lucene;
  @NotBlank
  @JsonProperty
  private @OneOf({"DBpedia", "Musicbrainz"}) String from;
  @NotBlank
  @JsonProperty
  private String name;
  private String indexType;
  
  public IndexConfiguration() {}
  
  public final String getName() {
    return this.name;
  }
  
  public final void setName(final String newName) {
    this.name = newName;
  }
  
  public final String getFrom() {
    return this.from;
  }
  
  public final void setFrom(final String newFrom) {
    this.from = newFrom;
  }
  
  public ElasticsearchConfiguration getElasticsearch() {
    return this.elasticsearch;
  }
  
  public void setElasticsearch(final ElasticsearchConfiguration newElasticsearch) {
    this.elasticsearch = newElasticsearch;
    this.indexType = "Elasticsearch";
  }
  
  public LuceneConfiguration getLucene() {
    return this.lucene;
  }
  
  public void setLucene(final LuceneConfiguration newLucene) {
    this.lucene = newLucene;
    this.indexType = "Lucene";
  }
  
  public String getIndexType() {
    return this.indexType;
  }
  
  @Override
  public final String toString() {
    return "IndexConfiguration{"
        + "name='" + this.name + '\''
        + ", from='" + this.from + '\''
        + ", elasticsearch='" + this.elasticsearch + '\''
        + ", lucene='" + this.lucene + '\''
        + '}';
  }
}
