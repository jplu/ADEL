package fr.eurecom.adel.recognition.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import fr.eurecom.adel.recognition.validators.ContentPriorityList;
import fr.eurecom.adel.recognition.validators.HasTokenizer;
import fr.eurecom.adel.recognition.validators.NameExistsForRecognition;
import fr.eurecom.adel.recognition.validators.SizePriorityList;
import fr.eurecom.adel.recognition.validators.UniqueName;
import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties("recognition")
@Validated
@Getter
@Setter
@SizePriorityList
@ContentPriorityList
@Component
@NameExistsForRecognition
public class RecognitionConfig {
  private @UniqueName @HasTokenizer @NotEmpty List<@Valid AnnotatorConfig> annotators = new ArrayList<>();
  private @NotNull @Valid TypeOverlappingConfig typeoverlapping;
  private @Valid TweetNormalizationConfig tweetnormalization;
  private @NotEmpty String mentionoverlapping;
  
  // Used in ValidationMessages.properties
  public final String implementationNames() {
    final Collection<String> names = new ArrayList<>();
    
    for (final AnnotatorConfig annotatorConfig : this.annotators) {
      names.add(annotatorConfig.getAnnotator());
    }
    
    names.add(this.typeoverlapping.getMethod());
    names.add(this.mentionoverlapping);
    names.add(this.tweetnormalization.getUsermention());
    names.add(this.tweetnormalization.getHashtag());
    
    return names.toString();
  }
  
  // Used in ValidationMessages.properties
  public final String getPriority() {
    return this.typeoverlapping.getPriority().toString();
  }
  
  // Used in ValidationMessages.properties
  public final String annotatorsName() {
    return this.annotators.stream().map(AnnotatorConfig::getName).collect(Collectors.toList()).toString();
  }
}