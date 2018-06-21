package fr.eurecom.adel.outputs;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

/**
 * @author Julien Plu
 */
public interface IOutput<E> {
    String write(Map<String, Pair<String, List<E>>> entries,
                 final Map<String, String> indexProperties);
}
