package fr.eurecom.adel.tools;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import fr.eurecom.adel.configurations.AdelConfiguration;
import fr.eurecom.adel.datatypes.Entity;
import fr.eurecom.adel.outputs.ConllOutput;
import fr.eurecom.adel.outputs.IOutput;
import fr.eurecom.adel.utils.TacUtils;

/**
 * @author Julien Plu
 */
public class Tac2Conll {
  public final void run (final Path tac, final Path goldStandard, final Path conll,
                         final String lang, final AdelConfiguration conf) throws IOException {
    final TacUtils nifUtils = new TacUtils(tac, goldStandard, conf, lang);
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
    
    final IOutput<Entity> output = new ConllOutput(conf.getExtract().getTokenize());
    final String conllStr = output.write(entries, conf.getIndexProperties());
    
    Files.write(conll, conllStr.getBytes(Charset.forName("UTF-8")), StandardOpenOption.CREATE_NEW,
        StandardOpenOption.WRITE);
  }
}
