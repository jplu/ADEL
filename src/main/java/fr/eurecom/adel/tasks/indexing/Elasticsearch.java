package fr.eurecom.adel.tasks.indexing;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;

import fr.eurecom.adel.configurations.AdelConfiguration;
import fr.eurecom.adel.datatypes.Candidate;
import fr.eurecom.adel.datatypes.Entity;
import fr.eurecom.adel.utils.StringUtils;

/**
 * @author Julien Plu
 */
public class Elasticsearch implements Index {
  private final AdelConfiguration adelConf;
  
  public Elasticsearch(final AdelConfiguration newAdelConf) {
    this.adelConf = newAdelConf;
  }
  
  @Override
  public final void searchCandidates(final Entity newEntity) {
    final String scheme = this.adelConf.getIndex().getElasticsearch().getAddress().split(":")[0];
    int port = 80;
    final String hostname = this.adelConf.getIndex().getElasticsearch().getAddress().split(
        ":")[1].split("/")[2];
  
    if (this.adelConf.getIndex().getElasticsearch().getAddress().split(":").length == 3) {
      port = Integer.parseInt(this.adelConf.getIndex().getElasticsearch().getAddress().split(
          ":")[2].split("/")[0]);
    }
  
    String extraAddress = "";
    
    if (this.adelConf.getIndex().getElasticsearch().getAddress().split("/").length >= 4) {
      final int size = this.adelConf.getIndex().getElasticsearch().getAddress().split("/").length;
      
      extraAddress = String.join("/", Arrays.asList(
          this.adelConf.getIndex().getElasticsearch().getAddress().split("/")).subList(3,
          size));
    }
  
    RestClient restClient = null;
    
    try {
      final String query = new String(Files.readAllBytes(Paths.get("queries" +
          FileSystems.getDefault().getSeparator() + "elasticsearch" +
          FileSystems.getDefault().getSeparator() +
            this.adelConf.getIndex().getElasticsearch().getQuery() + ".qry")),
          Charset.forName("UTF-8"));
      
      String phrase = newEntity.getCleanPhrase().replaceAll(":", "\\\\\\\\:").replaceAll(
          "/", "\\\\\\\\/").replaceAll("!", "\\\\\\\\!").replaceAll("\\+",
          "\\\\\\\\+").replaceAll("\\)", "\\\\\\\\)").replaceAll("\\(", "\\\\\\\\(").replaceAll(
              "\"", "\\\\\\\\\"").replaceAll("~", "\\\\\\\\~");
      
      if (!Boolean.valueOf(this.adelConf.getIndex().getElasticsearch().getStrict())) {
        final Collection<String> words = new ArrayList<>();
        final String[] splits = newEntity.getCleanPhrase().replaceAll(" {2,}", " ").split(" ");
  
        for (final String split : splits) {
          if (!split.contains("-") && !split.contains(".") && !split.contains("+")
              && !split.contains(":") && !split.contains("!")) {
            words.add(split + '~');
          } else {
            words.add(split);
          }
        }
        
        phrase = String.join(" ", words).replaceAll(":", "\\\\\\\\:").replaceAll(
            "/", "\\\\\\\\/").replaceAll("!", "\\\\\\\\!").replaceAll("\\+",
            "\\\\\\\\+").replaceAll("\\)", "\\\\\\\\)").replaceAll("\\(", "\\\\\\\\(").replaceAll(
                "\"", "\\\\\\\\\"").replaceAll("~", "\\\\\\\\~");
      }
  
      restClient = RestClient.builder(new HttpHost(hostname, port, scheme))
          .setRequestConfigCallback(requestConfigBuilder ->
              requestConfigBuilder.setConnectTimeout(120000).setSocketTimeout(120000))
          .setMaxRetryTimeoutMillis(120000)
          .build();
      
      final String[] tokens = newEntity.getCleanPhrase().split(" ");
      final StringBuilder filter = new StringBuilder();
      
      for (int i = 0;i < tokens.length;i++) {
        filter.append("\"");
        filter.append(String.join("\",\"", StringUtils.combination(Arrays.asList(tokens), i + 1)));
        filter.append("\",");
      }
      
      try (final NStringEntity header = new NStringEntity(query.replace("%s0", phrase).replace(
          "%s1", filter.substring(0, filter.length() - 1)), ContentType.APPLICATION_JSON)) {
        final Response searchResponse;
        
        if (extraAddress.isEmpty()) {
          searchResponse = restClient.performRequest("GET", '/' + this.adelConf.getIndex().getName()
                  + "/entity/_search", Collections.singletonMap("pretty", "false"), header);
        } else {
          searchResponse = restClient.performRequest("GET", '/' + extraAddress + '/' +
                  this.adelConf.getIndex().getName() + "/entity/_search", Collections.singletonMap(
                      "pretty", "false"), header);
        }
        
        final Configuration conf = Configuration.defaultConfiguration().addOptions(
            Option.SUPPRESS_EXCEPTIONS, Option.AS_PATH_LIST, Option.ALWAYS_RETURN_LIST);
        final Object document = conf.jsonProvider().parse(EntityUtils.toString(
            searchResponse.getEntity()));
        
        //System.out.println(JsonPath.read(document, "$.took").toString());
        //System.out.println(JsonPath.read(document, "$.hits.total").toString());
        final List<String> results = JsonPath.parse(document, conf).read("$.._source");
        final List<Candidate> candidates = new ArrayList<>();
        
        for (final String result : results) {
          final Candidate candidate = new Candidate();
          
          for (final Object key : ((Map)JsonPath.read(document, result)).keySet()) {
            if (((List)JsonPath.read(document, result + ".." + key + ".*")).isEmpty()) {
              candidate.putProperty(key.toString(), JsonPath.read(document, result + ".." + key));
            } else {
              candidate.putProperty(key.toString(), JsonPath.read(document, result + ".." + key +
                  ".*"));
            }
          }
  
          candidate.setFrom(this.adelConf.getIndex().getFrom());
  
          candidates.add(candidate);
        }

        newEntity.setCandidates(candidates);
      }
    } catch (final IOException ex) {
      ex.printStackTrace();
      throw new WebApplicationException("Issue to read the answer from Elasticsearch", ex,
          javax.ws.rs.core.Response.Status.PRECONDITION_FAILED);
    } finally {
      if (restClient != null) {
        try {
          restClient.close();
        } catch (final IOException ex) {
          throw new WebApplicationException("Issue to close the Elasticsearch client", ex,
              javax.ws.rs.core.Response.Status.PRECONDITION_FAILED);
        }
      }
    }
  }
  
  public final Candidate searchLink(final String link) {
    final Candidate candidate = new Candidate();
    final String query = "{\n" +
        "  \"from\": 0,\n" +
        "  \"size\": 1,\n" +
        "  \"_source\":[\"rdfs_label\",\"dbo_wikiPageRedirects\",\"dbo_wikiPageDisambiguates\",\"rdf_type\",\"link\",\"pagerank\"],\n" +
        "  \"query\": {\n" +
        "    \"bool\": {\n" +
        "      \"must\": {\n" +
        "        \"query_string\": {\n" +
        "          \"fields\": [\"link\"],\n" +
        "          \"query\": \"%s0\"\n" +
        "        }\n" +
        "      },\n" +
        "      \"filter\": {\n" +
        "        \"bool\": {\n" +
        "          \"should\": [\n" +
        "            {\n" +
        "              \"terms\": {\n" +
        "                \"link.keyword\":[\"%s1\"]\n" +
        "              }\n" +
        "            }\n" +
        "          ],\n" +
        "          \"minimum_should_match\": 1\n" +
        "        }\n" +
        "      }\n" +
        "    }\n" +
        "  }\n" +
        "}";
    final String scheme = this.adelConf.getIndex().getElasticsearch().getAddress().split(":")[0];
    int port = 80;
    final String hostname = this.adelConf.getIndex().getElasticsearch().getAddress().split(
        ":")[1].split("/")[2];
  
    if (this.adelConf.getIndex().getElasticsearch().getAddress().split(":").length == 3) {
      port = Integer.parseInt(this.adelConf.getIndex().getElasticsearch().getAddress().split(
          ":")[2].split("/")[0]);
    }
  
    String extraAddress = "";
  
    if (this.adelConf.getIndex().getElasticsearch().getAddress().split("/").length >= 4) {
      final int size = this.adelConf.getIndex().getElasticsearch().getAddress().split("/").length;
    
      extraAddress = String.join("/", Arrays.asList(
          this.adelConf.getIndex().getElasticsearch().getAddress().split("/")).subList(3,
          size));
    }
  
    RestClient restClient = null;
    
    try {
      restClient = RestClient.builder(new HttpHost(hostname, port, scheme))
          .setRequestConfigCallback(requestConfigBuilder ->
              requestConfigBuilder.setConnectTimeout(120000).setSocketTimeout(120000))
          .setMaxRetryTimeoutMillis(120000)
          .build();
      
      String newLink = link;
      
      if (org.apache.commons.lang.StringUtils.countMatches(link, "/") > 4) {
        final int ind = link.lastIndexOf("/");
        
        newLink = new StringBuilder(link).replace(ind, ind+1, "\\\\/").toString();
      }
      
      try (final NStringEntity header = new NStringEntity(query.replace("%s0", newLink.replace("!",
          "\\\\!")).replace("%s1", link), ContentType.APPLICATION_JSON)) {
        final Response searchResponse;
        
        if (extraAddress.isEmpty()) {
          searchResponse = restClient.performRequest("GET", '/' + this.adelConf.getIndex().getName()
              + "/entity/_search", Collections.singletonMap("pretty", "false"), header);
        } else {
          searchResponse = restClient.performRequest("GET", '/' + extraAddress + '/' +
              this.adelConf.getIndex().getName() + "/entity/_search", Collections.singletonMap(
              "pretty", "false"), header);
        }
        
        final Configuration conf = Configuration.defaultConfiguration().addOptions(
            Option.SUPPRESS_EXCEPTIONS, Option.AS_PATH_LIST, Option.ALWAYS_RETURN_LIST);
        final Object document = conf.jsonProvider().parse(EntityUtils.toString(
            searchResponse.getEntity()));
        final List<String> results = JsonPath.parse(document, conf).read("$.._source");
        
        for (final String result : results) {
          for (final Object key : ((Map)JsonPath.read(document, result)).keySet()) {
            if (((List)JsonPath.read(document, result + ".." + key + ".*")).isEmpty()) {
              candidate.putProperty(key.toString(), JsonPath.read(document, result + ".." + key));
            } else {
              candidate.putProperty(key.toString(), JsonPath.read(document, result + ".." + key +
                  ".*"));
            }
          }
    
          candidate.setFrom(this.adelConf.getIndex().getFrom());
        }
      }
    } catch (final IOException ex) {
      ex.printStackTrace();
      throw new WebApplicationException("Issue to read the answer from Elasticsearch", ex,
          javax.ws.rs.core.Response.Status.PRECONDITION_FAILED);
    } finally {
      if (restClient != null) {
        try {
          restClient.close();
        } catch (final IOException ex) {
          throw new WebApplicationException("Issue to close the Elasticsearch client", ex,
              javax.ws.rs.core.Response.Status.PRECONDITION_FAILED);
        }
      }
    }
    
    return candidate;
  }
}
