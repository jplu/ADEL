package fr.eurecom.adel.annotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import fr.eurecom.adel.configurations.AdelConfiguration;
import fr.eurecom.adel.configurations.ExtractorConfiguration;

/**
 * @author Julien Plu
 */
public class ExtractorExistsPriorityListValidator implements
    ConstraintValidator<ExtractorExistsPriorityList, AdelConfiguration> {
  @Override
  public final void initialize(final ExtractorExistsPriorityList newA) {}
  
  @Override
  public final boolean isValid(final AdelConfiguration newT,
                               final ConstraintValidatorContext newConstraintValidatorContext) {
    boolean res = true;

    final Set<String> nerdExtractPriority = new HashSet<>(Arrays.asList(
        newT.getExtract().getPriority().split(",")));
  
    nerdExtractPriority.addAll(Arrays.asList(newT.getNerd().getPriority().split(",")));
    
    final Collection<String> extractors = new ArrayList<>();
    
    for(final ExtractorConfiguration extractor : newT.getExtract().getDate()) {
      extractors.add(extractor.getName());
    }
  
    for(final ExtractorConfiguration extractor : newT.getExtract().getNumber()) {
      extractors.add(extractor.getName());
    }
  
    for(final ExtractorConfiguration extractor : newT.getExtract().getNer()) {
      extractors.add(extractor.getName());
    }
  
    for(final ExtractorConfiguration extractor : newT.getExtract().getGaz()) {
      extractors.add(extractor.getName());
    }
    
    for (final String extractor : nerdExtractPriority) {
      if (!extractors.contains(extractor) && !extractor.equals("none")
          && !extractor.equals("uri")) {
        res = false;
      }
    }
    
    return res;
  }
}
