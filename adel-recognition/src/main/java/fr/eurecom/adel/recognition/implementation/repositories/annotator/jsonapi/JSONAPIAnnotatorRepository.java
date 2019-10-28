package fr.eurecom.adel.recognition.implementation.repositories.annotator.jsonapi;

import org.apache.commons.io.IOUtils;
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
  private final String recognizeAddress;
  private final String tokenizeAddress;
  
  public JSONAPIAnnotatorRepository(final String newAddress) {
    this.recognizeAddress = newAddress + "recognize";
    this.tokenizeAddress = newAddress + "tokenize";
  }
  
  @Override
  public final List<Entity> annotate(final AnnotatorConfig config, final String text) {
    final HttpClient httpClient = HttpClientBuilder.create().build();
    final List<Entity> recognizedEntities = new ArrayList<>();
    
    try {
      final HttpPost post = new HttpPost(this.recognizeAddress);
      final String inputJson = "{\"text\": \"" + text + "\"}";
      final HttpEntity requestEntity = new StringEntity(inputJson, ContentType.APPLICATION_JSON);
      
      post.setEntity(requestEntity);
      
      final HttpResponse response = httpClient.execute(post);

      if (null != response.getEntity().getContent()) {
        final String answer = IOUtils.toString(response.getEntity().getContent(),
            StandardCharsets.UTF_8);
        final JSONObject json = new JSONObject(answer);
        final JSONArray entities = json.getJSONArray("entities");
        
  
        for (int i = 0; i < entities.length(); i++) {
          final JSONObject entity = (JSONObject) entities.get(i);
          
          recognizedEntities.add(Entity.builder()
              .phrase(entity.getString("phrase"))
              .cleanPhrase(entity.getString("phrase"))
              .type(entity.getString("type"))
              .startOffset(entity.getInt("startOffset"))
              .endOffset(entity.getInt("endOffset"))
              .build());
        }
      }
    } catch (final IOException ex) {
      JSONAPIAnnotatorRepository.logger.error("Issue to connect to {}", this.recognizeAddress, ex);
    }
    
    return recognizedEntities;
  }
  
  @Override
  public final List<List<Token>> tokenize(final String text) {
    final HttpClient httpClient = HttpClientBuilder.create().build();
    final List<List<Token>> document = new ArrayList<>();
  
    try {
      final HttpPost post = new HttpPost(this.tokenizeAddress);
      final String inputJson = "{\"text\": \"" + text + "\"}";
      final HttpEntity requestEntity = new StringEntity(inputJson, ContentType.APPLICATION_JSON);
    
      post.setEntity(requestEntity);
    
      final HttpResponse response = httpClient.execute(post);
    
      if (null != response.getEntity().getContent()) {
        final String answer = IOUtils.toString(response.getEntity().getContent(),
            StandardCharsets.UTF_8);
        final JSONObject json = new JSONObject(answer);
        final JSONArray sentences = json.getJSONArray("sentences");
      
      
        for (int i = 0; i < sentences.length(); i++) {
          final JSONObject sentence = (JSONObject) sentences.get(i);
          final JSONArray apiTokens = sentence.getJSONArray("tokens");
          final List<Token> tokens = new ArrayList<>();
          
          for (int j = 0; j < apiTokens.length(); j++) {
            final JSONObject token = (JSONObject) apiTokens.get(j);
  
            tokens.add(Token.builder()
                .value(token.getString("value"))
                .begin(token.getInt("begin"))
                .end(token.getInt("end"))
                .build());
          }
          
          document.add(tokens);
        }
      }
    } catch (final IOException ex) {
      JSONAPIAnnotatorRepository.logger.error("Issue to connect to {}", this.tokenizeAddress , ex);
    }
  
    return document;
  }
}
