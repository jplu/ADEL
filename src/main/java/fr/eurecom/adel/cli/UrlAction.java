package fr.eurecom.adel.cli;

import java.util.Map;
import java.util.regex.Pattern;

import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentAction;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;

/**
 * @author Julien Plu
 */
public class UrlAction implements ArgumentAction {
  @Override
  public final void run(ArgumentParser parser, Argument arg, Map<String, Object> attrs, String flag,
                        Object value) throws ArgumentParserException {
    if (!this.validUrl((CharSequence) value)) {
      throw new ArgumentParserException("The URL is not valid", parser);
    }
    attrs.put(arg.getDest(), value);
  }
  
  @Override
  public void onAttach(Argument arg) {
  }
  
  @Override
  public boolean consumeArgument() {
    return true;
  }
  
  private boolean validUrl(final CharSequence url) {
    boolean res = true;/*
    final Pattern pattern = Pattern.compile("^(?:(?:https?|ftp):\\/\\/)(?:\\S+(?::\\S*)?@)?(?:(?!"
        + "(?:10|127)(?:\\.\\d{1,3}){3})(?!(?:169\\.254|192\\.168)(?:\\.\\d{1,3}){2})(?!172\\."
        + "(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\."
        + "(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:"
        + "(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]-"
        + "*)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,}))\\.?)(?::\\d{2,5})?(?:"
        + "[/?#]\\S*)?$");
    
    if (!pattern.matcher(url).matches()) {
      res = false;
    }*/
    
    return res;
  }
}