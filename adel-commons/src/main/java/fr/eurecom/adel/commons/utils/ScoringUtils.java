package fr.eurecom.adel.commons.utils;

import de.vandermeer.asciitable.AsciiTable;

import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.eurecom.adel.commons.datatypes.Entity;

/**
 * @author Julien Plu on 2019-02-28.
 */
public class ScoringUtils {
  public static String scoreAnnotations(final Map<String, Triplet<Integer, Integer, Integer>> annotations, final String task) {
    final AsciiTable at = new AsciiTable();
  
    at.addRule();
    at.addRow(task, "precision", "recall", "f1-score");
    at.addRule();
  
    int totalTP = 0;
    int totalFP = 0;
    int totalFN = 0;
    double macroPrecision = 0.0;
    double macroRecall = 0.0;
    double macroF1 = 0.0;
  
    for (final Map.Entry<String, Triplet<Integer, Integer, Integer>> entry : annotations.entrySet()) {
      totalTP += entry.getValue().getValue0();
      totalFP += entry.getValue().getValue1();
      totalFN += entry.getValue().getValue2();
    
      final double precision = (entry.getValue().getValue0() / (double) (entry.getValue().getValue0() + entry.getValue().getValue1()));
      final double recall = (entry.getValue().getValue0() / (double) (entry.getValue().getValue0() + entry.getValue().getValue2()));
    
      macroPrecision += precision;
      macroRecall += recall;
    
      final double f1 = 2.0 * (precision * recall) / (precision + recall);
    
      macroF1 += f1;
    
      at.addRow(entry.getKey(), Math.round(precision * 10000.0) /10000.0, Math.round(recall * 10000.0) /10000.0, Math.round(f1*10000.0)/10000.0);
      at.addRule();
    }
  
    final double totalPrecision = (totalTP / (double) (totalTP + totalFP));
    final double totalRecall = (totalTP / (double) (totalTP + totalFN));
    final double totalF1 = 2.0 * (totalPrecision * totalRecall) / (totalPrecision + totalRecall);
  
    at.addRow("micro avg", Math.round(totalPrecision*10000.0)/10000.0, Math.round(totalRecall*10000.0)/10000.0, Math.round(totalF1*10000.0)/10000.0);
    at.addRule();
    at.addRow("macro avg", Math.round((macroPrecision / annotations.size())*10000.0)/10000.0, Math.round((macroRecall / annotations.size())*10000.0)/10000.0, Math.round((macroF1 / annotations.size())*10000.0)/10000.0);
    at.addRule();
  
    return at.render();
  }
  
  public static String scoreExtractionNIFAndTAC(final Map<String, Pair<String, List<Entity>>> initialDocuments, final Map<String, Pair<String, List<Entity>>> annotatedDocuments) {
    final Map<String, Triplet<Integer, Integer, Integer>> annotations = new HashMap<>();
  
    for (final Map.Entry<String, Pair<String, List<Entity>>> entry : initialDocuments.entrySet()) {
      final List<Entity> annotatedEntities = annotatedDocuments.get(entry.getKey()).getValue1();
    
      for (final Entity initialEntity : entry.getValue().getValue1()) {
        boolean found = false;
      
        for (final Entity annotatedEntity : annotatedEntities) {
          if (initialEntity.getStartOffset().equals(annotatedEntity.getStartOffset()) && initialEntity.getEndOffset().equals(annotatedEntity.getEndOffset())) {
            annotations.computeIfPresent("", (key, val) -> val.setAt0(val.getValue0() + 1));
            annotations.computeIfAbsent("", x -> Triplet.with(1, 0, 0));
          
            found = true;
          }
        }
      
        if (!found) {
          annotations.computeIfPresent("", (key, val) -> val.setAt2(val.getValue2() + 1));
          annotations.computeIfAbsent("", x -> Triplet.with(0, 0, 1));
        }
      }
    
      for (final Entity annotatedEntity : annotatedEntities) {
        boolean found = false;
      
        for (final Entity initialEntity : entry.getValue().getValue1()) {
          if (initialEntity.getStartOffset().equals(annotatedEntity.getStartOffset()) && initialEntity.getEndOffset().equals(annotatedEntity.getEndOffset())) {
            found = true;
          }
        }
      
        if (!found) {
          annotations.computeIfPresent("", (key, val) -> val.setAt1(val.getValue1() + 1));
          annotations.computeIfAbsent("", x -> Triplet.with(0, 1, 0));
        }
      }
    }
  
    return ScoringUtils.scoreAnnotations(annotations, "Extraction");
  }
  
  public static String scoreNERNIFAndTAC(final Map<String, Pair<String, List<Entity>>> initialDocuments, final Map<String, Pair<String, List<Entity>>> annotatedDocuments) {
    final Map<String, Triplet<Integer, Integer, Integer>> annotations = new HashMap<>();
  
    for (final Map.Entry<String, Pair<String, List<Entity>>> entry : initialDocuments.entrySet()) {
      final List<Entity> annotatedEntities = annotatedDocuments.get(entry.getKey()).getValue1();
    
      for (final Entity initialEntity : entry.getValue().getValue1()) {
        boolean found = false;
      
        for (final Entity annotatedEntity : annotatedEntities) {
          if (initialEntity.getStartOffset().equals(annotatedEntity.getStartOffset()) && initialEntity.getEndOffset().equals(annotatedEntity.getEndOffset())
              && initialEntity.getType().equals(annotatedEntity.getType())) {
            annotations.computeIfPresent(initialEntity.getType(), (key, val) -> val.setAt0(val.getValue0() + 1));
            annotations.computeIfAbsent(initialEntity.getType(), x -> Triplet.with(1, 0, 0));
          
            found = true;
          }
        }
      
        if (!found) {
          annotations.computeIfPresent(initialEntity.getType(), (key, val) -> val.setAt2(val.getValue2() + 1));
          annotations.computeIfAbsent(initialEntity.getType(), x -> Triplet.with(0, 0, 1));
        }
      }
    
      for (final Entity annotatedEntity : annotatedEntities) {
        boolean found = false;
      
        for (final Entity initialEntity : entry.getValue().getValue1()) {
          if (initialEntity.getStartOffset().equals(annotatedEntity.getStartOffset()) && initialEntity.getEndOffset().equals(annotatedEntity.getEndOffset())
              && initialEntity.getType().equals(annotatedEntity.getType())) {
            found = true;
            break;
          }
        }
      
        if (!found) {
          annotations.computeIfPresent(annotatedEntity.getType(), (key, val) -> val.setAt1(val.getValue1() + 1));
          annotations.computeIfAbsent(annotatedEntity.getType(), x -> Triplet.with(0, 1, 0));
        }
      }
    }
    
    return ScoringUtils.scoreAnnotations(annotations, "Recognition");
  }
}
