package fr.eurecom.adel.datatypes;

import org.hibernate.validator.constraints.URL;

import fr.eurecom.adel.annotations.Content;
import io.dropwizard.validation.OneOf;

/**
 * @author Julien Plu
 */
@Content
public class NerdQuery implements Query {
  private String content;
  @URL
  private String url;
  @OneOf({"raw", "nif", "srt", "ttml", "html"})
  private String input;
  @OneOf({"nif", "tac", "brat", "naf"})
  private String output;
  
  public NerdQuery() {
    this.input = "raw";
    this.output = "nif";
  }
  
  @Override
  public final String getUrl() {
    return this.url;
  }
  
  @Override
  public final void setUrl(final String newUrl) {
    this.url = newUrl;
  }
  
  @Override
  public final String getInput() {
    return this.input;
  }
  
  @Override
  public final void setInput(final String newInput) {
    this.input = newInput;
  }
  
  @Override
  public final String getOutput() {
    return this.output;
  }
  
  @Override
  public final void setOutput(final String newOutput) {
    this.output = newOutput;
  }
  
  @Override
  public final String getContent() {
    return this.content;
  }
  
  @Override
  public final void setContent(final String newContent) {
    this.content = newContent;
  }
}
