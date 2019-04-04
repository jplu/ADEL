package fr.eurecom.adel.recognition.usecases;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.eurecom.adel.recognition.configuration.RecognitionConfig;
import fr.eurecom.adel.commons.datatypes.Document;
import fr.eurecom.adel.commons.datatypes.Entity;
import fr.eurecom.adel.commons.datatypes.Token;
import fr.eurecom.adel.commons.datatypes.TweetEntity;
import fr.eurecom.adel.recognition.configuration.AnnotatorConfig;
import fr.eurecom.adel.recognition.exceptions.MappingNotExistsException;
import fr.eurecom.adel.recognition.exceptions.TypeNotExistsException;

/**
 * @author Julien Plu on 2018-12-09.
 */
public class RecognitionPipeline {
  private final List<Annotator> annotators;
  private final OverlapResolution overlapResolution;
  private final TweetNormalization tweetNormalization;
  private final int indexAnnotatorAsTokenizer;
  private final RecognitionConfig config;
  
  public RecognitionPipeline(final List<Annotator> newAnnotators, final OverlapResolution newOverlapResolution, final TweetNormalization newTweetNormalization, final int newIndexAnnotatorAsTokenizer, final RecognitionConfig newConfig) {
    this.annotators = new ArrayList<>(newAnnotators);
    this.overlapResolution = newOverlapResolution;
    this.tweetNormalization = newTweetNormalization;
    this.indexAnnotatorAsTokenizer = newIndexAnnotatorAsTokenizer;
    this.config = newConfig;
  }
  
  public final RecognitionConfig getConfig() {
    return this.config;
  }
  
  public final Map<String, Document> run(final String text) throws MappingNotExistsException, TypeNotExistsException {
    Pair<String, List<TweetEntity>> normalizeTweet = Pair.of(text, new ArrayList<>());
    
    if (this.tweetNormalization != null) {
      normalizeTweet = this.tweetNormalization.normalize(text);
    }
    
    final Map<AnnotatorConfig, List<Entity>> documents = new HashMap<>();
    
    for (final Annotator annotator : this.annotators) {
      final List<Entity> tmpEntities = annotator.annotate(normalizeTweet.getLeft());
      
      documents.put(annotator.getConfig(), tmpEntities);
    }
    
    final List<Entity> noOverlapEntities = this.overlapResolution.resolveOverlap(documents);
    final List<List<Token>> tokens = this.annotators.get(this.indexAnnotatorAsTokenizer).tokenize(text);
    Document adelDocument = Document.builder().text(text).entities(noOverlapEntities).tokens(tokens).build();
    
    if (this.config.getTweetnormalization().getActivate()) {
      adelDocument = Document.builder().text(text).entities(this.alignment(noOverlapEntities, normalizeTweet.getRight())).tokens(tokens).build();
    }
    
    final Map<String, Document> allDocuments = new HashMap<>(Map.of("adel", adelDocument));
    
    if (this.annotators.size() == 1) {
      return allDocuments;
    }
    
    for (final Map.Entry<AnnotatorConfig, List<Entity>> document : documents.entrySet()) {
      allDocuments.put(document.getKey().getName(), Document.builder().text(text).entities(document.getValue()).tokens(tokens).build());
    }
    
    return allDocuments;
  }
  
  private List<Entity> alignment(final List<Entity> entities, final List<TweetEntity> tweetEntities) {
    if (tweetEntities.isEmpty()) {
      return entities;
    }
    
    entities.sort(Comparator.comparing(Entity::getStartOffset));
    tweetEntities.sort(Comparator.comparing(TweetEntity::getStartOffset));
    int offset = 0;
  
    for (final TweetEntity tweetEntity : tweetEntities) {
      offset += StringUtils.countMatches(tweetEntity.getCleanPhrase(), " ");
      offset -= 1;
      
      this.overlapIndex(entities, tweetEntity, offset);
    }
  
    offset = 0;
    
    for (final Entity entity : entities) {
      if (entity.getPhrase().startsWith("#") || entity.getPhrase().startsWith("@")) {
        for (final TweetEntity tweetEntity : tweetEntities) {
          if (entity.getPhrase().equals(tweetEntity.getPhrase())) {
            offset += StringUtils.countMatches(tweetEntity.getCleanPhrase(), " ");
            offset -= 1;
            break;
          }
        }
      } else {
        entity.setStartOffset(entity.getStartOffset() - offset);
        entity.setEndOffset(entity.getEndOffset() - offset);
      }
    }
    
    return entities;
  }
  
  private void overlapIndex(final List<Entity> entities, final TweetEntity tweetEntity, final int offset) {
    int index = -1;
    
    for (final Entity entity : entities) {
      if (tweetEntity.getStartOffset().equals(entity.getStartOffset()) ||
          tweetEntity.getStartOffset() + offset == entity.getStartOffset() ||
          tweetEntity.getEndOffset().equals(entity.getEndOffset()) ||
          tweetEntity.getEndOffset() + offset == entity.getEndOffset() ||
          tweetEntity.getStartOffset() <= entity.getStartOffset() && tweetEntity.getEndOffset() >= entity.getEndOffset()){
        index = entities.indexOf(entity);
        break;
      }
    }
    
    if (index != -1) {
      entities.get(index).setStartOffset(tweetEntity.getStartOffset());
      entities.get(index).setEndOffset(tweetEntity.getEndOffset());
      entities.get(index).setPhrase(tweetEntity.getPhrase());
    }
  }
}
