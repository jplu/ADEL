package fr.eurecom.adel.configurations;

/**
 * @author Julien Plu
 */
public class GazConfiguration extends AbstractExtractorConfiguration {
  public GazConfiguration() {
    this.setTags("");
  }
  
  @Override
  public final String toString() {
    return "GazConfiguration{"
        + "address='" + this.getAddress() + '\''
        + ", method='" + this.getMethod() + '\''
        + ", className='" + this.getClassName() + '\''
        + ", name='" + this.getName() + '\''
        + '}';
  }
}
