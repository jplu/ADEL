package fr.eurecom.adel.api.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.sql.Date;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import javax.servlet.ServletContext;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author Julien Plu on 2019-03-19.
 */
@Configuration
@EnableSwagger2
public class SwaggerDocumentationConfig {
  ApiInfo apiInfo() {
    return new ApiInfoBuilder()
        .title("ADEL API")
        .description("REST API of ADEL. More information on [Github](https://github.com/jplu/ADEL)")
        .license("Apache 2.0")
        .licenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html")
        .termsOfServiceUrl("https://github.com/jplu/ADEL")
        .version("2.0.0")
        .contact(new Contact("Julien Plu", "https://jplu.github.io", ""))
        .build();
  }
  
  @Bean
  public Docket apiDocket(ServletContext servletContext, @Value("${openapi.ADEL.base-path:/}") String basePath) {
    return new Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(RequestHandlerSelectors.basePackage("fr.eurecom.adel.api"))
        .build()
        .pathProvider(new BasePathAwareRelativePathProvider(servletContext, basePath))
        .apiInfo(this.apiInfo());
  }
  
}
