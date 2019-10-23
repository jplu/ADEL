package fr.eurecom.adel.commons.formats;

import org.apache.commons.io.IOUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;
import org.javatuples.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import fr.eurecom.adel.commons.datatypes.Document;
import fr.eurecom.adel.commons.datatypes.Entity;
import fr.eurecom.adel.commons.exceptions.NIFMalformedException;
import fr.eurecom.adel.commons.utils.RDFUtils;
import fr.eurecom.adel.commons.utils.ScoringUtils;
import fr.eurecom.adel.commons.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Julien Plu on 2019-02-26.
 */
@Slf4j
public class NIF {
  private static final String NIFCORE = "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#";
  private static final String ITSRDF = "http://www.w3.org/2005/11/its/rdf#";
  private static final String HOST = "http://adel.eurecom.fr";
  private Path input;
  private Path output;
  private final Map<String, Pair<String, List<Entity>>> goldDocuments;
  private Map<String, Pair<String, List<Entity>>> annotatedDocuments;
  
  public NIF(final String newInput, final String newOutput) throws IOException, NIFMalformedException {
    this.input = Paths.get(newInput);
    this.output = Paths.get(newOutput);
    this.goldDocuments = new HashMap<>();
    this.annotatedDocuments = new HashMap<>();
    
    this.readFromInputFile("");
  }
  
  public NIF(final String newOutput) {
    this.output = Paths.get(newOutput);
    this.goldDocuments = new HashMap<>();
    this.annotatedDocuments = new HashMap<>();
  }
  
  public NIF() {
    this.goldDocuments = new HashMap<>();
    this.annotatedDocuments = new HashMap<>();
  }
  
  public final void setInputFile(final String newInputFile) throws IOException, NIFMalformedException {
    this.readFromInputFile(newInputFile);
  }
  
  public final Map<String, Pair<String, List<Entity>>> goldAnnotations() {
    return Collections.unmodifiableMap(this.goldDocuments);
  }
  
  public final void setAnnotations(final Map<String, Pair<String, List<Entity>>> annotations) {
    this.annotatedDocuments = new HashMap<>(annotations);
  }
  
  public final void setNIF(final String nifString) throws NIFMalformedException {
    this.readFromString(nifString);
  }
  
  public final void setOutputFile(final String newOutputFile) {
    this.output = Paths.get(newOutputFile);
  }
  
  private void readFromString(final String nifString) throws NIFMalformedException {
    if (!RDFUtils.isValidRdf(nifString)) {
      throw new NIFMalformedException("Malformed NIF file");
    }
  
    final Model model = ModelFactory.createDefaultModel();
    final InputStream targetStream = IOUtils.toInputStream(nifString, StandardCharsets.UTF_8);
    
    RDFDataMgr.read(model, targetStream, Lang.TURTLE);
    
    this.read("", model);
  }
  
  private void readFromInputFile(final String gold) throws IOException, NIFMalformedException {
    final Model model = ModelFactory.createDefaultModel();
  
    if (gold.isEmpty()) {
      if (!RDFUtils.isValidRdf(this.input)) {
        throw new NIFMalformedException("Malformed NIF file");
      }
      
      RDFDataMgr.read(model, Files.newInputStream(this.input), Lang.TURTLE);
    } else {
      if (!RDFUtils.isValidRdf(Paths.get(gold))) {
        throw new NIFMalformedException("Malformed NIF file");
      }
      
      RDFDataMgr.read(model, Files.newInputStream(Paths.get(gold)), Lang.TURTLE);
    }
    
    this.read(gold, model);
  }
  
  public final String stringOutput() {
    Model model = ModelFactory.createDefaultModel();
  
    for (final Map.Entry<String, Pair<String, List<Entity>>> entry : this.annotatedDocuments.entrySet()) {
      model = model.union(this.writeDocument(entry.getKey(), entry.getValue().getValue0(), entry.getValue().getValue1()));
    }
    
    final StringWriter sw = new StringWriter();
  
    RDFDataMgr.write(sw, model, RDFFormat.TURTLE_PRETTY);
    
    return sw.toString();
  }
  
  private void read(final String gold, final Model model) {
    final String getMentionsQuery = "" +
        "PREFIX nif: <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#>" +
        "PREFIX itsrdf: <http://www.w3.org/2005/11/its/rdf#>" +
        "PREFIX owl: <" + OWL2.NS + '>' +
        "PREFIX dbo: <http://dbpedia.org/ontology/>" +
        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
        "SELECT DISTINCT ?document (STR(?docTxt) AS ?doc) (STR(?beginIndex) AS ?begin) (STR(?endIndex) AS ?end) ?type " +
        "(STR(?anchorOf) AS ?txt) ?link (STR(?labelLink) AS ?label) (STR(?source) AS ?sc) where {" +
        "    ?document a nif:Context ." +
        "    ?document nif:isString ?docTxt ." +
        "    OPTIONAL {" +
        "        ?mention nif:referenceContext ?document ." +
        "        ?mention nif:beginIndex ?beginIndex ." +
        "        ?mention nif:endIndex ?endIndex ." +
        "        ?mention nif:anchorOf ?anchorOf ." +
        "    }" +
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
  
    try (final QueryExecution qeResourcesSentences = QueryExecutionFactory.create(getMentionsQuery, model)) {
      final ResultSet rsResourcesSentences = qeResourcesSentences.execSelect();
    
      while (rsResourcesSentences.hasNext()) {
        final QuerySolution solTxt = rsResourcesSentences.nextSolution();
        final String doc = solTxt.get("doc").toString();
        final String[] documentURI = solTxt.get("document").toString().split("/");
        final String id = documentURI[documentURI.length - 1].split("#")[0];
        
        if (null != solTxt.get("txt")) {
          String type = solTxt.get("type").toString();

          if (!type.contains("dbpedia")) {
            type = type.substring(type.lastIndexOf('/') + 1);
          }

          final Entity entity = Entity.builder()
              .phrase(solTxt.get("txt").toString())
              .cleanPhrase(solTxt.get("txt").toString())
              .startOffset(Integer.parseInt(solTxt.get("begin").toString()))
              .endOffset(Integer.valueOf(solTxt.get("end").toString()))
              .type(type)
              .build();
  
          if (gold.isEmpty()) {
            if (this.annotatedDocuments.containsKey(doc)) {
              this.annotatedDocuments.get(doc).getValue1().add(entity);
            } else {
              this.annotatedDocuments.put(doc, Pair.with(id,
                  new ArrayList<>(Collections.singleton(entity))));
            }
          } else {
            if (this.goldDocuments.containsKey(doc)) {
              this.goldDocuments.get(doc).getValue1().add(entity);
            } else {
              this.goldDocuments.put(doc, Pair.with(id,
                  new ArrayList<>(Collections.singleton(entity))));
            }
          }
        } else {
          if (gold.isEmpty()) {
            this.annotatedDocuments.put(doc, Pair.with(id,
                new ArrayList<>(Collections.emptyList())));
          } else {
            this.goldDocuments.put(doc, Pair.with(id,
                new ArrayList<>(Collections.emptyList())));
          }
        }
      }
    }
  }
  
  public final List<String> documents() {
    if (this.goldDocuments.isEmpty()) {
      return new ArrayList<>(this.annotatedDocuments.keySet());
    }
    
    return new ArrayList<>(this.goldDocuments.keySet());
  }
  
  public final void addDocument(final Document document) {
    String id = UUID.randomUUID().toString();
    
    if (this.goldDocuments.containsKey(document.getText())) {
      id = this.goldDocuments.get(document.getText()).getValue0();
    }
  
    if (this.annotatedDocuments.containsKey(document.getText())) {
      id = this.annotatedDocuments.get(document.getText()).getValue0();
    }
    
    this.annotatedDocuments.put(document.getText(), Pair.with(id, document.getEntities()));
  }
  
  private Model writeDocument(final String text, final String id, final Iterable<Entity> entities) {
    final Model nifModel = ModelFactory.createDefaultModel();
    final Map<String, String> prefixes = new HashMap<>();
    final Resource sentenceResource = ResourceFactory.createResource(NIF.HOST + "/context/" + id + "#char=0," + StringUtils.printLength(text));
    final Resource stringType = ResourceFactory.createResource(NIF.NIFCORE + "String");
    final Resource rfcStringType = ResourceFactory.createResource(NIF.NIFCORE + "RFC5147String");
    final Resource phrase = ResourceFactory.createResource(NIF.NIFCORE + "Phrase");
    final Resource contextType = ResourceFactory.createResource(NIF.NIFCORE + "Context");
    final Property beginIndex = ResourceFactory.createProperty(NIF.NIFCORE + "beginIndex");
    final Property endIndex = ResourceFactory.createProperty(NIF.NIFCORE + "endIndex");
    final Property isString = ResourceFactory.createProperty(NIF.NIFCORE + "isString");
    final Property anchorOf = ResourceFactory.createProperty(NIF.NIFCORE + "anchorOf");
    final Property referenceContext = ResourceFactory.createProperty(NIF.NIFCORE + "referenceContext");
    final Property taClassRef = ResourceFactory.createProperty(NIF.ITSRDF + "taClassRef");
  
    prefixes.put("nif", NIF.NIFCORE);
    prefixes.put("itsrdf", NIF.ITSRDF);
  
    prefixes.put("xsd", XSD.getURI());
    prefixes.put("rdf", RDF.getURI());
  
    nifModel.setNsPrefixes(prefixes);
  
    nifModel.add(sentenceResource, RDF.type, stringType);
    nifModel.add(sentenceResource, RDF.type, rfcStringType);
    nifModel.add(sentenceResource, RDF.type, contextType);
    nifModel.addLiteral(sentenceResource, beginIndex, ResourceFactory.createTypedLiteral("0",
        XSDDatatype.XSDnonNegativeInteger));
    nifModel.addLiteral(sentenceResource, endIndex, ResourceFactory.createTypedLiteral(
        Integer.toString(StringUtils.printLength(text)), XSDDatatype.XSDnonNegativeInteger));
    nifModel.addLiteral(sentenceResource, isString, ResourceFactory.createPlainLiteral(text));
  
    for (final Entity entity : entities) {
      final Resource entityResource = ResourceFactory.createResource(NIF.HOST + "/entity/" + id + "#char=" + entity.getStartOffset() + ',' + entity.getEndOffset());
    
      nifModel.add(entityResource, RDF.type, stringType);
      nifModel.add(entityResource, RDF.type, rfcStringType);
      nifModel.add(entityResource, RDF.type, phrase);
    
      nifModel.add(entityResource, anchorOf, ResourceFactory.createPlainLiteral(entity.getPhrase()));
      nifModel.add(entityResource, beginIndex, ResourceFactory.createTypedLiteral(
          Integer.toString(entity.getStartOffset()), XSDDatatype.XSDnonNegativeInteger));
      nifModel.add(entityResource, endIndex, ResourceFactory.createTypedLiteral(Integer.toString(
          entity.getEndOffset()), XSDDatatype.XSDnonNegativeInteger));
      nifModel.add(entityResource, referenceContext, sentenceResource);
      nifModel.add(entityResource, taClassRef, ResourceFactory.createResource(NIF.HOST + "/type/" + entity.getType()));
    }
    
    return nifModel;
  }
  
  public final void write(final boolean print) throws IOException {
    Model model = ModelFactory.createDefaultModel();
    
    for (final Map.Entry<String, Pair<String, List<Entity>>> entry : this.annotatedDocuments.entrySet()) {
      model = model.union(this.writeDocument(entry.getKey(), entry.getValue().getValue0(), entry.getValue().getValue1()));
    }
    
    if (print) {
      final StringWriter sw = new StringWriter();
      
      RDFDataMgr.write(sw, model, RDFFormat.TURTLE_PRETTY);
      NIF.log.info("{}{}", System.lineSeparator(), sw);
    }

    if (null != this.output) {
      RDFDataMgr.write(Files.newOutputStream(this.output), model, RDFFormat.TURTLE_PRETTY);
    }
  }
  
  public final void scorerExtraction(final String inputFile, final String gold) throws IOException, NIFMalformedException {
    if (!inputFile.isEmpty()) {
      this.input = Paths.get(inputFile);

      this.readFromInputFile("");
    }
    
    this.readFromInputFile(gold);
  
    NIF.log.info("{}{}", System.lineSeparator(), ScoringUtils.scoreExtractionNIFAndTAC(this.goldDocuments, this.annotatedDocuments));
  }
  
  public final void scorerNER(final String inputFile, final String gold) throws IOException, NIFMalformedException {
    if (!inputFile.isEmpty()) {
      this.input = Paths.get(inputFile);
    
      this.readFromInputFile("");
    }
    
    this.readFromInputFile(gold);
    
    NIF.log.info("{}{}", System.lineSeparator(), ScoringUtils.scoreNERNIFAndTAC(this.goldDocuments, this.annotatedDocuments));
  }
}
