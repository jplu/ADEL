package fr.eurecom.adel.utils;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.OWL2;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import fr.eurecom.adel.configurations.AdelConfiguration;
import fr.eurecom.adel.datatypes.Candidate;
import fr.eurecom.adel.datatypes.Entity;
import fr.eurecom.adel.tasks.indexing.Elasticsearch;

/**
 * @author Julien Plu
 */
public class NifUtils {
  private final Map<String, String> sentences;
  private final Map<String, List<Entity>> entitiesPerSentence;
  private final Map<String, List<Entity>> gs;
  private final AdelConfiguration adelConf;
  private final String lang;
  
  public NifUtils(final Path newPath, final AdelConfiguration newAdelConf, final String newLang) {
    this.sentences = new HashMap<>();
    this.entitiesPerSentence = new HashMap<>();
    this.gs = new HashMap<>();
    this.adelConf = newAdelConf;
    this.lang = newLang;
    
    this.parseSentences(newPath);
    this.parseEntitiesPerSentence(newPath);
  }
  
  public NifUtils(final String nif, final AdelConfiguration newAdelConf, final String newLang) {
    this.sentences = new HashMap<>();
    this.entitiesPerSentence = new HashMap<>();
    this.gs = new HashMap<>();
    this.adelConf = newAdelConf;
    this.lang = newLang;
    
    this.parseSentences(nif);
    this.parseEntitiesPerSentence(nif);
  }
  
  public final Map<String, String> getSentences() {
    return Collections.unmodifiableMap(this.sentences);
  }
  
  public final Map<String, List<Entity>> getEntitiesPerSentence() {
    return Collections.unmodifiableMap(this.entitiesPerSentence);
  }
  
  public final Map<String, List<Entity>> getGs() {
    return Collections.unmodifiableMap(this.gs);
  }
  
  private void createCandidate(final String link, final String label,
                               final Candidate candidate) {
    candidate.putProperty(this.adelConf.getIndexProperties().get("types"), new ArrayList<>());
    candidate.putProperty(this.adelConf.getIndexProperties().get("link"),
        Collections.singletonList(link));
    candidate.putProperty(this.adelConf.getIndexProperties().get("label"),
        Collections.singletonList(label));
    
    candidate.setFrom(this.adelConf.getIndex().getFrom());
  }
  
  private String segmentedPhrase(final String id, final String phrase, final int start) {
    String res = phrase;
    final String sentence = StringUtils.normalizeString(this.sentences.get(id));
    
    if (start > 0) {
      if ("@".equals(Character.toString(sentence.charAt(start - 1)))) {
        res = StringUtils.twitterQueryUserName(phrase);
      } else if ("#".equals(Character.toString(sentence.charAt(start - 1)))) {
        res = HashtagSegmentation.segment(phrase, this.lang);
      }
    }
    
    return res;
  }
  
  private void parseEntitiesPerSentence(final String nif) {
    final Model model = ModelFactory.createDefaultModel();
  
    RDFDataMgr.read(model, new ByteArrayInputStream(nif.getBytes(Charset.forName("UTF-8"))),
        Lang.TURTLE);
  
    final String getMentionsQuery = "" +
        "PREFIX nif: <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#>" +
        "PREFIX itsrdf: <http://www.w3.org/2005/11/its/rdf#>" +
        "PREFIX owl: <" + OWL2.NS + '>' +
        "PREFIX dbo: <http://dbpedia.org/ontology/>" +
        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
        "SELECT DISTINCT ?sentence (STR(?beginIndex) AS ?begin) (STR(?endIndex) AS ?end) ?type " +
        "(STR(?anchorOf) AS ?txt) ?link (STR(?labelLink) AS ?label) (STR(?source) AS ?sc) where {" +
        "    ?sentence a nif:Context ." +
        "    ?mention nif:referenceContext ?sentence ." +
        "    ?mention nif:beginIndex ?beginIndex ." +
        "    ?mention nif:endIndex ?endIndex ." +
        "    ?mention nif:anchorOf ?anchorOf ." +
        "    OPTIONAL {" +
        "        ?mention itsrdf:taIdentRef ?entity ." +
        "    }" +
        "    OPTIONAL {" +
        "        ?entity a ?mentionType ." +
        "        FILTER(?mentionType != <http://www.w3.org/2002/07/owl#Individual>) ." +
        "        OPTIONAL {" +
        "            ?entity owl:sameAs ?sameAs ." +
        "        }" +
        "        ?entity rdfs:label ?labelTmp ." +
        "    }" +
        "    OPTIONAL {" +
        "        ?mention itsrdf:taClassRef ?mentionType ." +
        "    }" +
        "    OPTIONAL {" +
        "        ?mention itsrdf:taSource ?mentionSource ." +
        "    }" +
        "    BIND(COALESCE(?mentionType, \"NO_TYPE\") AS ?type) ." +
        "    BIND(COALESCE(?mentionSource, \"NO_SOURCE\") AS ?source) ." +
        "    BIND(COALESCE(?sameAs, ?entity, \"NO_ENTITY\") AS ?link) ." +
        "    BIND(COALESCE(?labelTmp, ?anchorOf) AS ?labelLink) ." +
        "} ORDER BY ASC (?beginIndex)";
  
    try (final QueryExecution qeResourcesSentences = QueryExecutionFactory.create(getMentionsQuery,
        model)) {
      final ResultSet rsResourcesSentences = qeResourcesSentences.execSelect();
      int count = 0;
      final Map<String, String> nils = new HashMap<>();
      final Elasticsearch es = new Elasticsearch(this.adelConf);
      
      while (rsResourcesSentences.hasNext()) {
        final QuerySolution solTxt = rsResourcesSentences.nextSolution();
        final String[] sentenceComponents = solTxt.get("sentence").toString().split("/");
        final String id = sentenceComponents[sentenceComponents.length - 1].split("#")[0];
        final Candidate candidate = es.searchLink(solTxt.get("link").toString());
        
        if (candidate.getProperties().isEmpty()) {
          if (nils.containsKey(solTxt.get("link").toString())) {
            this.createCandidate(nils.get(solTxt.get("link").toString()),
                solTxt.get("label").toString(), candidate);
          } else {
            this.createCandidate("NIL" + Integer.toString(count),
                solTxt.get("label").toString(), candidate);
  
            nils.put(solTxt.get("link").toString(), "NIL" + Integer.toString(count));
            
            count++;
          }
        }
  
        final Entity entity;
        
        if ("NO_TYPE".equals(solTxt.get("type").toString())) {
          entity = new Entity(solTxt.get("txt").toString(), this.segmentedPhrase(id,
              solTxt.get("txt").toString(), Integer.parseInt(solTxt.get("begin").toString())),
              candidate, Integer.valueOf(solTxt.get("begin").toString()),
              Integer.valueOf(solTxt.get("end").toString()), "GS");
        } else {
          entity = new Entity(solTxt.get("txt").toString(), this.segmentedPhrase(id,
              solTxt.get("txt").toString(), Integer.parseInt(solTxt.get("begin").toString())),
              candidate, Integer.valueOf(solTxt.get("begin").toString()),
              Integer.valueOf(solTxt.get("end").toString()), "GS", solTxt.get("type").toString());
        }
        
        if (this.entitiesPerSentence.containsKey(id)) {
          this.gs.get(id).add(entity);
          this.entitiesPerSentence.get(id).add(new Entity(solTxt.get("txt").toString(),
              new ArrayList<>(), Integer.parseInt(solTxt.get("begin").toString()),
              Integer.parseInt(solTxt.get("end").toString()), ""));
        } else {
          this.gs.put(id, new ArrayList<>(Collections.singleton(entity)));
          this.entitiesPerSentence.put(id, new ArrayList<>(Collections.singleton(new Entity(
              solTxt.get("txt").toString(), new ArrayList<>(),
              Integer.parseInt(solTxt.get("begin").toString()),
              Integer.parseInt(solTxt.get("end").toString()), ""))));
        }
      }
    }
  }
  
  private void parseEntitiesPerSentence(final Path newPath) {
    try {
      this.parseEntitiesPerSentence(new String(Files.readAllBytes(newPath),
          Charset.forName("UTF-8")));
    } catch (final IOException ex) {
      throw new WebApplicationException("Failed to read the NIF file", ex,
          Response.Status.PRECONDITION_FAILED);
    }
  }
  
  private void parseSentences(final String nif) {
    final Model model = ModelFactory.createDefaultModel();
  
    RDFDataMgr.read(model, new ByteArrayInputStream(nif.getBytes(Charset.forName("UTF-8"))),
        Lang.TURTLE);
    
    final String getTxtQuery = "" +
        "PREFIX nif: <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#>" +
        "SELECT DISTINCT ?sentence (STR(?tagTxt) AS ?txt) where {" +
        "    ?sentence a nif:Context ." +
        "    ?sentence nif:isString ?tagTxt ." +
        '}';
  
    try (final QueryExecution qeResourcesSentences = QueryExecutionFactory.create(getTxtQuery,
        model)) {
      final ResultSet rsResourcesSentences = qeResourcesSentences.execSelect();
    
      while (rsResourcesSentences.hasNext()) {
        final QuerySolution solTxt = rsResourcesSentences.nextSolution();
        final String[] sentenceComponents = solTxt.get("sentence").toString().split("/");
        final String id = sentenceComponents[sentenceComponents.length - 1].split("#")[0];
        
        this.sentences.put(id, solTxt.get("txt").toString().replace("\\\"", "\""));
      }
    }
  }
  
  private void parseSentences(final Path newPath) {
    try {
      this.parseSentences(new String(Files.readAllBytes(newPath), Charset.forName("UTF-8")));
    } catch (final IOException ex) {
      throw new WebApplicationException("Failed to read the NIF file", ex,
          Response.Status.PRECONDITION_FAILED);
    }
  }
}
