package fr.eurecom.adel.utils;

import com.submerge.api.subtitle.srt.SRTLine;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import com.vdurmont.emoji.Fitzpatrick;

import org.apache.jena.sparql.pfunction.library.concat;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.reflections.Reflections;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import emoji4j.EmojiUtils;
import fr.eurecom.adel.annotations.Name;
import fr.eurecom.adel.datatypes.Entity;

/**
 * @author Julien Plu
 */
public class StringUtils {
  public static String segment(final String hashtag, final String lang) {
    return HashtagSegmentation.segment(hashtag, lang);
  }
  
  public static String getTextFromSrt(final Set<SRTLine> srt) {
    final StringBuilder sb = new StringBuilder();
    
    srt.forEach(line -> line.getTextLines().forEach(textLine -> sb.append(textLine.replaceAll(
        "<[^>]*>", "")).append("\n")));
    
    return sb.append("\n").toString();
  }
  
  static String cleanText(final String str) {
    final char[] tmpArray = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    final List<Character> alphabet = new ArrayList<>();
    final StringBuilder sb = new StringBuilder();
    
    for (final char c : tmpArray) {
      alphabet.add(c);
    }
    
    for (int i = 0;i < str.length();i++) {
      if (alphabet.contains(Character.toLowerCase(str.charAt(i)))) {
        sb.append(Character.toLowerCase(str.charAt(i)));
      }
    }
    
    return sb.toString();
  }
  
  public static String concat(String s1, String s2) {
    if (s2.toLowerCase().contains(s1.toLowerCase())) {
      return s2;
    }
    
    if (s1.toLowerCase().contains(s2.toLowerCase())) {
      return s1;
    }
    
    int len = Math.min(s1.length(), s2.length());
    int index = -1;
    
    for (int i = len; i > 0; i--) {
      String substring = s2.substring(0, i);
      
      if (s1.toLowerCase().endsWith(substring.toLowerCase())) {
        index = i;
        break;
      }
    }
    
    StringBuilder sb = new StringBuilder(s1);
    
    if (index < 0) {
      sb.append(s2);
    } else if (index <= s2.length()) {
      sb.append(s2.substring(index));
    }
    
    return sb.toString();
  }
  
  public static String setToString(Collection<String> list, String separator) {
    return list.stream().collect(Collectors.joining(separator));
  }
  
  public static Map<String, Integer> stringToMap(String str) {
    Map<String, Integer> result = new HashMap<>();
    
    if (!str.isEmpty()) {
      List<String> pairs = Arrays.asList(str.split(";"));
      
      for (String pair : pairs) {
        String[] tmp = pair.split(" ");
        
        result.put(StringUtils.toDelimitedString(Arrays.copyOfRange(tmp, 0, tmp.length - 1), " "), Integer.parseInt(tmp[tmp.length - 1]));
      }
    }
    
    return result;
  }
  
  public static String getClassNameFromMethod(final String method) {
    final Reflections ref = new Reflections("fr.eurecom.adel");
    String className = "";
    
    for (final Class<?> cl : ref.getTypesAnnotatedWith(Name.class)) {
      final Name name = cl.getAnnotation(Name.class);
      
      if (name.name().equals(method)) {
        className = cl.getCanonicalName();
      }
    }
    
    return className;
  }
  
  public static String toDelimitedString(String[] array, String delimiter) {
    StringBuilder sb = new StringBuilder();
    
    for (String value : array) {
      sb.append(value);
      sb.append(delimiter);
    }
    
    if (sb.toString().isEmpty()) {
      return "";
    }
    
    return sb.substring(0, sb.length() - delimiter.length());
  }
  
  public static String capitalizeFirstCharacterString(final String line) {
    return Character.toUpperCase(line.charAt(0)) + line.substring(1).toLowerCase(
        Locale.ENGLISH);
  }
  
  public static List<String> shrinkStringInTwo(String text) {
    String[] array = text.split(" ");
    List<String> result = new ArrayList<>();
    
    if (array.length == 1) {
      return new ArrayList<>();
    }
    
    StringBuilder sb = new StringBuilder();
    
    for (int i = 0; i < (array.length - 1); i++) {
      sb.append(array[i]);
      sb.append(" ");
    }
    
    result.add(sb.substring(0, sb.length() - 1));
    result.add(array[array.length - 1]);
    
    return result;
  }
  
  /*public static String normalizeString(final String str) {
    String newStr = str;
  
    newStr = StringUtils.replace("\uFE0F", newStr, ".");
    newStr = StringUtils.replace("\uDBB9\uDCF6", newStr, "..");
    
    return newStr;
  }*/
  
  public static String normalizeString(final String str) {
    String newStr = str;
  
    newStr = StringUtils.replace("\uFE0F", newStr, ".");
    newStr = StringUtils.replace(Fitzpatrick.TYPE_1_2.unicode, newStr, ".");
    newStr = StringUtils.replace(Fitzpatrick.TYPE_3.unicode, newStr, ".");
    newStr = StringUtils.replace(Fitzpatrick.TYPE_4.unicode, newStr, ".");
    newStr = StringUtils.replace(Fitzpatrick.TYPE_5.unicode, newStr, ".");
    newStr = StringUtils.replace(Fitzpatrick.TYPE_6.unicode, newStr, ".");
    
    for (final String emoji : EmojiParser.extractEmojis(str)) {
      final StringBuilder sb = new StringBuilder(".");
      
      for (long l = 1L;l < emoji.codePoints().count();l++) {
        sb.append('.');
      }
  
      newStr = newStr.replaceAll(emoji, sb.toString());
    }
    
    return newStr;
  }
  
  public static String replace(final String pattern, final String str, final String replacement) {
    String newStr = str;
    final Pattern regex = Pattern.compile(pattern, Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
    final Matcher matcher = regex.matcher(str);
    
    while (matcher.find()) {
      newStr = matcher.replaceAll(replacement);
    }
    
    return newStr;
  }
  
  public static String buildFilterSparqlQuery(final String tags, final String variableName) {
    String filter = "";
    
    if (!tags.isEmpty()) {
      filter = "FILTER(lcase(STR(?" + variableName +"))=\"" + String.join(
          "\"||lcase(STR(?" + variableName + "))=\"", Arrays.asList(tags.toLowerCase(
              Locale.ENGLISH).split(","))) + "\") .";
    }
    
    return filter;
  }
  
  public static List<Entity> extractAnnotedEntities(final CharSequence text) {
    final List<Entity> entities = new ArrayList<>();
    final Pattern p = Pattern.compile("\\[\\[(.*?)]]");
    final Matcher m = p.matcher(text);
    int count = 1;
    
    while (m.find()) {
      String specialCharacter = "";
      int start = m.start();
      
      if (m.start() != 0 && "@".equals(Character.toString(text.charAt(m.start() - 1)))) {
        specialCharacter = "@";
        start--;
      } else if (m.start() != 0 && "#".equals(Character.toString(text.charAt(m.start() - 1)))) {
        specialCharacter = "#";
        start--;
      }
      
      entities.add(new Entity(specialCharacter + m.group(1), new ArrayList<>(), start - (count - 1)
          * 4, m.end() - count * 4, ""));
      
      count++;
    }
    
    return entities;
  }
  
  public static String twitterQueryUserName(final String account) {
    final String userName;
    
    // For the case where an entity is composed of multiple tokens and starts with a user mention
    // (ex: @nike shox)
    if (account.contains(" ")) {
      userName = account;
    } else {
      try {
        final Document doc = Jsoup.connect("https://twitter.com/" + account).ignoreHttpErrors(true)
            .get();
        final Element title = doc.select("title").first();
        
        userName = title.text().split(" \\(")[0];
      } catch (final IOException ex) {
        throw new WebApplicationException("Issue to read the HTTP connection for " +
            "https://twitter.com/" + account, ex, Response.Status.PRECONDITION_FAILED);
      }
    }
    
    return userName;
  }
  
  public static List<String> combination(final List<String>  elements, final int nb){
    final List<String> res = new ArrayList<>();
    final int[] combination = new int[nb];
    int r = 0;
    int index = 0;
    
    while (r >= 0) {
      if (index <= (elements.size() + (r - nb))) {
        combination[r] = index;
        
        if (r == nb - 1) {
          final StringBuilder output = new StringBuilder();
  
          for (final int aCombination : combination) {
            output.append(elements.get(aCombination));
            output.append(' ');
          }
          
          res.add(output.toString().trim());
          
          index++;
        } else {
          index = combination[r] + 1;
          
          r++;
        }
      } else {
        r--;
        
        if (r > 0) {
          index = combination[r] + 1;
        } else {
          index = combination[0] + 1;
        }
      }
    }
    
    return res;
  }
}
