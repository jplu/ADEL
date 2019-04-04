package fr.eurecom.adel.recognition.domain.repositories;

import java.util.List;

import fr.eurecom.adel.commons.datatypes.Entity;
import fr.eurecom.adel.recognition.configuration.AnnotatorConfig;
import fr.eurecom.adel.commons.datatypes.Token;

/**
 * @author Julien Plu on 17/11/2018.
 */
public interface AnnotatorRepository {
  List<Entity> annotate(AnnotatorConfig config, String text);
  List<List<Token>> tokenize(String text);
}
