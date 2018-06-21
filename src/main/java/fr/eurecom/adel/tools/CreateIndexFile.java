package fr.eurecom.adel.tools;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.util.FileManager;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import fr.eurecom.adel.utils.FileUtils;

/**
 * @author Julien Plu
 */
public class CreateIndexFile {
  public final void run(final Iterable<Path> files, final String datasetName, final String output)
      throws IOException, CompressorException {
    final Path store = Files.createTempDirectory("tdb");
    final Dataset dataset = this.createStorage(files, store);
    
    this.query(dataset, datasetName, output);
    
    FileUtils.deleteFolder(store);
    FileUtils.compressArchive(output, output + ".gz", CompressorStreamFactory.GZIP);
    
    Files.delete(Paths.get(output));
  }
  
  private void query(final Dataset dataset, final String datasetName, final String output) throws
      IOException {
    final String queryString = new String(Files.readAllBytes(Paths.get("queries" +
        FileSystems.getDefault().getSeparator() + "sparql" +
        FileSystems.getDefault().getSeparator() + datasetName + ".qry")),
        Charset.forName("UTF-8"));
    /*final String countQueryString = queryString.replaceAll("\\?p \\(GROUP_CONCAT\\(DISTINCT " +
        "\\?o;separator=\"-----\"\\) AS \\?vals\\) \\?id \\?pr \\?link",
        "(STR(COUNT(DISTINCT ?link)) AS ?count)").replaceAll(" LIMIT 1 OFFSET %offset", "");*/
    final String countQueryString = queryString.replaceAll("\\?p \\(GROUP_CONCAT\\(DISTINCT " +
            "\\?o;separator=\"-----\"\\) AS \\?vals\\) \\?id \\?pr \\?link",
        "?count").replaceAll(" LIMIT 1 OFFSET %offset", "").replaceAll("\\?link \\(STR\\(\\?o3\\)" +
        " AS \\?id\\) \\(STR\\(\\?o2\\) AS \\?pr\\)", "(STR(COUNT(DISTINCT ?link)) AS " +
        "?count)").replaceAll(" GROUP BY \\?p \\?id \\?pr \\?link", "");
    int count = 0;
  
    try (final QueryExecution qexec = QueryExecutionFactory.create(countQueryString, dataset)) {
      final ResultSet results = qexec.execSelect();
  
      while (results.hasNext()) {
        final QuerySolution soln = results.nextSolution();
        
        count = Integer.parseInt(soln.get("count").toString());
      }
    }

    dataset.begin(ReadWrite.READ);
  
    /*try (final BufferedWriter writer = Files.newBufferedWriter(Paths.get(output), Charset.forName(
        "UTF-8"))) {*/
      System.out.println("Start querying");
      
      final Map<String, Map<String, Object>> indexEntries = new HashMap<>();
      int offset = 0;
      int linkNumbers = 0;
      
      while (offset < count) {
        final Query query = QueryFactory.create(queryString.replace("%offset", Integer.toString(
            offset)));
    
        try (final QueryExecution qexec = QueryExecutionFactory.create(query, dataset)) {
          final ResultSet results = qexec.execSelect();
          
          while (results.hasNext()) {
            final QuerySolution soln = results.nextSolution();
            
            if (soln.contains("id")) {
              linkNumbers++;
              
              if (indexEntries.containsKey(soln.get("id").toString())) {
                if (soln.get("vals").toString().contains("-----")) {
                  indexEntries.get(soln.get("id").toString()).put(
                      query.getPrefixMapping().shortForm(soln.get("p").toString()).replace(
                          ":", "_"), Arrays.asList(soln.get("vals").toString().split("-----")));
                } else {
                  indexEntries.get(soln.get("id").toString()).put(
                      query.getPrefixMapping().shortForm(soln.get("p").toString()).replace(
                          ":", "_"), soln.get("vals").toString());
                }
              } else {
                indexEntries.put(soln.get("id").toString(), new HashMap<>());
                indexEntries.get(soln.get("id").toString()).put("link", soln.get(
                    "link").toString());
                indexEntries.get(soln.get("id").toString()).put("pagerank", Float.parseFloat(
                    soln.get("pr").toString()));
  
                if (soln.get("vals").toString().contains("-----")) {
                  indexEntries.get(soln.get("id").toString()).put(
                      query.getPrefixMapping().shortForm(soln.get("p").toString()).replace(
                          ":", "_"), Arrays.asList(soln.get("vals").toString().split("-----")));
                } else {
                  indexEntries.get(soln.get("id").toString()).put(
                      query.getPrefixMapping().shortForm(soln.get("p").toString()).replace(
                          ":", "_"), soln.get("vals").toString());
                }
              }
  
              if (linkNumbers % 1000 == 0) {
                System.out.println(linkNumbers + " links processed");
              }
            } else {
              System.out.println("Wrong offset: " + offset);
            }
          }
        }
        
        /*if (!indexEntries.isEmpty()) {
          writer.write(new ObjectMapper().writeValueAsString(indexEntries) +
              System.lineSeparator());
        }*/
        
        //indexEntries.clear();
  
        
        
        offset++;
      }
  
      System.out.println(count + " links processed");
    //}
    
    dataset.end();
  
    try (final BufferedWriter writer = Files.newBufferedWriter(Paths.get(output), Charset.forName(
        "UTF-8"))) {
      for (final Map.Entry<String, Map<String, Object>> entry : indexEntries.entrySet()) {
        writer.write(new ObjectMapper().writeValueAsString(entry) +
            System.lineSeparator());
      }
    }
  }
  
  private Dataset createStorage(final Iterable<Path> files, final Path store) throws IOException,
      CompressorException {
    final Path tmpDir = Files.createTempDirectory("adel");
    final Dataset dataset = TDBFactory.createDataset(store.toString()) ;
    final Model tdb = dataset.getDefaultModel();
    
    for (final Path file : files) {
      System.out.println("load " + file.getFileName());
      final String uncompressedFile = tmpDir.toString() + '/' + file.getFileName().toString().split(
          "\\.")[0] + '.' + file.getFileName().toString().split("\\.")[1];
      
      FileUtils.uncompressArchive(file.toString(), uncompressedFile, CompressorStreamFactory.BZIP2);
      
      FileManager.get().readModel(tdb, uncompressedFile);
      
      Files.delete(Paths.get(uncompressedFile));
    }
    
    FileUtils.deleteFolder(tmpDir);
    
    return dataset;
  }
}
