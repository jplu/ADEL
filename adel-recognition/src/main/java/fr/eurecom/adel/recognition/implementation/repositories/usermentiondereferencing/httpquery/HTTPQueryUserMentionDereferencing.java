package fr.eurecom.adel.recognition.implementation.repositories.usermentiondereferencing.httpquery;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.eurecom.adel.recognition.domain.repositories.UserMentionDereferencingRepository;
import fr.eurecom.adel.commons.validators.Name;

/**
 * @author Julien Plu on 2018-12-09.
 */
@Name(name = "HTTPQuery")
public class HTTPQueryUserMentionDereferencing implements UserMentionDereferencingRepository {
  private static final Logger logger = LoggerFactory.getLogger(HTTPQueryUserMentionDereferencing.class);
  
  @Override
  public final String dereference(final String userMention) {
    String userName = "";
    
    try {
      final Document doc = Jsoup.connect("https://twitter.com/" + userMention).ignoreHttpErrors(true).get();
      final Element title = doc.select("title").first();
      
      if ("Twitter / ?".equals(title.text())) {
        userName = userMention;
      } else {
        userName = title.text().split(" \\(")[0];
      }
    } catch (final IOException ex) {
      HTTPQueryUserMentionDereferencing.logger.error("", ex);
    }
  
    return userName;
  }
}
