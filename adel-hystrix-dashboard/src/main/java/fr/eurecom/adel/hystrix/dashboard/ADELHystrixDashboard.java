package fr.eurecom.adel.hystrix.dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Julien Plu on 2019-04-02.
 */
@SpringBootApplication
@EnableHystrixDashboard
@Controller
public class ADELHystrixDashboard {
  public static void main(final String... args) {
    SpringApplication.run(ADELHystrixDashboard.class, args);
  }
  
  @RequestMapping("/")
  public String home() {
    return "forward:/hystrix";
  }
}
