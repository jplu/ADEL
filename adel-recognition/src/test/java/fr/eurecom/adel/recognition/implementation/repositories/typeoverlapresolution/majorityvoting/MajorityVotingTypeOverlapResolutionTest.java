package fr.eurecom.adel.recognition.implementation.repositories.typeoverlapresolution.majorityvoting;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.eurecom.adel.recognition.configuration.TypeOverlappingConfig;
import fr.eurecom.adel.commons.datatypes.Document;
import fr.eurecom.adel.commons.datatypes.Entity;
import fr.eurecom.adel.recognition.domain.repositories.TypeOverlapResolutionRepository;
import fr.eurecom.adel.recognition.exceptions.MappingNotExistsException;
import fr.eurecom.adel.recognition.exceptions.TypeNotExistsException;

/**
 * @author Julien Plu on 2018-12-18.
 */
class MajorityVotingTypeOverlapResolutionTest {
  @Test
  final void resolveTypeOverlappingHappyMajority() throws MappingNotExistsException, TypeNotExistsException {
    final TypeOverlappingConfig config = new TypeOverlappingConfig();
    
    config.setTo("CoNLL");
    config.setPriority(Arrays.asList("ann1", "ann2", "ann3"));
    
    final List<Entity> entities = new ArrayList<>();
    final Entity entity = Entity.builder().type("PERSONfromCoNLL--ann1||dbo_PersonfromDBpedia--ann2||dbo_PlacefromDBpedia--ann3").build();
    
    entities.add(entity);
    
    final Document document = Document.builder().entities(entities).build();
    final TypeOverlapResolutionRepository voting = new MajorityVotingTypeOverlapResolution();
    
    voting.resolveTypeOverlapping(config, document.getEntities());
    
    Assertions.assertEquals("PERSON", document.getEntities().get(0).getType(), "Must be equals");
  }
  
  @Test
  final void resolveTypeOverlappingHappyDrawPerson() throws MappingNotExistsException, TypeNotExistsException {
    final TypeOverlappingConfig config = new TypeOverlappingConfig();
    
    config.setTo("CoNLL");
    config.setPriority(Arrays.asList("ann1", "ann3"));
    
    final List<Entity> entities = new ArrayList<>();
    final Entity entity = Entity.builder().type("PERSONfromCoNLL--ann1||dbo_PlacefromDBpedia--ann3").build();
    
    entities.add(entity);
    
    final Document document = Document.builder().entities(entities).build();
    final TypeOverlapResolutionRepository voting = new MajorityVotingTypeOverlapResolution();
    
    voting.resolveTypeOverlapping(config, document.getEntities());
    
    Assertions.assertEquals("PERSON", document.getEntities().get(0).getType(), "Must be equals");
  }
  
  @Test
  final void resolveTypeOverlappingHappyDrawPlace() throws MappingNotExistsException, TypeNotExistsException {
    final TypeOverlappingConfig config = new TypeOverlappingConfig();
    
    config.setTo("CoNLL");
    config.setPriority(Arrays.asList("ann3", "ann1"));
    
    final List<Entity> entities = new ArrayList<>();
    final Entity entity = Entity.builder().type("PERSONfromCoNLL--ann1||dbo_PlacefromDBpedia--ann3").build();
    
    entities.add(entity);
    
    final Document document = Document.builder().entities(entities).build();
    final TypeOverlapResolutionRepository voting = new MajorityVotingTypeOverlapResolution();
    
    voting.resolveTypeOverlapping(config, document.getEntities());
    
    Assertions.assertEquals("LOCATION", document.getEntities().get(0).getType(), "Must be equals");
  }
  
  @Test
  final void resolveTypeOverlappingUnHappyWrongMapping() {
    final TypeOverlappingConfig config = new TypeOverlappingConfig();
    
    config.setTo("CoNLL");
    config.setPriority(Arrays.asList("ann3", "ann1"));
    
    final List<Entity> entities = new ArrayList<>();
    final Entity entity = Entity.builder().type("PERSONfromCNL--ann1||dbo_PlacefromDBpedia--ann3").build();
    
    entities.add(entity);
    
    final Document document = Document.builder().entities(entities).build();
    final TypeOverlapResolutionRepository voting = new MajorityVotingTypeOverlapResolution();
    
    Assertions.assertThrows(MappingNotExistsException.class, () -> voting.resolveTypeOverlapping(config, document.getEntities()));
  }
  
  @Test
  final void resolveTypeOverlappingUnHappyWrongType() {
    final TypeOverlappingConfig config = new TypeOverlappingConfig();
    
    config.setTo("CoNLL");
    config.setPriority(Arrays.asList("ann3", "ann1"));
    
    final List<Entity> entities = new ArrayList<>();
    final Entity entity = Entity.builder().type("PEOfromCoNLL0203--ann1||dbo_PlacefromDBpedia--ann3").build();
    
    entities.add(entity);
    
    final Document document = Document.builder().entities(entities).build();
    final TypeOverlapResolutionRepository voting = new MajorityVotingTypeOverlapResolution();
    
    Assertions.assertThrows(TypeNotExistsException.class, () -> voting.resolveTypeOverlapping(config, document.getEntities()));
  }
}