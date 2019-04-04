package fr.eurecom.adel.config.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * @author Julien Plu on 2019-03-21.
 */
@EnableConfigServer
@SpringBootApplication
public class ADELConfigServer {
  public static void main(final String... args) {
    SpringApplication.run(ADELConfigServer.class, args);
  }
}
