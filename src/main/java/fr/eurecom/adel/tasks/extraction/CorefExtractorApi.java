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
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import fr.eurecom.adel.annotations.Name;
import fr.eurecom.adel.configurations.ExtractorConfiguration;
import fr.eurecom.adel.datatypes.Entity;
import fr.eurecom.adel.utils.StringUtils;

/**
 * @author Julien Plu
 */
@Name(name = "Coref API")
public class CorefExtractorApi extends AbstractApiExtractor {
  public CorefExtractorApi(final fr.eurecom.adel.datatypes.Query query,
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
            "SELECT ?txt ?txtHead ?beginHead ?endHead ?begin ?end WHERE {" +
            "    ?s a nif:Phrase ." +
            "    ?s nif:anchorOf ?txt ." +
            StringUtils.buildFilterSparqlQuery(this.getConf().getTags(), "txt") +
            "    ?s nif:beginIndex ?begin ." +
            "    ?s nif:endIndex ?end ." +
            "    ?s local:head ?head ." +
            "    ?head nif:anchorOf ?txtHead ." +
            "    ?head nif:beginIndex ?beginHead ." +
            "    ?head nif:endIndex ?endHead ." +
            '}', local);
    
    final Query query = QueryFactory.create(sparqlQuery);
    
    try (final QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
      final ResultSet results = qexec.execSelect();
      final Set<Entity> heads = new HashSet<>();
      
      while (results.hasNext()) {
        final QuerySolution binding = results.nextSolution();
        
        this.addEntity(new Entity(binding.getLiteral("txt").toString(),
            binding.getLiteral("txtHead").toString(), new ArrayList<>(),
            Integer.valueOf(binding.getLiteral("begin").getValue().toString()),
            Integer.valueOf(binding.getLiteral("end").getValue().toString()),
            this.getConf().getName(), this.getConf().getFrom()));
        
        if (Arrays.asList(this.getConf().getTags().toLowerCase(Locale.ENGLISH).split(
            ",")).contains(binding.getLiteral("txtHead").toString().toLowerCase(Locale.ENGLISH))
            || this.getConf().getTags().isEmpty()) {
          heads.add(new Entity(binding.getLiteral("txtHead").toString(),
              binding.getLiteral("txtHead").toString(), new ArrayList<>(),
              Integer.valueOf(binding.getLiteral("beginHead").getValue().toString()),
              Integer.valueOf(binding.getLiteral("endHead").getValue().toString()),
              this.getConf().getName(), this.getConf().getFrom()));
        }
      }
      
      for (final Entity head : heads) {
        this.addEntity(head);
      }
    }
  }
  
  @Override
  public final String toString() {
    return "CorefExtractorApi{"
        + "name=" + this.getConf().getName()
        + ", entities=" + this.getEntities()
        + '}';
  }
}
