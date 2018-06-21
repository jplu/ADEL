package fr.eurecom.adel.datatypes;

/**
 * @author Julien Plu
 */
public interface Query {
  String getContent();
  void setContent(final String newContent);
  String getInput();
  void setInput(final String newInput);
  String getOutput();
  void setOutput(final String newOutput);
  String getUrl();
  void setUrl(final String newUrl);
}
