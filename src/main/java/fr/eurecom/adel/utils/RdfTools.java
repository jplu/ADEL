package fr.eurecom.adel.utils;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModelSpec;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Giuseppe Rizzo
 * @author Julien Plu
 */
public class RdfTools {
    /*public static OntClass getFinedGrainedType(List<OntClass> hierarchy) {
        if (hierarchy.size() > 2) {
            return hierarchy.get(2);
        } else if (hierarchy.size() > 0) {
            return hierarchy.get(hierarchy.size() - 1);
        }

        return ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM).createClass("http://www.w3.org/2002/07/owl#Thing");
    }*/
    
    public static List<OntClass> stringTypesToOntClasses(final Iterable<String> stringTypes) {
        final List<OntClass> result = new ArrayList<>();
        
        for (final String str : stringTypes) {
            result.add(ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM).createClass(str.replace(
                "\\", "")));
        }
        
        return result;
    }
    
    public static boolean isValidRdf(final String content) {
        try {
            final Model model = ModelFactory.createDefaultModel();
            
            model.read(new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8"))), null,
                "TTL");
        } catch (final Exception ex) {
            return false;
        }
        
        return true;
    }
}