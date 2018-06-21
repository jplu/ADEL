package fr.eurecom.adel.cli;

import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import io.dropwizard.Configuration;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;

/**
 * @author Julien Plu
 */
abstract class AbstractToolsCommand<T extends Configuration> extends ConfiguredCommand<T> {
  AbstractToolsCommand(final String name, final String description) {
    super(name, description);
  }
  
  @Override
  protected void run(final Bootstrap<T> newBootstrap, final Namespace newNamespace, final T newT)
      throws Exception {
    this.run(newNamespace);
  }
  
  protected abstract void run(final Namespace newNamespace) throws Exception;
}
