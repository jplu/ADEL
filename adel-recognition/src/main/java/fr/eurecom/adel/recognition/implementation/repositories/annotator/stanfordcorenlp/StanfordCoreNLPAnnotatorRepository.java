package fr.eurecom.adel.recognition.implementation.repositories.annotator.stanfordcorenlp;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreSentence;
import fr.eurecom.adel.recognition.configuration.AnnotatorConfig;
import fr.eurecom.adel.commons.datatypes.Entity;
import fr.eurecom.adel.commons.datatypes.Token;
import fr.eurecom.adel.recognition.domain.repositories.AnnotatorRepository;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import fr.eurecom.adel.commons.validators.Name;

/**
 * @author Julien Plu on 17/11/2018.
 */
@Name(name = "StanfordCoreNLP")
public class StanfordCoreNLPAnnotatorRepository implements AnnotatorRepository {
  private final StanfordCoreNLP pipeline;
  
  public StanfordCoreNLPAnnotatorRepository(final String path) {
    this.pipeline = new StanfordCoreNLP(path.replace("classpath:", ""));
  }
  
  @Override
  public final List<Entity> annotate(final AnnotatorConfig config, final String text) {
    final CoreDocument doc = new CoreDocument(text);
  
    this.pipeline.annotate(doc);
    
    final List<CoreEntityMention> entityMentions = doc.entityMentions();
    final List<Entity> entities = new ArrayList<>();
    
    if (null != entityMentions) {
      for (final CoreEntityMention entityMention : entityMentions) {
        //TODO: add score with entityTypeConfidences
        if (null == config.getTags() || config.getTags().contains(entityMention.entityType())) {
          entities.add(
              Entity.builder()
                  .phrase(entityMention.text())
                  .cleanPhrase(entityMention.canonicalEntityMention().orElse(entityMention).text())
                  .type(entityMention.entityType())
                  .startOffset(entityMention.charOffsets().first)
                  .endOffset(entityMention.charOffsets().second)
                  .build());
        }
      }
    }
  
    return entities;
  }
  
  @Override
  public final List<List<Token>> tokenize(final String text) {
    final CoreDocument doc = new CoreDocument(text);
  
    this.pipeline.annotate(doc);
    
    final List<CoreSentence> sentences = doc.sentences();
    final List<List<Token>> document = new ArrayList<>();
    
    for (final CoreSentence sentence : sentences) {
      final List<Token> tokens = new ArrayList<>();
      
      for (final CoreLabel label : sentence.tokens()) {
        tokens.add(
            Token.builder()
                .value(label.value())
                .begin(label.beginPosition())
                .end(label.endPosition())
                .build());
      }
      
      document.add(tokens);
    }
    
    return document;
  }
}
