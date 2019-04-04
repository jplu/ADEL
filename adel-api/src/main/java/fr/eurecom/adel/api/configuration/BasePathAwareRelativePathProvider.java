package fr.eurecom.adel.api.configuration;

import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletContext;

import springfox.documentation.spring.web.paths.Paths;
import springfox.documentation.spring.web.paths.RelativePathProvider;

/**
 * @author Julien Plu on 2019-03-19.
 */
class BasePathAwareRelativePathProvider extends RelativePathProvider {
  private String basePath;
  
  public BasePathAwareRelativePathProvider(ServletContext servletContext, String basePath) {
    super(servletContext);
    this.basePath = basePath;
  }
  
  @Override
  protected String applicationPath() {
    return Paths.removeAdjacentForwardSlashes(UriComponentsBuilder.fromPath(super.applicationPath()).path(this.basePath).build().toString());
  }
  
  @Override
  public String getOperationPath(String operationPath) {
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromPath("/");
    return Paths.removeAdjacentForwardSlashes(
        uriComponentsBuilder.path(operationPath.replaceFirst("^" + this.basePath, "")).build().toString());
  }
}
