package fr.eurecom.adel.utils;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * @author Julien Plu
 */
public class HashtagSegmentation {
  private static final Map<String, Map<String, Double>> UNIGRAMS = new HashMap<>();
  private static final Map<String, Map<String, Double>> BIGRAMS = new HashMap<>();
  // Total number of words in the Google Billion words corpus
  private static final Double TOTAL = 1024908267229.0;
  
  public static void loadAllDictionaries() {
    try {
      Files.list(Paths.get("dictionaries")).forEach(file -> {
        try {
          System.out.println("load " + file);
        
          HashtagSegmentation.readDictionary(file, file.getFileName().toString().split("_")[0]);
        } catch (final IOException ex) {
          throw new WebApplicationException("Failed to read the dictionary " + file, ex,
              Response.Status.PRECONDITION_FAILED);
        }
      });
    } catch (final IOException ex) {
      throw new WebApplicationException("Failed to read the directory dictionaries", ex,
          Response.Status.PRECONDITION_FAILED);
    }
  }
  
  public static void loadDictionaries(final String lang) {
    try {
      Files.list(Paths.get("dictionaries")).filter(file -> file.getFileName().toString().split(
          "_")[0].equals(lang)).forEach(file -> {
            try {
              System.out.println("load " + file);
              
              HashtagSegmentation.readDictionary(file, lang);
        } catch (final IOException ex) {
          throw new WebApplicationException("Failed to read the dictionary " + file, ex,
              Response.Status.PRECONDITION_FAILED);
        }
      });
    } catch (final IOException ex) {
      throw new WebApplicationException("Failed to read the directory dictionaries", ex,
          Response.Status.PRECONDITION_FAILED);
    }
  }
  
  private static Double score(final String word, final String prev, final String lang) {
    if (prev.isEmpty()) {
      if (HashtagSegmentation.UNIGRAMS.get(lang).containsKey(word)) {
        return HashtagSegmentation.UNIGRAMS.get(lang).get(word) / HashtagSegmentation.TOTAL;
      } else {
        return 10.0 / (HashtagSegmentation.TOTAL * StrictMath.pow(10.0, Double.parseDouble(
            Integer.toString(word.length()))));
      }
    } else {
      final String bigram = prev + ' ' + word;
      
      if (HashtagSegmentation.BIGRAMS.get(lang).containsKey(bigram) &&
          HashtagSegmentation.UNIGRAMS.get(lang).containsKey(prev)) {
        return HashtagSegmentation.BIGRAMS.get(lang).get(bigram) / HashtagSegmentation.TOTAL /
            HashtagSegmentation.score(prev, "", lang);
      } else {
        return HashtagSegmentation.score(word, "", lang);
      }
    }
  }
  
  private static void readDictionary(final Path newPath, final String lang) throws IOException {
    final List<String> lines = Files.readAllLines(newPath, Charset.forName("UTF-8"));
    
    if (newPath.toString().contains("unigram")) {
      HashtagSegmentation.UNIGRAMS.put(lang, new HashMap<>());
      
      for (final String line : lines) {
        HashtagSegmentation.UNIGRAMS.get(lang).put(line.split("\t")[0], Double.valueOf(line.split(
            "\t")[1]));
      }
    } else {
      HashtagSegmentation.BIGRAMS.put(lang, new HashMap<>());
      
      for (final String line : lines) {
        HashtagSegmentation.BIGRAMS.get(lang).put(line.split("\t")[0], Double.valueOf(line.split(
            "\t")[1]));
      }
    }
  }
  
  static String segment(final String hashtag, final String lang) {
    final StringBuilder wellFormed = new StringBuilder(String.join(" ",
        HashtagSegmentation.isegment(hashtag, lang)));
    int count = 0;
    
    for (int i = 0;i < wellFormed.length();i++) {
      if (!" ".equals(Character.toString(wellFormed.charAt(i)))) {
        if (Character.isUpperCase(hashtag.charAt(count))) {
          wellFormed.setCharAt(i, hashtag.charAt(count));
        }
        
        count++;
      }
    }
    
    return wellFormed.toString();
  }
  
  private static Iterable<String> isegment(final String hashtag, final String lang) {
    final String lowerCase = StringUtils.cleanText(hashtag);
    int size = lowerCase.length();
    
    if (lowerCase.length() > 250) {
      size = 250;
    }
    
    String prefix = "";
    
    for (int offset = 0;offset < lowerCase.length();offset += size) {
      final String chunk = lowerCase.substring(offset, offset + size);
      
      final List<String> chunkWords = new ArrayList<>(HashtagSegmentation.search(prefix + chunk,
          "<s>", lang).getRight());
      
      if (chunkWords.size() < 5) {
        prefix = String.join("", chunkWords);
      } else {
        prefix = String.join("", chunkWords.subList(chunkWords.size() - 5, chunkWords.size()));
        chunkWords.subList(0, 5).clear();
      }
    }
    
    return HashtagSegmentation.search(prefix, "<s>", lang).getRight();
  }
  
  private static Pair<Double, List<String>> search(final String text, final String prev,
                                                   final String lang) {
    if (text.isEmpty()) {
      return ImmutablePair.of(0.0, Collections.emptyList());
    }
    
    final List<Pair<Double, List<String>>> candidates = HashtagSegmentation.candidates(text, prev,
        lang);
    int maxIndex = 0;
    double value = candidates.get(0).getLeft();
    
    for (int i = 1;i < candidates.size();i++) {
      if (candidates.get(i).getLeft() > value) {
        value = candidates.get(i).getLeft();
        maxIndex = i;
      }
    }
    
    return candidates.get(maxIndex);
  }
  
  private static List<Pair<Double, List<String>>> candidates(final String text, final String prev,
                                                             final String lang) {
    final List<Pair<Double, List<String>>> candidates = new ArrayList<>();
    final Map<Pair<String, String>, Pair<Double, List<String>>> memo = new HashMap<>();
    
    for (final Pair<String, String> pair : HashtagSegmentation.divide(text)) {
      final double prefixScore = StrictMath.log10(HashtagSegmentation.score(pair.getLeft(), prev,
          lang));
      
      if (!memo.containsKey(pair)) {
        memo.put(pair, HashtagSegmentation.search(pair.getRight(), pair.getLeft(), lang));
      }
  
      final List<String> segments = new ArrayList<>();
      
      segments.add(pair.getLeft());
      segments.addAll(memo.get(pair).getRight());
  
      candidates.add(ImmutablePair.of(prefixScore + memo.get(pair).getLeft(), segments));
    }
    
    return candidates;
  }
  
  private static Iterable<Pair<String, String>> divide(final String text) {
    final Collection<Pair<String, String>> segments = new ArrayList<>();
    
    for (int pos = 1;pos < Math.min(text.length(), 24) + 1;pos++) {
      segments.add(ImmutablePair.of(text.substring(0, pos), text.substring(pos)));
    }
    
    return segments;
  }
}
