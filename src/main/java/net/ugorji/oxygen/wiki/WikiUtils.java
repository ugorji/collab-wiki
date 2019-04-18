/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki;

import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import net.ugorji.oxygen.markup.MarkupUtils;
import net.ugorji.oxygen.util.OxygenUtils;
import net.ugorji.oxygen.util.StringUtils;
import net.ugorji.oxygen.web.ViewContext;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.web.WebUserSession;

/**
 * Collection of shared utility functions
 *
 * @author ugorji
 */
public class WikiUtils {
  private WikiUtils() {}

  // extends MarkupUtils
  public static void render() throws Exception {
    ViewContext vctx = WebLocal.getViewContext();
    WikiLocal.getWikiEngine().getAction(vctx.getAction()).render();
  }

  /**
   * Helper function to extract the WikiPage
   *
   * @param req
   * @return
   */
  public static WikiProvidedObject getWikiPage() throws Exception {
    WikiProvidedObject wp = null;
    WikiLinkHolder lh = WikiLocal.getWikiLinkHolder();
    // System.out.println("lh.getWikiPage(): " + lh.getWikiPage());
    WikiCategoryEngine wce = lh.getWikiCategoryEngine(true);
    // Always return it, whether or not the page still exists (ie not deleted)
    // if(wce.getIndexingManager().isAReferrer(lh.getWikiPage())) {
    wp = wce.getPageProvider().getPage(lh.getWikiPage(), lh.getVersion());
    // }
    if (wp == null) {
      wp = new WikiProvidedObject(lh.getWikiPage());
    }
    return wp;
  }

  public static void extractProps(String pfx, Properties p2) throws Exception {
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    int pfxlen = pfx.length();
    for (Enumeration enum0 = request.getParameterNames(); enum0.hasMoreElements(); ) {
      String key = (String) enum0.nextElement();
      if (key.startsWith(pfx)) {
        p2.setProperty(key.substring(pfxlen), request.getParameter(key));
      }
    }
  }

  public static int getInt(String key, int defValue) throws Exception {
    String s = getString(key, null);
    if (s != null) {
      return Integer.parseInt(s);
    } else {
      return defValue;
    }
  }

  public static int[] getIntArray(String key, int[] defvalue) throws Exception {
    String[] vs = getStringArray(key, null);
    int[] vi = defvalue;
    if (vs != null) {
      vi = new int[vs.length];
      for (int i = 0; i < vs.length; i++) {
        vi[i] = Integer.parseInt(vs[i]);
      }
    }
    return vi;
  }

  public static String[] getStringArray(String key, String[] defValue) throws Exception {
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    Object sa = null;
    if (sa == null) {
      sa = request.getParameterValues(key);
    }
    if (sa == null) {
      WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
      sa = wlh.getAttribute(key);
    }
    // if(sa == null) {
    //  sa = wlh.getExtraparam(key);
    // }
    String[] sa2 = null;
    if (sa != null) {
      if (sa instanceof String[]) {
        sa2 = (String[]) sa;
      } else {
        sa2 = new String[] {sa.toString()};
      }
    }
    if (sa2 == null) {
      sa2 = defValue;
    }
    return sa2;
  }

  public static String getString(String key, String defValue) throws Exception {
    String[] sa = getStringArray(key, new String[] {defValue});
    return sa[0];
  }

  public static String getCategoryURL(String category, String action, String page)
      throws Exception {
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    WikiLinkHolder wlh = new WikiLinkHolder();
    wlh.setAction(action);
    wlh.setCategory(category);
    wlh.setWikiPage(page);
    return request.toURLString(wlh, null);
  }

  public static String getErrorHandlerURL() throws Exception {
    return getCategoryURL("builtin", "showerror", null);
  }

  public static Locale extractLocaleFromRequest(WebInteractionContext req) throws Exception {
    String s = req.getHeader("Accept-Language");
    int commaIndx = 0;
    // System.out.println("Accept-Language=|" + s + "|");
    if (s != null && (commaIndx = s.indexOf(",")) > 0) {
      s = s.substring(0, commaIndx);
    }
    if (s != null && (commaIndx = s.indexOf(";")) > 0) {
      s = s.substring(0, commaIndx);
    }
    // System.out.println("Accept-Language=|" + s + "|");
    Locale locale = OxygenUtils.stringToLocale(s);
    return locale;
  }

  public static long getMaxUploadSize(long defaultValue) {
    long l = defaultValue;
    try {
      String _max = null;
      WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
      if (wce != null) {
        _max = wce.getProperty(WikiConstants.MAX_ATTACHMENT_UPLOAD_SIZE_KEY);
      }
      if (_max == null) {
        WikiEngine we = WikiLocal.getWikiEngine();
        if (we != null) {
          _max = we.getProperty(WikiConstants.MAX_ATTACHMENT_UPLOAD_SIZE_KEY, null);
        }
      }
      if (_max != null) {
        l = Integer.parseInt(_max);
      }
    } catch (Throwable thr) {
      OxygenUtils.error("Error getting max upload size", thr);
    }
    return l;
  }

  public static String getWikiURL(WikiLinkHolder wlh, String action, Map extraparams) {
    String s = null;
    String a = wlh.getAction();
    try {
      wlh.setAction(action);
      WebInteractionContext wikiwebctx = WebLocal.getWebInteractionContext();
      if (wikiwebctx == null) {
        // TODO HACK: Happens if doing an out-of-request execution e.g. running WikiTest
        s = "/oxywiki" + WikiWebServletContext.getServletURL(wlh, extraparams);
      } else {
        s = wikiwebctx.toURLString(wlh, extraparams);
      }
      return s;
    } finally {
      wlh.setAction(a);
    }
  }

  public static WikiProvidedObject[] getWikiPagesGivenTimeWindow(
      WikiCategoryEngine wce, Date from, Date to) throws Exception {
    int version = WikiProvidedObject.VERSION_LATEST_DETAILS_NECESSARY;
    String[] sarr = wce.getIndexingManager().lookupPageNames(from, to);
    WikiProvidedObject[] wikipages = new WikiProvidedObject[sarr.length];
    for (int i = 0; i < sarr.length; i++) {
      wikipages[i] = wce.getPageProvider().getPage(sarr[i], version);
    }
    return wikipages;
  }

  public static String toUsablePageRep(String pagerep) {
    // return toCamelCase(pagerep);
    String pagerep2 = pagerep;
    Matcher punctMatcher = MarkupUtils.punctPattern.matcher(pagerep2);
    if (punctMatcher.find()) {
      punctMatcher = punctMatcher.reset();
      StringBuffer sb = new StringBuffer();
      while (punctMatcher.find()) {
        String s = punctMatcher.group();
        char c = s.charAt(0);
        if (c == '.' || c == '/' || c == '-' || c == '_') {
          punctMatcher.appendReplacement(sb, s);
        } else {
          punctMatcher.appendReplacement(sb, "");
        }
      }
      punctMatcher.appendTail(sb);
      pagerep2 = sb.toString();
      // pagerep2 = punctMatcher.replaceAll("");
    }
    // System.out.println(". . . " + pagerep2);
    Matcher spaceMatcher = MarkupUtils.spacePattern.matcher(pagerep2);
    if (spaceMatcher.find()) {
      spaceMatcher = spaceMatcher.reset();
      StringBuffer sb = new StringBuffer();
      while (spaceMatcher.find()) {
        String s = spaceMatcher.group();
        spaceMatcher.appendReplacement(sb, s.trim().toUpperCase());
      }
      spaceMatcher.appendTail(sb);
      pagerep2 = sb.toString();
    }
    pagerep2 = pagerep2.trim();
    // System.out.println(". . . " + pagerep2);
    return pagerep2;
  }

  public static boolean isCaptchaEnabled() {
    WebUserSession wus = WebLocal.getWebUserSession();
    boolean b1 =
        "true"
            .equals(
                WikiLocal.getWikiCategoryEngine().getProperty(WikiConstants.CAPTCHA_ENABLED_KEY));
    return (b1 && !wus.isCaptchaChecked());
  }

  public static boolean isSetUsernameSupported() {
    return "true"
        .equals(
            WikiLocal.getWikiEngine()
                .getProperty(WikiConstants.ENGINE_USERNAME_SET_SUPPORTED_KEY, null));
  }

  public static boolean allowPublish() throws Exception {
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    boolean b = false;
    if (wce != null && request != null) {
      if (wce.getIndexingManager().isOnlyIndexPublishedPages()) {
        String s = wce.getProperty(WikiConstants.PUBLISHER_GROUPS_KEY);
        List l = StringUtils.tokens(s, ", ", true, true);
        if (l.contains(WikiConstants.NULL)) {
          b = true;
        } else {
          for (Iterator itr = l.iterator(); itr.hasNext(); ) {
            s = (String) itr.next();
            if (request.isUserInRole(s)) {
              b = true;
              break;
            }
          }
        }
      }
    }
    return b;
  }

  public static String fullQualifiedWikiName(String category, String pagename) {
    return category + ":" + pagename;
  }

  /**
   * Calendar should only ever be shown, if we index all details - else the index dates will not be
   * in sync with the dates from the repository - and we get diff stuff for "details" and
   * "non-details"
   */
  public static void ensureCalendarBrowseCanBeDone(WikiCategoryEngine wce) throws Exception {
    boolean detailsAreIndexed =
        "true".equals(wce.getProperty(WikiConstants.INDEX_DETAILS_OF_PAGE_KEY));
    if (!detailsAreIndexed) {
      throw new Exception("Cannot view calendar, since details are not indexed");
    }
  }

  public static String getUserLink(String username) {
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    String shorthand = wce.getProperty(WikiConstants.USER_SHORTCUT_KEY);
    return wce.getShorthandManager().getEvaluatedString(shorthand, username);
  }

  public static void main(String[] args) throws Exception {
    System.out.println(toUsablePageRep("a/c Main's Style:Abc;jf 'sAb a/b a_c"));
    System.out.println(toUsablePageRep("Customer Site Visits - Schedules and Current Statuses"));
  }

  public static void throwAnError() throws Exception {
    throw new RuntimeException("Ugorji is still cool");
  }
}
