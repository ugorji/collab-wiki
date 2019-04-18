/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletURL;
import javax.portlet.RenderResponse;
import net.ugorji.oxygen.util.StringUtils;
import net.ugorji.oxygen.web.PortletInteractionContext;
import net.ugorji.oxygen.web.ViewContext;
import net.ugorji.oxygen.web.WebAction;
import net.ugorji.oxygen.web.WebConstants;

public class WikiWebPortletContext extends PortletInteractionContext {
  private WikiLinkHolder wlh;

  protected Set actionsRequiringServletURL;
  // protected String servletContextPath;

  public WikiWebPortletContext(
      PortletContext pctx0, PortletRequest request0, PortletResponse response0) throws Exception {
    super(pctx0, request0, response0);
  }

  public String getUserName() throws Exception {
    String username = super.getUserName();
    if (StringUtils.isBlank(username)) {
      username =
          toWikiLinkHolder()
              .getWikiCategoryEngine(true)
              .getProperty(WikiConstants.DEFAULT_USER_KEY);
    }
    return username;
  }

  public ViewContext toViewContext() throws Exception {
    return toWikiLinkHolder();
  }

  public String toURLString(ViewContext v, Map extraparams) {
    return getURL((WikiLinkHolder) v, extraparams);
  }

  // use request parameters
  public WikiLinkHolder toWikiLinkHolder() throws Exception {
    if (wlh == null) {
      String category = getParameter(WikiConstants.REQUEST_PARAM_CATEGORY_KEY);
      String action = getParameter(WikiConstants.REQUEST_PARAM_ACTION_KEY);
      String pagerep = getParameter(WikiConstants.REQUEST_PARAM_PAGE_KEY);
      if (!StringUtils.isBlank(pagerep)) {
        pagerep = WikiUtils.toUsablePageRep(pagerep);
      }
      int version = -1;
      try {
        version = Integer.parseInt(getParameter(WikiConstants.REQUEST_PARAM_VER_KEY));
      } catch (NumberFormatException nfe) {
      }
      String anchor = null;
      String extrainfo = getParameter(WikiConstants.REQUEST_PARAM_EXTRAINFO_KEY);

      WikiLinkHolder lh = new WikiLinkHolder();
      WikiEngine we = WikiLocal.getWikiEngine();
      lh.setCategory(category);
      lh.setWikiPage(pagerep);
      lh.setAction(action);
      lh.setCategory(category);
      lh.setContextPage(pagerep);
      lh.setVersion(version);
      lh.setAnchor(anchor);
      lh.setExtrainfo(extrainfo);

      wlh = lh;
    }

    return wlh;
  }

  // use request parameters, create an action url, ...
  public String getURL(WikiLinkHolder lh, Map extraparams) {
    WebAction action = WikiLocal.getWikiEngine().getAction(lh.getAction());
    if (action.isFlagSet(WikiWebAction.FLAG_NOT_HANDLED_BY_PORTLET)) {
      return getServletURL(lh, extraparams);
    } else {
      return getPortletURL(lh, extraparams);
    }
  }

  protected String getServletURL(WikiLinkHolder lh, Map extraparams) {
    return getBaseURL() + WikiWebServletContext.getServletURL(lh, extraparams);
  }

  protected String getPortletURL(WikiLinkHolder lh, Map extraparams) {
    // TODO(2019-04-11): may need to be fixed, to ensure that baseurl is prepended
    String s = null;
    WikiEngine engine = WikiLocal.getWikiEngine();
    WikiCategoryEngine wce = lh.getWikiCategoryEngine(false);
    PortletURL purl = ((RenderResponse) response).createActionURL();

    if (extraparams != null && extraparams.size() > 0) {
      for (Iterator itr = extraparams.entrySet().iterator(); itr.hasNext(); ) {
        Map.Entry ent = (Map.Entry) itr.next();
        String key = (String) ent.getKey();
        if (WikiConstants.REQUEST_PARAM_VER_KEY.equals(key)) {
          continue;
        }
        Object val = ent.getValue();
        if (val != null) {
          if (val instanceof String) {
            purl.setParameter(key, (String) val);
          } else if (val instanceof String[]) {
            purl.setParameter(key, (String[]) val);
          }
        }
      }
    }

    purl.setParameter(WikiConstants.REQUEST_PARAM_CATEGORY_KEY, lh.getCategory());
    purl.setParameter(WikiConstants.REQUEST_PARAM_ACTION_KEY, lh.getAction());
    WebAction wwaction = lh.getWikiWebAction();
    boolean includePage = wwaction.isFlagSet(WikiWebAction.FLAG_REQUIRES_PAGENAME);
    if (includePage) {
      if (lh.getWikiPage() != null) {
        purl.setParameter(WikiConstants.REQUEST_PARAM_PAGE_KEY, lh.getWikiPage());
      } else {
        s = ((wce == null) ? WikiConstants.DEFAULT_ENTRY_PAGE : wce.getEntryPage());
        purl.setParameter(WikiConstants.REQUEST_PARAM_PAGE_KEY, s);
      }
      if (wce != null && wwaction.isFlagSet(WikiWebAction.FLAG_REQUIRES_EXTRAINFO)) {
        if (lh.getExtrainfo() != null) {
          purl.setParameter(WikiConstants.REQUEST_PARAM_EXTRAINFO_KEY, lh.getExtrainfo());
        }
      }
    }

    int myver = lh.getVersion();
    if (myver >= 0 && wwaction.isFlagSet(WikiWebAction.FLAG_HONORS_VERSION)) {
      purl.setParameter(WikiConstants.REQUEST_PARAM_VER_KEY, String.valueOf(myver));
    }

    s = purl.toString();
    // don't urlencode ... since it breaks / also
    // s = WikiUtils.urlEncode(s);
    return s;
  }

  public String getBaseURL() {
    String s = WikiLocal.getWikiEngine().getProperty(WebConstants.BASE_URL_KEY, null);
    if (StringUtils.isBlank(s)) s = super.getBaseURL();
    return s;
  }

  protected long getMaxUploadSize() {
    return WikiUtils.getMaxUploadSize(super.getMaxUploadSize());
  }
}
