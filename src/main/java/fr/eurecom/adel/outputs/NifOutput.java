package fr.eurecom.adel.outputs;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.datatypes.xsd.XSDDatatype;

import fr.eurecom.adel.datatypes.Entity;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;

import java.io.StringWriter;
import java.util.Map;
import java.util.List;
import java.util.HashMap;

/**
 * @author Julien Plu
 */
public class NifOutput implements IOutput<Entity> {
  private static final String NIF =
      "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#";
  private static final String ITSRDF = "http://www.w3.org/2005/11/its/rdf#";
  private static final String DUL = "http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#";
  private final String host;
  private final String lang;
  private final String to;
  
  public NifOutput(final String newLang, final String newHost, final String newTo) {
    this.lang = newLang;
    this.host = newHost;
    this.to = newTo;
  }
  
  @Override
  public final String write(final Map<String, Pair<String, List<Entity>>> entries,
                            final Map<String, String> indexProperties) {
    final StringWriter sw = new StringWriter();
    Model model = ModelFactory.createDefaultModel();
    
    for (final Map.Entry<String, Pair<String, List<Entity>>> entry : entries.entrySet()) {
      model = model.union(this.createNifModel(entry.getKey(), entry.getValue().getLeft(),
          entry.getValue().getRight(), indexProperties));
    }
    
    RDFDataMgr.write(sw, model, RDFFormat.TURTLE_PRETTY);
    
    return sw.toString();
  }
  
  private Model createNifModel(final String uuid, final String sentence,
                               final Iterable<Entity> entries,
                               final Map<String, String> indexProperties) {
    final Model nifModel = ModelFactory.createDefaultModel();
    final Map<String, String> prefixes = new HashMap<>();
    final Resource sentenceResource = ResourceFactory.createResource(this.host + "/context/" + uuid
        + "#char=0," + sentence.codePoints().count());
    final Resource stringType = ResourceFactory.createResource(NifOutput.NIF + "String");
    final Resource rfcStringType = ResourceFactory.createResource(NifOutput.NIF + "RFC5147String");
    final Resource phrase = ResourceFactory.createResource(NifOutput.NIF + "Phrase");
    final Resource contextType = ResourceFactory.createResource(NifOutput.NIF + "Context");
    final Property beginIndex = ResourceFactory.createProperty(NifOutput.NIF + "beginIndex");
    final Property endIndex = ResourceFactory.createProperty(NifOutput.NIF + "endIndex");
    final Property isString = ResourceFactory.createProperty(NifOutput.NIF + "isString");
    final Property anchorOf = ResourceFactory.createProperty(NifOutput.NIF + "anchorOf");
    final Property referenceContext = ResourceFactory.createProperty(NifOutput.NIF +
        "referenceContext");
    final Property taIdentRef = ResourceFactory.createProperty(NifOutput.ITSRDF + "taIdentRef");
    final Property taClassRef = ResourceFactory.createProperty(NifOutput.ITSRDF + "taClassRef");
    final Property taSource = ResourceFactory.createProperty(NifOutput.ITSRDF + "taSource");
    
    prefixes.put("nif", NifOutput.NIF);
    prefixes.put("itsrdf", NifOutput.ITSRDF);
    
    if ("OKE".equals(this.to)) {
      prefixes.put("dul", NifOutput.DUL);
    }
    
    prefixes.put("xsd", XSD.getURI());
    prefixes.put("rdf", RDF.getURI());
  
    Resource type = null;
    
    if ("Stanford".equals(this.to) || "NEEL".equals(this.to)) {
      type = ResourceFactory.createResource(this.host + '/' + this.to + '/');
    }
    
    nifModel.setNsPrefixes(prefixes);
    
    nifModel.add(sentenceResource, RDF.type, stringType);
    nifModel.add(sentenceResource, RDF.type, rfcStringType);
    nifModel.add(sentenceResource, RDF.type, contextType);
    nifModel.addLiteral(sentenceResource, beginIndex, ResourceFactory.createTypedLiteral("0",
        XSDDatatype.XSDnonNegativeInteger));
    nifModel.addLiteral(sentenceResource, endIndex, ResourceFactory.createTypedLiteral(
        Long.toString(sentence.codePoints().count()), XSDDatatype.XSDnonNegativeInteger));
    nifModel.addLiteral(sentenceResource, isString, ResourceFactory.createLangLiteral(sentence,
        this.lang));
    
    for (final Entity entity : entries) {
      final Resource entityResource = ResourceFactory.createResource(this.host + "/entity/" + uuid
          + "#char=" + entity.getStartPosition() + ',' + entity.getEndPosition());
  
      nifModel.add(entityResource, RDF.type, stringType);
      nifModel.add(entityResource, RDF.type, rfcStringType);
      nifModel.add(entityResource, RDF.type, phrase);
      
      if (!entity.getBestCandidate().getFrom().isEmpty()) {
        if (entity.getBestCandidate().getProperties().get(indexProperties.get("link")).get(
            0).startsWith("NIL")) {
          prefixes.put("adel", this.host + '/');
  
          nifModel.add(entityResource, taIdentRef, ResourceFactory.createResource(this.host +
              "/NIL/" + entity.getPhrase().replaceAll(" ", "_") + '_' + uuid));
        } else {
          nifModel.add(entityResource, taIdentRef, ResourceFactory.createResource(
              entity.getBestCandidate().getProperties().get(indexProperties.get("link")).get(0)));
        }
      }
      
      nifModel.add(entityResource, anchorOf, ResourceFactory.createPlainLiteral(
          entity.getPhrase()));
      nifModel.add(entityResource, beginIndex, ResourceFactory.createTypedLiteral(
          Integer.toString(entity.getStartPosition()), XSDDatatype.XSDnonNegativeInteger));
      nifModel.add(entityResource, endIndex, ResourceFactory.createTypedLiteral(Integer.toString(
          entity.getEndPosition()), XSDDatatype.XSDnonNegativeInteger));
      nifModel.add(entityResource, referenceContext, sentenceResource);
      
      if (type == null) {
        nifModel.add(entityResource, taClassRef, ResourceFactory.createResource(
            entity.getType()));
      } else {
        nifModel.add(entityResource, taClassRef, ResourceFactory.createResource(type +
            entity.getType()));
      }
      
      if (!entity.getBestCandidate().getFrom().isEmpty()) {
        nifModel.add(entityResource, taSource, ResourceFactory.createPlainLiteral(
            entity.getBestCandidate().getFrom()));
      }
    }
    
    return nifModel;
  }
}
