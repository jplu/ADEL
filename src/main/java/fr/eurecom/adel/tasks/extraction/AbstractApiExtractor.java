package fr.eurecom.adel.tasks.extraction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.jena.rdf.model.Model;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import fr.eurecom.adel.configurations.ExtractorConfiguration;
import fr.eurecom.adel.datatypes.Entity;
import fr.eurecom.adel.datatypes.Query;
import fr.eurecom.adel.utils.APICallerFactory;
import fr.eurecom.adel.utils.StringUtils;

/**
 * @author Julien Plu
 */
public abstract class AbstractApiExtractor implements Extractor {
  private List<Entity> newEntities;
  private ExtractorConfiguration conf;
  private Map<String, String> parameters;
  private String jsonQuery;
  
  AbstractApiExtractor(final ExtractorConfiguration newConf) {
    this.newEntities = new ArrayList<>();
    this.conf = newConf;
    this.parameters = new HashMap<>();
    this.jsonQuery = "";
  }
  
  @Override
  public final  List<Entity> getEntities() {
    return Collections.unmodifiableList(this.newEntities);
  }
  
  @Override
  public final void setEntities(final List<Entity> newEntities) {
    this.newEntities = Collections.unmodifiableList(newEntities);
  }
  
  final void addEntity(final Entity newEntity) {
    if (!this.newEntities.contains(newEntity)) {
      this.newEntities.add(newEntity);
    }
  }
  
  @Override
  public final ExtractorConfiguration getConf() {
    return this.conf;
  }
  
  @Override
  public final void setConf(final ExtractorConfiguration newConf) {
    this.conf = newConf;
  }
  
  @Override
  public final void extract() {
    final APICallerFactory apiCall = APICallerFactory.getInstance();
    final Model model = (Model) apiCall.adelQuery(this.conf.getAddress(), this.parameters,
        this.jsonQuery);
    
    this.fillMentions(model);
  }
  
  final void fillParameters(final Query query, final String lang) {
    final Map<String, String> tmpJsonQuery = new HashMap<>();
    
    if (query.getUrl() != null && !query.getUrl().isEmpty()) {
      tmpJsonQuery.put("url", query.getUrl());
    } else if ("html".equals(query.getInput())) {
      tmpJsonQuery.put("content", Jsoup.parse(query.getContent()).text());
    } else {
      tmpJsonQuery.put("content", StringUtils.normalizeString(query.getContent()));
    }
    
    try {
      this.jsonQuery = new ObjectMapper().writeValueAsString(tmpJsonQuery);
    } catch (final JsonProcessingException ex) {
      throw new WebApplicationException("Issue to create the extraction adelQuery", ex,
          Response.Status.PRECONDITION_FAILED);
    }
    
    this.parameters.put("setting", this.conf.getProfile());
    this.parameters.put("lang", lang);
  }
  
  abstract void fillMentions(final Model model);
}
