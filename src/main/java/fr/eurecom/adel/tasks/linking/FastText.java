package fr.eurecom.adel.tasks.linking;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import fr.eurecom.adel.annotations.Name;
import fr.eurecom.adel.configurations.AdelConfiguration;
import fr.eurecom.adel.datatypes.Entity;

/**
 * @author Julien Plu
 */
@Name(name = "fastText")
public class FastText implements Linking {
  public void link(final List<Entity> newEntities, final AdelConfiguration conf) {
    final ObjectMapper toJson = new ObjectMapper();
    final HttpClient httpClient = HttpClientBuilder.create().build();
  
    newEntities.parallelStream().forEach(mention -> {
      final Map<String, Object> params = new HashMap<>();
      
      params.put("mention", mention.getCleanPhrase());
      params.put("entities", mention.getCandidates().stream().map(entity ->
          entity.getProperties().get(conf.getIndexProperties().get("label")).get(0)).collect(
              Collectors.toList()));
      params.put("text", conf.getText());
      
      try {
        final String json = toJson.writeValueAsString(params);
        final HttpPost post = new HttpPost(conf.getLink().getAddress());
        final HttpEntity postingString = new StringEntity(json, "UTF-8");
  
        post.setHeader("Content-type", "application/json;charset=utf-8");
        post.setEntity(postingString);
        
        final HttpResponse response = httpClient.execute(post);
        
        if (response.getEntity().getContent() != null) {
          final String answer = IOUtils.toString(response.getEntity().getContent(),
              Charset.forName("UTF-8"));
          final ObjectMapper mapper = new ObjectMapper();
          final Map<String, Object> userData = mapper.readValue(answer, Map.class);
          int i = 0;
          System.out.println(answer);
          for (Map.Entry<String, Object> entry : userData.entrySet()) {
          
          }
          
          System.out.println(i);
        }
      } catch (final IOException ex) {
        throw new WebApplicationException("Issue with the fastText API", ex,
            Response.Status.PRECONDITION_FAILED);
      }
    });
  }
}
