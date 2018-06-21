package fr.eurecom.adel.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentAction;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import fr.eurecom.adel.configurations.AdelConfiguration;
import fr.eurecom.adel.tools.DatasetStats;
import fr.eurecom.adel.utils.HashtagSegmentation;
import io.dropwizard.Configuration;

/**
 * @author Julien Plu
 */
public class NifStatsCommand<T extends Configuration> extends AbstractToolsCommand<T> {
  NifStatsCommand() {
    super("nif-stats", "Compute NIF datasets statistics.");
  }
  
  @Override
  public final void configure(final Subparser subparser) {
    super.configure(subparser);
  
    subparser.addArgument("-i", "--input-file")
        .dest("ifile")
        .type(Arguments.fileType().verifyExists().verifyIsFile().verifyCanRead())
        .help("Input dataset");
    subparser.addArgument("-l", "--language")
        .dest("lang")
        .type(String.class)
        .required(false)
        .setDefault("en")
        .help("Select the language");
    subparser.addArgument("-s", "--setting")
        .dest("setting")
        .type(String.class)
        .required(false)
        .setDefault("default")
        .help("Select the setting");
  }
  
  @Override
  protected final void run(final Namespace newNamespace) throws Exception {
    final long time = System.currentTimeMillis();
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
    
    final DatasetStats stats = new DatasetStats();
    
    stats.runNif(Paths.get(newNamespace.getString("ifile")), newNamespace.getString("language"),
        conf);
  
    System.out.println("Time: " + (System.currentTimeMillis() - time) + "ms");
  }
}
