package fr.eurecom.adel.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentAction;
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import fr.eurecom.adel.configurations.AdelConfiguration;
import fr.eurecom.adel.configurations.PipelineConfiguration;
import fr.eurecom.adel.core.Adel;
import fr.eurecom.adel.datatypes.ExtractQuery;
import fr.eurecom.adel.datatypes.Query;
import fr.eurecom.adel.utils.HashtagSegmentation;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;

/**
 * @author Julien Plu
 */
public class ExtractCommand<T extends PipelineConfiguration> extends ConfiguredCommand<T> {
  public ExtractCommand() {
    super("extract", "Only extract and type entities");
  }
  
  @Override
  public final void configure(final Subparser subparser) {
    super.configure(subparser);
    // Add command line options
    final MutuallyExclusiveGroup group = subparser.addMutuallyExclusiveGroup("inputs")
        .required(true);
    final ArgumentAction urlAction = new UrlAction();
  
    group.addArgument("-t", "--text")
        .dest("text")
        .type(String.class)
        .help("text to analyse");
    group.addArgument("-i", "--input-file")
        .dest("ifile")
        .type(Arguments.fileType().verifyExists().verifyIsFile().verifyCanRead())
        .help("Input file name which contain the text to process");
    group.addArgument("-u", "--url")
        .dest("url")
        .type(String.class)
        .action(urlAction)
        .help("URL to process");
    
    subparser.addArgument("-of", "--output-format")
        .dest("oformat")
        .type(String.class)
        .required(false)
        .setDefault("nif")
        .choices("nif", "brat", "conll", "naf", "tsv")
        .help("the output format for the annotations");
    subparser.addArgument("-if", "--input-format")
        .dest("iformat")
        .type(String.class)
        .required(false)
        .setDefault("raw")
        .choices("raw", "srt", "ttml", "tsv", "nif")
        .help("the input format for the text");
    subparser.addArgument("-s", "--setting")
        .dest("setting")
        .type(String.class)
        .required(false)
        .setDefault("default")
        .help("Select the setting");
    subparser.addArgument("-l", "--language")
        .dest("lang")
        .type(String.class)
        .required(false)
        .setDefault("en")
        .help("Select the language");
    subparser.addArgument("-o", "--output-file")
        .dest("ofile")
        .type(Arguments.fileType().verifyNotExists().verifyCanWriteParent())
        .required(false)
        .help("Output file name which will contain the annotations");
  }
  
  @Override
  protected final void run(final Bootstrap<T> newBootstrap, final Namespace
      newNamespace, final T newT) throws Exception {
    final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    final AdelConfiguration conf = mapper.readValue(new File("profiles"
        + FileSystems.getDefault().getSeparator() + newNamespace.getString("lang") + '_'
        + newNamespace.getString("setting") + ".yaml"), AdelConfiguration.class);
    final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    final Validator validator = factory.getValidator();
    final Set<ConstraintViolation<AdelConfiguration>> violations = validator.validate(conf);
  
    if (!violations.isEmpty()) {
      final StringBuilder sb = new StringBuilder();
    
      violations.forEach(error -> sb.append(error.getPropertyPath() + " " +
          error.getMessage()).append("\n"));
    
      throw new WebApplicationException(sb.toString(), Response.Status.PRECONDITION_FAILED);
    }
    
    conf.prepareTypesMapping();
    HashtagSegmentation.loadDictionaries(newNamespace.getString("lang"));
    
    final Adel pipeline = new Adel(conf);
    
    final long time = System.currentTimeMillis();
  
    if (newNamespace.getString("ifile") == null &&
        ("srt".equals(newNamespace.getString("iformat")) ||
            "ttml".equals(newNamespace.getString("iformat")))) {
      System.out.println("The input formats srt and ttml can be used only with an input file");
      
      return;
    }
  
    String text = "";
    
    if (newNamespace.getString("ifile") != null) {
      text = FileUtils.readFileToString(new File(newNamespace.getString("ifile")),
          Charset.forName("UTF-8"));
    } else if (newNamespace.getString("text") != null) {
      text = newNamespace.getString("text");
    }
  
    final Query query = new ExtractQuery();
    
    query.setOutput(newNamespace.getString("oformat"));
    query.setInput(newNamespace.getString("iformat"));
    
    if (text.isEmpty()) {
      query.setContent(newNamespace.getString("url"));
    } else {
      query.setContent(text);
    }
    
    if ("nif".equals(newNamespace.getString("iformat"))) {
      if (newNamespace.getString("url") != null) {
        query.setContent(IOUtils.toString(new URL(newNamespace.getString("url")),
            Charset.forName("UTF-8")));
      }
    }
    
    final String result = pipeline.extract(query, newNamespace.getString("lang"),
        "http://localhost");
    
    if (newNamespace.getString("ofile") != null) {
      FileUtils.write(new File(newNamespace.getString("ofile")), result, Charset.forName("UTF-8"));
    } else {
      System.out.println(result);
    }
  
    System.out.println("Time: " + (System.currentTimeMillis() - time) + "ms");
  }
}
