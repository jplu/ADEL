package fr.eurecom.adel.shell.command;

import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.eurecom.adel.commons.datatypes.Document;
import fr.eurecom.adel.commons.datatypes.Entity;
import fr.eurecom.adel.commons.exceptions.CoNLLMalformedException;
import fr.eurecom.adel.commons.exceptions.NIFMalformedException;
import fr.eurecom.adel.commons.exceptions.TACMalformedException;
import fr.eurecom.adel.commons.formats.CoNLL;
import fr.eurecom.adel.commons.formats.NIF;
import fr.eurecom.adel.commons.formats.TAC;
import fr.eurecom.adel.commons.validators.File;
import fr.eurecom.adel.commons.validators.MustExists;
import fr.eurecom.adel.commons.validators.OneOf;
import fr.eurecom.adel.commons.validators.Readable;
import fr.eurecom.adel.commons.validators.Writable;
import fr.eurecom.adel.recognition.exceptions.MappingNotExistsException;
import fr.eurecom.adel.recognition.exceptions.TypeNotExistsException;
import fr.eurecom.adel.recognition.usecases.RecognitionPipeline;

@ShellComponent
public class ADELCommands {
  private RecognitionPipeline pipeline;
  
  @Autowired
  public final void setPipeline(final RecognitionPipeline newPipeline) {
    this.pipeline = newPipeline;
  }
  
  @ShellMethod("Recognize entities from an input text or an input text file")
  public final String ner(@ShellOption(defaultValue = "", help = "Text to process") final String text,
                          @ShellOption(defaultValue = "", help = "Text file to process") @File @MustExists @Readable final String inputFile,
                          @ShellOption(defaultValue = "CoNLL") final @OneOf({"CoNLL", "NIF", "TAC"}) String format,
                          @ShellOption(defaultValue = "") @File @Writable final String outputFile,
                          @ShellOption(defaultValue = "", help = "Contains the text for the TAC formats") @File @Writable final String outputText,
                          @ShellOption(help = "Delete the file represented by --output-file", arity = 0) final boolean force,
                          @ShellOption(help = "Print the result", arity = 0) final boolean print,
                          @ShellOption(help = "Write the output of each annotator in a file", arity = 0) final boolean allAnnotators) throws IOException, MappingNotExistsException, TypeNotExistsException {
    if (text.isEmpty() && inputFile.isEmpty()) {
      return "You have to give a text or an input file";
    }
    
    if (outputFile.isEmpty() && !print) {
      return "You have to use the --print and/or --output-file options";
    }
    
    if ("TAC".equals(format) && !outputFile.isEmpty() && outputText.isEmpty()) {
      return "You have to give an output text file with the option --output-text";
    }
    
    if (!outputFile.isEmpty() && Files.exists(Paths.get(outputFile)) && !force) {
      return "The file " + outputFile + " already exists, change the name or use the --force option to delete it";
    }
  
    if (!outputText.isEmpty() && Files.exists(Paths.get(outputText)) && !force) {
      return "The file " + outputText + " already exists, change the name or use the --force option to delete it";
    }
    
    if (!outputFile.isEmpty()) {
      Files.deleteIfExists(Paths.get(outputFile));
    }
    
    if (!outputText.isEmpty()) {
      Files.deleteIfExists(Paths.get(outputText));
    }
    
    String input = text;
    
    if (!inputFile.isEmpty()) {
      input = String.join(" ", Files.readAllLines(Paths.get(inputFile)));
    }
    
    final Map<String, Document> document = this.pipeline.run(input);
    
    if ("CoNLL".equals(format)) {
      if (allAnnotators && document.size() > 1) {
        for (final Map.Entry<String, Document> entry : document.entrySet()) {
          if (!"adel".equals(entry.getKey())) {
            final String name = entry.getKey() + ".conll";
            final CoNLL conllOutput = new CoNLL(name);

            Files.deleteIfExists(Paths.get(name));
            
            conllOutput.addDocument(entry.getValue());
            conllOutput.write(print);
          }
        }
      } else {
        CoNLL conll = new CoNLL();
        
        if (!outputFile.isEmpty()) {
          conll = new CoNLL(outputFile);
        }
  
        conll.addDocument(document.get("adel"));
        
        conll.write(print);
      }
    } else if ("NIF".equals(format)) {
      if (allAnnotators && 1 < document.size()) {
        for (final Map.Entry<String, Document> entry : document.entrySet()) {
          if (!"adel".equals(entry.getKey())) {
            final String name = entry.getKey() + ".nif";
            final NIF nif = new NIF(name);
            
            Files.deleteIfExists(Paths.get(name));
  
            nif.addDocument(entry.getValue());
            nif.write(print);
          }
        }
      } else {
        NIF nif = new NIF();
        
        if (!outputFile.isEmpty()) {
          nif = new NIF(outputFile);
        }
  
        nif.addDocument(document.get("adel"));
  
        nif.write(print);
      }
    } else {
      if (allAnnotators && 1 < document.size()) {
        for (final Map.Entry<String, Document> entry : document.entrySet()) {
          if (!"adel".equals(entry.getKey())) {
            final String nameAnnotations = entry.getKey() + ".tac";
            final String nameText = entry.getKey() + ".tac";
            final TAC tac = new TAC(nameText, nameAnnotations);
      
            Files.deleteIfExists(Paths.get(nameAnnotations));
            Files.deleteIfExists(Paths.get(nameText));
      
            tac.addDocument(entry.getValue());
            tac.write(print);
          }
        }
      } else {
        TAC tac = new TAC();
  
        if (!outputFile.isEmpty() && !outputText.isEmpty()) {
          tac = new TAC(outputText, outputFile);
        }
  
        tac.addDocument(document.get("adel"));
  
        tac.write(print);
      }
    }
    
    return "";
  }
  
  @ShellMethod("Test the recognition over a dataset")
  public final String nerTest(@ShellOption @MustExists @File @Readable final String inputFile,
                              @ShellOption(defaultValue = "", help = "Gold standard file for a TAC/NIF dataset") @MustExists @File @Readable final String gold,
                              @ShellOption @Writable final String outputFile,
                              @ShellOption(help = "Delete the file represented by --output-file", arity = 0) final boolean force,
                              @ShellOption(defaultValue = "CoNLL") final @OneOf({"CoNLL", "CoNLL0203", "NIF", "TAC"}) String format,
                              @ShellOption(arity = 0) final boolean print) throws IOException {
    if (Files.exists(Paths.get(outputFile)) && !force) {
      return "The file " + outputFile + " already exists, change the name or use the --force option to delete it";
    }
    
    if (("TAC".equals(format) || "NIF".equals(format)) && gold.isEmpty()) {
      return "The gold standard file for the TAC/NIF dataset is missing. You must provide it with the option --gold";
    }
    
    Files.deleteIfExists(Paths.get(outputFile));
    
    if ("CoNLL".equals(format) || "CoNLL0203".equals(format)) {
      final CoNLL conll;
      
      try {
        if ("CoNLL0203".equals(format)) {
          conll = new CoNLL(inputFile, outputFile, true);
        } else {
          conll = new CoNLL(inputFile, outputFile, false);
        }
      } catch (final CoNLLMalformedException ex) {
        return ex.getMessage();
      }
      
      conll.documents().parallelStream().forEach(doc -> {
        try {
          conll.addDocument(this.pipeline.run(doc).get("adel"));
        } catch (final MappingNotExistsException | TypeNotExistsException e) {
          throw new RuntimeException(e);
        }
      });
      conll.write(print);
      conll.scorerExtraction();
      conll.scorerNER();
    } else if ("NIF".equals(format)) {
      try {
        final NIF nif = new NIF(inputFile, outputFile);
        
        nif.documents().parallelStream().forEach(doc -> {
          try {
            nif.addDocument(this.pipeline.run(doc).get("adel"));
          } catch (final MappingNotExistsException | TypeNotExistsException e) {
            throw new RuntimeException(e);
          }
        });
        nif.write(print);
        nif.scorerExtraction("", gold);
        nif.scorerNER("", gold);
      } catch (final NIFMalformedException ex) {
        return ex.getMessage();
      }
    } else {
      try {
        final TAC tac = new TAC(inputFile);
        
        tac.documents().parallelStream().forEach(doc -> {
          try {
            tac.addDocument(this.pipeline.run(doc).get("adel"));
          } catch (final MappingNotExistsException | TypeNotExistsException e) {
            throw new RuntimeException(e);
          }
        });
        tac.setAnnotationsOutput(outputFile);
        tac.write(print);
        tac.scorerExtraction("", gold);
        tac.scorerNER("", gold);
      } catch (final TACMalformedException ex) {
        return ex.getMessage();
      }
    }
    return "";
  }
  
  @ShellMethod("Score a NER output")
  public final String nerScore(@ShellOption @MustExists @File @Readable final String inputFile,
                               @ShellOption(defaultValue = "") @MustExists @File @Readable final String gold,
                               @ShellOption(defaultValue = "CoNLL") final @OneOf({"CoNLL", "NIF", "TAC"}) String format) throws IOException {
    if (!"CoNLL".equals(format) && gold.isEmpty()) {
      return "You have to provide a gold file with the option --gold";
    }
    
    if ("CoNLL".equals(format)) {
      try {
        final CoNLL conll = new CoNLL(inputFile, true, false);
        
        conll.scorerExtraction();
        conll.scorerNER();
      } catch (final CoNLLMalformedException ex) {
        return ex.getMessage();
      }
    } else if ("NIF".equals(format)) {
      final NIF nif = new NIF();
      
      try {
        nif.scorerExtraction(inputFile, gold);
        nif.scorerNER(inputFile, gold);
      } catch (final NIFMalformedException ex) {
        return ex.getMessage();
      }
    } else {
      final TAC tac = new TAC();
      
      try {
        tac.scorerExtraction(inputFile, gold);
        tac.scorerNER(inputFile, gold);
      } catch (final TACMalformedException ex) {
        return ex.getMessage();
      }
    }
    
    return "";
  }
  
  @ShellMethod("Convert a dataset from a formats to another")
  public final String convert(@ShellOption final @OneOf({"NIF", "TAC"}) String from,
                              @ShellOption final @OneOf({"CoNLL", "NIF", "TAC"}) String to,
                              @ShellOption @MustExists @File @Readable final String inputAnnotations) throws IOException {
    if (from.equals(to)) {
      return "The options --from and --to cannot be the same formats";
    }
    
    Map<String, Pair<String, List<Entity>>> nifTacAnnotations = new HashMap<>();
    final String fileName = com.google.common.io.Files.getNameWithoutExtension(inputAnnotations);
    
    if ("NIF".equals(from)) {
     final NIF nif = new NIF();
     
     try {
      nif.setInputFile(inputAnnotations);
     } catch (final NIFMalformedException ex) {
       return ex.getMessage();
     }
     
     nifTacAnnotations = nif.goldAnnotations();
    } else if ("TAC".equals(from)) {
      final TAC tac = new TAC();
      final String textInput = fileName + ".txt";
      
      if (!Files.exists(Paths.get(textInput))) {
        return "The text file " + fileName + ".txt does not exists";
      }
      
      try {
        tac.setAnnotationsTextInput(textInput, inputAnnotations);
      } catch (final TACMalformedException ex) {
        return ex.getMessage();
      }
      
      nifTacAnnotations = tac.goldAnnotations();
    }
    
    if ("TAC".equals(to)) {
        final TAC tac = new TAC(fileName + ".txt", fileName + ".tac");
        
        tac.setAnnotations(nifTacAnnotations);
        tac.write(false);
    } else if ("NIF".equals(to)) {
      final NIF nif = new NIF(fileName + ".nif");
      
      nif.setAnnotations(nifTacAnnotations);
      nif.write(false);
    } else {
      final CoNLL conll = new CoNLL(fileName + ".conll");
      
      for (final Map.Entry<String, Pair<String, List<Entity>>> entry : nifTacAnnotations.entrySet()) {
        conll.addDocument(Document.builder().text(entry.getKey()).entities(entry.getValue().getValue1()).tokens(Collections.emptyList()).build());
      }
      
      conll.write(false);
    }
    
    return "";
  }
}
