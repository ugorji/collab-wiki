/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.ugorji.oxygen.web.BaseWebApplicationImpl;
import net.ugorji.oxygen.web.OxygenWebException;
import net.ugorji.oxygen.web.TemplateHandler;
import net.ugorji.oxygen.web.WebAction;
import net.ugorji.oxygen.web.WebConstants;
import net.ugorji.oxygen.web.WebContainerEngine;
import net.ugorji.oxygen.web.WebErrorModel;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.web.WebUserSession;

public class WikiApplication extends BaseWebApplicationImpl {

  public WebInteractionContext newWebInteractionContext(
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    return new WikiWebServletContext(sctx, request, response);
  }

  public WebInteractionContext newWebInteractionContext(
      PortletRequest request, PortletResponse response) throws Exception {
    return new WikiWebPortletContext(pctx, request, response);
  }

  public WebUserSession newWebUserSession() throws Exception {
    return new WikiUserSession();
  }

  public String getRedirectAfterPostSuffix() {
    return engine().getProperty(WikiConstants.SERVLET_MAPPING_PREFIX_KEY, null)
        + engine().getProperty(WikiConstants.REDIRECT_AFTER_POST_SUFFIX_KEY, null);
  }

  public TemplateHandler getTemplateHandler() {
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    return wce.getWikiTemplateHandler();
  }

  public String getEncoding() {
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    return wce.getCharacterEncoding();
  }

  public WebAction getAction(String s) {
    WebAction wa = engine().getAction(s);
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    if (!(wce.isActionSupported(s))) {
      throw new OxygenWebException(
          WebLocal.getI18n().str("general.action_not_supported", new String[] {s, wce.getName()}));
    }
    return wa;
  }

  public void atInitOfRequest() throws Exception {
    super.atInitOfRequest();
    WikiLocal.setWikiEngine(engine());
  }

  public void atStartOfRequest() throws Exception {
    WebErrorModel.setFreemarkerTemplateErrorHandler(true);
    engine().ensureReadyForRequestHandling();

    // now, do the rest of your stuff
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    WikiCategoryEngine wce = wlh.getWikiCategoryEngine(true);
    WikiLocal.setWikiCategoryEngine(wce);

    WikiUserSession wus = WikiLocal.getWikiUserSession();
    wlh.setLocale(wus.getLocale(wce.getName()));

    super.atStartOfRequest();

    WebAction action = getWebActionAtStartOfRequest();
    if (action.isFlagSet(WebAction.FLAG_WRITE_ACTION) && wce.getLongTermLock().isHeld()) {
      throw new OxygenWebException(wce.getLongTermLock().getHoldMessage());
    }
  }

  public void atEndOfRequest() {
    super.atEndOfRequest();
    WikiLocal.setWikiEngine(null);
    WebErrorModel.setFreemarkerTemplateErrorHandler(false);
  }

  public boolean isRetainReferenceToLastViewContext() {
    return true;
  }

  public String getRedirectURL() {
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    return WebLocal.getWebInteractionContext().toURLString(wlh, null);
  }

  /** TBD ... remove all attribute.*, action.*, ... parameters */
  public Map getPortletRenderParameters() throws Exception {
    Map m = new HashMap(WebLocal.getWebInteractionContext().getParameterMap());
    for (Iterator itr = m.entrySet().iterator(); itr.hasNext(); ) {
      Map.Entry me = (Map.Entry) itr.next();
      String mekey = (String) me.getKey();
      if (mekey.startsWith(WebConstants.NON_RENDER_PARAMETER_PREFIX)
          || mekey.startsWith(WikiConstants.REQUEST_PARAM_ATTRIBUTE_PREFIX)
          || mekey.equals(WikiConstants.PARAMETER_TEXT)) {
        itr.remove();
      }
    }
    return m;
  }

  public void shutdown() {
    engine().close();
    sctx = null;
    pctx = null;
  }

  private WikiEngine engine() {
    return (WikiEngine) oce();
  }

  protected WebContainerEngine createWebContainerEngine(Properties p) throws Exception {
    return new WikiEngine(p);
  }

  protected void updatePropertiesForInit(Properties p) {}
}
