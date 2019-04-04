package fr.eurecom.adel.admin.server;

import de.codecentric.boot.admin.server.config.EnableAdminServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author Julien Plu on 2019-03-26.
 */
@SpringBootApplication
@EnableAdminServer
@EnableDiscoveryClient
public class ADELAdminServer {
  public static void main(final String... args) {
    SpringApplication.run(ADELAdminServer.class, args);
  }
}
