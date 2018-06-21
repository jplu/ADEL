package fr.eurecom.adel.utils;

import org.eclipse.jetty.servlets.CrossOriginFilter;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;

import io.dropwizard.setup.Environment;

/**
 * @author Julien Plu
 */
public final class Cors {
  private Cors() {}
  
  public static void activate(final Environment env) {
    final FilterRegistration.Dynamic corsFilter = env.servlets().addFilter("CORS",
        CrossOriginFilter.class);
    corsFilter.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM,
        "GET,PUT,POST,DELETE,OPTIONS");
    corsFilter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
    corsFilter.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM,
        "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
    corsFilter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
  }
}
