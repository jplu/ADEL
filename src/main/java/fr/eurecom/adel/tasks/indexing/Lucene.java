package fr.eurecom.adel.tasks.indexing;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import fr.eurecom.adel.configurations.AdelConfiguration;
import fr.eurecom.adel.datatypes.Candidate;
import fr.eurecom.adel.datatypes.Entity;

/**
 * @author Julien Plu
 */
public class Lucene implements Index {
  private final AdelConfiguration adelConf;
  
  public Lucene(final AdelConfiguration newAdelConf) {
    this.adelConf = newAdelConf;
  }
  
  @Override
  public final void searchCandidates(final Entity newEntity) {
    try {
      final StandardAnalyzer analyzer = new StandardAnalyzer();
      final MultiFieldQueryParser qp = new MultiFieldQueryParser(
          this.adelConf.getIndex().getLucene().getFields().split(","), analyzer);
    
      qp.setDefaultOperator(QueryParser.Operator.AND);
    
      final Query q = qp.parse(QueryParserBase.escape(newEntity.getCleanPhrase().toLowerCase(
          Locale.ENGLISH)));
      final Directory index = FSDirectory.open(Paths.get(
          this.adelConf.getIndex().getLucene().getFolder()));
      final IndexReader reader = DirectoryReader.open(index);
      final IndexSearcher searcher = new IndexSearcher(reader);
      final TopScoreDocCollector collector = TopScoreDocCollector.create(
          this.adelConf.getIndex().getLucene().getSize());
    
      searcher.search(q, collector);
    
      final ScoreDoc[] hits = collector.topDocs().scoreDocs;
  
      final List<Candidate> candidates = new ArrayList<>();
      
      for (final ScoreDoc hit : hits) {
        final int docId = hit.doc;
        final Document doc = searcher.doc(docId);
        final Candidate candidate = new Candidate();
  
        for (final IndexableField field : doc.getFields()) {
          final List<Object> fieldValues = new ArrayList<>(Arrays.asList(field.stringValue().split(
              ";")));
          
          candidate.putProperty(field.name(), fieldValues);
        }
        
        candidate.setFrom(this.adelConf.getIndex().getFrom());
        
        candidates.add(candidate);
      }
      
      newEntity.setCandidates(candidates);
      
      reader.close();
      analyzer.close();
    } catch (final ParseException | IOException ex) {
      throw new WebApplicationException("Issue to read the Lucene index " +
          this.adelConf.getIndex().getLucene().getFolder(), ex,
          Response.Status.PRECONDITION_FAILED);
    }
  }
}
