package fr.eurecom.adel.recognition.implementation.repositories.typeoverlapresolution.majorityvoting;

import com.google.common.io.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import fr.eurecom.adel.recognition.configuration.TypeOverlappingConfig;
import fr.eurecom.adel.commons.datatypes.Entity;
import fr.eurecom.adel.recognition.domain.repositories.TypeOverlapResolutionRepository;
import fr.eurecom.adel.commons.validators.Name;
import fr.eurecom.adel.recognition.exceptions.MappingNotExistsException;
import fr.eurecom.adel.recognition.exceptions.TypeNotExistsException;

/**
 * @author Julien Plu on 2018-11-26.
 */
@Name(name = "MajorityVoting")
public class MajorityVotingTypeOverlapResolution implements TypeOverlapResolutionRepository {
  private static final Logger logger = LoggerFactory.getLogger(MajorityVotingTypeOverlapResolution.class);
  private final Map<String, Map<String, String>> typesMapping;
  
  public MajorityVotingTypeOverlapResolution() {
    this.typesMapping = new HashMap<>();
    this.init();
  }
  
  private void init() {
    try {
      final ClassLoader cl = this.getClass().getClassLoader();
      final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
      final Resource[] resources = resolver.getResources("classpath:mappings/**/*.map");
      
      for (final Resource resource: resources) {
        final List<String> lines = this.readResource(resource.getInputStream());
        final Map<String, String> mapping = new HashMap<>();
  
        for (final String line : lines) {
          final String left = line.split("\t")[0];
          final String right = line.split("\t")[1];
    
          for (final String type : left.split(",")) {
            mapping.put(type, right);
          }
        }
  
        this.typesMapping.put(Files.getNameWithoutExtension(Objects.requireNonNull(resource.getFilename())), mapping);
      }
    } catch (final IOException ex) {
      MajorityVotingTypeOverlapResolution.logger.error("Issue to read the mapping files", ex);
    }
  }
  
  private List<String> readResource(final InputStream stream) {
    List<String> lines = new ArrayList<>();
    
    try (final BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
      lines = br.lines().parallel().collect(Collectors.toList());
    } catch (final IOException ex) {
      MajorityVotingTypeOverlapResolution.logger.error("Issue to transform the stream of a dictionary into a list", ex);
    }
    
    return lines;
  }
  
  @Override
  public final void resolveTypeOverlapping(final TypeOverlappingConfig config, final List<Entity> entities) throws MappingNotExistsException, TypeNotExistsException {
    for (final Entity entity : entities) {
      StringBuilder newType = new StringBuilder();
      
      for (final String singleType : entity.getType().split("\\|\\|")) {
        final String[] splittedType = singleType.split("from");
        
        if (newType.toString().isEmpty()) {
          newType = new StringBuilder(this.resolveTypeMapping(splittedType[1].split("--")[0], config.getTo(), splittedType[0]));
        } else {
          newType.append("||");
          newType.append(this.resolveTypeMapping(splittedType[1].split("--")[0], config.getTo(), splittedType[0]));
        }
        
        newType.append("--");
        newType.append(config.getPriority().indexOf(splittedType[1].split("--")[1]));
      }
      
      entity.setType(newType.toString());
    }
    
    this.majorityVote(entities);
  }
  
  private void majorityVote(final Iterable<Entity> newEntities) {
    for (final Entity entity : newEntities) {
      final Map<String, Long> map = Arrays.stream(entity.getType().split("\\|\\|")).collect(Collectors.groupingBy(s -> s.split("--")[0], Collectors.counting()));
      
      if (Collections.max(map.values()).equals(Collections.min(map.values()))) {
        final List<String> types = Arrays.asList(entity.getType().split("\\|\\|"));
        
        types.sort(Comparator.comparing(newS -> Integer.valueOf(newS.split("--")[1])));
        
        entity.setType(types.get(0).split("--")[0]);
      } else {
        entity.setType(map.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey());
      }
    }
  }
  
  private String resolveTypeMapping(final String from, final String to, final String type) throws MappingNotExistsException, TypeNotExistsException {
    final String newType;
    
    if (from.equals(to)) {
      newType = type;
    } else {
      if (!this.typesMapping.containsKey(from + '2' + to)) {
        throw new MappingNotExistsException("The mapping file " + from + '2' + to + " does not exists");
      }
      
      if (!this.typesMapping.get(from + '2' + to).containsKey(type)) {
        throw new TypeNotExistsException("The type " + type + " does not exists in the vocabulary " + from);
      }
      
      newType = this.typesMapping.get(from + '2' + to).get(type);
    }
    
    return newType;
  }
}