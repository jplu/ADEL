package fr.eurecom.adel.recognition.configuration;

import org.hibernate.validator.constraints.UniqueElements;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import fr.eurecom.adel.commons.validators.OneOf;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Julien Plu on 2018-12-08.
 */
@Getter
@Setter
public class TypeOverlappingConfig {
  private @NotBlank @OneOf({"NEEL", "CoNLL", "DUL", "Musicbrainz", "DBpedia", "MUC"}) String to;
  private @UniqueElements @NotEmpty List<String> priority;
  private @NotBlank String method;
}
