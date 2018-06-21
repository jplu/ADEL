package fr.eurecom.adel.tools;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Julien Plu
 */
public class GenerateDictionary {
  public void run(final Path output, final String endpoint, final String filter) throws
      IOException {
    final String sparqlFilter = String.join(" || ", Arrays.stream(filter.split(",")).map(
        s -> "?type = <" + s + '>').collect(Collectors.toList()));
    int offset = 0;
    final Map<String, String> result = new HashMap<>();
    int count = 0;
    
    while (result.isEmpty() || count < result.size()) {
      count = result.size();
      
      final String query = "" +
          "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
          "PREFIX foaf: <http://xmlns.com/foaf/0.1/>" +
          "PREFIX dc: <http://purl.org/dc/elements/1.1/>" +
          "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
          "SELECT DISTINCT (STR(?label) AS ?txt) ?type WHERE {" +
          "    ?s a ?type ." +
          "    {" +
          "        ?s dc:title ?label ." +
          "    } UNION {" +
          "        ?s rdfs:label ?label ." +
          "    } UNION {" +
          "        ?s foaf:name ?label ." +
          "    }" +
          "    FILTER(" + sparqlFilter + ") ." +
          "    FILTER(DATATYPE(?label) = xsd:string || LANG(?label) = \"en\") ." +
          "} LIMIT 10000 OFFSET " + offset;
  
      try (final QueryExecution qeType = QueryExecutionFactory.sparqlService(endpoint, query)) {
        final ResultSet rsType = qeType.execSelect();
        
        while (rsType.hasNext()) {
          final QuerySolution solTxt = rsType.nextSolution();
          final Pattern pattern = Pattern.compile("[$-/:-?{-~!\"^_'\\[\\]\\\\]");
          final Matcher matcher = pattern.matcher(solTxt.get("txt").toString());
          
          if (!matcher.find()) {
            result.put(solTxt.get("txt").toString(), solTxt.get("type").toString());
          }
        }
        
        offset += 10000;
      }
    }
  
    final StringBuilder sb = new StringBuilder();
    
    for (final Map.Entry<String, String> entry : result.entrySet()) {
      sb.append(entry.getKey());
      sb.append('\t');
      sb.append(entry.getValue());
      sb.append(System.lineSeparator());
    }
  
    Files.write(output, sb.toString().getBytes(Charset.forName("UTF-8")),
        StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
  }
}
