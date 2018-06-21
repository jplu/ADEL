package fr.eurecom.adel.cli;

import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentAction;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.nio.file.Paths;

import fr.eurecom.adel.tools.GenerateDictionary;
import io.dropwizard.Configuration;

/**
 * @author Julien Plu
 */
public class GenerateDictionaryCommand<T extends Configuration> extends AbstractToolsCommand<T> {
  GenerateDictionaryCommand() {
    super("gendic", "Generate a dictionary from a SPARQL endppoint");
  }
  
  @Override
  public final void configure(final Subparser subparser) {
    super.configure(subparser);
  
    final ArgumentAction urlAction = new UrlAction();
  
    subparser.addArgument("-s", "--sparql-endpoint")
        .dest("sendpoint")
        .type(String.class)
        .action(urlAction)
        .help("SPARQL endpoint");
    subparser.addArgument("-o", "--output-file")
        .dest("ofile")
        .type(Arguments.fileType().verifyNotExists().verifyCanWriteParent())
        .help("Output file name which will contain the dictionary");
    subparser.addArgument("-f", "--filter")
        .dest("filter")
        .type(String.class)
        .required(false)
        .help("types filter");
  }
  
  @Override
  protected void run(final Namespace newNamespace) throws Exception {
    final long time = System.currentTimeMillis();
    final GenerateDictionary dictionary = new GenerateDictionary();
    
    dictionary.run(Paths.get(newNamespace.getString("ofile")), newNamespace.getString("sendpoint"),
        newNamespace.getString("filter"));
  
    System.out.println("Time: " + (System.currentTimeMillis() - time) + "ms");
  }
}
