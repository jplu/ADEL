package fr.eurecom.adel.utils;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import fr.eurecom.adel.configurations.AdelConfiguration;
import fr.eurecom.adel.datatypes.Candidate;
import fr.eurecom.adel.datatypes.Entity;
import fr.eurecom.adel.tasks.indexing.Elasticsearch;

/**
 * @author Julien Plu
 */
public class TacUtils {
  private final Map<String, String> sentences;
  private final Map<String, List<Entity>> entitiesPerSentence;
  private final Map<String, List<Entity>> gs;
  private final AdelConfiguration adelConf;
  private final String lang;
  
  public TacUtils() {
    this.sentences = Collections.emptyMap();
    this.entitiesPerSentence = Collections.emptyMap();
    this.gs = Collections.emptyMap();
    this.adelConf = new AdelConfiguration();
    this.lang = "";
  }
  
  public TacUtils(final String tac, final String goldStandard,
                  final AdelConfiguration newAdelConf, final String newLang) {
    this.sentences = new ConcurrentHashMap<>();
    this.entitiesPerSentence = new HashMap<>();
    this.gs = new HashMap<>();
    this.adelConf = newAdelConf;
    this.lang = newLang;
  
    this.parseSentences(tac);
    this.parseEntitiesPerSentence(goldStandard);
  }
  
  public TacUtils(final Path tac, final Path goldStandard,
                  final AdelConfiguration newAdelConf, final String newLang) {
    this.sentences = new ConcurrentHashMap<>();
    this.entitiesPerSentence = new HashMap<>();
    this.gs = new HashMap<>();
    this.adelConf = newAdelConf;
    this.lang = newLang;
    
    this.parseSentences(tac);
    this.parseEntitiesPerSentence(goldStandard);
  }
  
  public final Map<String, String> getSentences() {
    return Collections.unmodifiableMap(this.sentences);
  }
  
  public final Map<String, List<Entity>> getEntitiesPerSentence() {
    return Collections.unmodifiableMap(this.entitiesPerSentence);
  }
  
  public final Map<String, List<Entity>> getGs() {
    return Collections.unmodifiableMap(this.gs);
  }
  
  private void createCandidate(final String[] tacLine, final String sentence,
                                    final Candidate candidate) {
    candidate.putProperty(this.adelConf.getIndexProperties().get("types"), new ArrayList<>());
    candidate.putProperty(this.adelConf.getIndexProperties().get("link"),
        Collections.singletonList(tacLine[3]));
    candidate.putProperty(this.adelConf.getIndexProperties().get("label"),
        Collections.singletonList(sentence.substring(Integer.valueOf(tacLine[1]),
            Integer.valueOf(tacLine[2]))));
    
    candidate.setFrom(this.adelConf.getIndex().getFrom());
  }
  
  private String segmentedPhrase(final String[] tacLine, final String sentence) {
    final int start = Integer.parseInt(tacLine[1]);
    final int end = Integer.parseInt(tacLine[2]);
    String res = sentence.substring(start, end);
    
    if (start > 0) {
      if ("@".equals(Character.toString(sentence.charAt(start - 1)))) {
        res = StringUtils.twitterQueryUserName(res);
      } else if ("#".equals(Character.toString(sentence.charAt(start - 1)))) {
        res = HashtagSegmentation.segment(res, this.lang);
      }
    }
    
    return res;
  }
  
  private void parseEntitiesPerSentence(final Path goldStandard) {
    try {
      this.parseEntitiesPerSentence(FileUtils.readFileToString(goldStandard.toFile(),
          Charset.forName("UTF-8")));
    } catch (final IOException ex) {
      throw new WebApplicationException("Failed to read the TAC gold standard file", ex,
          Response.Status.PRECONDITION_FAILED);
    }
  }
  
  private void parseSentences(final Path dataset) {
    try {
      this.parseSentences(FileUtils.readFileToString(dataset.toFile(), Charset.forName("UTF-8")));
    } catch (final IOException ex) {
      throw new WebApplicationException("Failed to read the TAC text file", ex,
          Response.Status.PRECONDITION_FAILED);
    }
  }
  
  private void parseEntitiesPerSentence(final String goldStandard) {
    final List<String> annotations = Arrays.asList(goldStandard.split(System.lineSeparator()));
    final Elasticsearch es = new Elasticsearch(this.adelConf);
  
    annotations.forEach(line -> {
      final String[] splitLine = line.replace(System.lineSeparator(), "").split("\t");
      final String sentence = StringUtils.normalizeString(this.sentences.get(splitLine[0]));
      final Candidate candidate = es.searchLink(splitLine[3]);
  
      if (candidate.getProperties().isEmpty()) {
        this.createCandidate(splitLine, sentence, candidate);
      }
      
      final Entity entity;
      
      if (splitLine.length == 5) {
        entity = new Entity(sentence.substring(Integer.valueOf(splitLine[1]),
            Integer.valueOf(splitLine[2])), this.segmentedPhrase(splitLine, sentence), candidate,
            Integer.valueOf(splitLine[1]), Integer.valueOf(splitLine[2]), "GS");
      } else {
        entity = new Entity(sentence.substring(Integer.valueOf(splitLine[1]),
            Integer.valueOf(splitLine[2])), this.segmentedPhrase(splitLine, sentence), candidate,
            Integer.valueOf(splitLine[1]), Integer.valueOf(splitLine[2]), "GS", splitLine[5]);
      }

      if (this.entitiesPerSentence.containsKey(splitLine[0])) {
        this.gs.get(splitLine[0]).add(entity);
        this.entitiesPerSentence.get(splitLine[0]).add(new Entity(sentence.substring(
            Integer.valueOf(splitLine[1]), Integer.valueOf(splitLine[2])),
            new ArrayList<>(), Integer.valueOf(splitLine[1]), Integer.valueOf(splitLine[2]), ""));
      } else {
        this.gs.put(splitLine[0], new ArrayList<>(Collections.singletonList(
            entity)));
        this.entitiesPerSentence.put(splitLine[0], new ArrayList<>(Collections.singleton(new Entity(
            sentence.substring(Integer.valueOf(splitLine[1]), Integer.valueOf(splitLine[2])),
            new ArrayList<>(), Integer.valueOf(splitLine[1]), Integer.valueOf(splitLine[2]),
            ""))));
      }
    });
  }
  
  private void parseSentences(final String dataset) {
    final List<String> lines = Collections.synchronizedList(Arrays.asList(dataset.split(
        System.lineSeparator())));
    
    lines.parallelStream().forEach(line -> this.sentences.put(line.split("\t")[0],
        line.split("\t")[1].replaceAll("\\[\\[", "").replaceAll("]]", "")));
  }
}
