package fr.eurecom.adel.api.controllers;

import fr.eurecom.adel.api.formatter.DocumentFormatter;
import fr.eurecom.adel.api.formatter.ProfileFormatter;
import fr.eurecom.adel.commons.datatypes.Document;
import fr.eurecom.adel.api.converters.DocumentConverter;
import fr.eurecom.adel.commons.exceptions.NIFMalformedException;
import fr.eurecom.adel.commons.formats.NIF;
import fr.eurecom.adel.recognition.exceptions.MappingNotExistsException;
import fr.eurecom.adel.recognition.exceptions.TypeNotExistsException;
import fr.eurecom.adel.recognition.usecases.RecognitionPipeline;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * @author Julien Plu on 17/11/2018.
 */

@RestController
@RequestMapping("/api/v2/recognize")
@Api(description = "Set of endpoints for entity recognition.")
public class EntityRecognition {
  private RecognitionPipeline pipeline;

  @Autowired
  public final void setPipeline(final RecognitionPipeline newPipeline) {
    this.pipeline = newPipeline;
  }
  
  @ApiOperation(value = "Get current entity recognition profile", notes = "Get current entity recognition profile", response = ProfileFormatter.class, tags = "entity-recognition")
  @ApiResponses(@ApiResponse(code = 200, message = "The process went well", response = ProfileFormatter.class))
  @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public final ProfileFormatter profile() {
    return new ProfileFormatter(this.pipeline.getConfig());
  }
  
  @ApiOperation(value = "Entity recognition over a text", notes = "Entity recognition over a text", response = DocumentFormatter.class, tags = "entity-recognition")
  @ApiResponses(@ApiResponse(code = 200, message = "The process went well", response = DocumentFormatter.class))
  @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public final DocumentFormatter recognize(@ApiParam(value = "Input of the recognize endpoint" ,required=true ) @RequestBody final DocumentConverter documentConverter) {
    if (documentConverter.getText() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The text property is missing");
    }
  
    final Document document;
    
    try {
      document = this.pipeline.run(documentConverter.toDocument().getText()).get("adel");
    } catch (final MappingNotExistsException | TypeNotExistsException ex) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
    }
  
    return new DocumentFormatter(document);
  }
  
  @ApiOperation(value = "Entity recognition over a NIF content", nickname = "recognizeNIF", notes = "Entity recognition over a NIF content")
  @ApiResponses(@ApiResponse(code = 200, message = "The process went well"))
  @PostMapping(value = "/nif", consumes = "application/x-turtle;charset=utf-8", produces = "application/x-turtle;charset=utf-8")
  public String recognizeNIF(@ApiParam(required=true) @RequestBody final String request) {
    final NIF nif = new NIF();
    
    try {
      nif.setNIF(request);
      
      final List<String> documents = nif.documents();
      
      for (final String doc : documents) {
        nif.addDocument(this.pipeline.run(doc).get("adel"));
      }
    } catch (final NIFMalformedException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
    } catch (final MappingNotExistsException | TypeNotExistsException ex) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
    }
    
    return nif.stringOutput();
  }
}
