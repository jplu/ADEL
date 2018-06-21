package fr.eurecom.adel.configurations;

/**
 * @author Julien Plu
 */
public class DateConfiguration extends AbstractExtractorConfiguration {
  public DateConfiguration() {
    this.setTags("");
  }
  
  @Override
  public final String toString() {
    return "DateConfiguration{"
        + "address='" + this.getAddress() + '\''
        + ", method='" + this.getMethod() + '\''
        + ", className='" + this.getClassName() + '\''
        + ", name='" + this.getName() + '\''
        + '}';
  }
}
