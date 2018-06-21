package fr.eurecom.adel.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * @author Julien Plu
 */
public final class APICallerFactory {
  private static final APICallerFactory INSTANCE = new APICallerFactory();
  
  private APICallerFactory() {
  }
  
  public static APICallerFactory getInstance() {
    return APICallerFactory.INSTANCE;
  }
  
  public Model adelQuery(final String address, final Map<String, String> params,
                          final String json) {
    final HttpClient httpClient = HttpClientBuilder.create().build();
    final StringBuilder sb = new StringBuilder();
    
    sb.append('?');
    
    for (final Map.Entry<String, String> entry : params.entrySet()) {
      sb.append(entry.getKey());
      sb.append('=');
      sb.append(entry.getValue());
      sb.append('&');
    }
  
    Model apiResult = null;
    
    try {
      final HttpPost post = new HttpPost(address + sb.substring(0, sb.length() - 1));
      final HttpEntity postingString = new StringEntity(json, "UTF-8");

      post.setHeader("Content-type", "application/json;charset=utf-8");
      post.setEntity(postingString);
      
      final HttpResponse response = httpClient.execute(post);
      
      if (response.getEntity().getContent() != null) {
        final String answer = IOUtils.toString(response.getEntity().getContent(),
            Charset.forName("UTF-8"));
        
        if (RdfTools.isValidRdf(answer)) {
          apiResult = ModelFactory.createDefaultModel();
          
          RDFDataMgr.read(apiResult, new ByteArrayInputStream(answer.getBytes(Charset.forName(
              "UTF-8"))), Lang.TTL);
        } else {
          final ObjectMapper mapper = new ObjectMapper();
          
          final Map<String, Object> userData = mapper.readValue(answer, Map.class);
          
          throw new WebApplicationException("Error from " + address + ": " + userData.get(
              "message"), Response.Status.PRECONDITION_FAILED);
        }
      }
    } catch (final IOException ex) {
      throw new WebApplicationException("Issue to read the HTTP connection for " + address, ex,
          Response.Status.PRECONDITION_FAILED);
    } catch (final RiotException ex) {
      throw new WebApplicationException("The RDF got from " + address + " is not valid", ex,
          Response.Status.PRECONDITION_FAILED);
    }
    
    if (apiResult == null) {
      throw new WebApplicationException("Issue to get data with the call: " + address
          + sb.substring(0, sb.length() - 1), Response.Status.PRECONDITION_FAILED);
    }
    
    return apiResult;
  }
}
