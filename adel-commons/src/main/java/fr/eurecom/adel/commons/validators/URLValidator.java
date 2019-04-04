package fr.eurecom.adel.commons.validators;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Julien Plu on 2019-02-09.
 */
public class URLValidator implements ConstraintValidator<URL, String> {
  @Override
  public boolean isValid(final String t, final ConstraintValidatorContext constraintValidatorContext) {
    if ( t == null || t.isEmpty()) {
      return true;
    }
    
    if (t.startsWith("classpath")) {
      return this.loadProperties(t.replaceAll("classpath:", ""), Thread.currentThread().getContextClassLoader());
    }
    
    try {
      final java.net.URL url = new java.net.URL(t);
      final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
  
      connection.setRequestMethod("HEAD");
      
      final int responseCode = connection.getResponseCode();
      
      return (responseCode >= 200 && responseCode <= 399);
    } catch (final IOException ex) {
      return false;
    }
  }
  
  private boolean loadProperties(final String file, final ClassLoader loader) {
    String name = file;
    
    if (name.endsWith(".properties")) {
      name = name.substring(0, name.length() - ".properties".length());
    }
    
    name = name.replace('.', '/');
    name += ".properties";
    
    try (final InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(loader.getResourceAsStream(name)), StandardCharsets.UTF_8)) {
      return true;
    } catch (final Exception var8) {
      return false;
    }
  }
}
