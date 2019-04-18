/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki;

import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.ugorji.oxygen.util.NullEnumeration;
import net.ugorji.oxygen.util.OxygenUtils;
import net.ugorji.oxygen.util.StringUtils;
import net.ugorji.oxygen.web.ServletInteractionContext;
import net.ugorji.oxygen.web.ViewContext;
import net.ugorji.oxygen.web.WebAction;
import net.ugorji.oxygen.web.WebConstants;
import net.ugorji.oxygen.web.WebLocal;

public class WikiWebServletContext extends ServletInteractionContext {
  private WikiLinkHolder lh;
  private boolean noParameters;

  public WikiWebServletContext(
      ServletContext sctx0, HttpServletRequest request0, HttpServletResponse response0)
      throws Exception {
    super(sctx0, request0, response0);
    WebLocal.setWebInteractionContext(this);
    String s = WikiUtils.getErrorHandlerURL();
    noParameters = (request0.getRequestURI().startsWith(s));
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

  public String getParameter(String s) throws Exception {
    return (noParameters ? null : super.getParameter(s));
  }

  public String[] getParameterValues(String s) throws Exception {
    return (noParameters ? null : super.getParameterValues(s));
  }

  public Enumeration getParameterNames() throws Exception {
    return (noParameters ? new NullEnumeration() : super.getParameterNames());
  }

  public Map getParameterMap() throws Exception {
    return (noParameters ? new Hashtable() : super.getParameterMap());
  }

  /** request is like http://host:port$ctxpath$urlpfx/$action/$category/$page */
  public WikiLinkHolder toWikiLinkHolder() throws Exception {
    if (lh == null) {
      String category = null;
      String action = null;
      String pagerep = null;
      int version = -1;
      String anchor = null;
      String extrainfo = null;

      lh = new WikiLinkHolder();

      String path = request.getRequestURI();
      OxygenUtils.cdebug("Servlet Request URI before urlDecode: " + path);
      path = StringUtils.decodeURLEncodedPercentHexHex(path);
      OxygenUtils.cdebug("Servlet Request URI after urlDecode: " + path);
      // strip context path
      WikiEngine we = WikiLocal.getWikiEngine();
      String ctxpath = getContextPath();
      path = path.substring(ctxpath.length());
      String urlprefix = we.getProperty(WikiConstants.SERVLET_MAPPING_PREFIX_KEY, null);
      path = path.substring(urlprefix.length());
      OxygenUtils.cdebug("Path to decode further: " + path);

      // pagerep = WikiConstants.DEFAULT_ENTRY_PAGE;
      // trim out the ctxpath
      int i0 = path.indexOf("/", 1);
      action = path.substring(1, i0);

      int i1 = path.indexOf("/", i0 + 1);
      if (i1 >= 0) {
        category = path.substring(i0 + 1, i1);
        pagerep = path.substring(i1 + 1);
      } else {
        category = path.substring(i0 + 1);
      }
      if (getParameter(WikiConstants.REQUEST_PARAM_CATEGORY_KEY) != null) {
        category = getParameter(WikiConstants.REQUEST_PARAM_CATEGORY_KEY);
      }
      if (getParameter(WikiConstants.REQUEST_PARAM_PAGE_KEY) != null) {
        pagerep = getParameter(WikiConstants.REQUEST_PARAM_PAGE_KEY);
      }
      lh.setCategory(category);
      WebAction wwaction = we.getAction(action);

      if (wwaction.isFlagSet(WikiWebAction.FLAG_REQUIRES_EXTRAINFO)) {
        OxygenUtils.debug("In WikiLinkHolder<init>: extrainfo: pagerep = " + pagerep);
        int i2 = pagerep.lastIndexOf("/");
        extrainfo = pagerep.substring(i2 + 1);
        OxygenUtils.cdebug("In WikiLinkHolder<init>: extrainfo: extrainfo = " + extrainfo);
        pagerep = pagerep.substring(0, i2);
      } else {
        if (pagerep != null) {
          int i3 = pagerep.indexOf("#");
          if (i3 >= 0) {
            anchor = pagerep.substring(13 + 1);
            pagerep = pagerep.substring(0, i3);
          }
        }
      }
      // Thread.currentThread().dumpStack();
      // System.out.println("pagerep: " + pagerep);
      if (wwaction.isFlagSet(WikiWebAction.FLAG_REQUIRES_PAGENAME)
          && StringUtils.isBlank(pagerep)) {
        pagerep = we.retrieveWikiCategoryEngine(category).getEntryPage();
      }
      if (!StringUtils.isBlank(pagerep)) {
        pagerep = WikiUtils.toUsablePageRep(pagerep);
      }

      if (getParameter(WikiConstants.REQUEST_PARAM_VER_KEY) != null) {
        version = Integer.parseInt(getParameter(WikiConstants.REQUEST_PARAM_VER_KEY));
      }

      lh.setWikiPage(pagerep);
      lh.setAction(action);
      lh.setCategory(category);
      lh.setContextPage(pagerep);
      lh.setVersion(version);
      lh.setAnchor(anchor);
      lh.setExtrainfo(extrainfo);
    }
    return lh;
  }

  public String getURL(WikiLinkHolder wlh, Map extraparams) {
    return getBaseURL() + getServletURL(wlh, extraparams);
  }

  /**
   * Gets the URL for this object, for an appropriate action. Note that it does not include the
   * context path.
   *
   * <p>Returns something like: /p/view/help/dynamic (if includePage = true) /p/misc/help (here,
   * includePage = false)
   *
   * @param action2
   * @param includePage if false, do not include the page to the link
   * @return
   */
  static String getServletURL(WikiLinkHolder lh, Map extraparams) {
    String s = null;
    WikiEngine engine = WikiLocal.getWikiEngine();
    StringBuffer buf = new StringBuffer();
    WikiCategoryEngine wce = lh.getWikiCategoryEngine(false);
    // buf.append(lh.getRequestContextPath());
    // buf.append(contextPath);
    if (wce != null) {
      String urlprefix = wce.getProperty(WikiConstants.SERVLET_MAPPING_PREFIX_KEY);
      buf.append(urlprefix);
    } else {
      buf.append("/UNKNOWN_URL_PREFIX");
    }
    buf.append("/").append(lh.getAction());
    buf.append("/").append(lh.getCategory());
    WebAction wwaction = lh.getWikiWebAction();
    boolean includePage = wwaction.isFlagSet(WikiWebAction.FLAG_REQUIRES_PAGENAME);
    if (includePage) {
      if (!(StringUtils.isBlank(lh.getWikiPage()))) {
        buf.append("/").append(lh.getWikiPage());
      } else {
        s = ((wce == null) ? WikiConstants.DEFAULT_ENTRY_PAGE : wce.getEntryPage());
        buf.append("/").append(s);
      }
      if (wce != null && wwaction.isFlagSet(WikiWebAction.FLAG_REQUIRES_EXTRAINFO)) {
        buf.append("/").append(lh.getExtrainfo());
      }
    }
    boolean firstReqParam = true;
    int myver = lh.getVersion();
    try {
      if (extraparams != null) {
        s = (String) extraparams.get(WikiConstants.REQUEST_PARAM_VER_KEY);
        myver = Integer.parseInt(s);
      }
    } catch (Exception exc) {
    } // ignore

    // System.out.println("lh.getVersion(): " + lh.getAction() + " ... " + lh.getVersion());
    if (myver >= 0 && wwaction.isFlagSet(WikiWebAction.FLAG_HONORS_VERSION)) {
      firstReqParam =
          addReqParamToBuf(
              buf, firstReqParam, WikiConstants.REQUEST_PARAM_VER_KEY, String.valueOf(myver));
    }
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
            firstReqParam = addReqParamToBuf(buf, firstReqParam, key, val);
          } else if (val instanceof String[]) {
            String[] val2 = (String[]) val;
            for (int i = 0; i < val2.length; i++) {
              firstReqParam = addReqParamToBuf(buf, firstReqParam, key, val2[i]);
            }
          }
        }
      }
    }
    // if last char is ?, remove it
    int idxOfLastChar = buf.length() - 1;
    if (buf.charAt(idxOfLastChar) == '?') {
      buf.deleteCharAt(idxOfLastChar);
    }

    if (includePage) {
      if (!(StringUtils.isBlank(lh.getAnchor()))) {
        buf.append("#").append(lh.getAnchor());
      }
    }

    s = buf.toString();
    // don't urlencode ... since it breaks / also
    // s = WikiUtils.urlEncode(s);
    return s;
  }

  protected long getMaxUploadSize() {
    return WikiUtils.getMaxUploadSize(super.getMaxUploadSize());
  }

  public String getBaseURL() {
    String s = WikiLocal.getWikiEngine().getProperty(WebConstants.BASE_URL_KEY, null);
    if (StringUtils.isBlank(s)) s = super.getBaseURL();
    return s;
  }

  private static boolean addReqParamToBuf(
      StringBuffer buf, boolean firstParam, String key, Object val) {
    if (firstParam) {
      buf.append("?");
    } else {
      buf.append("&");
    }
    // buf.append(key).append("=").append(val);
    buf.append(arptbe(key)).append("=").append(arptbe(val));
    return false;
  }

  private static String arptbe(Object s) {
    try {
      return URLEncoder.encode(String.valueOf(s), "UTF-8");
    } catch (Exception exc) {
      throw new RuntimeException(exc);
    }
  }
}
