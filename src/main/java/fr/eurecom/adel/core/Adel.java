package fr.eurecom.adel.core;

import com.fasterxml.uuid.Generators;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang.SerializationUtils;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import fr.eurecom.adel.configurations.AdelConfiguration;
import fr.eurecom.adel.configurations.ExtractorConfiguration;
import fr.eurecom.adel.configurations.PosConfiguration;
import fr.eurecom.adel.datatypes.ExtractQuery;
import fr.eurecom.adel.datatypes.Entity;
import fr.eurecom.adel.datatypes.LinkQuery;
import fr.eurecom.adel.datatypes.Query;
import fr.eurecom.adel.outputs.BratOutput;
import fr.eurecom.adel.outputs.ConllOutput;
import fr.eurecom.adel.outputs.IOutput;
import fr.eurecom.adel.outputs.Micropost2014Output;
import fr.eurecom.adel.outputs.NifOutput;
import fr.eurecom.adel.outputs.TacOutput;
import fr.eurecom.adel.outputs.TsvOutput;
import fr.eurecom.adel.tasks.extraction.Extractor;
import fr.eurecom.adel.tasks.extraction.OverlapResolution;
import fr.eurecom.adel.tasks.indexing.Index;
import fr.eurecom.adel.tasks.linking.Linking;
import fr.eurecom.adel.utils.NifUtils;
import fr.eurecom.adel.utils.StringUtils;
import fr.eurecom.adel.utils.TacUtils;

/**
 * @author Julien Plu
 */
public final class Adel {
  private final AdelConfiguration adelConf;
  
  public Adel(final AdelConfiguration conf) {
    this.adelConf = conf;
  }
  
  public String extract(final Query query, final String lang, final String host) {
    final Map<String, Pair<String, List<Entity>>> annotations;
    
    if ("nif".equals(query.getInput())) {
      final NifUtils nifUtils = new NifUtils(query.getContent(), this.adelConf, lang);
      final Map<String, String> sentences = nifUtils.getSentences();
  
      if ((sentences.size() > 1) && "brat".equals(query.getOutput())) {
        throw new WebApplicationException("The BRAT output is not available for more than one " +
            "document", Response.Status.PRECONDITION_FAILED);
      }
      
      annotations = new ConcurrentHashMap<>();
      
      sentences.entrySet().parallelStream().forEach(entry -> {
        final Query newQuery = new ExtractQuery();
  
        newQuery.setContent(entry.getValue());
  
        final List<Entity> entities = OverlapResolution.resolve(this.extract(newQuery, lang),
            this.adelConf, this.adelConf.getExtract().getTo(), Arrays.asList(
                this.adelConf.getExtract().getPriority().split(",")));
  
        OverlapResolution.majorityVote(entities, this.adelConf, this.adelConf.getExtract().getTo());
        
        annotations.put(entry.getKey(), new ImmutablePair<>(entry.getValue(), entities));
      });
    } else if ("tsv".equals(query.getInput())) {
      final List<String> lines = Arrays.asList(query.getContent().split("\n"));
  
      if ((lines.size() > 1) && "brat".equals(query.getOutput())) {
        throw new WebApplicationException("The BRAT output is not available for more than one " +
            "document", Response.Status.PRECONDITION_FAILED);
      }
  
      annotations = new ConcurrentHashMap<>();
  
      lines.parallelStream().forEach(line -> {
        final Query newQuery = new ExtractQuery();
        
        newQuery.setContent(line.split("\t")[1]);
  
        final List<Entity> entities = OverlapResolution.resolve(this.extract(newQuery, lang),
            this.adelConf, this.adelConf.getExtract().getTo(), Arrays.asList(
                this.adelConf.getExtract().getPriority().split(",")));
  
        OverlapResolution.majorityVote(entities, this.adelConf, this.adelConf.getNerd().getTo());
        annotations.put(line.split("\t")[0], new ImmutablePair<>(line.split("\t")[1], entities));
      });
    } else {
      final List<Entity> entities = OverlapResolution.resolve(this.extract(query, lang),
          this.adelConf, this.adelConf.getExtract().getTo(), Arrays.asList(
              this.adelConf.getExtract().getPriority().split(",")));
      //entities.forEach(System.out::println);
      OverlapResolution.majorityVote(entities, this.adelConf, this.adelConf.getExtract().getTo());
      
      annotations = Collections.singletonMap(Generators.randomBasedGenerator().generate()
              .toString(), new ImmutablePair<>(this.adelConf.getText(), entities));
    }
    
    return this.outputExtraction(annotations, query.getOutput(), host, lang);
  }
  
  private List<Entity> extract(final Query query, final String lang) {
    final String text;
    
    if (query.getUrl() != null && !query.getUrl().isEmpty()) {
      try {
        final String tmp = IOUtils.toString(new URL(query.getUrl()),
            Charset.forName("UTF-8"));
  
        text = Jsoup.parse(tmp).text();
      } catch (final IOException ex) {
        throw new WebApplicationException("Fail to connect to the URL: " + query.getUrl(), ex,
            Response.Status.PRECONDITION_FAILED);
      }
    } else if ("html".equals(query.getInput())) {
      text = Jsoup.parse(query.getContent()).text();
    } else {
      text = query.getContent();
    }
    
    this.adelConf.setText(text);
  
    final List<Entity> entities = new ArrayList<>();
    
    Collections.synchronizedCollection(entities);
    
    this.adelConf.getExtract().getExtractors().parallelStream().forEach(conf -> {
      entities.addAll(this.runExtractor(conf, query, lang));
    });
    
    this.processHashtagsUserMentions(entities, lang, false);
    
    return entities;
  }
  
  private void processHashtagsUserMentions(final Collection<Entity> entities, final String lang,
                                           final boolean link) {
    final Collection<Entity> toRemove = new ArrayList<>();
    
    entities.parallelStream().filter(entity -> entity.getPhrase().startsWith("#")).forEach(
        entity -> {
          final Query tmpQuery = new ExtractQuery();
      
          tmpQuery.setContent(StringUtils.segment(entity.getPhrase().substring(1), lang));
      
          final Collection<ExtractorConfiguration> extractors = new ArrayList<>();
      
          if (!this.adelConf.getExtract().getPos().isEmpty()) {
            final PosConfiguration confPos = new PosConfiguration();
            final ExtractorConfiguration tmp =
                this.adelConf.getExtract().getPos().iterator().next();
        
            confPos.setAddress(tmp.getAddress());
            confPos.setTags("NNP,NNPS");
            confPos.setFrom(tmp.getFrom());
            confPos.setClassName(tmp.getClassName());
            confPos.setName(tmp.getName());
            confPos.setProfile(tmp.getProfile());
        
            extractors.add(confPos);
          }
      
          if (!this.adelConf.getExtract().getNer().isEmpty()) {
            extractors.addAll(this.adelConf.getExtract().getNer());
          }
      
          if (!this.adelConf.getExtract().getGaz().isEmpty()) {
            extractors.addAll(this.adelConf.getExtract().getGaz());
          }
      
          final List<Entity> tmp = new ArrayList<>();
      
          if (!extractors.isEmpty()) {
            for (final ExtractorConfiguration configuration : extractors) {
              if (tmp.isEmpty()) {
                tmp.addAll(this.runExtractor(configuration, tmpQuery, lang));
              }
            }
          }
      
          if ((tmp.isEmpty() || tmp.size() > 1) && !link) {
            toRemove.add(entity);
          } else {
            if (!link) {
              entity.setCleanPhrase(tmp.get(0).getPhrase());
            } else {
              entity.setCleanPhrase(entity.getPhrase().substring(1));
            }
        
            entity.setPhrase(entity.getPhrase().substring(1));
        
            if (entity.getType().isEmpty() && !link) {
              entity.setType(tmp.get(0).getType());
            }
          }
        });
  
    entities.parallelStream().filter(entity -> entity.getPhrase().startsWith("@")).forEach(
        entity ->{
          final String userMention = StringUtils.twitterQueryUserName(entity.getPhrase().substring(
              1));
        
          if (userMention.startsWith("Twitter /") && !link) {
            toRemove.add(entity);
          } else {
            if (!link) {
              entity.setCleanPhrase(userMention);
            } else {
              entity.setCleanPhrase(entity.getPhrase().substring(1));
            }
            
            entity.setPhrase(entity.getPhrase().substring(1));
          }
        });
  
    entities.removeAll(toRemove);
  }
  
  private List<Entity> runExtractor(final ExtractorConfiguration conf, final Query query,
                                    final String lang) {
    final List<Entity> entities = new ArrayList<>();
    
    try {
      final Constructor constructor = Class.forName(conf.getClassName()).getConstructor(
          Query.class, ExtractorConfiguration.class, String.class);
      final Extractor extractor = (Extractor) constructor.newInstance(query, conf, lang);
      
      extractor.extract();
      
      entities.addAll(extractor.getEntities());
    } catch (final ClassNotFoundException | NoSuchMethodException | InstantiationException
        | IllegalAccessException | InvocationTargetException ex) {
      throw new WebApplicationException("Issue with the className property", ex,
          Response.Status.PRECONDITION_FAILED);
    }
    
    return entities;
  }
 
  public String link(final LinkQuery query, final String lang, final String host) {
    final Map<String, Pair<String, List<Entity>>> annotations;
  
    if ("nif".equals(query.getInput())) {
      final NifUtils nifUtils = new NifUtils(query.getContent(), this.adelConf, lang);
      final Map<String, String> sentences = nifUtils.getSentences();
      final Map<String, List<Entity>> annotationsPerSentence = nifUtils.getEntitiesPerSentence();
  
      if ((sentences.size() > 1) && "brat".equals(query.getOutput())) {
        throw new WebApplicationException("The BRAT output is not available for more than one " +
            "document", Response.Status.PRECONDITION_FAILED);
      }
      
      annotations = new ConcurrentHashMap<>();
      
      sentences.entrySet().parallelStream().forEach(entry -> {
        this.processHashtagsUserMentions(annotationsPerSentence.getOrDefault(entry.getKey(),
            Collections.emptyList()), lang, true);
  
        if (query.getPerfectCandidateGeneration()) {
          this.perfectCandidateGeneration(annotationsPerSentence.getOrDefault(entry.getKey(),
              Collections.emptyList()), nifUtils.getGs().getOrDefault(entry.getKey(),
              Collections.emptyList()));
        } else if (query.getPerfectLinking()) {
          this.perfectLinking(annotationsPerSentence.getOrDefault(entry.getKey(),
              Collections.emptyList()), nifUtils.getGs().getOrDefault(entry.getKey(),
              Collections.emptyList()));
        } else {
          this.link(annotationsPerSentence.getOrDefault(entry.getKey(), Collections.emptyList()));
        }
  
        OverlapResolution.resolveTypesMappingLink(annotationsPerSentence.getOrDefault(
            entry.getKey(), Collections.emptyList()), this.adelConf);
  
        annotations.put(entry.getKey(), new ImmutablePair<>(entry.getValue(),
            annotationsPerSentence.getOrDefault(entry.getKey(), Collections.emptyList())));
      });
    } else if ("tsv".equals(query.getInput())) {
      final List<String> lines = Arrays.asList(query.getContent().split("\n"));
      final TacUtils tacUtils = ((query.getPerfectLinking() ||
          query.getPerfectCandidateGeneration()) ? new TacUtils(query.getContent(),
          query.getGoldStandard(), this.adelConf, lang) : new TacUtils());
  
      annotations = new ConcurrentHashMap<>();
      
      lines.parallelStream().forEach(line -> {
        final List<Entity> entities = StringUtils.extractAnnotedEntities(line.split("\t")[1]);
        
        this.adelConf.setText(line.split("\t")[1].replaceAll("\\[\\[", "").replaceAll(
            "]]", ""));
  
        this.processHashtagsUserMentions(entities, lang, true);
        
        if (query.getPerfectCandidateGeneration()) {
          this.perfectCandidateGeneration(entities, tacUtils.getGs().getOrDefault(
              line.split("\t")[0], Collections.emptyList()));
        } else if (query.getPerfectLinking()) {
          this.perfectLinking(entities, tacUtils.getGs().getOrDefault(
              line.split("\t")[0], Collections.emptyList()));
        } else {
          this.link(entities);
        }
  
        OverlapResolution.resolveTypesMappingLink(entities, this.adelConf);
        
        annotations.put(line.split("\t")[0], new ImmutablePair<>(this.adelConf.getText(),
            entities));
      });
    } else {
      final List<Entity> entities = StringUtils.extractAnnotedEntities(query.getContent());
      
      this.adelConf.setText(query.getContent().replaceAll("\\[\\[", "").replaceAll(
          "]]", ""));
  
      this.processHashtagsUserMentions(entities, lang, true);
      this.link(entities);
  
      OverlapResolution.resolveTypesMappingLink(entities, this.adelConf);
      
      annotations = Collections.singletonMap(Generators.randomBasedGenerator().generate()
              .toString(), new MutablePair<>(this.adelConf.getText(), entities));
    }
    
    return this.outputLinking(annotations, query.getOutput(), host, "",
        this.adelConf.getLink().getTo());
  }
  
  private void perfectCandidateGeneration(final List<Entity> newEntities,
                                          final Iterable<Entity> goldEntities) {
    final Index index;
    
    try {
      final Constructor constructor = Class.forName("fr.eurecom.adel.tasks.indexing." +
          this.adelConf.getIndex().getIndexType()).getConstructor(AdelConfiguration.class);
    
      index = (Index) constructor.newInstance(this.adelConf);
    } catch (final ClassNotFoundException | NoSuchMethodException | InstantiationException
        | IllegalAccessException | InvocationTargetException ex) {
      throw new WebApplicationException("Issue to instanciate the " +
          this.adelConf.getIndex().getIndexType() + " class", ex,
          Response.Status.PRECONDITION_FAILED);
    }
  
    final List<Entity> syncEntities = Collections.synchronizedList(newEntities);
  
    syncEntities.parallelStream().forEach(index::searchCandidates);
    
    for (final Entity entity : syncEntities) {
      for (final Entity goldEntity : goldEntities) {
        if (entity.getStartPosition().equals(goldEntity.getStartPosition())
            && entity.getEndPosition().equals(goldEntity.getEndPosition())
            && !goldEntity.getBestCandidate().getProperties().get(
                this.adelConf.getIndexProperties().get("link")).get(0).startsWith("NIL")
            && goldEntity.getBestCandidate().getProperties().get(
                this.adelConf.getIndexProperties().get("pagerank")) != null
            && !entity.getCandidates().contains(goldEntity.getBestCandidate())) {
          entity.addCandidate(goldEntity.getBestCandidate());
        }
      }
    }
  
    try {
      final Constructor constructor = Class.forName(this.adelConf.getLink().getClassName())
          .getConstructor();
      final Linking linking = (Linking) constructor.newInstance();
    
      linking.link(syncEntities, this.adelConf);
    } catch (final ClassNotFoundException | NoSuchMethodException | InstantiationException
        | IllegalAccessException | InvocationTargetException ex) {
      throw new WebApplicationException("Issue with the method property", ex,
          Response.Status.PRECONDITION_FAILED);
    }
  
    for (final Entity entity : syncEntities) {
      entity.setBestCandidate(entity.getCandidates().get(0));
      entity.getBestCandidate().setFrom(this.adelConf.getIndex().getFrom());
    }
  }
  
  private void perfectLinking(final List<Entity> newEntities, final Iterable<Entity> goldEntities) {
    final Index index;
    
    try {
      final Constructor constructor = Class.forName("fr.eurecom.adel.tasks.indexing." +
          this.adelConf.getIndex().getIndexType()).getConstructor(AdelConfiguration.class);
    
      index = (Index) constructor.newInstance(this.adelConf);
    } catch (final ClassNotFoundException | NoSuchMethodException | InstantiationException
        | IllegalAccessException | InvocationTargetException ex) {
      throw new WebApplicationException("Issue to instanciate the " +
          this.adelConf.getIndex().getIndexType() + " class", ex,
          Response.Status.PRECONDITION_FAILED);
    }
  
    final List<Entity> syncEntities = Collections.synchronizedList(newEntities);
  
    syncEntities.parallelStream().forEach(index::searchCandidates);
    
    try {
      final Constructor constructor = Class.forName(this.adelConf.getLink().getClassName())
          .getConstructor();
      final Linking linking = (Linking) constructor.newInstance();
    
      linking.link(syncEntities, this.adelConf);
    } catch (final ClassNotFoundException | NoSuchMethodException | InstantiationException
        | IllegalAccessException | InvocationTargetException ex) {
      throw new WebApplicationException("Issue with the method property", ex,
          Response.Status.PRECONDITION_FAILED);
    }
  
    for (final Entity entity : syncEntities) {
      for (final Entity goldEntity : goldEntities) {
        if (entity.getStartPosition().equals(goldEntity.getStartPosition())
            && entity.getEndPosition().equals(goldEntity.getEndPosition())
            && !goldEntity.getBestCandidate().getProperties().get(
                this.adelConf.getIndexProperties().get("link")).get(0).startsWith("NIL")
            && entity.getCandidates().contains(goldEntity.getBestCandidate())) {
          entity.setBestCandidate(goldEntity.getBestCandidate());
          entity.getBestCandidate().setFrom(this.adelConf.getIndex().getFrom());
        }
      }
      
      if (entity.getBestCandidate().getProperties().isEmpty()) {
        entity.setBestCandidate(entity.getCandidates().get(0));
        entity.getBestCandidate().setFrom(this.adelConf.getIndex().getFrom());
      }
    }
  }
  
  private void link(final List<Entity> newEntities) {
    final Index index;
    
    try {
      final Constructor constructor = Class.forName("fr.eurecom.adel.tasks.indexing." +
          this.adelConf.getIndex().getIndexType()).getConstructor(AdelConfiguration.class);
      
      index = (Index) constructor.newInstance(this.adelConf);
    } catch (final ClassNotFoundException | NoSuchMethodException | InstantiationException
          | IllegalAccessException | InvocationTargetException ex) {
        throw new WebApplicationException("Issue to instanciate the " +
            this.adelConf.getIndex().getIndexType() + " class", ex,
            Response.Status.PRECONDITION_FAILED);
    }
    
    final List<Entity> syncEntities = Collections.synchronizedList(newEntities);
  
    syncEntities.parallelStream().forEach(index::searchCandidates);
    
    //System.out.println(syncEntities.get(0).getCandidates().size());
    try {
      final Constructor constructor = Class.forName(this.adelConf.getLink().getClassName())
          .getConstructor();
      final Linking linking = (Linking) constructor.newInstance();
      
      linking.link(syncEntities, this.adelConf);
    } catch (final ClassNotFoundException | NoSuchMethodException | InstantiationException
        | IllegalAccessException | InvocationTargetException ex) {
      throw new WebApplicationException("Issue with the method property", ex,
          Response.Status.PRECONDITION_FAILED);
    }
    
    for (final Entity entity : syncEntities) {
      entity.setBestCandidate(entity.getCandidates().get(0));
      entity.getBestCandidate().setFrom(this.adelConf.getIndex().getFrom());
    }
  }
  
  public String nerd(final Query query, final String lang, final String host) {
    final Map<String, Pair<String, List<Entity>>> annotations;
  
    if ("nif".equals(query.getInput())) {
      final NifUtils nifUtils = new NifUtils(query.getContent(), this.adelConf, lang);
      final Map<String, String> sentences = nifUtils.getSentences();
  
      annotations = new ConcurrentHashMap<>();
      
      if ((sentences.size() > 1) && "brat".equals(query.getOutput())) {
        throw new WebApplicationException("The BRAT output is not available for more than one " +
            "document", Response.Status.PRECONDITION_FAILED);
      }
  
      sentences.entrySet().parallelStream().forEach(entry -> {
        final Query newQuery = new ExtractQuery();
  
        newQuery.setContent(entry.getValue());
  
        final List<Entity> entities = OverlapResolution.resolve(this.extract(newQuery, lang),
            this.adelConf, this.adelConf.getNerd().getTo(), Arrays.asList(
                this.adelConf.getNerd().getPriority().split(",")));
  
        this.link(entities);
        
        OverlapResolution.resolveTypesMappingNerd(entities, this.adelConf);
  
        OverlapResolution.majorityVote(entities, this.adelConf, this.adelConf.getNerd().getTo());
        
        annotations.put(entry.getKey(), new ImmutablePair<>(entry.getValue(),
            entities));
      });
    } else if ("tsv".equals(query.getInput())) {
      final List<String> lines = Arrays.asList(query.getContent().split("\n"));
      
      if ((lines.size() > 1) && "brat".equals(query.getOutput())) {
        throw new WebApplicationException("The BRAT output is not available for more than one " +
            "document", Response.Status.PRECONDITION_FAILED);
      }
  
      annotations = new HashMap<>();
      
      lines.parallelStream().forEach(line -> {
        final Query newQuery = new ExtractQuery();
        
        newQuery.setContent(line.split("\t")[1]);
        
        final List<Entity> entities = OverlapResolution.resolve(this.extract(newQuery, lang),
            this.adelConf, this.adelConf.getNerd().getTo(), Arrays.asList(
                this.adelConf.getNerd().getPriority().split(",")));
        
        this.link(entities);
        
        OverlapResolution.resolveTypesMappingNerd(entities, this.adelConf);
  
        OverlapResolution.majorityVote(entities, this.adelConf, this.adelConf.getNerd().getTo());
        
        annotations.put(line.split("\t")[0], new ImmutablePair<>(line.split("\t")[1], entities));
      });
    } else {
      final List<Entity> entities = OverlapResolution.resolve(this.extract(query, lang),
          this.adelConf, this.adelConf.getNerd().getTo(), Arrays.asList(
              this.adelConf.getNerd().getPriority().split(",")));
      
      this.link(entities);
      
      OverlapResolution.resolveTypesMappingNerd(entities, this.adelConf);
      
      OverlapResolution.majorityVote(entities, this.adelConf, this.adelConf.getNerd().getTo());
  
      annotations = Collections.singletonMap(Generators.randomBasedGenerator().generate()
              .toString(), new MutablePair<>(query.getContent(), entities));
    }
    
    return this.outputLinking(annotations, query.getOutput(), host, lang,
        this.adelConf.getNerd().getTo());
  }
  
  private String outputLinking(final Map<String, Pair<String, List<Entity>>> annotations,
                               final String format, final String host, final String lang,
                               final String to) {
    IOutput<Entity> output = new NifOutput(lang, host, to);
    
    if ("naf".equals(format)) {
      //output = new NAFOutput();
    } else if ("tac".equals(format)) {
      output = new TacOutput();
    } else if ("brat".equals(format)) {
      output = new BratOutput();
    } else if ("neel2014".equals(format)) {
      output = new Micropost2014Output();
    }
  
    return output.write(annotations, this.adelConf.getIndexProperties());
  }
  
  private String outputExtraction(final Map<String, Pair<String, List<Entity>>> annotations,
                                  final String format, final String host, final String lang) {
    IOutput<Entity> output = new NifOutput(lang, host, this.adelConf.getExtract().getTo());
    
    if ("naf".equals(format)) {
      //output = new NAFOutput();
    } else if ("conll".equals(format)) {
      output = new ConllOutput(this.adelConf);
    } else if ("brat".equals(format)) {
      output = new BratOutput();
    } else if ("tsv".equals(format)) {
      output = new TsvOutput();
    }
    
    return output.write(annotations, this.adelConf.getIndexProperties());
  }
  
  @Override
  public String toString() {
    return "Adel{"
        + "adelConf=" + this.adelConf
        + '}';
  }
}
