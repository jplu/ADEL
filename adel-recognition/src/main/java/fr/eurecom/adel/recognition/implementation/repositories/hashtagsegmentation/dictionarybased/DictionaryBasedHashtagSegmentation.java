package fr.eurecom.adel.recognition.implementation.repositories.hashtagsegmentation.dictionarybased;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import fr.eurecom.adel.recognition.domain.repositories.HashtagSegmentationRepository;
import fr.eurecom.adel.commons.validators.Name;

/**
 * @author Julien Plu on 2018-12-09.
 */
@Name(name = "Dictionary")
public class DictionaryBasedHashtagSegmentation implements HashtagSegmentationRepository {
  private static final Logger logger = LoggerFactory.getLogger(DictionaryBasedHashtagSegmentation.class);
  private final Map<String, Double> unigrams;
  private final Map<String, Double> bigrams;
  // Total number of words in the Google Billion words corpus
  private static final Double TOTAL = 1024908267229.0;
  
  public DictionaryBasedHashtagSegmentation() {
    this.unigrams = new HashMap<>();
    this.bigrams = new HashMap<>();
    
    this.loadAllDictionaries();
  }
  
  @Override
  public final String segment(final String hashtag) {
    final StringBuilder wellFormed = new StringBuilder(String.join(" ", this.isegment(hashtag)));
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
  
  private Iterable<String> isegment(final String hashtag) {
    final String lowerCase = hashtag.toLowerCase(Locale.ENGLISH);
    int size = lowerCase.length();
    
    if (250 < lowerCase.length()) {
      size = 250;
    }
    
    String prefix = "";
    
    for (int offset = 0;offset < lowerCase.length();offset += size) {
      final String chunk = lowerCase.substring(offset, offset + size);
      
      final List<String> chunkWords = new ArrayList<>(this.search(prefix + chunk, "<s>").getRight());
      
      if (5 > chunkWords.size()) {
        prefix = String.join("", chunkWords);
      } else {
        prefix = String.join("", chunkWords.subList(chunkWords.size() - 5, chunkWords.size()));
        chunkWords.subList(0, 5).clear();
      }
    }
    
    return this.search(prefix, "<s>").getRight();
  }
  
  private void loadAllDictionaries() {
    try {
      final ClassLoader cl = this.getClass().getClassLoader();
      final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
      final Resource[] resources = resolver.getResources("classpath:dictionaries/**/*.tsv");
    
      for (final Resource resource: resources) {
        this.readDictionary(this.readResource(resource.getInputStream()), Objects.requireNonNull(resource.getFilename()));
      }
    } catch (final IOException ex) {
      DictionaryBasedHashtagSegmentation.logger.error("", ex);
    }
  }
  
  private List<String> readResource(final InputStream stream) {
    List<String> lines = new ArrayList<>();
    
    try (final BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
      lines = br.lines().parallel().collect(Collectors.toList());
    } catch (final IOException ex) {
      DictionaryBasedHashtagSegmentation.logger.error("", ex);
    }
    
    return lines;
  }
  
  private void readDictionary(final Iterable<String> lines, final String name) {
    if (name.contains("unigrams")) {
      for (final String line : lines) {
        this.unigrams.put(line.split("\t")[0], Double.valueOf(line.split("\t")[1]));
      }
    } else {
      for (final String line : lines) {
        this.bigrams.put(line.split("\t")[0], Double.valueOf(line.split("\t")[1]));
      }
    }
  }
  
  private Double score(final String word, final String prev) {
    if (prev.isEmpty()) {
      if (this.unigrams.containsKey(word)) {
        return this.unigrams.get(word) / DictionaryBasedHashtagSegmentation.TOTAL;
      } else {
        return 10.0 / (DictionaryBasedHashtagSegmentation.TOTAL * StrictMath.pow(10.0, Double.parseDouble(Integer.toString(word.length()))));
      }
    } else {
      final String bigram = prev + ' ' + word;
      
      if (this.bigrams.containsKey(bigram) && this.unigrams.containsKey(prev)) {
        return this.bigrams.get(bigram) / DictionaryBasedHashtagSegmentation.TOTAL / this.score(prev, "");
      } else {
        return this.score(word, "");
      }
    }
  }
  
  private Pair<Double, List<String>> search(final String text, final String prev) {
    if (text.isEmpty()) {
      return ImmutablePair.of(0.0, Collections.emptyList());
    }
    
    final List<Pair<Double, List<String>>> candidates = this.candidates(text, prev);
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
  
  private List<Pair<Double, List<String>>> candidates(final String text, final String prev) {
    final List<Pair<Double, List<String>>> candidates = new ArrayList<>();
    final Map<Pair<String, String>, Pair<Double, List<String>>> memo = new HashMap<>();
    
    for (final Pair<String, String> pair : this.divide(text)) {
      final double prefixScore = StrictMath.log10(this.score(pair.getLeft(), prev));
      
      if (!memo.containsKey(pair)) {
        memo.put(pair, this.search(pair.getRight(), pair.getLeft()));
      }
      
      final List<String> segments = new ArrayList<>();
      
      segments.add(pair.getLeft());
      segments.addAll(memo.get(pair).getRight());
      
      candidates.add(ImmutablePair.of(prefixScore + memo.get(pair).getLeft(), segments));
    }
    
    return candidates;
  }
  
  private Iterable<Pair<String, String>> divide(final String text) {
    final Collection<Pair<String, String>> segments = new ArrayList<>();
    
    for (int pos = 1;pos < Math.min(text.length(), 24) + 1;pos++) {
      segments.add(ImmutablePair.of(text.substring(0, pos), text.substring(pos)));
    }
    
    return segments;
  }
}
