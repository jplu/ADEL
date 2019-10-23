package fr.eurecom.adel.commons.formats;

import org.javatuples.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import fr.eurecom.adel.commons.datatypes.Document;
import fr.eurecom.adel.commons.datatypes.Entity;
import fr.eurecom.adel.commons.exceptions.TACMalformedException;
import fr.eurecom.adel.commons.utils.ScoringUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Julien Plu on 2019-03-02.
 */
@Slf4j
public class TAC {
  private Path annotationsInput;
  private Path textsInput;
  private Path annotationsOutput;
  private Path textsOutput;
  private Map<String, Pair<String, List<Entity>>> goldDocuments;
  private Map<String, Pair<String, List<Entity>>> annotatedDocuments;
  
  public TAC(final String newAnnotationsInput, final String newTextsInput, final String newTextsOutput, final String newAnnotationsOutput) throws IOException, TACMalformedException {
    this.annotationsInput = Paths.get(newAnnotationsInput);
    this.textsInput = Paths.get(newTextsInput);
    this.annotationsOutput = Paths.get(newAnnotationsOutput);
    this.textsOutput = Paths.get(newTextsOutput);
    this.goldDocuments = new HashMap<>();
    this.annotatedDocuments = new HashMap<>();
    
    this.read("");
  }
  
  public TAC(final String newTextsOutput, final String newAnnotationsOutput) {
    this.textsOutput = Paths.get(newTextsOutput);
    this.annotationsOutput = Paths.get(newAnnotationsOutput);
    this.goldDocuments = new HashMap<>();
    this.annotatedDocuments = new HashMap<>();
  }
  
  public TAC(final String newTextsInput) throws IOException, TACMalformedException {
    this.textsInput = Paths.get(newTextsInput);
    this.goldDocuments = new HashMap<>();
    this.annotatedDocuments = new HashMap<>();
    
    this.read("");
  }
  
  public TAC() {
    this.goldDocuments = new HashMap<>();
    this.annotatedDocuments = new HashMap<>();
  }
  
  public final void setAnnotationsTextInput(final String newTextInput, final String newAnnotationsInput) throws IOException, TACMalformedException {
    this.textsInput = Paths.get(newTextInput);
    
    this.read(newAnnotationsInput);
  }
  
  public final void setAnnotationsOutput(final String newAnnotationsOutput) {
    this.annotationsOutput = Paths.get(newAnnotationsOutput);
  }
  
  public final Map<String, Pair<String, List<Entity>>> goldAnnotations() {
    return Collections.unmodifiableMap(this.goldDocuments);
  }
  
  public final void setAnnotations(final Map<String, Pair<String, List<Entity>>> annotations) {
    this.annotatedDocuments = new HashMap<>(annotations);
  }
  
  private void read(final String gold) throws IOException, TACMalformedException {
    List<String> annotationsLines = new ArrayList<>();
    
    if (gold.isEmpty() && null != this.annotationsInput) {
      annotationsLines = Files.readAllLines(this.annotationsInput);
    } else if (!gold.isEmpty()) {
      annotationsLines = Files.readAllLines(Paths.get(gold));
    }
    
    final Map<String, String> tmpTexts = new HashMap<>();
    int lineCount = 0;

    if (null != this.textsInput) {
      final List<String> textsLines = Files.readAllLines(this.textsInput);

      for (final String textLine : textsLines) {
        lineCount++;
        final String[] columns = textLine.split("\t");

        if (2 != columns.length) {
          throw new TACMalformedException("Malformed TAC file: the line " + lineCount + " in file " + this.textsInput + " has " + columns.length + " columns instead of 2");
        }

        tmpTexts.put(columns[0], columns[1]);
      }
    }
    
    lineCount = 0;
    
    for (final String annotationLine : annotationsLines) {
      lineCount++;
      final String[] columns = annotationLine.split("\t");
      
      if (6 != columns.length) {
        throw new TACMalformedException("Malformed TAC file: the line " + lineCount + " in file " + this.annotationsInput + " has " + columns.length + " columns instead of 6");
      }
      
      if (!tmpTexts.containsKey(columns[0]) && null != this.textsInput) {
        throw new TACMalformedException("Malformed TAC file: the document ID " + columns[0] + " at line " + lineCount + " in file " + this.annotationsInput + " does not exists in " + this.textsInput);
      }
      
      String mention = "";

      if (!tmpTexts.isEmpty()) {
        mention = tmpTexts.get(columns[0]).substring(Integer.parseInt(columns[1]), Integer.parseInt(columns[2]));
      }
      
      final Entity entity = Entity.builder().phrase(mention)
                                            .cleanPhrase(mention)
                                            .startOffset(Integer.parseInt(columns[1]))
                                            .endOffset(Integer.parseInt(columns[2]))
                                            .type(columns[5]).build();
  
      if (gold.isEmpty()) {
        if (this.annotatedDocuments.containsKey(tmpTexts.get(columns[0]))) {
          this.annotatedDocuments.get(tmpTexts.get(columns[0])).getValue1().add(entity);
        } else {
          this.annotatedDocuments.put(tmpTexts.get(columns[0]), Pair.with(columns[0], new ArrayList<>(Collections.singleton(entity))));
        }
      } else {
        if (this.goldDocuments.containsKey(tmpTexts.get(columns[0]))) {
          this.goldDocuments.get(tmpTexts.get(columns[0])).getValue1().add(entity);
        } else {
          this.goldDocuments.put(tmpTexts.get(columns[0]), Pair.with(columns[0], new ArrayList<>(Collections.singleton(entity))));
        }
      }
    }
    
    if (this.goldDocuments.isEmpty() && !tmpTexts.isEmpty()) {
      for (final Map.Entry<String, String> entry : tmpTexts.entrySet()) {
        this.goldDocuments.put(entry.getValue(), Pair.with(entry.getKey(), new ArrayList<>()));
      }
    }
  }
  
  public final List<String> documents () {
    return new ArrayList<>(this.goldDocuments.keySet());
  }
  
  public final void addDocument(final Document document) {
    final String id;
  
    if (this.goldDocuments.containsKey(document.getText())) {
      id = this.goldDocuments.get(document.getText()).getValue0();
    } else {
      id = UUID.randomUUID().toString();
    }
  
    if (!this.annotatedDocuments.containsKey(document.getText())) {
      this.annotatedDocuments.put(document.getText(), Pair.with(id, document.getEntities()));
    }
  }
  
  public final void write(final boolean print) throws IOException {
    final StringBuilder linesText = new StringBuilder();
    final StringBuilder linesAnnotations = new StringBuilder();
  
    for (final Map.Entry<String, Pair<String, List<Entity>>> entry : this.annotatedDocuments.entrySet()) {
      linesText.append(entry.getValue().getValue0());
      linesText.append('\t');
      linesText.append(entry.getKey());
      linesText.append(System.lineSeparator());
      
      for (final Entity entity : entry.getValue().getValue1()) {
        linesAnnotations.append(entry.getValue().getValue0());
        linesAnnotations.append('\t');
        linesAnnotations.append(entity.getStartOffset());
        linesAnnotations.append('\t');
        linesAnnotations.append(entity.getEndOffset());
        linesAnnotations.append('\t');
        linesAnnotations.append('-');
        linesAnnotations.append('\t');
        linesAnnotations.append("0.0");
        linesAnnotations.append('\t');
        linesAnnotations.append(entity.getType());
        linesAnnotations.append(System.lineSeparator());
      }
    }
    
    if (print) {
      TAC.log.info("{}{}", System.lineSeparator(), linesAnnotations);
      TAC.log.info("{}{}", System.lineSeparator(), linesText);
    }
    
    if (null != this.annotationsOutput) {
      Files.write(this.annotationsOutput, Arrays.asList(linesAnnotations.toString().split(System.lineSeparator())));
    }
  
    if (null != this.textsOutput) {
      Files.write(this.textsOutput, Arrays.asList(linesText.toString().split(System.lineSeparator())));
    }
  }
  
  public final void scorerExtraction(final String inputAnnotationsFile, final String gold) throws IOException, TACMalformedException {
    if (!inputAnnotationsFile.isEmpty()) {
      this.annotationsInput = Paths.get(inputAnnotationsFile);
  
      this.read("");
    }
    
    this.read(gold);
    
    TAC.log.info("{}{}", System.lineSeparator(), ScoringUtils.scoreExtractionNIFAndTAC(this.goldDocuments, this.annotatedDocuments));
  }
  
  public final void scorerNER(final String inputAnnotationsFile, final String gold) throws IOException, TACMalformedException {
    if (!inputAnnotationsFile.isEmpty()) {
      this.annotationsInput = Paths.get(inputAnnotationsFile);
    
      this.read("");
    }
    
    this.read(gold);
    
    TAC.log.info("{}{}", System.lineSeparator(), ScoringUtils.scoreNERNIFAndTAC(this.goldDocuments, this.annotatedDocuments));
  }
}
