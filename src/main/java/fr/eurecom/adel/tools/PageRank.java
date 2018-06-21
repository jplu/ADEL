package fr.eurecom.adel.tools;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;

import edu.uci.ics.jung.algorithms.importance.Ranking;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Hypergraph;

/**
 * @author Julien Plu
 */
public class PageRank {
  private final Map<String, Integer> resources;
  private final Map<Integer, String> resourceIds;
  private final int maxIterations;
  private final AtomicInteger lastResourceId;
  private final double tolerance;
  private final double alpha;
  
  public PageRank() {
    this.resources = new HashMap<>();
    this.resourceIds = new HashMap<>();
    this.lastResourceId = new AtomicInteger();
    this.maxIterations = 100;
    this.tolerance = 0.01;
    this.alpha = 0.15;
  }
  
  public final void run (final Path output, final Path input) throws IOException {
    final Graph<Integer, Integer> graph = new DirectedSparseGraph<>();
  
    this.loadData(input, graph);
  
    final edu.uci.ics.jung.algorithms.scoring.PageRank<Integer, Integer> ranker =
        new edu.uci.ics.jung.algorithms.scoring.PageRank<>(graph, this.alpha);
  
    ranker.setTolerance(this.tolerance);
    ranker.setMaxIterations(this.maxIterations);
    ranker.evaluate();
    
    this.writePageRankResultsToFile(ranker, graph, output);
  
    System.out.println("Dump factor = " + (1.00d - ranker.getAlpha()));
    System.out.println("Max iterations = " + ranker.getMaxIterations());
    System.out.println("Iterations = " + ranker.getIterations());
  }
  
  private void loadData(final Path input, final Graph<Integer, Integer> graph)
      throws IOException {
    int edgeCnt = 0;
    final LineIterator it = FileUtils.lineIterator(input.toFile(), "UTF-8");
  
    while (it.hasNext()) {
      final String line = it.nextLine();
      final String[] tokens = line.split(" ");
  
      if (!tokens[0].equals(tokens[2]) && !line.startsWith("#")) {
        this.resources.putIfAbsent(tokens[0], this.lastResourceId.getAndIncrement());
        this.resourceIds.putIfAbsent(this.resources.get(tokens[0]), tokens[0]);
    
        graph.addVertex(this.resources.get(tokens[0]));
    
        this.resources.putIfAbsent(tokens[2], this.lastResourceId.getAndIncrement());
        this.resourceIds.putIfAbsent(this.resources.get(tokens[2]), tokens[2]);
    
        graph.addVertex(this.resources.get(tokens[2]));
        graph.addEdge(edgeCnt, this.resources.get(tokens[0]), this.resources.get(tokens[2]));
        
        edgeCnt++;
      }
    }
  
    LineIterator.closeQuietly(it);
    
    System.out.println("Edge count: " + graph.getEdgeCount());
    System.out.println("Vertex count: " + graph.getVertexCount());
  }
  
  private void writePageRankResultsToFile(
      final edu.uci.ics.jung.algorithms.scoring.PageRank<Integer,Integer> ranker,
      final Hypergraph<Integer, Integer> graph, final Path output) throws IOException {
  
    final PriorityQueue<Ranking<Integer>> q = new PriorityQueue<>();
    int i = 0;
    
    for (final Integer pmid : graph.getVertices()) {
      q.add(new Ranking<>(i, ranker.getVertexScore(pmid), pmid));
      
      i++;
    }
    
    Ranking<Integer> r;
    final Collection<String> lines = new ArrayList<>();
    
    while ((r = q.poll()) != null) {
      lines.add(this.resourceIds.get(r.getRanked()) +
          " <http://dbpedia.org/ontology/wikiPageRank> \"" + r.rankScore +
      "\"^^<http://www.w3.org/2001/XMLSchema#float> .");
    }
    
    Files.write(output, lines, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
  }
}
