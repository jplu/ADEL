package fr.eurecom.adel.discovery.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @author Julien Plu on 2019-03-21.
 */
@EnableEurekaServer
@SpringBootApplication
public class AdelDiscoveryServer {
  public static void main(String[] args) {
    SpringApplication.run(AdelDiscoveryServer.class, args);
  }
}
