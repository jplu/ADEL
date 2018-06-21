package fr.eurecom.adel.annotations;

import java.nio.file.FileSystems;
import java.nio.file.Paths;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import fr.eurecom.adel.configurations.IndexConfiguration;

/**
 * @author Julien Plu
 */
public class IndexValidator implements ConstraintValidator<Index, IndexConfiguration> {
  @Override
  public final void initialize(final Index newA) {
  }
  
  @Override
  public final boolean isValid(final IndexConfiguration newT,
                               final ConstraintValidatorContext newConstraintValidatorContext) {
    final boolean res = Paths.get("mappings" + FileSystems.getDefault().getSeparator() +
        "index" + FileSystems.getDefault().getSeparator() + newT.getName() +
        ".map").toFile().exists();
    
    return !((newT.getElasticsearch() != null) && (newT.getLucene() != null)) && res
        && !((newT.getElasticsearch() == null) && (newT.getLucene() == null));
  }
}
