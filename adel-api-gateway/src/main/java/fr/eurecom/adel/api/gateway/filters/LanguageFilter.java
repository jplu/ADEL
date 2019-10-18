package fr.eurecom.adel.api.gateway.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import com.github.pemistahl.lingua.api.LanguageDetector;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Julien Plu on 2019-04-01.
 */
public class LanguageFilter extends ZuulFilter {
  private static Logger log = LoggerFactory.getLogger(LanguageFilter.class);
  private final LanguageDetector detector;
  // private static final Pattern TEXT = Pattern.compile("\\\"text\\\":.?\\\"(.*)\\\"");

  public LanguageFilter() {
    this.detector = LanguageDetectorBuilder.fromIsoCodes("en", "fr").build();
  }
  
  @Override
  public final String filterType() {
    return FilterConstants.PRE_TYPE;
  }
  
  @Override
  public final int filterOrder() {
    return FilterConstants.PRE_DECORATION_FILTER_ORDER - 1;
  }
  
  @Override
  public final boolean shouldFilter() {
    return true;
  }
  
  @Override
  public final Object run() throws ZuulException {
    final RequestContext ctx = RequestContext.getCurrentContext();
    final HttpServletRequest req = ctx.getRequest();
    String body = "";
    
    try {
      body = req.getReader().lines().collect(Collectors.joining());
    } catch (final IOException ex) {
      throw new ZuulException(ex, 500, ex.getMessage());
    }
  
    final JSONObject json = new JSONObject(body);
    
    String txt = "";
    
    try {
      txt = json.getString("text");
    } catch (final JSONException ex) {
      throw new ZuulException("The body of the request in not properly formed", 500, "The body" +
          "has to be a JSON with a \"text\" property");
    }
    
    final String language = this.detector.detectLanguageOf(txt).getIsoCode();
    
    req.setAttribute(WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE, req.getRequestURI().replace("adel",
        "adel-" + language));
    
    return null;
  }
}
