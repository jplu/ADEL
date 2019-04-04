package fr.eurecom.adel.recognition.usecases;

import java.util.List;

import fr.eurecom.adel.commons.datatypes.Entity;
import fr.eurecom.adel.commons.datatypes.Token;
import fr.eurecom.adel.recognition.domain.repositories.AnnotatorRepository;
import fr.eurecom.adel.recognition.configuration.AnnotatorConfig;

/**
 * @author Julien Plu on 17/11/2018.
 */
public class Annotator {
  private final AnnotatorRepository annotatorRepository;
  private final AnnotatorConfig config;
  
  public Annotator(final AnnotatorRepository newAnnotatorRepository, final AnnotatorConfig newConfig) {
    this.annotatorRepository = newAnnotatorRepository;
    this.config = newConfig;
  }
  
  List<Entity> annotate(final String text) {
    return this.annotatorRepository.annotate(this.config, text);
  }
  
  List<List<Token>> tokenize(final String text) {
    return this.annotatorRepository.tokenize(text);
  }
  
  public final AnnotatorConfig getConfig() {
    return this.config;
  }
}
