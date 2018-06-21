package fr.eurecom.adel.cli;

import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.nio.file.Paths;

import fr.eurecom.adel.tools.PageRank;
import io.dropwizard.Configuration;

/**
 * @author Julien Plu
 */
public class PageRankCommand<T extends Configuration> extends AbstractToolsCommand<T> {
  PageRankCommand() {
    super("pagerank", "Compute the PageRank of a RDF graph");
  }
  
  @Override
  public final void configure(final Subparser subparser) {
    super.configure(subparser);
  
    subparser.addArgument("-o", "--output-file")
        .dest("ofile")
        .type(Arguments.fileType().verifyNotExists().verifyCanWriteParent())
        .help("Output file name which will contain the PageRank result");
    subparser.addArgument("-i", "--input-file")
        .dest("ifile")
        .type(Arguments.fileType().verifyExists().verifyIsFile().verifyCanRead())
        .help("Input file name which contain the RDF links in NT format");
    
  }
  
  @Override
  protected void run(final Namespace newNamespace) throws Exception {
    final long time = System.currentTimeMillis();
    final PageRank pageRank = new PageRank();
    
    pageRank.run(Paths.get(newNamespace.getString("ofile")), Paths.get(newNamespace.getString(
        "ifile")));
  
    System.out.println("Time: " + (System.currentTimeMillis() - time) + "ms");
  }
}
