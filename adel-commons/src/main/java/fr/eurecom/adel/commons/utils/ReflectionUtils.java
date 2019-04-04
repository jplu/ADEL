package fr.eurecom.adel.commons.utils;

import org.reflections.Reflections;

import fr.eurecom.adel.commons.validators.Name;

/**
 * @author Julien Plu on 2019-02-09.
 */
public class ReflectionUtils {
  public static String getClassNameFromMethod(final String method, final String subPackage) {
    final Reflections ref = new Reflections("fr.eurecom.adel.recognition.implementation.repositories." + subPackage);
    String className = "";
    
    for (final Class<?> cl : ref.getTypesAnnotatedWith(Name.class)) {
      final Name name = cl.getAnnotation(Name.class);
      
      if (name.name().equals(method)) {
        className = cl.getCanonicalName();
      }
    }
    
    return className;
  }
}
