package fr.eurecom.adel.configurations;

/**
 * @author Julien Plu
 */
public interface ExtractorConfiguration {
  String getAddress();
  void setAddress(final String newAddress);
  String getMethod();
  void setMethod(final String newMethod);
  String getTags();
  void setTags(final String newTags);
  String getProfile();
  void setProfile(final String newProfile);
  String getFrom();
  void setFrom(final String newName);
  String getName();
  void setName(final String newName);
  String getClassName();
}
