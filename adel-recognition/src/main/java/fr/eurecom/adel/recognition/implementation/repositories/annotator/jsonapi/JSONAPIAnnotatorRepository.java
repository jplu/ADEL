package fr.eurecom.adel.recognition.implementation.repositories.annotator.jsonapi;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
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
  private final String address;
  
  public JSONAPIAnnotatorRepository(final String newAddress) {
    this.address = newAddress;
  }
  
  @Override
  public final List<Entity> annotate(final AnnotatorConfig config, final String text) {
    final HttpClient httpClient = HttpClientBuilder.create().build();
    final List<Entity> recognizedEntities = new ArrayList<>();
    
    try {
      final HttpPost post = new HttpPost(this.address);
      final String inputJson = "{\"text\": \"" + text + "\"}";
      final HttpEntity requestEntity = new StringEntity(inputJson, ContentType.APPLICATION_JSON);
      
      post.setEntity(requestEntity);
      
      final HttpResponse response = httpClient.execute(post);

      if (response.getEntity().getContent() != null) {
        final String answer = IOUtils.toString(response.getEntity().getContent(),
            Charset.forName("UTF-8"));
        final JSONObject json = new JSONObject(answer);
        final JSONArray entities = json.getJSONArray("entities");
        
  
        for (int i = 0; i < entities.length(); i++) {
          final JSONObject entity = (JSONObject) entities.get(i);
          
          recognizedEntities.add(Entity.builder()
              .phrase((String) entity.get("phrase"))
              .cleanPhrase((String) entity.get("phrase"))
              .type((String) entity.get("type"))
              .startOffset((Integer) entity.get("startOffset"))
              .endOffset((Integer) entity.get("endOffset"))
              .build());
        }
        
      }
    } catch (final IOException ex) {
      JSONAPIAnnotatorRepository.logger.error("Issue to connect to {}", this.address , ex);
    }
    
    return recognizedEntities;
  }
  
  @Override
  public final List<List<Token>> tokenize(final String text) {
    throw new NotImplementedException("Not implemented for the JSON APIs.");
  }
}
