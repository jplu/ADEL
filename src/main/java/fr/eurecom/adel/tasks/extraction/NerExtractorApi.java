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

import fr.eurecom.adel.annotations.Name;
import fr.eurecom.adel.configurations.ExtractorConfiguration;
import fr.eurecom.adel.datatypes.Entity;
import fr.eurecom.adel.utils.StringUtils;

/**
 * @author Julien Plu
 */
@Name(name = "NER API")
public class NerExtractorApi extends AbstractApiExtractor {
  public NerExtractorApi(final fr.eurecom.adel.datatypes.Query query,
                         final ExtractorConfiguration newConf, final String lang) {
    super(newConf);
    this.fillParameters(query, lang);
  }
  
  @Override
  final void fillMentions(final Model model) {
    final String local = model.getNsPrefixURI("local");
    final String sparqlQuery = String.format(
        "PREFIX nif:   <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#>" +
        "PREFIX local: <%s>" +
        "SELECT ?txt ?type ?begin ?end WHERE {" +
        "    ?s a nif:Phrase ." +
        "    ?s local:type ?type ." +
            StringUtils.buildFilterSparqlQuery(this.getConf().getTags(), "type") +
        "    FILTER(!REGEX(STR(?txt), \"-LSB-|-LCB-|-LRB-|-RRB-|-RCB-|-RSB-\", \"i\")) ." +
        "    ?s nif:anchorOf ?txt ." +
        "    ?s nif:beginIndex ?begin ." +
        "    ?s nif:endIndex ?end ." +
        '}', local);
    
    final Query query = QueryFactory.create(sparqlQuery);
    
    try (final QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
      final ResultSet results = qexec.execSelect();
  
      while (results.hasNext()) {
        final QuerySolution binding = results.nextSolution();
        if (!binding.getLiteral("txt").toString().equals("#")
            && !binding.getLiteral("txt").toString().equals("#")) {
          this.addEntity(new Entity(binding.getLiteral("txt").toString(), new ArrayList<>(),
              Integer.valueOf(binding.getLiteral("begin").getValue().toString()),
              Integer.valueOf(binding.getLiteral("end").getValue().toString()),
              this.getConf().getName(), binding.getLiteral("type").toString(),
              this.getConf().getFrom()));
        }
      }
    }
  }
  
  @Override
  public final String toString() {
    return "NerExtractorApi{"
        + "name=" + this.getConf().getName()
        + ", entities=" + this.getEntities()
        + '}';
  }
}
