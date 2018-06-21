package fr.eurecom.adel.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.submerge.api.parser.SRTParser;
import com.submerge.api.parser.exception.InvalidFileException;
import com.submerge.api.parser.exception.InvalidSubException;
import com.submerge.api.subtitle.srt.SRTSub;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import fr.eurecom.adel.configurations.AdelConfiguration;
import fr.eurecom.adel.configurations.PipelineConfiguration;
import fr.eurecom.adel.core.Adel;
import fr.eurecom.adel.datatypes.ExtractQuery;
import fr.eurecom.adel.datatypes.LinkQuery;
import fr.eurecom.adel.datatypes.NerdQuery;
import fr.eurecom.adel.datatypes.Query;
import fr.eurecom.adel.utils.HashtagSegmentation;
import fr.eurecom.adel.utils.RdfTools;
import fr.eurecom.adel.utils.StringUtils;
import io.dropwizard.validation.OneOf;
import subtitleFile.FatalParsingException;
import subtitleFile.FormatTTML;
import subtitleFile.TimedTextFileFormat;
import subtitleFile.TimedTextObject;

/**
 * @author Julien Plu
 */
@Path("/v1")
public class PipelineResource {
  private final Map<String, AdelConfiguration> profiles = new HashMap<>();
  
  public PipelineResource() {
    this.loadProfiles();
    HashtagSegmentation.loadAllDictionaries();
  }
  
  private void loadProfiles() {
    try {
      Files.list(Paths.get("profiles")).forEach(file -> {
        try {
          System.out.println("load " + file);
          final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
          final AdelConfiguration conf = mapper.readValue(file.toFile(), AdelConfiguration.class);
          final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
          final Validator validator = factory.getValidator();
          final Set<ConstraintViolation<AdelConfiguration>> violations = validator.validate(conf);
  
          if (!violations.isEmpty()) {
            final StringBuilder sb = new StringBuilder();
    
            violations.forEach(error -> sb.append(error.getPropertyPath() + " " +
                error.getMessage()).append("\n"));
    
            throw new WebApplicationException(sb.toString(), Response.Status.PRECONDITION_FAILED);
          }
  
          conf.prepareTypesMapping();
          conf.prepareIndexProperties();
          
          this.profiles.put(file.getFileName().toString().split("\\.")[0], conf);
        } catch (final IOException ex) {
          throw new WebApplicationException("Failed to read the profile " + file, ex,
              Response.Status.PRECONDITION_FAILED);
        }
      });
    } catch (final IOException ex) {
      throw new WebApplicationException("Failed to read the directory profiles", ex,
          Response.Status.PRECONDITION_FAILED);
    }
  }
  
  @POST
  @Timed
  @Produces({"application/json;charset=utf-8", "application/x-turtle;charset=utf-8",
      "text/plain;charset=utf-8"})
  @Consumes({"application/json;charset=utf-8", "application/x-turtle;charset=utf-8"})
  @Path("/extract/")
  public final Response extract(@Context final HttpServletRequest request,
                                @QueryParam("setting") @DefaultValue("default")
                                final String setting,
                                @QueryParam("lang") @DefaultValue("en") final String lang) {
    final Response result;
    final StringWriter writer = new StringWriter();
    final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    final Validator validator = factory.getValidator();
    
    try {
      IOUtils.copy(request.getInputStream(), writer, Charset.forName("UTF-8"));
      
      final ExtractQuery query;
      
      if (RdfTools.isValidRdf(writer.toString())) {
        query = new ExtractQuery();
        
        query.setContent(writer.toString());
        query.setInput("nif");
      } else {
        final ObjectMapper mapper = new ObjectMapper();
        
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        
        query = mapper.readValue(writer.toString(), ExtractQuery.class);
        this.checkSubtitles(query);
      }
  
      final Set<ConstraintViolation<ExtractQuery>> violations = validator.validate(query);
  
      if (!violations.isEmpty()) {
        final StringBuilder sb = new StringBuilder();
        
        violations.forEach(error -> sb.append(error.getPropertyPath() + " " +
            error.getMessage()).append("\n"));
  
        throw new WebApplicationException(sb.toString(), Response.Status.PRECONDITION_FAILED);
      }
      
      if (!this.profiles.containsKey(lang + '_' + setting)) {
        throw new WebApplicationException("The profile " + lang + '_' + setting + " does not "
            + "exists", Response.Status.PRECONDITION_FAILED);
      }
  
      result = Response.ok(new Adel(this.profiles.get(lang + '_' + setting)).extract(query, lang,
          this.getHost(request))).build();
    } catch (final JsonParseException ex) {
      throw new WebApplicationException("Invalid JSON " + writer, ex,
          Response.Status.PRECONDITION_FAILED);
    } catch (final IOException ex) {
      throw new WebApplicationException("Failed to read the HTTP request " + writer, ex,
          Response.Status.PRECONDITION_FAILED);
    } catch (final InvalidSubException | InvalidFileException | FatalParsingException ex) {
      throw new WebApplicationException("Issue with the subtitles", ex,
          Response.Status.PRECONDITION_FAILED);
    }
    
    return result;
  }
  
  @POST
  @Timed
  @Produces({"application/json;charset=utf-8", "application/x-turtle;charset=utf-8",
      "text/plain;charset=utf-8"})
  @Consumes({"application/json;charset=utf-8", "application/x-turtle;charset=utf-8"})
  @Path("/link/")
  public final Response link(@Context final HttpServletRequest request,
                             @QueryParam("setting") @DefaultValue("default")
                             final String setting,
                             @QueryParam("lang") @DefaultValue("en") final String lang) {
    final Response result;
    final StringWriter writer = new StringWriter();
    final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    final Validator validator = factory.getValidator();
    
    try {
      IOUtils.copy(request.getInputStream(), writer, Charset.forName("UTF-8"));
      
      final LinkQuery query;
      
      if (RdfTools.isValidRdf(writer.toString())) {
        query = new LinkQuery();
        
        query.setContent(writer.toString());

        query.setInput("nif");
      } else {
        final ObjectMapper mapper = new ObjectMapper();
        
        query = mapper.readValue(writer.toString(), LinkQuery.class);
      }
  
      final Set<ConstraintViolation<LinkQuery>> violations = validator.validate(query);
  
      if (!violations.isEmpty()) {
        final StringBuilder sb = new StringBuilder();
    
        violations.forEach(error -> sb.append(error.getPropertyPath() + " " +
            error.getMessage()).append("\n"));
    
        throw new WebApplicationException(sb.toString(), Response.Status.PRECONDITION_FAILED);
      }
      
      if (query.getUrl() != null && !query.getUrl().isEmpty()) {
        throw new WebApplicationException("The link method cannot handle URLs",
            Response.Status.PRECONDITION_FAILED);
      }
  
      if (!this.profiles.containsKey(lang + '_' + setting)) {
        throw new WebApplicationException("The profile " + lang + '_' + setting + " does not "
            + "exists", Response.Status.PRECONDITION_FAILED);
      }
      
      result = Response.ok(new Adel(this.profiles.get(lang + '_' + setting)).link(query, lang,
          this.getHost(request))).build();
    } catch (final JsonParseException ex) {
      throw new WebApplicationException("Invalid JSON " + writer, ex,
          Response.Status.PRECONDITION_FAILED);
    } catch (final IOException ex) {
      throw new WebApplicationException("Failed to read the HTTP request " + writer, ex,
          Response.Status.PRECONDITION_FAILED);
    }
    
    return result;
  }
  
  @POST
  @Timed
  @Produces({"application/json;charset=utf-8", "application/x-turtle;charset=utf-8",
      "text/plain;charset=utf-8"})
  @Consumes({"application/json;charset=utf-8", "application/x-turtle;charset=utf-8"})
  @Path("/nerd/")
  public final Response nerd(@Context final HttpServletRequest request,
                             @QueryParam("setting") @DefaultValue("default")
                             final String setting,
                             @QueryParam("lang") @DefaultValue("en") final String lang) {
    final Response result;
    final StringWriter writer = new StringWriter();
    final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    final Validator validator = factory.getValidator();
    
    try {
      IOUtils.copy(request.getInputStream(), writer, Charset.forName("UTF-8"));
      
      final NerdQuery query;
  
      if (RdfTools.isValidRdf(writer.toString())) {
        query = new NerdQuery();
        
        query.setContent(writer.toString());
        
        query.setInput("nif");
      } else {
        final ObjectMapper mapper = new ObjectMapper();
  
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        
        query = mapper.readValue(writer.toString(), NerdQuery.class);
        
        this.checkSubtitles(query);
      }
      
      final Set<ConstraintViolation<NerdQuery>> violationsQuery = validator.validate(query);
  
      if (!violationsQuery.isEmpty()) {
        final StringBuilder sb = new StringBuilder();
  
        violationsQuery.forEach(error -> sb.append(error.getPropertyPath() + " " +
            error.getMessage()).append("\n"));
    
        throw new WebApplicationException(sb.toString(), Response.Status.PRECONDITION_FAILED);
      }
  
      if (!this.profiles.containsKey(lang + '_' + setting)) {
        throw new WebApplicationException("The profile " + lang + '_' + setting + " does not "
            + "exists", Response.Status.PRECONDITION_FAILED);
      }
  
      result = Response.ok(new Adel(this.profiles.get(lang + '_' + setting)).nerd(query,
          lang, this.getHost(request))).build();
    } catch (final JsonParseException ex) {
      throw new WebApplicationException("Invalid JSON " + writer, ex,
          Response.Status.PRECONDITION_FAILED);
    } catch (final IOException ex) {
      throw new WebApplicationException("Failed to read the HTTP request " + writer, ex,
          Response.Status.PRECONDITION_FAILED);
    } catch (final InvalidSubException | InvalidFileException | FatalParsingException ex) {
      throw new WebApplicationException("Issue with the subtitles", ex,
          Response.Status.PRECONDITION_FAILED);
    }
    
    return result;
  }
  
  private void checkSubtitles(final Query query) throws InvalidSubException, InvalidFileException,
   FatalParsingException, IOException {
    if ("srt".equals(query.getInput())) {
      final SRTParser parser = new SRTParser();
      final SRTSub subtitle;
      
      if (query.getUrl() != null && !query.getUrl().isEmpty()) {
        subtitle = parser.parse(IOUtils.toInputStream(IOUtils.toString(new URL(query.getUrl()),
            Charset.forName("UTF-8")), Charset.forName("UTF-8")), "");
      } else {
        subtitle = parser.parse(IOUtils.toInputStream(query.getContent(),
            Charset.forName("UTF-8")), "");
      }
  
      query.setContent(StringUtils.getTextFromSrt(subtitle.getLines()));
      
      query.setUrl(null);
    } else if ("ttml".equals(query.getInput())) {
      final TimedTextFileFormat ttff = new FormatTTML();
      final TimedTextObject tto;
  
      if (query.getUrl() != null && !query.getUrl().isEmpty()) {
        tto = ttff.parseFile("", IOUtils.toInputStream(IOUtils.toString(new URL(query.getUrl()),
            Charset.forName("UTF-8")), Charset.forName("UTF-8")));
      } else {
        tto = ttff.parseFile("", IOUtils.toInputStream(query.getContent(),
            Charset.forName("UTF-8")));
      }
      
      final SRTParser parser = new SRTParser();
      final SRTSub subtitle = parser.parse(IOUtils.toInputStream(String.join("\n",
          Arrays.asList(tto.toSRT())), Charset.forName("UTF-8")), "");
    
      query.setContent(StringUtils.getTextFromSrt(subtitle.getLines()));
      query.setUrl(null);
    }
  }
  
  private String getHost(final HttpServletRequest request) {
    final StringBuffer url = request.getRequestURL();
    final String uri = request.getRequestURI();
    
    return url.substring(0, url.indexOf(uri));
  }
}
