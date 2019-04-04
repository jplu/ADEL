package fr.eurecom.adel.recognition.implementation.repositories.mentionoverlapresolution.merge;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.eurecom.adel.recognition.configuration.AnnotatorConfig;
import fr.eurecom.adel.commons.datatypes.Document;
import fr.eurecom.adel.commons.datatypes.Entity;
import fr.eurecom.adel.recognition.domain.repositories.MentionOverlapResolutionRepository;

/**
 * @author Julien Plu on 2018-12-19.
 */
class MergeMentionOverlapResolutionTest {
  @Test
  final void resolveMentionOverlappingHappy1() {
    final AnnotatorConfig config1 = new AnnotatorConfig();
    
    config1.setName("ann1");
    config1.setFrom("CoNLL");
  
    final AnnotatorConfig config2 = new AnnotatorConfig();
  
    config2.setName("ann2");
    config2.setFrom("CoNLL");
  
    final Entity entity1 = Entity.builder().phrase("United States").cleanPhrase("United States").startOffset(0).endOffset(13).type("LOCATION").build();
    final Entity entity2 = Entity.builder().phrase("States of America").cleanPhrase("States of America").startOffset(7).endOffset(24).type("LOCATION").build();
    final Document doc1 = Document.builder().entities(Collections.singletonList(entity1)).text("United States of America").build();
    final Document doc2 = Document.builder().entities(Collections.singletonList(entity2)).text("United States of America").build();
    final Map<AnnotatorConfig, List<Entity>> documents = new HashMap<>();
    
    documents.put(config1, doc1.getEntities());
    documents.put(config2, doc2.getEntities());
    
    final MentionOverlapResolutionRepository mergeMentionOverlapResolution = new MergeMentionOverlapResolution();
    final List<Entity> newEntities = mergeMentionOverlapResolution.resolveMentionOverlapping(documents);
  
    Assertions.assertEquals(1, newEntities.size(), "Must be equals");
    Assertions.assertEquals(0, newEntities.get(0).getStartOffset().intValue(), "Must be equals");
    Assertions.assertEquals(24, newEntities.get(0).getEndOffset().intValue(), "Must be equals");
    Assertions.assertTrue("LOCATIONfromCoNLL--ann2||LOCATIONfromCoNLL--ann1".equals(newEntities.get(0).getType()) || "LOCATIONfromCoNLL--ann1||LOCATIONfromCoNLL--ann2".equals(newEntities.get(0).getType()), "Must be equals");
    Assertions.assertEquals("United States of America", newEntities.get(0).getPhrase(), "Must be equals");
    Assertions.assertEquals("United States of America", newEntities.get(0).getCleanPhrase(), "Must be equals");
  }
  
  @Test
  final void resolveMentionOverlappingHappy2() {
    final AnnotatorConfig config1 = new AnnotatorConfig();
    
    config1.setName("ann1");
    config1.setFrom("CoNLL");
    
    final AnnotatorConfig config2 = new AnnotatorConfig();
    
    config2.setName("ann2");
    config2.setFrom("CoNLL");
    
    final Entity entity1 = Entity.builder().phrase("United States").cleanPhrase("United States").startOffset(0).endOffset(13).type("LOCATION").build();
    final Entity entity2 = Entity.builder().phrase("United States of America").cleanPhrase("United States of America").startOffset(0).endOffset(24).type("LOCATION").build();
    final Document doc1 = Document.builder().entities(Collections.singletonList(entity1)).text("United States of America").build();
    final Document doc2 = Document.builder().entities(Collections.singletonList(entity2)).text("United States of America").build();
    final Map<AnnotatorConfig, List<Entity>> documents = new HashMap<>();
    
    documents.put(config1, doc1.getEntities());
    documents.put(config2, doc2.getEntities());
    
    final MentionOverlapResolutionRepository mergeMentionOverlapResolution = new MergeMentionOverlapResolution();
    final List<Entity> newEntities = mergeMentionOverlapResolution.resolveMentionOverlapping(documents);
  
    Assertions.assertEquals(1, newEntities.size(), "Must be equals");
    Assertions.assertEquals(0, newEntities.get(0).getStartOffset().intValue(), "Must be equals");
    Assertions.assertEquals(24, newEntities.get(0).getEndOffset().intValue(), "Must be equals");
    Assertions.assertTrue("LOCATIONfromCoNLL--ann2||LOCATIONfromCoNLL--ann1".equals(newEntities.get(0).getType()) || "LOCATIONfromCoNLL--ann1||LOCATIONfromCoNLL--ann2".equals(newEntities.get(0).getType()), "Must be equals");
    Assertions.assertEquals("United States of America", newEntities.get(0).getPhrase(), "Must be equals");
    Assertions.assertEquals("United States of America", newEntities.get(0).getCleanPhrase(), "Must be equals");
  }
  
  @Test
  final void resolveMentionOverlappingHappy3() {
    final AnnotatorConfig config1 = new AnnotatorConfig();
    
    config1.setName("ann1");
    config1.setFrom("CoNLL");
    
    final AnnotatorConfig config2 = new AnnotatorConfig();
    
    config2.setName("ann2");
    config2.setFrom("CoNLL");
    
    final Entity entity1 = Entity.builder().phrase("America").cleanPhrase("America").startOffset(17).endOffset(24).type("LOCATION").build();
    final Entity entity2 = Entity.builder().phrase("United States of America").cleanPhrase("United States of America").startOffset(0).endOffset(24).type("LOCATION").build();
    final Document doc1 = Document.builder().entities(Collections.singletonList(entity1)).text("United States of America").build();
    final Document doc2 = Document.builder().entities(Collections.singletonList(entity2)).text("United States of America").build();
    final Map<AnnotatorConfig, List<Entity>> documents = new HashMap<>();
    
    documents.put(config1, doc1.getEntities());
    documents.put(config2, doc2.getEntities());
    
    final MentionOverlapResolutionRepository mergeMentionOverlapResolution = new MergeMentionOverlapResolution();
    final List<Entity> newEntities = mergeMentionOverlapResolution.resolveMentionOverlapping(documents);
  
    Assertions.assertEquals(1, newEntities.size(), "Must be equals");
    Assertions.assertEquals(0, newEntities.get(0).getStartOffset().intValue(), "Must be equals");
    Assertions.assertEquals(24, newEntities.get(0).getEndOffset().intValue(), "Must be equals");
    Assertions.assertTrue("LOCATIONfromCoNLL--ann2||LOCATIONfromCoNLL--ann1".equals(newEntities.get(0).getType()) || "LOCATIONfromCoNLL--ann1||LOCATIONfromCoNLL--ann2".equals(newEntities.get(0).getType()), "Must be equals");
    Assertions.assertEquals("United States of America", newEntities.get(0).getPhrase(), "Must be equals");
    Assertions.assertEquals("United States of America", newEntities.get(0).getCleanPhrase(), "Must be equals");
  }
  
  @Test
  final void resolveMentionOverlappingHappy4() {
    final AnnotatorConfig config1 = new AnnotatorConfig();
    
    config1.setName("ann1");
    config1.setFrom("CoNLL");
    
    final AnnotatorConfig config2 = new AnnotatorConfig();
    
    config2.setName("ann2");
    config2.setFrom("CoNLL");
    
    final Entity entity1 = Entity.builder().phrase("States").cleanPhrase("States").startOffset(7).endOffset(13).type("LOCATION").build();
    final Entity entity2 = Entity.builder().phrase("United States of America").cleanPhrase("United States of America").startOffset(0).endOffset(24).type("LOCATION").build();
    final Document doc1 = Document.builder().entities(Collections.singletonList(entity1)).text("United States of America").build();
    final Document doc2 = Document.builder().entities(Collections.singletonList(entity2)).text("United States of America").build();
    final Map<AnnotatorConfig, List<Entity>> documents = new HashMap<>();
    
    documents.put(config1, doc1.getEntities());
    documents.put(config2, doc2.getEntities());
    
    final MentionOverlapResolutionRepository mergeMentionOverlapResolution = new MergeMentionOverlapResolution();
    final List<Entity> newEntities = mergeMentionOverlapResolution.resolveMentionOverlapping(documents);
  
    Assertions.assertEquals(1, newEntities.size(), "Must be equals");
    Assertions.assertEquals(0, newEntities.get(0).getStartOffset().intValue(), "Must be equals");
    Assertions.assertEquals(24, newEntities.get(0).getEndOffset().intValue(), "Must be equals");
    Assertions.assertTrue("LOCATIONfromCoNLL--ann2||LOCATIONfromCoNLL--ann1".equals(newEntities.get(0).getType()) || "LOCATIONfromCoNLL--ann1||LOCATIONfromCoNLL--ann2".equals(newEntities.get(0).getType()), "Must be equals");
    Assertions.assertEquals("United States of America", newEntities.get(0).getPhrase(), "Must be equals");
    Assertions.assertEquals("United States of America", newEntities.get(0).getCleanPhrase(), "Must be equals");
  }
  
  @Test
  final void resolveMentionOverlappingHappy5() {
    final AnnotatorConfig config1 = new AnnotatorConfig();
    
    config1.setName("ann1");
    config1.setFrom("CoNLL");
    
    final AnnotatorConfig config2 = new AnnotatorConfig();
    
    config2.setName("ann2");
    config2.setFrom("CoNLL");
    
    final Entity entity2 = Entity.builder().phrase("States").cleanPhrase("States").startOffset(7).endOffset(13).type("LOCATION").build();
    final Entity entity1 = Entity.builder().phrase("United States of America").cleanPhrase("United States of America").startOffset(0).endOffset(24).type("LOCATION").build();
    final Document doc2 = Document.builder().entities(Collections.singletonList(entity1)).text("United States of America").build();
    final Document doc1 = Document.builder().entities(Collections.singletonList(entity2)).text("United States of America").build();
    
    final Map<AnnotatorConfig, List<Entity>> documents = new HashMap<>();
    
    documents.put(config1, doc1.getEntities());
    documents.put(config2, doc2.getEntities());
    
    final MentionOverlapResolutionRepository mergeMentionOverlapResolution = new MergeMentionOverlapResolution();
    final List<Entity> newEntities = mergeMentionOverlapResolution.resolveMentionOverlapping(documents);
  
    Assertions.assertEquals(1, newEntities.size(), "Must be equals");
    Assertions.assertEquals(0, newEntities.get(0).getStartOffset().intValue(), "Must be equals");
    Assertions.assertEquals(24, newEntities.get(0).getEndOffset().intValue(), "Must be equals");
    Assertions.assertTrue("LOCATIONfromCoNLL--ann2||LOCATIONfromCoNLL--ann1".equals(newEntities.get(0).getType()) || "LOCATIONfromCoNLL--ann1||LOCATIONfromCoNLL--ann2".equals(newEntities.get(0).getType()), "Must be equals");
    Assertions.assertEquals("United States of America", newEntities.get(0).getPhrase(), "Must be equals");
    Assertions.assertEquals("United States of America", newEntities.get(0).getCleanPhrase(), "Must be equals");
  }
  
  @Test
  final void resolveMentionOverlappingHappy6() {
    final AnnotatorConfig config1 = new AnnotatorConfig();
    
    config1.setName("ann1");
    config1.setFrom("CoNLL");
    
    final AnnotatorConfig config2 = new AnnotatorConfig();
    
    config2.setName("ann2");
    config2.setFrom("CoNLL");
    
    final Entity entity1 = Entity.builder().phrase("United States of America").cleanPhrase("United States of America").startOffset(0).endOffset(24).type("").build();
    final Entity entity2 = Entity.builder().phrase("States").cleanPhrase("States").startOffset(7).endOffset(13).type("").build();
    final Document doc1 = Document.builder().entities(Collections.singletonList(entity1)).text("United States of America").build();
    final Document doc2 = Document.builder().entities(Collections.singletonList(entity2)).text("United States of America").build();
    final Map<AnnotatorConfig, List<Entity>> documents = new HashMap<>();
    
    documents.put(config1, doc1.getEntities());
    documents.put(config2, doc2.getEntities());
    
    final MentionOverlapResolutionRepository mergeMentionOverlapResolution = new MergeMentionOverlapResolution();
    final List<Entity> newEntities = mergeMentionOverlapResolution.resolveMentionOverlapping(documents);
  
    Assertions.assertEquals(1, newEntities.size(), "Must be equals");
    Assertions.assertEquals(0, newEntities.get(0).getStartOffset().intValue(), "Must be equals");
    Assertions.assertEquals(24, newEntities.get(0).getEndOffset().intValue(), "Must be equals");
    Assertions.assertEquals("", newEntities.get(0).getType(), "Must be equals");
    Assertions.assertEquals("United States of America", newEntities.get(0).getPhrase(), "Must be equals");
    Assertions.assertEquals("United States of America", newEntities.get(0).getCleanPhrase(), "Must be equals");
  }
  
  @Test
  final void resolveMentionOverlappingHappy7() {
    final AnnotatorConfig config1 = new AnnotatorConfig();
    
    config1.setName("ann1");
    config1.setFrom("CoNLL");
    
    final AnnotatorConfig config2 = new AnnotatorConfig();
    
    config2.setName("ann2");
    config2.setFrom("CoNLL");
    
    final Entity entity1 = Entity.builder().phrase("United States of America").cleanPhrase("United States of America").startOffset(0).endOffset(24).type("LOCATION").build();
    final Entity entity2 = Entity.builder().phrase("States").cleanPhrase("States").startOffset(7).endOffset(13).type("").build();
    final Document doc1 = Document.builder().entities(Collections.singletonList(entity1)).text("United States of America").build();
    final Document doc2 = Document.builder().entities(Collections.singletonList(entity2)).text("United States of America").build();
    final Map<AnnotatorConfig, List<Entity>> documents = new HashMap<>();
    
    documents.put(config1, doc1.getEntities());
    documents.put(config2, doc2.getEntities());
    
    final MentionOverlapResolutionRepository mergeMentionOverlapResolution = new MergeMentionOverlapResolution();
    final List<Entity> newEntities = mergeMentionOverlapResolution.resolveMentionOverlapping(documents);
  
    Assertions.assertEquals(1, newEntities.size(), "Must be equals");
    Assertions.assertEquals(0, newEntities.get(0).getStartOffset().intValue(), "Must be equals");
    Assertions.assertEquals(24, newEntities.get(0).getEndOffset().intValue(), "Must be equals");
    Assertions.assertTrue("LOCATIONfromCoNLL--ann2".equals(newEntities.get(0).getType()) || "LOCATIONfromCoNLL--ann1".equals(newEntities.get(0).getType()), "Must be equals");
    Assertions.assertEquals("United States of America", newEntities.get(0).getPhrase(), "Must be equals");
    Assertions.assertEquals("United States of America", newEntities.get(0).getCleanPhrase(), "Must be equals");
  }
  
  @Test
  final void resolveMentionOverlappingHappy8() {
    final AnnotatorConfig config1 = new AnnotatorConfig();
    
    config1.setName("ann1");
    config1.setFrom("CoNLL");
    
    final AnnotatorConfig config2 = new AnnotatorConfig();
    
    config2.setName("ann2");
    config2.setFrom("CoNLL");
    
    final Entity entity1 = Entity.builder().phrase("United States of America").cleanPhrase("United States of America").startOffset(0).endOffset(24).type("").build();
    final Entity entity2 = Entity.builder().phrase("States").cleanPhrase("States").startOffset(7).endOffset(13).type("LOCATION").build();
    final Document doc1 = Document.builder().entities(Collections.singletonList(entity1)).text("United States of America").build();
    final Document doc2 = Document.builder().entities(Collections.singletonList(entity2)).text("United States of America").build();
    final Map<AnnotatorConfig, List<Entity>> documents = new HashMap<>();
    
    documents.put(config1, doc1.getEntities());
    documents.put(config2, doc2.getEntities());
    
    final MentionOverlapResolutionRepository mergeMentionOverlapResolution = new MergeMentionOverlapResolution();
    final List<Entity> newEntities = mergeMentionOverlapResolution.resolveMentionOverlapping(documents);
  
    Assertions.assertEquals(1, newEntities.size(), "Must be equals");
    Assertions.assertEquals(0, newEntities.get(0).getStartOffset().intValue(), "Must be equals");
    Assertions.assertEquals(24, newEntities.get(0).getEndOffset().intValue(), "Must be equals");
    Assertions.assertTrue("LOCATIONfromCoNLL--ann2".equals(newEntities.get(0).getType()) || "LOCATIONfromCoNLL--ann1".equals(newEntities.get(0).getType()), "Must be equals");
    Assertions.assertEquals("United States of America", newEntities.get(0).getPhrase(), "Must be equals");
    Assertions.assertEquals("United States of America", newEntities.get(0).getCleanPhrase(), "Must be equals");
  }
  
  @Test
  final void resolveMentionOverlappingHappy9() {
    final AnnotatorConfig config1 = new AnnotatorConfig();
    
    config1.setName("ann1");
    config1.setFrom("CoNLL");
    
    final AnnotatorConfig config2 = new AnnotatorConfig();
    
    config2.setName("ann2");
    config2.setFrom("CoNLL");
  
    final AnnotatorConfig config3 = new AnnotatorConfig();
  
    config3.setName("ann3");
    config3.setFrom("CoNLL");
    
    final Entity entity1 = Entity.builder().phrase("United States of America").cleanPhrase("United States of America").startOffset(0).endOffset(24).type("LOCATION").build();
    final Entity entity2 = Entity.builder().phrase("States").cleanPhrase("States").startOffset(7).endOffset(13).type("LOCATION").build();
    final Entity entity3 = Entity.builder().phrase("America").cleanPhrase("America").startOffset(17).endOffset(24).type("LOCATION").build();
    final Document doc1 = Document.builder().entities(Collections.singletonList(entity1)).text("United States of America").build();
    final Document doc2 = Document.builder().entities(Collections.singletonList(entity2)).text("United States of America").build();
    final Document doc3 = Document.builder().entities(Collections.singletonList(entity3)).text("United States of America").build();
    final Map<AnnotatorConfig, List<Entity>> documents = new HashMap<>();
    
    documents.put(config1, doc1.getEntities());
    documents.put(config2, doc2.getEntities());
    documents.put(config3, doc3.getEntities());
    
    final MentionOverlapResolutionRepository mergeMentionOverlapResolution = new MergeMentionOverlapResolution();
    final List<Entity> newEntities = mergeMentionOverlapResolution.resolveMentionOverlapping(documents);
    
    Assertions.assertEquals(1, newEntities.size(), "Must be equals");
    Assertions.assertEquals(0, newEntities.get(0).getStartOffset().intValue(), "Must be equals");
    Assertions.assertEquals(24, newEntities.get(0).getEndOffset().intValue(), "Must be equals");
    Assertions.assertTrue("LOCATIONfromCoNLL--ann1||LOCATIONfromCoNLL--ann2||LOCATIONfromCoNLL--ann3".equals(newEntities.get(0).getType()) ||
        "LOCATIONfromCoNLL--ann1||LOCATIONfromCoNLL--ann3||LOCATIONfromCoNLL--ann2".equals(newEntities.get(0).getType()) ||
        "LOCATIONfromCoNLL--ann2||LOCATIONfromCoNLL--ann1||LOCATIONfromCoNLL--ann3".equals(newEntities.get(0).getType()) ||
        "LOCATIONfromCoNLL--ann2||LOCATIONfromCoNLL--ann3||LOCATIONfromCoNLL--ann1".equals(newEntities.get(0).getType()) ||
        "LOCATIONfromCoNLL--ann3||LOCATIONfromCoNLL--ann2||LOCATIONfromCoNLL--ann1".equals(newEntities.get(0).getType()) ||
        "LOCATIONfromCoNLL--ann3||LOCATIONfromCoNLL--ann1||LOCATIONfromCoNLL--ann2".equals(newEntities.get(0).getType()), "Must be equals");
    Assertions.assertEquals("United States of America", newEntities.get(0).getPhrase(), "Must be equals");
    Assertions.assertEquals("United States of America", newEntities.get(0).getCleanPhrase(), "Must be equals");
  }
}