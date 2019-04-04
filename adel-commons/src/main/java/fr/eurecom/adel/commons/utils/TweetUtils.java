package fr.eurecom.adel.commons.utils;

import com.twitter.twittertext.Extractor;
import com.vdurmont.emoji.EmojiParser;

import java.util.ArrayList;
import java.util.List;

import fr.eurecom.adel.commons.datatypes.TweetEntity;

/**
 * @author Julien Plu on 2019-02-13.
 */
public class TweetUtils {
  public static List<TweetEntity> getHashtags(final String tweet) {
    final Extractor extractor = new Extractor();
    final List<TweetEntity> tweetEntities = new ArrayList<>();
  
    for (final var entity : extractor.extractHashtagsWithIndices(tweet)) {
      final TweetEntity tweetEntity = TweetEntity.builder().phrase('#' + entity.getValue()).startOffset(entity.getStart()).endOffset(entity.getEnd()).build();
    
      tweetEntities.add(tweetEntity);
    }
  
    return tweetEntities;
  }
  
  public static List<TweetEntity> getUserMentions(final String tweet) {
    final Extractor extractor = new Extractor();
    final List<TweetEntity> tweetEntities = new ArrayList<>();
  
    for (final var entity : extractor.extractMentionedScreennamesWithIndices(tweet)) {
      final TweetEntity tweetEntity = TweetEntity.builder().phrase('@' + entity.getValue()).startOffset(entity.getStart()).endOffset(entity.getEnd()).build();
    
      tweetEntities.add(tweetEntity);
    }
  
    return tweetEntities;
  }
  
  public static String removeEmojis(final String tweet) {
    return EmojiParser.replaceAllEmojis(tweet, ".");
  }
}
