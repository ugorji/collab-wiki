/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.actions;

/**
 * Handles showing the RSS feed. This is no longer used, as we just leverage the SearchAction for
 * RSS, Recent Changes and Search. Make it private and abstract to show this.
 *
 * @author ugorji
 */
abstract class RSSAction extends GenericWikiWebAction {}

/*
  public int render() throws Exception {
    preRender();
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    Map model = (Map)wlh.getAttribute("wiki.model.rss");
    SyndFeed feed = (SyndFeed)model.get("feed");
    String encoding = (String)model.get("encoding");

    WebInteractionContext request = WebLocal.getWebInteractionContext();
    request.setContentType("text/xml; charset=" + encoding);

    SyndFeedOutput output = new SyndFeedOutput();
    output.output(feed, request.getWriter());
    //WikiUtils.includeJSPView("rss.jsp");
    return RENDER_COMPLETED;
  }

  private void preRender() throws Exception {
    Map results = RecentChangesAction.doWork();
    String encoding = (String)results.get("encoding");
    String[] categories = (String[])results.get("categories");
    Map wikipagesmap = (Map)results.get("wikipagesmap");
    WikiEngine we = WikiLocal.getWikiEngine();
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    WebInteractionContext request = WebLocal.getWebInteractionContext();

    String s = null;
    String baseurl = request.getBaseURL();
    SyndFeed feed = new SyndFeedImpl();
    s = request.getParameter("rss.format");
    s = (StringUtils.isBlank(s) ? we.getProperty(WikiConstants.RSS_FORMAT_KEY, "rss_2.0") : s);
    feed.setFeedType(s);
    feed.setTitle(we.getName() + " - " + Arrays.asList(categories));
    feed.setLink(baseurl);
    feed.setDescription(" ... ");

    List entries = new ArrayList();

    SyndLinkImpl sl = null;
    WikiLinkHolder wlh2 = new WikiLinkHolder();
    for(int i = 0; i < categories.length; i++) {
      WikiCategoryEngine wce =  we.retrieveWikiCategoryEngine(categories[i]);
      WikiLocal.setWikiCategoryEngine(wce);
      wlh2.setCategory(categories[i]);
      WikiProvidedObject[] wikipages = (WikiProvidedObject[])wikipagesmap.get(categories[i]);
      for(int j = 0; j < wikipages.length; j++) {
        wlh2.setWikiPage(wikipages[j].getName());
        SyndEntry entry = new SyndEntryImpl();
        entry.setTitle(categories[i] + " - " + wikipages[j].getName());
        entry.setPublishedDate(wikipages[j].getDate());
        entry.setAuthor(wikipages[j].getAttribute(WikiConstants.ATTRIBUTE_AUTHOR));
        List links = new ArrayList();

        work(request, wlh2, baseurl, "view", entry, links, true);
        work(request, wlh2, baseurl, "pageinfo", entry, links, false);

        entry.setLinks(links);
        SyndContent description = new SyndContentImpl();
        description.setType("text/plain");
        description.setValue("Page Updated");
        entry.setDescription(description);
        entries.add(entry);
      }
    }

    feed.setEntries(entries);

    Map model = new HashMap();
    model.put("feed", feed);
    model.put("encoding", encoding);

    wlh.setAttribute("wiki.model.rss", model);
  }

  private static void work(WebInteractionContext request, WikiLinkHolder wlh2,
                           String baseurl, String action,
                           SyndEntry entry, List links,
                           boolean doSetLink) {
    wlh2.setAction(action);
    String s = baseurl + request.toURLString(wlh2, null);
    s = request.encodeURL(s, true);
    if(links != null) {
      SyndLinkImpl l = new SyndLinkImpl();
      l.setHref(s);
      l.setType("text/html");
      links.add(l);
    }
    if(doSetLink) {
      entry.setLink(s);
    }

  }

}

*/
