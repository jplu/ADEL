package fr.eurecom.adel.datatypes;

import org.hibernate.validator.constraints.URL;

import fr.eurecom.adel.annotations.Content;
import io.dropwizard.validation.OneOf;

/**
 * @author Julien Plu
 */
@Content
public class LinkQuery implements Query {
  private String content;
  @URL
  private String url;
  @OneOf({"raw", "nif"})
  private String input;
  @OneOf({"nif", "tac", "brat", "naf"})
  private String output;
  private Boolean perfectCandidateGeneration;
  private Boolean perfectLinking;
  private String goldStandard;
  
  public LinkQuery() {
    this.input = "raw";
    this.output = "nif";
    this.perfectCandidateGeneration = false;
    this.perfectLinking = false;
    this.goldStandard = "";
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
  
  public final Boolean getPerfectCandidateGeneration() {
    return this.perfectCandidateGeneration;
  }
  
  public final void setPerfectCandidateGeneration(final Boolean newPerfectCandidateGeneration) {
    this.perfectCandidateGeneration = newPerfectCandidateGeneration;
  }
  
  public final Boolean getPerfectLinking() {
    return this.perfectLinking;
  }
  
  public final void setPerfectLinking(final Boolean newPerfectLinking) {
    this.perfectLinking = newPerfectLinking;
  }
  
  public final String getGoldStandard() {
    return this.goldStandard;
  }
  
  public final void setGoldStandard(final String newGoldStandard) {
    this.goldStandard = newGoldStandard;
  }
}
