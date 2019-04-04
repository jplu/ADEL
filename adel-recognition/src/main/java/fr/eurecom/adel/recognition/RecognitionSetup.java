package fr.eurecom.adel.recognition;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.eurecom.adel.recognition.configuration.AnnotatorConfig;
import fr.eurecom.adel.recognition.configuration.RecognitionConfig;
import fr.eurecom.adel.commons.utils.ReflectionUtils;
import fr.eurecom.adel.recognition.domain.repositories.AnnotatorRepository;
import fr.eurecom.adel.recognition.domain.repositories.HashtagSegmentationRepository;
import fr.eurecom.adel.recognition.domain.repositories.MentionOverlapResolutionRepository;
import fr.eurecom.adel.recognition.domain.repositories.TypeOverlapResolutionRepository;
import fr.eurecom.adel.recognition.domain.repositories.UserMentionDereferencingRepository;
import fr.eurecom.adel.recognition.usecases.Annotator;
import fr.eurecom.adel.recognition.usecases.OverlapResolution;
import fr.eurecom.adel.recognition.usecases.RecognitionPipeline;
import fr.eurecom.adel.recognition.usecases.TweetNormalization;

/**
 * @author Julien Plu on 2018-11-25.
 */
@Configuration
public class RecognitionSetup {
  private RecognitionConfig recognitionConfig;
  
  @Autowired
  public final void setRecognitionConfig(final RecognitionConfig newRecognitionConfig) {
    this.recognitionConfig = newRecognitionConfig;
  }
  
  @Bean
  public RecognitionPipeline init() throws  ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
    final Map<AnnotatorRepository, AnnotatorConfig> annotatorsRepository = new HashMap<>();
    Constructor constructor;
  
    for (final AnnotatorConfig conf : this.recognitionConfig.getAnnotators()) {
      constructor = Class.forName(ReflectionUtils.getClassNameFromMethod(conf.getAnnotator(), "annotator")).getConstructor(String.class);
      
      final AnnotatorRepository annotatorRepository = (AnnotatorRepository) constructor.newInstance(conf.getAddress());
      
      annotatorsRepository.put(annotatorRepository, conf);
    }
    
    constructor = Class.forName(ReflectionUtils.getClassNameFromMethod(this.recognitionConfig.getTypeoverlapping().getMethod(), "typeoverlapresolution")).getConstructor();
  
    final TypeOverlapResolutionRepository typeOverlapResolutionRepository = (TypeOverlapResolutionRepository) constructor.newInstance();
    
    final List<Annotator> annotators = new ArrayList<>();
  
    for (final Map.Entry<AnnotatorRepository, AnnotatorConfig> entry : annotatorsRepository.entrySet()) {
      annotators.add(new Annotator(entry.getKey(), entry.getValue()));
    }
    
    constructor = Class.forName(ReflectionUtils.getClassNameFromMethod(this.recognitionConfig.getMentionoverlapping(), "mentionoverlapresolution")).getConstructor();
  
    final MentionOverlapResolutionRepository mentionOverlapResolutionRepository = (MentionOverlapResolutionRepository) constructor.newInstance();
    int indexTokenizer = -1;
    
    for (final Annotator annotator : annotators) {
      if (annotator.getConfig().getTokenizer()) {
        indexTokenizer = annotators.indexOf(annotator);
        
        break;
      }
    }
    
    if (this.recognitionConfig.getTweetnormalization().getActivate()) {
      constructor = Class.forName(ReflectionUtils.getClassNameFromMethod(this.recognitionConfig.getTweetnormalization().getUsermention(), "usermentiondereferencing")).getConstructor();
  
      final UserMentionDereferencingRepository userMentionDereferencingRepository = (UserMentionDereferencingRepository) constructor.newInstance();
  
      constructor = Class.forName(ReflectionUtils.getClassNameFromMethod(this.recognitionConfig.getTweetnormalization().getHashtag(), "hashtagsegmentation")).getConstructor();
      
      final HashtagSegmentationRepository hashtagSegmentationRepository = (HashtagSegmentationRepository) constructor.newInstance();
      
      return new RecognitionPipeline(annotators, new OverlapResolution(typeOverlapResolutionRepository, this.recognitionConfig.getTypeoverlapping(), mentionOverlapResolutionRepository), new TweetNormalization(hashtagSegmentationRepository, userMentionDereferencingRepository), indexTokenizer, this.recognitionConfig);
    }
    
    return new RecognitionPipeline(annotators, new OverlapResolution(typeOverlapResolutionRepository, this.recognitionConfig.getTypeoverlapping(), mentionOverlapResolutionRepository), null, indexTokenizer, this.recognitionConfig);
  }
}
