package fr.eurecom.adel.configurations;

/**
 * @author Julien Plu
 */
public class NerConfiguration extends AbstractExtractorConfiguration {
  public NerConfiguration() {
    this.setTags("");
  }
  
  @Override
  public final String toString() {
    return "NerConfiguration{"
        + "address='" + this.getAddress() + '\''
        + ", method='" + this.getMethod() + '\''
        + ", className='" + this.getClassName() + '\''
        + ", name='" + this.getName() + '\''
        + ", tags=" + this.getTags()
        + '}';
  }
}
