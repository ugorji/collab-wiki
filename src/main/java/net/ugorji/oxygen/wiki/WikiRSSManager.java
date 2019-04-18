/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki;

/**
 * Generates an RSS report every day (for changes within that day). This file is stored in the
 * runtime directory. Someone else can serve it up (e.g. an action - RSSReportAction)
 *
 * <p>As of May 19, 2006. This class is no longer used. Along with rss.rss in templatefiles
 * directory. We now use ROME to generate the feeds.
 */
abstract class WikiRSSManager {
  /*
  private long delay = (24 * 60 * 60 * 1000l);
  private WikiCategoryEngine wce;

  private WikiRSSManager(final WikiCategoryEngine _wce) throws Exception {
    wce = _wce;
  }

  public void close() {
  }

  public void writeXMLContent(String flavor, long delay, Writer w) throws Exception {
    WRSSModel model = new WRSSModel(wce, delay);
    Map m0 = new HashMap();
    m0.put("model", model);
    WikiLocal.getWikiEngine().getWikiTemplateFilesHandler().write("rss." + flavor, m0, w);
  }

  // It must be visible by the template
  public static class WRSSModel {
    private WikiCategoryEngine wce;

    private String engineName;
    private String category;
    private WikiProvidedObject[] wikipages;

    public WRSSModel(WikiCategoryEngine _wce, long delay) throws Exception {
      wce = _wce;
      engineName = wce.getWikiEngine().getName();
      category = wce.getName();
      Date startdate = new Date(System.currentTimeMillis() - delay);
      wikipages = WikiUtils.getWikiPagesGivenTimeWindow(wce, startdate, null);
    }

    public Locale getLocale() {
      Locale l = null;
      WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
      if(wlh != null) {
        l = wlh.getLocale();
      }
      if(l == null) {
        l = WikiLocal.getWikiEngine().getDefaultUILocale();
      }
      return l;
    }

    public Date getPublishedDate() {
      return new Date();
    }

    public String getCategoryName() {
      return category;
    }

    public String getEngineName() {
      return engineName;
    }

    public WikiProvidedObject[] getWikipages() {
      return wikipages;
    }

    public String getBaseURL() {
      String baseurl = wce.getProperty(WikiConstants.BASE_URL_KEY);
      if(baseurl == null) {
        baseurl = "";
      }
      baseurl = baseurl.trim();
      return baseurl;
    }


    public String getURL(String pagename) throws Exception {
      return _getURL(WikiConstants.SERVLET_ACTION_VIEW, pagename);
    }

    public String getPageInfoURL(String pagename) throws Exception {
      return _getURL("pageinfo", pagename);
    }

    private String _getURL(String action, String pagename) throws Exception {
      WebInteractionContext wctx = WebLocal.getWebInteractionContext();
      String s = null;
      WikiLinkHolder wlh = new WikiLinkHolder();
      wlh.setAction(action);
      wlh.setCategory(wce.getName());
      wlh.setWikiPage(pagename);
      if(wctx == null) {
        s = getBaseURL() + WikiWebServletContext.getServletURL(wlh);
      } else {
        s = wctx.toURLString(wlh);
      }
      return s;
    }

  }
  */
}
