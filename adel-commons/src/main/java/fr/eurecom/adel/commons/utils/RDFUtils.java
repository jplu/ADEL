package fr.eurecom.adel.commons.utils;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Julien Plu on 2019-02-26.
 */
public class RDFUtils {
  public static boolean isValidRdf(final String content) {
    try {
      final Model model = ModelFactory.createDefaultModel();
      
      RDFDataMgr.read(model, IOUtils.toInputStream(content, Charset.forName("UTF-8")), Lang.TURTLE);
    } catch (final Exception ex) {
      return false;
    }
    
    return true;
  }
  
  public static boolean isValidRdf(final Path content) {
    try {
      final Model model = ModelFactory.createDefaultModel();
      
      RDFDataMgr.read(model, Files.newInputStream(content), Lang.TURTLE);
    } catch (final Exception ex) {
      return false;
    }
    
    return true;
  }
}
