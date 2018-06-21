package fr.eurecom.adel.cli;

import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.util.SortedMap;
import java.util.TreeMap;

import io.dropwizard.Configuration;

/**
 * @author Julien Plu
 */
public class ToolsCommand<T extends Configuration> extends AbstractToolsCommand<T> {
  private static final String COMMAND_NAME_ATTR = "subCommand";
  private final SortedMap<String, AbstractToolsCommand<T>> subCommands = new TreeMap<>();
  
  public ToolsCommand() {
    super("tools", "Run ADEL tools");
    
    this.addSubCommand(new Nif2TacCommand<>());
    this.addSubCommand(new Nif2ConllCommand<>());
    this.addSubCommand(new NifStatsCommand<>());
    this.addSubCommand(new TacStatsCommand<>());
    this.addSubCommand(new GenerateDictionaryCommand<>());
    this.addSubCommand(new PageRankCommand<>());
    this.addSubCommand(new Tac2ConllCommand<>());
    this.addSubCommand(new Neel20142TacCommand<>());
    this.addSubCommand(new CreateIndexFileCommand<>());
    this.addSubCommand(new Tac2NifCommand<>());
  }
  
  private void addSubCommand(final AbstractToolsCommand<T> subCommand) {
    this.subCommands.put(subCommand.getName(), subCommand);
  }
  
  @Override
  public void configure(final Subparser subparser) {
    for (final AbstractToolsCommand<T> subCommand : this.subCommands.values()) {
      final Subparser cmdParser = subparser.addSubparsers()
          .addParser(subCommand.getName())
          .setDefault(ToolsCommand.COMMAND_NAME_ATTR, subCommand.getName())
          .description(subCommand.getDescription());
      subCommand.configure(cmdParser);
    }
  }
  
  @Override
  public void run(final Namespace newNamespace) throws Exception {
    this.subCommands.get(newNamespace.getString(ToolsCommand.COMMAND_NAME_ATTR)).run(newNamespace);
  }
}
