/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki;

import java.io.StringReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.ugorji.oxygen.markup.MarkupUtils;
import net.ugorji.oxygen.util.OxygenRevision;
import net.ugorji.oxygen.util.StringUtils;
import net.ugorji.oxygen.web.ViewContext;
import net.ugorji.oxygen.web.WebAction;
import net.ugorji.oxygen.web.WebLocal;

public class WikiViewUtils {
  private static Random rand = new Random();

  public static String onOrOff(boolean b) {
    return (b ? WebLocal.getI18n().str("general.on") : WebLocal.getI18n().str("general.off"));
  }

  public static int nextRandomInt(int max) {
    return rand.nextInt(max);
  }

  public static String oneReqParam(Map reqParams, String key) throws Exception {
    return StringUtils.toHTMLEscape(
        StringUtils.nonNullString(StringUtils.getSingleValue(reqParams.get(key))), false, false);
  }

  public static String decipherURL(WikiLinkHolder wlh2, String action, String[] extraparams) {
    Map m2 = null;
    if (extraparams != null && extraparams.length > 1) {
      m2 = new HashMap();
      for (int i = 0; i < extraparams.length; i++) {
        List list = (List) m2.get(extraparams[i]);
        if (list == null) {
          list = new ArrayList(2);
          m2.put(extraparams[i], list);
        }
        list.add(extraparams[++i]);
      }
      for (Iterator itr = m2.entrySet().iterator(); itr.hasNext(); ) {
        Map.Entry e = (Map.Entry) itr.next();
        List list = (List) e.getValue();
        if (list.size() == 1) {
          e.setValue(list.get(0));
        } else {
          e.setValue(list.toArray(new String[0]));
        }
      }
    }
    return decipherURL(wlh2, action, m2);
  }

  public static String decipherURL(WikiLinkHolder wlh2, String action, Map extraparams) {
    // return WikiUtils.getWikiURL(wlh2, action, extraparams);
    String oldaction = wlh2.getAction();
    try {
      wlh2.setAction(action);
      String s = WebLocal.getWebInteractionContext().toURLString(wlh2, extraparams);
      return s;
    } finally {
      wlh2.setAction(oldaction);
    }
  }

  public static String decipherURL(WikiLinkHolder wlh2, String action) {
    // return WikiUtils.getWikiURL(wlh2, action, null);
    return decipherURL(wlh2, action, (Map) null);
  }

  public static WikiProvidedObject[] lookupAttachments(
      WikiCategoryEngine wce, String pagerep, boolean includeDeleted) throws Exception {
    Set set = new HashSet();
    set.addAll(Arrays.asList(wce.getIndexingManager().lookupAttachmentNames(pagerep, null, null)));
    if (includeDeleted) {
      set.addAll(Arrays.asList(wce.getAttachmentProvider().getAttachmentNames(pagerep, true)));
    }

    String[] attnames = (String[]) set.toArray(new String[0]);
    Arrays.sort(attnames);

    WikiProvidedObject[] wikipages = new WikiProvidedObject[attnames.length];
    int version = WikiProvidedObject.VERSION_LATEST_DETAILS_NECESSARY;
    for (int i = 0; i < wikipages.length; i++) {
      wikipages[i] = wce.getAttachmentProvider().getAttachment(pagerep, attnames[i], version);
    }
    return wikipages;
  }

  public static String getAttribute(WikiProvidedObject wp, String key, String def) {
    String s = wp.getAttribute(key);
    if (StringUtils.isBlank(s)) {
      s = def;
    }
    return s;
  }

  public static String getAuthor(WikiProvidedObject wp) throws Exception {
    String s = wp.getAttribute(WikiConstants.ATTRIBUTE_AUTHOR);
    if (StringUtils.isBlank(s)) {
      s =
          WikiLocal.getWikiLinkHolder()
              .getWikiCategoryEngine(true)
              .getProperty(WikiConstants.DEFAULT_USER_KEY);
    }
    return s;
  }

  public static void includeView(String action) throws Exception {
    WebAction waction = WikiLocal.getWikiEngine().getAction(action);
    waction.includeView();
  }

  public static void renderHTMLForEditOrView() throws Exception {
    ViewContext tctx = WebLocal.getViewContext();
    WikiProvidedObject wp0 =
        (WikiProvidedObject) tctx.getAttribute(WikiConstants.TEMPLATE_WIKIPAGE_KEY);
    Boolean showBorders = (Boolean) tctx.getAttribute(WikiConstants.TEMPLATE_SHOWBORDERS_KEY);
    Boolean realpageview = (Boolean) tctx.getAttribute(WikiConstants.TEMPLATE_REALPAGEVIEW_KEY);
    String text = (String) tctx.getAttribute(WikiConstants.TEMPLATE_WIKIPAGE_TEXT_KEY);
    if (text == null) {
      WikiLocal.getWikiCategoryEngine().writeHTML(wp0, realpageview.booleanValue());
    } else {
      WikiLocal.getWikiCategoryEngine()
          .writeHTML(wp0, new StringReader(text), realpageview.booleanValue());
    }
  }

  public static boolean isDecorationPageExist(String s) throws Exception {
    return _doDecorationPage(s, "edit", null, false);
  }

  public static void includeDecorationPage(String s) throws Exception {
    _doDecorationPage(s, "edit", null, true);
  }

  public static String visibilityJS(boolean b) {
    return (b ? "block" : "none");
  }

  public static String getDecorationEditLink(String s) throws Exception {
    return getColonisedLink(s, "edit");
  }

  public static String getColonisedLink(String s, String action) throws Exception {
    StringBuffer buf = new StringBuffer();
    _doDecorationPage(s, action, buf, false);
    return buf.toString();
  }

  private static boolean _doDecorationPage(
      String s, String action, StringBuffer editBuf, boolean writeHTML) throws Exception {
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    int idx = s.indexOf(':');
    if (idx != -1) {
      String s0 = s.substring(0, idx);
      wce = WikiLocal.getWikiEngine().retrieveWikiCategoryEngine(s0);
      s = s.substring(idx + 1);
    }
    boolean b = wce.getIndexingManager().isAReferrer(s);
    if (b && writeHTML) {
      wce.writeHTML(new WikiProvidedObject(s), true);
    }
    if (editBuf != null) {
      WikiLinkHolder wlh = new WikiLinkHolder();
      wlh.setAction(action);
      wlh.setWikiPage(s);
      wlh.setCategory(wce.getName());
      editBuf.append(WebLocal.getWebInteractionContext().toURLString(wlh, null));
    }

    return b;
  }

  public static class Menu {
    // there is a list of menuCategories
    // each menuCategory has a list of menuItems
    // each menuCategory has an index, and a name
    // U can find a menuCategory by looking up the index, or the name
    // # both col and row start from 1
    private List categories = new ArrayList();

    public Menu() {}

    public void addItem(int col, String link, String text) {
      List thecol = getCategory(col);
      String[] therow = new String[] {"", " "};
      therow[0] = StringUtils.nonNullString(link);
      therow[1] = StringUtils.nonNullString(text);
      thecol.add(therow);
    }

    public void setItem(int col, int row, String link, String text) {
      String[] therow = getItem(col, row);
      therow[0] = StringUtils.nonNullString(link);
      therow[1] = StringUtils.nonNullString(text);
    }

    public int getNumCategories() {
      return categories.size();
    }

    public int getNumItems(int col) {
      return ((List) categories.get(col - 1)).size();
    }

    public String getLink(int col, int row) {
      return getItem(col, row)[0];
    }

    public String getText(int col, int row) {
      return getItem(col, row)[1];
    }

    public List getCategory(int col) {
      List thecol = null;
      int colsize = categories.size();
      if (col > colsize) {
        for (int i = colsize; i < col; i++) {
          thecol = new ArrayList();
          categories.add(thecol);
        }
      }
      thecol = (List) categories.get(col - 1);
      return thecol;
    }

    public String[] getItem(int col, int row) {
      String[] therow = null;
      List thecol = getCategory(col);
      int rowsize = thecol.size();
      if (row > rowsize) {
        for (int i = rowsize; i <= row; i++) {
          therow = new String[] {"", " "};
          thecol.add(therow);
        }
      }
      therow = (String[]) thecol.get(row - 1);
      return therow;
    }

    public int getColumnGivenFirstItem(String s) {
      int x = categories.size();
      int y = -1;
      for (int i = 1; i <= x; i++) {
        if (s.equals(getItem(i, 1))) {
          y = i;
          break;
        }
      }
      return y;
    }
  }

  public static String getLinkHTML(
      WikiLinkHolder lh, boolean anImage, boolean decorateExternalLinks) throws Exception {
    String url = lh.getURL();
    String htmlstr = null;
    if (!lh.isALink() && !anImage) {
      htmlstr = url;
    } else {
      // if(!lh.isExtLink() && wce != null && lh.getWikiWebAction().isRequiresExtraInfo()) {
      //  url = WikiUtils.getWikiURL(lh, WikiConstants.SERVLET_ACTION_VIEW_ATTACHMENT, true, true,
      // true);
      //  use Servlet directly to access attachments??? (WHY??? TBD)
      //  url = WikiWebServletContext.getServletURL(lh);
      // }
      if (anImage) {
        String imageMetadata = StringUtils.nonNullString(lh.getText2());
        htmlstr =
            "<img id=\"wp_getlinkhtml_"
                + MarkupUtils.rand.nextInt(MarkupUtils.RAND_MAX)
                + "\" src=\""
                + url
                + "\" alt=\""
                + lh.getText()
                + "\" "
                + imageMetadata
                + " />";
      } else if (lh.isWikiPageDoesNotExists()) {
        lh.setAction(WikiConstants.ACTION_EDIT);
        url = lh.getURL();
        // htmlstr = "<a class=\"wikinonexistentpage\" href=\"" + url + "\" />" + lh.getText() +
        // "</a>";
        String promptMsg = WebLocal.getI18n().str("general.edit") + " " + lh.getWikiPage() + "?";
        htmlstr =
            "<a class=\"wikinonexistentpage\" href=\"#\" onClick=\"javascript:oxy_confirm_and_go('"
                + promptMsg
                + "', '"
                + url
                + "');\" />"
                + lh.getText()
                + "</a>";
      } else if (decorateExternalLinks && lh.isExtLink()) {
        htmlstr = "<a class=\"wikiexternallink\" href=\"" + url + "\" >" + lh.getText() + "</a>";
      } else {
        htmlstr = "<a href=\"" + url + "\" >" + lh.getText() + "</a>";
      }
    }
    return htmlstr;
  }

  public static DateFormat getDateTimeFormat(Locale locale) {
    return DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.FULL, locale);
  }

  public static DateFormat getDateFormat(Locale locale) {
    return DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
  }

  /** If no parameters are passed for r1 and r2, then just return the latest change */
  public static OxygenRevision extractDiffInfo(WikiCategoryEngine wce, String revPrefix)
      throws Exception {
    revPrefix = StringUtils.nonNullString(revPrefix, "");
    int r1 = wce.getPageProvider().getInitialVersion() - 1;
    int r2 = r1;
    WikiProvidedObject wp = WikiUtils.getWikiPage();
    String pagename = WikiUtils.getString("pagename", null);
    if (pagename == null && wp != null) {
      pagename = wp.getName();
    }
    r1 = (int) WikiUtils.getInt(revPrefix + "r1", r1);
    r2 = (int) WikiUtils.getInt(revPrefix + "r2", r2);
    return wce.getPageProvider().getPageRevision(pagename, r1, r2);
  }
}
