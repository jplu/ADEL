package fr.eurecom.adel;

import fr.eurecom.adel.cli.NerdCommand;
import fr.eurecom.adel.cli.LinkCommand;
import fr.eurecom.adel.cli.ExtractCommand;
import fr.eurecom.adel.cli.ToolsCommand;
import fr.eurecom.adel.configurations.PipelineConfiguration;
import fr.eurecom.adel.core.Adel;
import fr.eurecom.adel.resources.PipelineResource;
import fr.eurecom.adel.utils.Cors;
import io.dropwizard.Application;
import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * @author Julien Plu
 */
public class App extends Application<PipelineConfiguration> {
  @Override
  public final String getName() {
    return "adel";
  }
  
  @Override
  public final void initialize(final Bootstrap<PipelineConfiguration> bootstrap) {
    super.initialize(bootstrap);
    // add recognize, linking and full commands on cli
    bootstrap.addCommand((Command) new ExtractCommand());
    bootstrap.addCommand((Command) new LinkCommand());
    bootstrap.addCommand((Command) new NerdCommand());
    bootstrap.addCommand((Command) new ToolsCommand());
  }
  
  @Override
  public final void run(final PipelineConfiguration newT, final Environment newEnvironment)
      throws Exception {
    newEnvironment.jersey().register(new PipelineResource());
    Cors.activate(newEnvironment);
  }
  
  public static void main(final String... args) throws Exception {
    new App().run(args);
  }
}
