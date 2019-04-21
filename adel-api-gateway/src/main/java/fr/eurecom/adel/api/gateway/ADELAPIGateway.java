package fr.eurecom.adel.api.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;

import fr.eurecom.adel.api.gateway.filters.LanguageFilter;

/**
 * @author Julien Plu on 2019-03-27.
 */
@EnableZuulProxy
@EnableDiscoveryClient
@SpringBootApplication
public class ADELAPIGateway {
  public static void main(final String... args) {
    SpringApplication.run(ADELAPIGateway.class, args);
  }
  
  @Bean
  public LanguageFilter languageFilter() {
    return new LanguageFilter();
  }
}
