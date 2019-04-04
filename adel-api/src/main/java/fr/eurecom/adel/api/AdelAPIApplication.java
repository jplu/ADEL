package fr.eurecom.adel.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Julien Plu on 2019-03-19.
 */
@SpringBootApplication(scanBasePackages = "fr.eurecom.adel")
@EnableDiscoveryClient
public class AdelAPIApplication {
	public static void main(final String... args) {
		SpringApplication.run(AdelAPIApplication.class, args);
	}
	
	@Bean
	public WebMvcConfigurer webConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(final CorsRegistry registry) {
				registry.addMapping("/**")
						.allowedOrigins("*")
						.allowedMethods("*")
						.allowedHeaders("Content-Type");
			}
		};
	}
}
