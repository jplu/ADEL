package fr.eurecom.adel.recognition.implementation.repositories.annotator.jsonapi;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import fr.eurecom.adel.commons.datatypes.Entity;
import fr.eurecom.adel.commons.datatypes.Token;
import fr.eurecom.adel.commons.validators.Name;
import fr.eurecom.adel.recognition.configuration.AnnotatorConfig;
import fr.eurecom.adel.recognition.domain.repositories.AnnotatorRepository;

/**
 * @author Julien Plu on 05/06/19.
 */
@Name(name = "JSONAPI")
public class JSONAPIAnnotatorRepository implements AnnotatorRepository {
  private static final Logger logger = LoggerFactory.getLogger(JSONAPIAnnotatorRepository.class);
  private final HttpURLConnection con;
  
  public JSONAPIAnnotatorRepository(final String address) throws IOException {
    final URL url = new URL(address);
    this.con = (HttpURLConnection) url.openConnection();
    this.con.setRequestMethod("POST");
    this.con.setRequestProperty("Content-Type", "application/json; utf-8");
    this.con.setRequestProperty("Accept", "application/json");
    this.con.setDoOutput(true);
  }
  
  @Override
  public final List<Entity> annotate(final AnnotatorConfig config, final String text) {
    try(final OutputStream os = this.con.getOutputStream()) {
      final String jsonInputString = "{\"name\": " + text + "\"}";
      final byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
      
      os.write(input, 0, input.length);
    } catch (final IOException ex) {
      JSONAPIAnnotatorRepository.logger.error("", ex);
    }
  
    try(final BufferedReader br = new BufferedReader(
        new InputStreamReader(this.con.getInputStream(), StandardCharsets.UTF_8))) {
      final StringBuilder response = new StringBuilder();
      String responseLine;
      
      while ((responseLine = br.readLine()) != null) {
        response.append(responseLine.trim());
      }
  
      JSONAPIAnnotatorRepository.logger.info("{}", response);
    } catch (final IOException ex) {
      JSONAPIAnnotatorRepository.logger.error("", ex);
    }
  
    return new ArrayList<>();
  }
  
  @Override
  public final List<List<Token>> tokenize(final String text) {
    throw new NotImplementedException("Not implemented for the JSON APIs.");
  }
}
