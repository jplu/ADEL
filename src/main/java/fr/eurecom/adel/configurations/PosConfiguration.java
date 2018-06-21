package fr.eurecom.adel.configurations;

/**
 * @author Julien Plu
 */
public class PosConfiguration extends AbstractExtractorConfiguration {
  public PosConfiguration() {
    this.setTags("");
  }
  
  @Override
  public final String toString() {
    return "PosConfiguration{"
        + "address='" + this.getAddress() + '\''
        + ", method='" + this.getMethod() + '\''
        + ", className='" + this.getClassName() + '\''
        + ", name='" + this.getName() + '\''
        + ", tags=" + this.getTags()
        + '}';
  }
}
