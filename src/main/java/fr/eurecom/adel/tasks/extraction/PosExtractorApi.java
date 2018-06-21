package fr.eurecom.adel.tasks.extraction;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.eurecom.adel.annotations.Name;
import fr.eurecom.adel.configurations.ExtractorConfiguration;
import fr.eurecom.adel.datatypes.Entity;
import fr.eurecom.adel.utils.StringUtils;

/**
 * @author Julien Plu
 */
@Name(name = "POS API")
public final class PosExtractorApi extends AbstractApiExtractor {
  public PosExtractorApi(final fr.eurecom.adel.datatypes.Query query,
                         final ExtractorConfiguration newConf, final String lang) {
    super(newConf);
    this.fillParameters(query, lang);
  }
  
  @Override
  protected void fillMentions(final Model model) {
    final String sparqlQuery = String.format(
        "PREFIX nif:   <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#>" +
        "SELECT ?txt ?begin ?end ?tag WHERE {" +
        "    ?s nif:posTag ?tag." +
            StringUtils.buildFilterSparqlQuery(this.getConf().getTags(), "tag") +
        "    ?s nif:anchorOf ?txt ." +
        "    FILTER(!REGEX(STR(?txt), \"-LSB-|-LCB-|-LRB-|-RRB-|-RCB-|-RSB-\", \"i\")) ." +
        "    ?s nif:beginIndex ?begin ." +
        "    ?s nif:endIndex ?end ." +
        "} ORDER BY ?begin", String.join("|", Arrays.asList(this.getConf().getTags().split(","))));
    final Query query = QueryFactory.create(sparqlQuery);
    
    try (final QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
      final ResultSet results = qexec.execSelect();
      StringBuilder tmpTxt = new StringBuilder();
      int tmpBegin = 0;
      int tmpEnd = 0;
      String tmpTag = "";
      boolean first = true;
      final Pattern p = Pattern.compile("[!\"$%'()*+:;<=>?\\[\\\\\\]^`{|}~]");
      
      while (results.hasNext()) {
        final QuerySolution binding = results.nextSolution();
        final Matcher m = p.matcher(binding.getLiteral("txt").toString());
        
        if (("#".equals(Character.toString(binding.getLiteral("txt").toString().charAt(0)))
            || "@".equals(Character.toString(binding.getLiteral("txt").toString().charAt(0))))
            && !m.find() && !binding.getLiteral("txt").toString().equals("#")
            && !binding.getLiteral("txt").toString().equals("@")) {
          
          this.addEntity(new Entity(binding.getLiteral("txt").toString(), new ArrayList<>(),
              Integer.parseInt(binding.getLiteral("begin").getValue().toString()),
              Integer.parseInt(binding.getLiteral("end").getValue().toString()),
              this.getConf().getName(), "", this.getConf().getFrom()));
        } else if (first && !m.find() && !binding.getLiteral("txt").toString().equals("#")
            && !binding.getLiteral("txt").toString().equals("@")) {
          tmpTxt.append(binding.getLiteral("txt"));
          tmpBegin = Integer.parseInt(binding.getLiteral("begin").getValue().toString());
          tmpEnd = Integer.parseInt(binding.getLiteral("end").getValue().toString());
          first = false;
          tmpTag = binding.getLiteral("tag").toString();
        } else {
          if ((tmpEnd + 1) == Integer.parseInt(binding.getLiteral("begin").getValue().toString())
              && !"#".equals(Character.toString(binding.getLiteral("txt").toString().charAt(0)))
              && !"@".equals(Character.toString(binding.getLiteral("txt").toString().charAt(0)))
              && !m.find() && !binding.getLiteral("txt").toString().equals("#")
              && !binding.getLiteral("txt").toString().equals("@")
              && binding.getLiteral("tag").toString().equals(tmpTag)) {
            tmpTxt.append(' ');
            tmpTxt.append(binding.getLiteral("txt"));
            tmpEnd = Integer.parseInt(binding.getLiteral("end").getValue().toString());
          } else if (!m.find() && !binding.getLiteral("txt").toString().equals("#")
              && !binding.getLiteral("txt").toString().equals("@")
              && !tmpTxt.toString().isEmpty()) {
            this.addEntity(new Entity(tmpTxt.toString(), new ArrayList<>(), tmpBegin, tmpEnd,
                this.getConf().getName(), "", this.getConf().getFrom()));
            
            tmpTxt = new StringBuilder();
            
            tmpTxt.append(binding.getLiteral("txt"));
            tmpTag = binding.getLiteral("tag").toString();
            
            tmpBegin = Integer.parseInt(binding.getLiteral("begin").getValue().toString());
            tmpEnd = Integer.parseInt(binding.getLiteral("end").getValue().toString());
          }
        }
      }
  
      final Matcher m = p.matcher(tmpTxt.toString());
      
      if (!tmpTxt.toString().isEmpty() && tmpEnd != 0 && !m.find()) {
        this.addEntity(new Entity(tmpTxt.toString(), new ArrayList<>(), tmpBegin, tmpEnd,
            this.getConf().getName(), "", this.getConf().getFrom()));
      }
    }
  }
  
  @Override
  public String toString() {
    return "ExtractionPOS{"
        + "name=" + this.getConf().getName()
        + ", entities=" + this.getEntities()
        + '}';
  }
}
