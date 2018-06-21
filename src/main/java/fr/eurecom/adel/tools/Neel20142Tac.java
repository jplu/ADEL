package fr.eurecom.adel.tools;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import fr.eurecom.adel.configurations.AdelConfiguration;
import fr.eurecom.adel.datatypes.Candidate;
import fr.eurecom.adel.datatypes.Entity;
import fr.eurecom.adel.outputs.IOutput;
import fr.eurecom.adel.outputs.TacOutput;
import fr.eurecom.adel.tasks.indexing.Elasticsearch;

/**
 * @author Julien Plu
 */
public class Neel20142Tac {
  public final void run(final Path text, final Path ann, final Path tac,
                        final AdelConfiguration conf) throws IOException {
    final Map<String, String> sentences = this.getSentences(text);
    final Map<String, List<Entity>> gs = this.getGs(ann, sentences, conf);
    final Map<String, Pair<String, List<Entity>>> entries = new ConcurrentHashMap<>();
  
    sentences.entrySet().parallelStream().forEach(entry -> {
      if (gs.get(entry.getKey()) == null) {
        entries.put(entry.getKey(), new MutablePair<>(entry.getValue(), new ArrayList<>()));
      } else {
        entries.put(entry.getKey(), new MutablePair<>(entry.getValue(), gs.get(
            entry.getKey())));
      }
    });
  
    final IOutput<Entity> output = new TacOutput();
    final String tacStr = output.write(entries, conf.getIndexProperties());
  
    Files.write(tac, tacStr.getBytes(Charset.forName("UTF-8")), StandardOpenOption.CREATE_NEW,
        StandardOpenOption.WRITE);
  }
  
  private Map<String, String> getSentences(final Path text) throws IOException {
    final List<String> lines = Files.readAllLines(text, Charset.forName("UTF-8"));
    final Map<String, String> sentences = new HashMap<>();
    
    for (final String line : lines) {
      final String[] split = line.split("\t");
      
      sentences.put(split[0], split[1]);
    }
    
    return sentences;
  }
  
  private int[] offsetsWithoutHashes(final String mention, final String document, final int i) {
    String textWithoutHashes = "";
    final List<Integer> hashes = new ArrayList<>();
    
    if (textWithoutHashes.isEmpty()) {
      int pos = document.indexOf('#');
    
      while (pos >= 0) {
        hashes.add(pos);
        pos = document.indexOf('#', pos + 1);
      }
    
      textWithoutHashes = document.replaceAll("#", "");
    }
  
    int end = 0;
  
    for (int j = 0;(i < hashes.size()) && (hashes.get(j) < end);j++) {
      --end;
    }
  
    int start = textWithoutHashes.indexOf(mention, end);
  
    if (start >= 0) {
      end = start + mention.length();
    
      for (int j = 0;(j < hashes.size()) && (hashes.get(j) < end);j++) {
        ++end;
        if (hashes.get(j) < start) {
          ++start;
        }
      }
    }
  
    final int[] offset = new int[2];
    
    offset[0] = start;
    offset[1] = end;
    
    return offset;
  }
  
  private int[] offsetsWithoutMentions(final String mention, final String document, final int i) {
    String textWithoutMentions = "";
    final List<Integer> mentions = new ArrayList<>();
    
    if (textWithoutMentions.isEmpty()) {
      int pos = document.indexOf('@');
  
      while (pos >= 0) {
        mentions.add(pos);
        pos = document.indexOf('@', pos + 1);
      }
  
      textWithoutMentions = document.replaceAll("@", "");
    }
  
    int end = 0;
    
    for (int j = 0; (i < mentions.size()) && (mentions.get(j) < end); j++) {
      --end;
    }
  
    int start = textWithoutMentions.indexOf(mention, end);
  
    if (start >= 0) {
      end = start + mention.length();
      
      for (int j = 0;(j < mentions.size()) && (mentions.get(j) < end);j++) {
        ++end;
        if (mentions.get(j) < start) {
          ++start;
        }
      }
    }
  
    final int[] offset = new int[2];
    
    offset[0] = start;
    offset[1] = end;
    
    return offset;
  }
  
  private Map<String, List<Entity>> getGs(final Path ann, final Map<String, String> sentences,
                                          final AdelConfiguration conf)
      throws IOException {
    final List<String> lines = Files.readAllLines(ann, Charset.forName("UTF-8"));
    final Map<String, List<Entity>> gs = new HashMap<>();
    final Elasticsearch es = new Elasticsearch(conf);
    
    for (final String line : lines) {
      final String[] split = line.split("\t");
      int end = 0;
      
      for (int i = 1;i < split.length; i += 2) {
        int start = sentences.get(split[0]).indexOf(split[i], end);
        
        if (start == -1) {
          int[] offset = this.offsetsWithoutHashes(split[i], sentences.get(split[0]), i);
          
          if (offset[0] == -1) {
            offset = this.offsetsWithoutMentions(split[i], sentences.get(split[0]), i);
            
            if (offset[0] == -1) {
              System.out.println("Impossible to find: " + split[i] + " in "
                  + sentences.get(split[0]));
            } else {
              start = offset[0];
              end = offset[1];
            }
          } else {
            start = offset[0];
            end = offset[1];
          }
        } else {
          end = start + split[i].length();
        }
        
        final Candidate candidate = es.searchLink(split[i + 1]);
        
        if (candidate.getProperties().isEmpty()) {
          this.createCandidate(split, i, candidate, conf);
        }
        
        if (gs.containsKey(split[0])) {
          gs.get(split[0]).add(new Entity(split[i], candidate, start, end, "GS", "Thing"));
        } else {
          final List<Entity> tmp = new ArrayList<>();
          
          tmp.add(new Entity(split[i], candidate, start, end, "GS", "Thing"));
          
          gs.put(split[0], tmp);
        }
      }
    }
    
    return gs;
  }
  
  private void createCandidate(final String[] line, final int index, final Candidate candidate,
                               final AdelConfiguration conf) {
    candidate.putProperty(conf.getIndexProperties().get("types"), new ArrayList<>());
    candidate.putProperty(conf.getIndexProperties().get("link"),
        Collections.singletonList(line[index + 1]));
    candidate.putProperty(conf.getIndexProperties().get("label"),
        Collections.singletonList(line[index]));
    
    candidate.setFinalScore(0.0f);
    candidate.setFrom(conf.getIndex().getFrom());
  }
}
