package fr.eurecom.adel.configurations;

/**
 * @author Julien Plu
 */
public class CorefConfiguration extends AbstractExtractorConfiguration {
  public CorefConfiguration() {
    this.setTags("");
  }
  
  @Override
  public final String toString() {
    return "CorefConfiguration{"
        + "address='" + this.getAddress() + '\''
        + ", method='" + this.getMethod() + '\''
        + ", className='" + this.getClassName() + '\''
        + ", name='" + this.getName() + '\''
        + ", tags=" + this.getTags()
        + '}';
  }
}
