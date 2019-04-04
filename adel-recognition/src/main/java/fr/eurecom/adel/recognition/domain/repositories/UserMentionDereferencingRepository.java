package fr.eurecom.adel.recognition.domain.repositories;

/**
 * @author Julien Plu on 2018-12-09.
 */
public interface UserMentionDereferencingRepository {
  String dereference(String userMention);
}
