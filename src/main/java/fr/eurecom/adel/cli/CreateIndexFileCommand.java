package fr.eurecom.adel.cli;

import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import fr.eurecom.adel.tools.CreateIndexFile;
import fr.eurecom.adel.utils.FileUtils;
import io.dropwizard.Configuration;

/**
 * @author Julien Plu
 */
public class CreateIndexFileCommand<T extends Configuration> extends AbstractToolsCommand<T> {
  CreateIndexFileCommand() {
    super("createIndex", "Create an index for ADEL");
  }
  
  @Override
  public final void configure(final Subparser subparser) {
    super.configure(subparser);
    
    subparser.addArgument("-f", "--folder")
        .dest("folder")
        .type(Arguments.fileType().verifyExists().verifyIsDirectory().verifyCanRead())
        .required(true)
        .help("Folder where to find the files to load");
    subparser.addArgument("-p", "--pattern")
        .dest("pattern")
        .type(String.class)
        .required(true)
        .help("Name pattern of the files to load");
    subparser.addArgument("-i", "--index")
        .dest("index")
        .type(String.class)
        .required(false)
        .setDefault("es")
        .choices("lucene", "es")
        .help("Type of index to create");
    subparser.addArgument("-d", "--dataset")
        .dest("dataset")
        .type(String.class)
        .required(true)
        .help("Select the database to index");
    subparser.addArgument("-o", "--output-file")
        .dest("ofile")
        .type(Arguments.fileType().verifyNotExists().verifyCanWriteParent())
        .required(true)
        .help("Output file name which will contain the index");
  }
  
  @Override
  protected final void run(final Namespace newNamespace) throws Exception {
    final List<Path> files = FileUtils.getFilesWithPattern(Paths.get(newNamespace.getString(
        "folder")), newNamespace.getString("pattern"));
    final CreateIndexFile index = new CreateIndexFile();
    
    index.run(files, newNamespace.getString("dataset"), newNamespace.getString("ofile"));
  }
}
