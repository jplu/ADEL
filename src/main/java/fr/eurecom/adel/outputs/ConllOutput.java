package fr.eurecom.adel.outputs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import fr.eurecom.adel.configurations.AdelConfiguration;
import fr.eurecom.adel.datatypes.Entity;
import fr.eurecom.adel.datatypes.ConllEntry;
import fr.eurecom.adel.utils.APICallerFactory;

public class ConllOutput implements IOutput<Entity> {
  private final String tokenizer;
  
  public ConllOutput(final AdelConfiguration newAdelConf) {
    this.tokenizer = newAdelConf.getExtract().getTokenize();
  }
  
  public ConllOutput(final String newTokenizer) {
    this.tokenizer = newTokenizer;
  }
  
  @Override
  public final String write(final Map<String, Pair<String, List<Entity>>> entries,
                            final Map<String, String> indexProperties) {
    final StringBuilder sb = new StringBuilder();
  
    entries.entrySet().forEach(entry -> {
      sb.append(this.writeSingleDocument(entry.getValue().getLeft(), entry.getValue().getRight()));
      sb.append(System.lineSeparator());
    });
  
    sb.deleteCharAt(sb.length() - 1);
    
    return sb.toString();
  }
  
  private String writeSingleDocument(final String text, final Iterable<Entity> entries) {
    final StringBuilder sb = new StringBuilder();
    final List<ConllEntry> conllEntries = this.getConllFormat(text);
    
    for (final ConllEntry entry : conllEntries) {
      for (final Entity entity : entries) {
        if ((entry.getBegin() >= entity.getStartPosition()
            && entry.getEnd() <= entity.getEndPosition()) || ((entry.getToken().startsWith("#") ||
            entry.getToken().startsWith("@")) &&
            entry.getBegin() + 1 >= entity.getStartPosition() &&
            entry.getEnd() <= entity.getEndPosition())) {
          entry.setCategory(entity.getType());
          
          break;
        }
      }
      
      sb.append(entry.getToken());
      sb.append('\t');
      if (entry.getCategory().isEmpty()) {
        sb.append("O");
      } else {
        sb.append(entry.getCategory());
      }
      sb.append(System.lineSeparator());
    }
    
    return sb.toString();
  }
  
  private List<ConllEntry> getConllFormat(final String text) {
    final APICallerFactory apiCall = APICallerFactory.getInstance();
    final Map<String, String> parameters = new HashMap<>();
    
    parameters.put("content", text);
    
    final List<ConllEntry> entries = new ArrayList<>();
    
    try {
      final Model model = (Model) apiCall.adelQuery(this.tokenizer, new HashMap<>(),
          new ObjectMapper().writeValueAsString(parameters));
      
      final String sparqlQuery =
          "PREFIX nif:   <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#>" +
              "SELECT ?txt ?begin ?end WHERE {" +
              "    ?s nif:anchorOf ?txt ." +
              "    ?s nif:beginIndex ?begin ." +
              "    ?s nif:endIndex ?end ." +
              "    ?s a nif:Word ." +
              "} ORDER BY ?begin";
      final Query query = QueryFactory.create(sparqlQuery);
      
      
      try (final QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
        final ResultSet results = qexec.execSelect();
        
        while (results.hasNext()) {
          final QuerySolution binding = results.nextSolution();
          
          entries.add(new ConllEntry(binding.getLiteral("txt").toString(),
              Integer.parseInt(binding.getLiteral("begin").getValue().toString()),
              Integer.parseInt(binding.getLiteral("end").getValue().toString())));
        }
      }
    } catch (final JsonProcessingException ex) {
      throw new WebApplicationException("Issue to create the tokenize query", ex,
          Response.Status.PRECONDITION_FAILED);
    }
    
    return entries;
  }
}
