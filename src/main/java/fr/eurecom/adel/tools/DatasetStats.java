package fr.eurecom.adel.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import fr.eurecom.adel.configurations.AdelConfiguration;
import fr.eurecom.adel.datatypes.Entity;
import fr.eurecom.adel.utils.APICallerFactory;
import fr.eurecom.adel.utils.NifUtils;
import fr.eurecom.adel.utils.TacUtils;

/**
 * @author Julien Plu
 */
public class DatasetStats {
  private Map<String, String> sentences;
  private Map<String, List<Entity>> gs;
  private AdelConfiguration adelConf;
  
  public final void runTac(final Path dataset, final Path goldStandard, final String lang,
                      final AdelConfiguration newAdelConf) {
    final TacUtils utils = new TacUtils(dataset, goldStandard, newAdelConf, lang);
  
    this.adelConf = newAdelConf;
    this.sentences = utils.getSentences();
    this.gs = utils.getGs();
    
    this.displayStats();
  }
  
  public final void runNif(final Path dataset, final String lang,
                           final AdelConfiguration newAdelConf) {
    final NifUtils utils = new NifUtils(dataset, newAdelConf, lang);
    
    this.adelConf = newAdelConf;
    this.sentences = utils.getSentences();
    this.gs = utils.getGs();
    
    this.displayStats();
  }
  
  private void displayStats() {
    System.out.println("number of sentences: " + this.numberSentences());
    System.out.println("average of tokens per sentence: " + this.getAverageTokens(
        this.adelConf.getExtract().getTokenize()));
    System.out.println("average of characters per sentence: " + this.getAverageCharacters());
    System.out.println("number of mentions: " + this.numberMentions());
    System.out.println("number of unique mentions: " + this.numberUniqueMentions());
    System.out.println("number of entities: " + this.numberMentions());
    System.out.println("number of unique entities: " + this.numberUniqueEntities());
    System.out.println("number of NIL entities: " + this.numberNilEntities());
    System.out.println("number of unique NIL entities: " + this.numberUniqueNilEntities());
  
    for (final Map.Entry<String, List<String>> entry : this.numberMentionsPerType().entrySet()) {
      System.out.println("number of mentions of type " + entry.getKey() + ": " +
          entry.getValue().size());
    }
  
    for (final Map.Entry<String, Set<String>> entry :
        this.numberUniqueMentionsPerType().entrySet()) {
      System.out.println("number of unique mentions of type " + entry.getKey() + ": " +
          entry.getValue().size());
    }
  
    for (final Map.Entry<String, List<String>> entry : this.numberEntitiesPerType().entrySet()) {
      System.out.println("number of entities of type " + entry.getKey() + ": " +
          entry.getValue().size());
    }
  
    for (final Map.Entry<String, Set<String>> entry :
        this.numberUniqueEntitiesPerType().entrySet()) {
      System.out.println("number of unique entities of type " + entry.getKey() + ": " +
          entry.getValue().size());
    }
  
    for (final Map.Entry<String, List<String>> entry :
        this.numberNilEntitiesPerType().entrySet()) {
      System.out.println("number of NIL entities of type " + entry.getKey() + ": " +
          entry.getValue().size());
    }
  
    for (final Map.Entry<String, Set<String>> entry :
        this.numberUniqueNilEntitiesPerType().entrySet()) {
      System.out.println("number of unique NIL entities of type " + entry.getKey() + ": " +
          entry.getValue().size());
    }
  }
  
  private int getAverageTokens(final String tokenizer) {
    int count = 0;
    
    for (final Map.Entry<String, String> entry : this.sentences.entrySet()) {
      final APICallerFactory apiCall = APICallerFactory.getInstance();
      final Map<String, String> parameters = new HashMap<>();
      
      parameters.put("content", entry.getValue());
      try {
        final Model model = (Model) apiCall.adelQuery(tokenizer, new HashMap<>(),
            new ObjectMapper().writeValueAsString(parameters));
        
        final String sparqlQuery =
            "PREFIX nif:   <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#>" +
                "SELECT (COUNT(DISTINCT ?s) AS ?count) WHERE {" +
                "    ?s a nif:Word ." +
                '}';
        final Query query = QueryFactory.create(sparqlQuery);
        
        try (final QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
          final ResultSet res = qexec.execSelect();
          
          while (res.hasNext()) {
            final QuerySolution binding = res.nextSolution();
            
            count += Integer.valueOf(binding.getLiteral("count").getLexicalForm());
          }
        }
      } catch (final JsonProcessingException ex) {
        throw new WebApplicationException("Issue to create the tokenize query", ex,
            Response.Status.PRECONDITION_FAILED);
      }
    }
    
    return count / this.sentences.size();
  }
  
  private int getAverageCharacters() {
    int count = 0;
    
    for (final Map.Entry<String, String> entry : this.sentences.entrySet()) {
      count += entry.getValue().length();
    }
    
    return count / this.sentences.size();
  }
  
  private int numberSentences() {
    return this.sentences.size();
  }
  
  private int numberMentions() {
    int count = 0;
    
    for (final Map.Entry<String, List<Entity>> entry : this.gs.entrySet()) {
      count += entry.getValue().size();
    }
    
    return count;
  }
  
  private int numberUniqueMentions() {
    final Set<String> mentions = new HashSet<>();
    
    for (final Map.Entry<String, List<Entity>> entry : this.gs.entrySet()) {
      for (final Entity entity : entry.getValue()) {
        mentions.add(entity.getPhrase().toLowerCase(Locale.ENGLISH));
      }
    }
    
    return mentions.size();
  }
  
  private Map<String, List<String>> numberMentionsPerType() {
    final Map<String, List<String>> mentionsPerType = new HashMap<>();
    
    for (final Map.Entry<String, List<Entity>> entry : this.gs.entrySet()) {
      for (final Entity entity : entry.getValue()) {
        if (mentionsPerType.containsKey(entity.getType())) {
          mentionsPerType.get(entity.getType()).add(entity.getPhrase());
        } else {
          final List<String> tmp = new ArrayList<>();
          
          tmp.add(entity.getPhrase());
          
          mentionsPerType.put(entity.getType(), tmp);
        }
      }
    }
    
    return mentionsPerType;
  }
  
  private Map<String, Set<String>> numberUniqueMentionsPerType() {
    final Map<String, Set<String>> mentionsPerType = new HashMap<>();
    
    for (final Map.Entry<String, List<Entity>> entry : this.gs.entrySet()) {
      for (final Entity entity : entry.getValue()) {
        if (mentionsPerType.containsKey(entity.getType())) {
          mentionsPerType.get(entity.getType()).add(entity.getPhrase().toLowerCase(
              Locale.ENGLISH));
        } else {
          final Set<String> tmp = new HashSet<>();
          
          tmp.add(entity.getPhrase().toLowerCase(Locale.ENGLISH));
          
          mentionsPerType.put(entity.getType(), tmp);
        }
      }
    }
    
    return mentionsPerType;
  }
  
  private int numberNilEntities() {
    int count = 0;
    
    for (final Map.Entry<String, List<Entity>> entry : this.gs.entrySet()) {
      for (final Entity entity : entry.getValue()) {
        if (entity.getBestCandidate().getProperties().get(
            this.adelConf.getIndexProperties().get("link")).get(0).startsWith("NIL")) {
          count += 1;
        }
      }
    }
    
    return count;
  }
  
  private Map<String, List<String>> numberNilEntitiesPerType() {
    final Map<String, List<String>> nilEntitiesPerType = new HashMap<>();
    
    for (final Map.Entry<String, List<Entity>> entry : this.gs.entrySet()) {
      for (final Entity entity : entry.getValue()) {
        if (entity.getBestCandidate().getProperties().get(this.adelConf.getIndexProperties().get(
            "link")).get(0).startsWith("NIL")) {
          if (nilEntitiesPerType.containsKey(entity.getType())) {
            nilEntitiesPerType.get(entity.getType()).add(
                entity.getBestCandidate().getProperties().get(
                    this.adelConf.getIndexProperties().get("link")).get(0));
          } else {
            nilEntitiesPerType.put(entity.getType(), Collections.singletonList(
                entity.getBestCandidate().getProperties().get(
                    this.adelConf.getIndexProperties().get("link")).get(0)));
          }
        }
      }
    }
    
    return nilEntitiesPerType;
  }
  
  private int numberUniqueNilEntities() {
    final Set<String> uniqueNils = new HashSet<>();
    
    for (final Map.Entry<String, List<Entity>> entry : this.gs.entrySet()) {
      for (final Entity entity : entry.getValue()) {
        if (entity.getBestCandidate().getProperties().get(this.adelConf.getIndexProperties().get(
            "link")).get(0).startsWith("NIL")) {
          uniqueNils.add(entity.getBestCandidate().getProperties().get(
              this.adelConf.getIndexProperties().get("link")).get(0));
        }
      }
    }
    
    return uniqueNils.size();
  }
  
  private Map<String, Set<String>> numberUniqueNilEntitiesPerType() {
    final Map<String, Set<String>> uniqueNilEntitiesPerType = new HashMap<>();
    
    for (final Map.Entry<String, List<Entity>> entry : this.gs.entrySet()) {
      for (final Entity entity : entry.getValue()) {
        if (entity.getBestCandidate().getProperties().get(
            this.adelConf.getIndexProperties().get("link")).get(0).startsWith("NIL")) {
          if (uniqueNilEntitiesPerType.containsKey(entity.getType())) {
            uniqueNilEntitiesPerType.get(entity.getType()).add(
                entity.getBestCandidate().getProperties().get(
                    this.adelConf.getIndexProperties().get("link")).get(0));
          } else {
            final Set<String> tmp = new HashSet<>();
            
            tmp.add(entity.getBestCandidate().getProperties().get(
                this.adelConf.getIndexProperties().get("link")).get(0));
            
            uniqueNilEntitiesPerType.put(entity.getType(), tmp);
          }
        }
      }
    }
    
    return uniqueNilEntitiesPerType;
  }
  
  private int numberUniqueEntities() {
    final Set<String> uniqueEntities = new HashSet<>();
    
    for (final Map.Entry<String, List<Entity>> entry : this.gs.entrySet()) {
      for (final Entity entity : entry.getValue()) {
        uniqueEntities.add(entity.getBestCandidate().getProperties().get(
            this.adelConf.getIndexProperties().get("link")).get(0));
      }
    }
    
    return uniqueEntities.size();
  }
  
  private Map<String, List<String>> numberEntitiesPerType() {
    final Map<String, List<String>> nilEntitiesPerType = new HashMap<>();
    
    for (final Map.Entry<String, List<Entity>> entry : this.gs.entrySet()) {
      for (final Entity entity : entry.getValue()) {
        if (!entity.getBestCandidate().getProperties().get(
            this.adelConf.getIndexProperties().get("link")).get(0).startsWith("NIL")) {
          if (nilEntitiesPerType.containsKey(entity.getType())) {
            nilEntitiesPerType.get(entity.getType()).add(
                entity.getBestCandidate().getProperties().get(
                    this.adelConf.getIndexProperties().get("link")).get(0));
          } else {
            final List<String> tmp = new ArrayList<>();
            
            tmp.add(entity.getBestCandidate().getProperties().get(
                this.adelConf.getIndexProperties().get("link")).get(0));
            
            nilEntitiesPerType.put(entity.getType(), tmp);
          }
        }
      }
    }
    
    return nilEntitiesPerType;
  }
  
  private Map<String, Set<String>> numberUniqueEntitiesPerType() {
    final Map<String, Set<String>> uniqueNilEntitiesPerType = new HashMap<>();
    
    for (final Map.Entry<String, List<Entity>> entry : this.gs.entrySet()) {
      for (final Entity entity : entry.getValue()) {
        if (!entity.getBestCandidate().getProperties().get(
            this.adelConf.getIndexProperties().get("link")).get(0).startsWith("NIL")) {
          if (uniqueNilEntitiesPerType.containsKey(entity.getType())) {
            uniqueNilEntitiesPerType.get(entity.getType()).add(
                entity.getBestCandidate().getProperties().get(
                    this.adelConf.getIndexProperties().get("link")).get(0));
          } else {
            final Set<String> tmp = new HashSet<>();
            
            tmp.add(entity.getBestCandidate().getProperties().get(
                this.adelConf.getIndexProperties().get("link")).get(0));
            
            uniqueNilEntitiesPerType.put(entity.getType(), tmp);
          }
        }
      }
    }
    
    return uniqueNilEntitiesPerType;
  }
}
