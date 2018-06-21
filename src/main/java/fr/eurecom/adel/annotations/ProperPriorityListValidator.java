package fr.eurecom.adel.annotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import fr.eurecom.adel.configurations.AdelConfiguration;
import fr.eurecom.adel.configurations.ExtractorConfiguration;

/**
 * @author Julien Plu
 */
public class ProperPriorityListValidator implements ConstraintValidator<ProperPriorityList,
    AdelConfiguration> {
  @Override
  public final void initialize(final ProperPriorityList newA) {}
  
  @Override
  public final boolean isValid(final AdelConfiguration newT,
                               final ConstraintValidatorContext newConstraintValidatorContext) {
  
    final int extractorNumber = newT.getExtract().getDate().size()
        + newT.getExtract().getNumber().size() + newT.getExtract().getNer().size()
        + newT.getExtract().getGaz().size();
    final Collection<String> extractorsName = new ArrayList<>(Arrays.asList(
        newT.getExtract().getPriority().split(",")));
  
    extractorsName.addAll(Arrays.asList(newT.getNerd().getPriority().split(",")));
  
    boolean res = true;
    
    for (final ExtractorConfiguration extractor : newT.getExtract().getPos()) {
      if (extractorsName.contains(extractor.getName())) {
        res = false;
      }
    }
  
    for (final ExtractorConfiguration extractor : newT.getExtract().getCoref()) {
      if (extractorsName.contains(extractor.getName())) {
        res = false;
      }
    }
    
    if (extractorNumber != newT.getExtract().getPriority().split(",").length) {
      res = false;
    }
    
    if (extractorNumber + 1 != newT.getNerd().getPriority().split(",").length) {
      res = false;
    }
    
    if (extractorNumber == 0 && newT.getExtract().getPriority().equals("none")) {
      res = true;
    }
    
    return res;
  }
}
