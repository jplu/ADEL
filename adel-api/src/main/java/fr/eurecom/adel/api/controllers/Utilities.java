package fr.eurecom.adel.api.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import springfox.documentation.annotations.ApiIgnore;

/**
 * @author Julien Plu on 2019-03-19.
 */
@ApiIgnore
@RestController
public class Utilities {
  @GetMapping("/")
  public final ModelAndView index(final ModelMap model) {
    return new ModelAndView("redirect:/swagger-ui.html", model);
  }
  
  @GetMapping("/health")
  public final ResponseEntity healthcheck() {
    return new ResponseEntity(HttpStatus.OK);
  }
}
