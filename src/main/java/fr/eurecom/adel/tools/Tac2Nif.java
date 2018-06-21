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
import fr.eurecom.adel.outputs.IOutput;
import fr.eurecom.adel.outputs.NifOutput;
import fr.eurecom.adel.utils.TacUtils;

/**
 * @author Julien Plu
 */
public class Tac2Nif {
  public final void run(final Path nif, final Path tac, final Path text, final String lang,
                        final AdelConfiguration conf, final String to) throws IOException {
    final TacUtils tacUtils = new TacUtils(text, tac, conf, lang);
    final Map<String, String> sentences = tacUtils.getSentences();
    final Map<String, List<Entity>> gs = tacUtils.getGs();
    final Map<String, Pair<String, List<Entity>>> entries = new ConcurrentHashMap<>();

    sentences.entrySet().parallelStream().forEach(entry -> {
      if (gs.get(entry.getKey()) == null) {
        entries.put(entry.getKey(), new MutablePair<>(entry.getValue(), new ArrayList<>()));
      } else {
        entries.put(entry.getKey(), new MutablePair<>(entry.getValue(), gs.get(
            entry.getKey())));
      }
    });

    final IOutput<Entity> output = new NifOutput(lang, "http://localhost", to);
    final String nifStr = output.write(entries, conf.getIndexProperties());
    
    Files.write(nif, nifStr.getBytes(Charset.forName("UTF-8")), StandardOpenOption.CREATE_NEW,
        StandardOpenOption.WRITE);
  }
}
