package fr.eurecom.adel.configurations;

/**
 * @author Julien Plu
 */
public class NumberConfiguration extends AbstractExtractorConfiguration {
  public NumberConfiguration() {
    this.setTags("");
  }
  
  @Override
  public final String toString() {
    return "NumberConfiguration{"
        + "address='" + this.getAddress() + '\''
        + ", method='" + this.getMethod() + '\''
        + ", className='" + this.getClassName() + '\''
        + ", name='" + this.getName() + '\''
        + '}';
  }
}
