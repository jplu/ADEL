package fr.eurecom.adel.tasks.linking;

import org.simmetrics.StringMetric;
import org.simmetrics.metrics.StringMetrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import fr.eurecom.adel.annotations.Name;
import fr.eurecom.adel.configurations.AdelConfiguration;
import fr.eurecom.adel.datatypes.Candidate;
import fr.eurecom.adel.datatypes.Entity;
import fr.eurecom.adel.utils.EntityComparator;

/**
 * @author Julien Plu
 */
@Name(name = "ADEL Formula")
public class AdelFormula implements Linking {
  public void link(final List<Entity> newEntities, final AdelConfiguration conf) {
    newEntities.parallelStream().forEach(mention -> {
      final StringMetric metric = StringMetrics.levenshtein();
      boolean disamb = false;
      boolean redir = false;
      
      for (final Candidate candidate : mention.getCandidates()) {
        float labelScore = 0.0f;
        
        if (conf.getIndexProperties().get("label").split(",").length > 1) {
          for (final String label : conf.getIndexProperties().get("label").split(",")) {
            if (candidate.getProperties().containsKey(label)) {
              labelScore = metric.compare(candidate.getProperties().get(label).get(0).replaceAll(
                  "\\([^()]*\\)", "").trim().toLowerCase(Locale.ENGLISH), mention.getCleanPhrase());
              
              break;
            }
          }
        } else {
          labelScore = metric.compare(candidate.getProperties().get(
              conf.getIndexProperties().get("label")).get(0).replaceAll("\\([^()]*\\)",
              "").trim().toLowerCase(Locale.ENGLISH), mention.getCleanPhrase());
        }
        
        float redirectScore = 0.0f;

        if (candidate.getProperties().containsKey(conf.getIndexProperties().get("redirect"))) {
          redir = true;
          
          for (final String redirect : candidate.getProperties().get(
              conf.getIndexProperties().get("redirect"))) {
            final float tmpRedirectScore = metric.compare(redirect.replaceAll("\\([^()]*\\)", "")
                .trim().toLowerCase(Locale.ENGLISH), mention.getCleanPhrase());
  
            if (tmpRedirectScore > redirectScore) {
              redirectScore = tmpRedirectScore;
            }
          }
        }
  
        float disambiguationScore = 0.0f;
        
        if (candidate.getProperties().containsKey(conf.getIndexProperties().get(
            "disambiguation"))) {
          disamb = true;
          
          for (final String disambiguation : candidate.getProperties().get(
              conf.getIndexProperties().get("disambiguation"))) {
            final float tmpDisambiguationScore = metric.compare(disambiguation.replaceAll(
                "\\([^()]*\\)", "").trim().toLowerCase(Locale.ENGLISH), mention.getCleanPhrase());
  
            if (tmpDisambiguationScore > disambiguationScore) {
              disambiguationScore = tmpDisambiguationScore;
            }
          }
        }
        
        final float lexicalScore;
        
        if (disamb && redir) {
          lexicalScore = ((16.0f / 21.0f) * labelScore) + ((4.0f / 21.0f) * redirectScore) +
              ((1.0f / 21.0f) * disambiguationScore);
        } else if (disamb) {
          lexicalScore = ((16.0f / 21.0f) * labelScore) + ((5.0f / 21.0f) * disambiguationScore);
        } else if (redir) {
          lexicalScore = ((16.0f / 21.0f) * labelScore) + ((5.0f / 21.0f) * redirectScore);
        } else {
          lexicalScore = labelScore;
        }
        
        //if (candidate.getProperties().get(conf.getIndexProperties().get("pagerank")) != null) {
          candidate.setFinalScore(lexicalScore * Float.parseFloat(candidate.getProperties().get(
              conf.getIndexProperties().get("pagerank")).get(0)));
        //} else {
        //  System.out.println(candidate);
       // }
      }

      if (mention.getCandidates().isEmpty()) {
        final Candidate candidate = new Candidate();
  
        candidate.putProperty(conf.getIndexProperties().get("types"), new ArrayList<>());
        candidate.putProperty(conf.getIndexProperties().get("link"), Collections.singletonList(
            "NIL"));
        candidate.putProperty(conf.getIndexProperties().get("label"), Collections.singletonList(
            mention.getPhrase()));
  
        candidate.setFinalScore(0.0f);
        candidate.setFrom(conf.getIndex().getFrom());
  
        mention.setCandidates(Collections.singletonList(candidate));
      } else {
        mention.sortCandidates(EntityComparator.SCORE_SORT);
        
       /* for (final Entity entity : mention.getCandidates()) {
          if (entity.getProperties().containsKey(indexProperties.get("types"))) {
            final List<OntClass> types = RdfTools.stringTypesToOntClasses(entity.getProperties()
                .get(indexProperties.get("types")));

            for (final OntClass type : types) {
              if (type.getURI().toLowerCase(Locale.ENGLISH).contains("person")) {
                mention.setType("Person");
              } else if (type.getURI().toLowerCase(Locale.ENGLISH).contains("place")) {
                mention.setType("Location");
                //m.setNerType("Place");
              } else if (type.getURI().toLowerCase(Locale.ENGLISH).contains("organisation")) {
                mention.setType("Organization");
              } else if (type.getURI().toLowerCase(Locale.ENGLISH).contains("character")) {
                mention.setType("Character");
              } else if (type.getURI().toLowerCase(Locale.ENGLISH).contains("event")) {
                mention.setType("Event");
              } else if (type.getURI().toLowerCase(Locale.ENGLISH).contains("work")) {
                mention.setType("Product");
              }
            }
          }
        }*/
      }
    });
  }
}
