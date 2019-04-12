package fr.eurecom.adel.recognition.configuration;

import org.hibernate.validator.constraints.UniqueElements;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import fr.eurecom.adel.commons.validators.OneOf;
import fr.eurecom.adel.commons.validators.URL;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Julien Plu on 2018-11-25.
 */
@Getter
@Setter
public class AnnotatorConfig {
  private @NotBlank @URL String address;
  private @NotBlank String annotator;
  private @NotBlank @OneOf({"NEEL", "CoNLL", "DUL", "Musicbrainz", "DBpedia", "MUC"}) String from;
  private @UniqueElements List<String> tags;
  private @NotBlank String name;
  private @NotNull Boolean tokenizer;
}
