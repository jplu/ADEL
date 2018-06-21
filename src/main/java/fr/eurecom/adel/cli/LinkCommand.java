package fr.eurecom.adel.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentAction;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
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
import fr.eurecom.adel.datatypes.LinkQuery;
import fr.eurecom.adel.utils.HashtagSegmentation;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;

/**
 * @author Julien Plu
 */
public class LinkCommand<T extends PipelineConfiguration> extends ConfiguredCommand<T> {
  public LinkCommand() {
    super("link", "Only linking entities");
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
        .setDefault("tac")
        .choices("nif", "brat", "tac", "naf")
        .help("the output format for the annotations");
    subparser.addArgument("-if", "--input-format")
        .dest("iformat")
        .type(String.class)
        .required(false)
        .setDefault("raw")
        .choices("raw", "tsv", "nif")
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
    subparser.addArgument("-g", "--gold-standard")
        .dest("g")
        .type(Arguments.fileType().verifyExists().verifyIsFile().verifyCanRead())
        .required(false)
        .help("Gold standard in case of perfect linking or candidate generation if TSV dataset");
    
    final MutuallyExclusiveGroup perfectOption = subparser.addMutuallyExclusiveGroup(
        "perfect-option");
  
    perfectOption.addArgument("-pl", "--perfect-linking")
        .dest("pl")
        .type(Boolean.class)
        .action(Arguments.storeConst())
        .setConst(true)
        .setDefault(false)
        .help("If perfect linking");
    perfectOption.addArgument("-pcg", "--perfect-candidate-generation")
        .dest("pcg")
        .type(Boolean.class)
        .action(Arguments.storeConst())
        .setConst(true)
        .setDefault(false)
        .help("If perfect candidate generation");
    
  }
  
  @Override
  protected final void run(final Bootstrap<T> newBootstrap, final Namespace
      newNamespace, final T newT) throws Exception {
    if (newNamespace.getString("g") == null && (newNamespace.getBoolean("pl")
        || newNamespace.getBoolean("pcg")) && newNamespace.getString("iformat").equals("tsv")) {
      System.out.println("A gold standard must be given");
    }
  
    if (newNamespace.getString("g") != null && !newNamespace.getBoolean("pl")
        && !newNamespace.getBoolean("pcg") && newNamespace.getString("iformat").equals("tsv")) {
      System.out.println("The perfect linking or candidate generation option must be given");
    }
    
    if (newNamespace.getString("g") != null && !newNamespace.getString("iformat").equals("tsv")) {
      System.out.println("The gold standard is needed only for TSV datasets");
    }
  
    if (newNamespace.getString("g") == null && (newNamespace.getBoolean("pl")
        || newNamespace.getBoolean("pcg")) && newNamespace.getString("iformat").equals("raw")) {
      System.out.println("Impossible to run perfect linking or candidate generation in case of " +
          "raw input");
    }
    
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
    conf.prepareIndexProperties();
    
    HashtagSegmentation.loadDictionaries(newNamespace.getString("lang"));
  
    final Adel pipeline = new Adel(conf);
  
    final long time = System.currentTimeMillis();
    String text = "";
  
    if (newNamespace.getString("ifile") != null) {
      text = FileUtils.readFileToString(new File(newNamespace.getString("ifile")),
          Charset.forName("UTF-8"));
    } else if (newNamespace.getString("text") != null) {
      text = newNamespace.getString("text");
    }
    
    final LinkQuery query = new LinkQuery();
  
    query.setOutput(newNamespace.getString("oformat"));
    query.setInput(newNamespace.getString("iformat"));
    query.setContent(text);
    query.setPerfectCandidateGeneration(newNamespace.getBoolean("pcg"));
    query.setPerfectLinking(newNamespace.getBoolean("pl"));
    
    if (newNamespace.getString("g") != null) {
      query.setGoldStandard(FileUtils.readFileToString(new File(newNamespace.getString("g")),
          Charset.forName("UTF-8")));
    }
  
    if ("nif".equals(newNamespace.getString("iformat"))) {
      if (newNamespace.getString("url") != null) {
        query.setContent(IOUtils.toString(new URL(newNamespace.getString("url")),
            Charset.forName("UTF-8")));
      }
    }
    
    final String result = pipeline.link(query, newNamespace.getString("lang"), "http://localhost");
    
    if (newNamespace.getString("ofile") != null) {
      FileUtils.write(new File(newNamespace.getString("ofile")), result, Charset.forName("UTF-8"));
    } else {
      System.out.println(result);
    }
  
    System.out.println("Time: " + (System.currentTimeMillis() - time) + "ms");
  }
}
