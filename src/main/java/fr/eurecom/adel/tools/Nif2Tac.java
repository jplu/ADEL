package fr.eurecom.adel.tools;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import fr.eurecom.adel.configurations.AdelConfiguration;
import fr.eurecom.adel.datatypes.Entity;
import fr.eurecom.adel.outputs.IOutput;
import fr.eurecom.adel.outputs.TacOutput;
import fr.eurecom.adel.utils.NifUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Julien Plu
 */
public class Nif2Tac {
  public final void run(final Path nif, final Path tac, final Path text, final String lang,
                  final AdelConfiguration conf) throws IOException {
    final NifUtils nifUtils = new NifUtils(nif, conf, lang);
    final Map<String, String> sentences = nifUtils.getSentences();
    final Map<String, List<Entity>> gs = nifUtils.getGs();
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
    
    for (final Map.Entry<String, String> entry : sentences.entrySet()) {
      Files.write(text, (entry.getKey() + '\t' + entry.getValue() +
          System.lineSeparator()).getBytes(Charset.forName("UTF-8")), StandardOpenOption.CREATE,
          StandardOpenOption.APPEND);
    }
  }
}
