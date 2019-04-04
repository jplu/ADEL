package fr.eurecom.adel.recognition.usecases;

import com.vdurmont.emoji.EmojiParser;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.eurecom.adel.commons.datatypes.TweetEntity;
import fr.eurecom.adel.commons.utils.TweetUtils;
import fr.eurecom.adel.recognition.domain.repositories.HashtagSegmentationRepository;
import fr.eurecom.adel.recognition.domain.repositories.UserMentionDereferencingRepository;

/**
 * @author Julien Plu on 2018-12-09.
 */
public class TweetNormalization {
  private final HashtagSegmentationRepository hashtagSegmentationRepository;
  private final UserMentionDereferencingRepository userMentionDereferencingRepository;
  
  public TweetNormalization(final HashtagSegmentationRepository newHashtagSegmentationRepository, final UserMentionDereferencingRepository newUserMentionDereferencing) {
    this.hashtagSegmentationRepository = newHashtagSegmentationRepository;
    this.userMentionDereferencingRepository = newUserMentionDereferencing;
  }
  
  final Pair<String, List<TweetEntity>> normalize(final String tweet) {
    String normalizedTweet = TweetUtils.removeEmojis(tweet);
    final List<TweetEntity> hashtags = TweetUtils.getHashtags(normalizedTweet);
    final List<TweetEntity> userMentions = TweetUtils.getUserMentions(normalizedTweet);
    
    for (final TweetEntity hashtag : hashtags) {
      hashtag.setCleanPhrase(this.hashtagSegmentationRepository.segment(hashtag.getPhrase().replace("#", "")));
      
      normalizedTweet = normalizedTweet.replace(hashtag.getPhrase(), hashtag.getCleanPhrase());
    }
    
    for (final TweetEntity userMention : userMentions) {
      userMention.setCleanPhrase(this.userMentionDereferencingRepository.dereference(userMention.getPhrase().replace("@", "")));
      
      normalizedTweet = normalizedTweet.replace(userMention.getPhrase(), userMention.getCleanPhrase());
    }
    
    return Pair.of(normalizedTweet, Stream.concat(hashtags.stream(), userMentions.stream()).collect(Collectors.toList()));
  }
}
