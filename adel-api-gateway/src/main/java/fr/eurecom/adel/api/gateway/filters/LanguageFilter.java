package fr.eurecom.adel.api.gateway.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.web.util.WebUtils;

/**
 * @author Julien Plu on 2019-04-01.
 */
public class LanguageFilter extends ZuulFilter {
  private static Logger log = LoggerFactory.getLogger(LanguageFilter.class);
  
  @Override
  public String filterType() {
    return FilterConstants.PRE_TYPE;
  }
  
  @Override
  public int filterOrder() {
    return FilterConstants.PRE_DECORATION_FILTER_ORDER - 1;
  }
  
  @Override
  public boolean shouldFilter() {
    return true;
  }
  
  @Override
  public Object run() throws ZuulException {
    RequestContext ctx = RequestContext.getCurrentContext();
    HttpServletRequest req = ctx.getRequest();
    
    req.setAttribute(WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE, req.getRequestURI().replace("adel",
        "adel-en"));
    
    return null;
  }
}
