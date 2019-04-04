package fr.eurecom.adel.commons.formats;

import org.javatuples.Triplet;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.eurecom.adel.commons.datatypes.Document;
import fr.eurecom.adel.commons.datatypes.Entity;
import fr.eurecom.adel.commons.datatypes.Token;
import fr.eurecom.adel.commons.exceptions.CoNLLMalformedException;
import fr.eurecom.adel.commons.utils.ScoringUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Julien Plu on 2019-02-15.
 */
@Slf4j
public class CoNLL {
  private Path input;
  private Path output;
  private final List<List<String>> conllDocuments;
  private final List<List<String>> conllLabels;
  private final List<List<String>> conllGoldLabels;
  private String conllContent;
  
  public CoNLL(final String newInput, final String newOutput, final boolean is0203Format) throws CoNLLMalformedException, IOException {
    this.input = Paths.get(newInput);
    this.output = Paths.get(newOutput);
    this.conllDocuments = new ArrayList<>();
    this.conllLabels = new ArrayList<>();
    this.conllGoldLabels = new ArrayList<>();
    
    this.read(false, is0203Format);
  }
  
  public CoNLL(final String newInput, final boolean isEval, final boolean is0203Format) throws CoNLLMalformedException, IOException {
    this.input = Paths.get(newInput);
    this.conllDocuments = new ArrayList<>();
    this.conllLabels = new ArrayList<>();
    this.conllGoldLabels = new ArrayList<>();
  
    this.read(isEval, is0203Format);
  }
  
  public CoNLL(final String newOutput) {
    this.output = Paths.get(newOutput);
    this.conllDocuments = new ArrayList<>();
    this.conllLabels = new ArrayList<>();
    this.conllGoldLabels = new ArrayList<>();
  }
  
  public CoNLL() {
    this.conllDocuments = new ArrayList<>();
    this.conllLabels = new ArrayList<>();
    this.conllGoldLabels = new ArrayList<>();
  }
  
  public final List<String> documents() {
    final List<String> documentsAsText = new ArrayList<>();
    
    for (final List<String> doc : this.conllDocuments) {
      documentsAsText.add(String.join(" ", doc));
    }
    
    return documentsAsText;
  }
  
  private void read(final boolean isEval, final boolean is0203Format) throws CoNLLMalformedException, IOException {
    final List<String> lines = Files.readAllLines(this.input, Charset.forName("UTF-8"));
    
    if (!lines.get(lines.size() - 1).isEmpty()) {
      throw new CoNLLMalformedException("Malformed CoNLL: new line missing at the end of the file " + this.input);
    }
    
    if (is0203Format && isEval) {
      throw new CoNLLMalformedException("Cannot evaluate a CoNLL02/CoNLL03 formats");
    }
    
    int count = 2;

    if (isEval) {
      count = 3;
    } else if (is0203Format) {
      count = 4;
    }

    List<String> conllDocument = new ArrayList<>();
    List<String> conllGoldLabelsInDocument = new ArrayList<>();
    final List<String> conllLabelsInDocuments = new ArrayList<>();
    final Collection<String> words = new ArrayList<>();
    final Collection<String> goldLabels = new ArrayList<>();
    final Collection<String> labels = new ArrayList<>();
    int lineNumber = 0;
    
    for (final String line : lines) {
      lineNumber++;
      final String contents = line.trim();
      final String[] tokens = contents.split(" ");
      
      if (tokens.length == count) {
        if ("-DOCSTART-".equals(tokens[0]) && !conllDocument.isEmpty()) {
          this.conllDocuments.add(conllDocument);
          this.conllGoldLabels.add(conllGoldLabelsInDocument);

          conllDocument = new ArrayList<>();
          conllGoldLabelsInDocument = new ArrayList<>();
        } else if (!"-DOCSTART-".equals(tokens[0])) {
          words.add(tokens[0]);
  
          if (isEval) {
            labels.add(tokens[1].replaceAll("[I|B]-", ""));
            goldLabels.add(tokens[2].replaceAll("[I|B]-", ""));
          } else if (is0203Format) {
            goldLabels.add(tokens[3].replaceAll("[I|B]-", ""));
          } else {
            goldLabels.add(tokens[1].replaceAll("[I|B]-", ""));
          }
        }
      } else if (contents.isEmpty()) {
        if (!words.isEmpty()) {
          conllDocument.add(String.join(" ", words));
          conllGoldLabelsInDocument.add(String.join(" ", goldLabels));
          words.clear();
          goldLabels.clear();

          if (isEval) {
            conllLabelsInDocuments.add(String.join(" ", labels));
            labels.clear();
          }
        }
      } else {
        throw new CoNLLMalformedException("Malformed CoNLL: the line " + lineNumber + " in file " + this.input + " has " + contents.split(" ").length + " columns instead of " + count);
      }
    }

    this.conllDocuments.add(conllDocument);
    this.conllGoldLabels.add(conllGoldLabelsInDocument);
    
    if (isEval) {
      this.conllLabels.add(conllLabelsInDocuments);
    }
  }
  
  public final void addDocument(final Document document) {
    final List<String> conllLabelsInDocument = new ArrayList<>();
    final List<String> conllDocument = new ArrayList<>();
  
    for (final List<Token> sentence : document.getTokens() ) {
      final Collection<String> conllSentence = new ArrayList<>();
      final Collection<String> conllLabelsInSentence = new ArrayList<>();
    
      for (final Token token : sentence) {
        boolean found = false;
      
        for (final Entity entity : document.getEntities()) {
          if (token.getBegin() >= entity.getStartOffset() && token.getEnd() <= entity.getEndOffset()) {
            found = true;
            
            conllLabelsInSentence.add(entity.getType());
            break;
          }
        }
      
        if (!found) {
          conllLabelsInSentence.add("O");
        }
        
        if (this.conllGoldLabels.isEmpty()) {
          conllSentence.add(token.getValue());
        }
      }
      
      conllLabelsInDocument.add(String.join(" ", conllLabelsInSentence));
      
      if (this.conllGoldLabels.isEmpty()) {
        conllDocument.add(String.join(" ", conllSentence));
      }
    }
    
    this.conllLabels.add(conllLabelsInDocument);
    
    if (this.conllGoldLabels.isEmpty()) {
      this.conllDocuments.add(conllDocument);
    }
  }
  
  private String toCoNLLText() {
    final StringBuilder sb = new StringBuilder();
    
    for (int i = 0;i < this.conllDocuments.size();i++) {
      if (this.conllGoldLabels.isEmpty()) {
        sb.append("-DOCSTART- O");
      } else {
        sb.append("-DOCSTART- O O");
      }
    
      sb.append(System.lineSeparator());
      sb.append(System.lineSeparator());
    
      for (int j = 0; j < this.conllDocuments.get(i).size(); j++) {
        final List<String> labels = Arrays.asList(this.conllLabels.get(i).get(j).split(" "));
        final List<String> tokens = Arrays.asList(this.conllDocuments.get(i).get(j).split(" "));
        final List<List<String>> lines;
      
        if (this.conllGoldLabels.isEmpty()) {
          lines = this.zip(tokens, labels);
        } else {
          final List<String> goldLabels = Arrays.asList(this.conllGoldLabels.get(i).get(j).split(" "));
        
          lines = this.zip(tokens, labels, goldLabels);
        }
      
        for (final List<String> line : lines) {
          sb.append(String.join(" ", line));
          sb.append(System.lineSeparator());
        }
      
        sb.append(System.lineSeparator());
      }
    }
    
    return sb.toString();
  }
  
  public final void write(final boolean print) throws IOException {
    this.conllContent = this.toCoNLLText();
    
    if (print) {
      CoNLL.log.info("{}{}", System.lineSeparator(), this.conllContent);
    }
    
    if (this.output != null) {
      Files.write(this.output, Arrays.asList(this.conllContent.split(System.lineSeparator())));
    }
  }
  
  public final void scorerExtraction() {
    // Precision = TP / (TP + FP)
    // Rappel = TP / (TP + FN)
    // F1 = 2 . (P . R) / (P + R)
    // ACC = (TP + TN) / (TP + TN + FP + FN)
    // Triplet<TP, FP, FN>
    if (this.conllContent == null) {
      this.conllContent = this.toCoNLLText();
    }
    
    final String[] lines = this.conllContent.split(System.lineSeparator());
    final Map<String, Triplet<Integer, Integer, Integer>> annotations = new HashMap<>();
    
    for (final String line : lines) {
      final String[] columns = line.split(" ");
      
      if (columns.length == 3 && !"-DOCSTART-".equals(columns[0])) {
        if (!"O".equals(columns[2]) && !"O".equals(columns[1])) {
          annotations.computeIfPresent("", (key, val) -> val.setAt0(val.getValue0() + 1));
          annotations.computeIfAbsent("", x -> Triplet.with(1, 0, 0));
        } else {
          if (!"O".equals(columns[2])) {
            annotations.computeIfPresent("", (key, val) -> val.setAt2(val.getValue2() + 1));
            annotations.computeIfAbsent("", x -> Triplet.with(0, 0, 1));
          }
          
          if (!"O".equals(columns[1])) {
            annotations.computeIfPresent("", (key, val) -> val.setAt1(val.getValue1() + 1));
            annotations.computeIfAbsent("", x -> Triplet.with(0, 1, 0));
          }
        }
      }
    }
    
    CoNLL.log.info("{}{}", System.lineSeparator(), ScoringUtils.scoreAnnotations(annotations, "Extraction"));
  }
  
  public final void scorerNER() {
    // Precision = TP / (TP + FP)
    // Rappel = TP / (TP + FN)
    // F1 = 2 . (P . R) / (P + R)
    // ACC = (TP + TN) / (TP + TN + FP + FN)
    // Triplet<TP, FP, FN>
    if (this.conllContent == null) {
      this.conllContent = this.toCoNLLText();
    }
  
    final String[] lines = this.conllContent.split(System.lineSeparator());
    final Map<String, Triplet<Integer, Integer, Integer>> annotations = new HashMap<>();
    
    for (final String line : lines) {
      final String[] columns = line.split(" ");
      
      if (columns.length == 3 && !"-DOCSTART-".equals(columns[0])) {
        if (columns[1].equals(columns[2]) && !"O".equals(columns[1])) {
          annotations.computeIfPresent(columns[2], (key, val) -> val.setAt0(val.getValue0() + 1));
          annotations.computeIfAbsent(columns[2], x -> Triplet.with(1, 0, 0));
        } else {
          if (!"O".equals(columns[2])) {
            annotations.computeIfPresent(columns[2], (key, val) -> val.setAt2(val.getValue2() + 1));
            annotations.computeIfAbsent(columns[2], x -> Triplet.with(0, 0, 1));
          }

          if (!"O".equals(columns[1])) {
            annotations.computeIfPresent(columns[1], (key, val) -> val.setAt1(val.getValue1() + 1));
            annotations.computeIfAbsent(columns[1], x -> Triplet.with(0, 1, 0));
          }
        }
      }
    }
  
    CoNLL.log.info("{}{}", System.lineSeparator(), ScoringUtils.scoreAnnotations(annotations, "Recognition"));
  }
  
  @SafeVarargs
  private <T> List<List<T>> zip(final List<T>... lists) {
    final List<List<T>> zipped = new ArrayList<>();
    
    for (final List<T> list : lists) {
      for (int i = 0, listSize = list.size(); i < listSize; i++) {
        final List<T> list2;

        if (i >= zipped.size()) {
          zipped.add(list2 = new ArrayList<>());
        } else {
          list2 = zipped.get(i);
        }
        
        list2.add(list.get(i));
      }
    }
    
    return zipped;
  }
}
