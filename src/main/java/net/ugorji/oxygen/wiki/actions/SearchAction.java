/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.actions;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.feed.synd.SyndLinkImpl;
import com.sun.syndication.io.SyndFeedOutput;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.ugorji.oxygen.util.I18n;
import net.ugorji.oxygen.util.OxygenSearchResults;
import net.ugorji.oxygen.util.StringUtils;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiConstants;
import net.ugorji.oxygen.wiki.WikiEngine;
import net.ugorji.oxygen.wiki.WikiIndexingManager;
import net.ugorji.oxygen.wiki.WikiLinkHolder;
import net.ugorji.oxygen.wiki.WikiLocal;
import net.ugorji.oxygen.wiki.WikiProvidedObject;
import net.ugorji.oxygen.wiki.WikiUtils;
import net.ugorji.oxygen.wiki.WikiViewUtils;

/**
 * Shows the search screen
 *
 * @author ugorji
 */
public class SearchAction extends GenericWikiWebAction {

  private static final DecimalFormat decimalFormat = new DecimalFormat("##.##");

  {
    setFlag(FLAG_REQUIRES_PAGENAME);
    setFlag(FLAG_MAKE_SHORTHAND);
  }

  public int render() throws Exception {
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    String s = null;
    // WikiLinkHolder wlh2 = wlh.getClone();
    WikiEngine we = WikiLocal.getWikiEngine();
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    WikiCategoryEngine origwce = wce;
    // DateFormat dateformat = new SimpleDateFormat("MM/dd/yyyy");
    DateFormat dateformat = DateFormat.getDateInstance(DateFormat.SHORT, wce.getDefaultLocale());

    String simpleSearchVal =
        StringUtils.nonNullString(
            request.getParameter(WikiConstants.SEARCH_INDEX_SIMPLE_SEARCH_KEY));
    String[] categories = request.getParameterValues("cat");
    if (categories == null
        || categories.length == 0
        || (categories.length == 1 && StringUtils.isBlank(categories[0]))) {
      categories = new String[] {wce.getName()};
    }
    boolean allRequired = StringUtils.isBlank(simpleSearchVal);

    Map m = new HashMap();
    m.put(WikiConstants.SEARCH_INDEX_INDEX_TYPE, WikiConstants.SEARCH_INDEX_PAGE);
    if (allRequired) {
      String contents =
          StringUtils.nonNullString(request.getParameter(WikiConstants.SEARCH_INDEX_CONTENTS));
      String pagename =
          StringUtils.nonNullString(request.getParameter(WikiConstants.SEARCH_INDEX_PAGENAME));
      String attachmentName =
          StringUtils.nonNullString(
              request.getParameter(WikiConstants.SEARCH_INDEX_ATTACHMENT_NAME));
      String author =
          StringUtils.nonNullString(request.getParameter(WikiConstants.SEARCH_INDEX_AUTHOR));
      String comments =
          StringUtils.nonNullString(request.getParameter(WikiConstants.SEARCH_INDEX_COMMENTS));
      String tags =
          StringUtils.nonNullString(request.getParameter(WikiConstants.SEARCH_INDEX_TAGS));

      // put the date arguments
      Date date0 = null;
      Date date1 = null;
      long lastNumDays = -1;

      s = request.getParameter(WikiConstants.SEARCH_INDEX_LAST_MODIFIED);
      if (!(StringUtils.isBlank(s))) {
        lastNumDays = Integer.parseInt(s);
      }
      if (lastNumDays > 0) {
        date0 = new Date(System.currentTimeMillis() - (lastNumDays * 24 * 60 * 60 * 1000l));
        date1 = null;
      } else {
        s = request.getParameter(WikiConstants.SEARCH_INDEX_LAST_MODIFIED + ".0");
        if (!(StringUtils.isBlank(s))) {
          date0 = dateformat.parse(s, new ParsePosition(0));
        }
        s = request.getParameter(WikiConstants.SEARCH_INDEX_LAST_MODIFIED + ".1");
        if (!(StringUtils.isBlank(s))) {
          date1 = dateformat.parse(s, new ParsePosition(0));
        }
      }

      if (date0 != null || date1 != null) {
        m.put(
            WikiConstants.SEARCH_INDEX_LAST_MODIFIED,
            WikiIndexingManager.getDateRangeQueryString(date0, date1));
      }

      // put the other arguments
      if (!StringUtils.isBlank(contents)) {
        m.put(WikiConstants.SEARCH_INDEX_CONTENTS, contents);
      }
      if (!StringUtils.isBlank(pagename)) {
        m.put(WikiConstants.SEARCH_INDEX_PAGENAME, WikiUtils.toUsablePageRep(pagename));
      }
      if (!StringUtils.isBlank(author)) {
        m.put(WikiConstants.SEARCH_INDEX_AUTHOR, author);
      }
      if (!StringUtils.isBlank(attachmentName)) {
        m.put(WikiConstants.SEARCH_INDEX_ATTACHMENT_NAME, attachmentName);
      }
      if (!StringUtils.isBlank(comments)) {
        m.put(WikiConstants.SEARCH_INDEX_COMMENTS, comments);
      }
      if (!StringUtils.isBlank(tags)) {
        m.put(WikiConstants.SEARCH_INDEX_TAGS, tags);
      }
    } else {
      m.put(WikiConstants.SEARCH_INDEX_CONTENTS, simpleSearchVal);
      // m.put(WikiConstants.SEARCH_INDEX_PAGENAME, WikiUtils.toUsablePageRep(simpleSearchVal));
      // m.put(WikiConstants.SEARCH_INDEX_AUTHOR, simpleSearchVal);
      // m.put(WikiConstants.SEARCH_INDEX_ATTACHMENT_NAME, simpleSearchVal);
      m.put(WikiConstants.SEARCH_INDEX_COMMENTS, simpleSearchVal);
      // m.put(WikiConstants.SEARCH_INDEX_TAGS, simpleSearchVal);
    }

    // System.out.println("querystring: " + querystring);
    int maxHits = Integer.parseInt(wce.getProperty(WikiConstants.SEARCH_MAX_NUM_HITS_KEY));
    double minScore = Double.parseDouble(wce.getProperty(WikiConstants.SEARCH_MIN_SCORE_KEY));
    double thresholdScore =
        Double.parseDouble(wce.getProperty(WikiConstants.SEARCH_THRESHOLD_SCORE_KEY));

    OxygenSearchResults srch =
        wce.getIndexingManager()
            .searchCategories(m, allRequired, categories, maxHits, minScore, thresholdScore);

    Set existingTags = new HashSet();
    String[] allcnames = we.getRegisteredAndLoadedWikiCategoryNames();
    for (int i = 0; i < allcnames.length; i++) {
      existingTags.addAll(
          Arrays.asList(
              we.retrieveWikiCategoryEngine(allcnames[i])
                  .getIndexingManager()
                  .lookupExistingTags()));
    }
    String[] existingTagsArr = (String[]) existingTags.toArray(new String[0]);
    Arrays.sort(existingTagsArr);
    Arrays.sort(allcnames);

    String encoding = null;
    if (categories.length == 1) {
      wce = we.retrieveWikiCategoryEngine(categories[0]);
      encoding = wce.getCharacterEncoding();
    } else {
      encoding = we.getCharacterEncoding();
    }

    I18n i18n = WebLocal.getI18n();
    // System.out.println("rss parameter: " + request.getParameter("rss"));
    boolean doRSS = "true".equals(request.getParameter("rss"));
    if (doRSS) {
      boolean includeLastChange = "true".equals(request.getParameter("rss_includelastchange"));
      String baseurl = request.getBaseURL();
      SyndFeed feed = new SyndFeedImpl();
      s = request.getParameter("rss.format");
      s = (StringUtils.isBlank(s) ? we.getProperty(WikiConstants.RSS_FORMAT_KEY, "rss_2.0") : s);
      feed.setFeedType(s);
      feed.setTitle(i18n.str("actions.search.rss_title", we.getName()));
      feed.setLink(baseurl);
      feed.setDescription(
          i18n.str(
              "actions.search.rss_description",
              new String[] {we.getName(), String.valueOf(Arrays.asList(categories))}));

      OxygenSearchResults.Entry[] srchEntries = srch.getResults();
      List entries = new ArrayList();

      Map tmplctx = new HashMap();
      tmplctx.put("i18n", i18n);
      tmplctx.put("dateformat", dateformat);

      WikiLinkHolder wlh2 = new WikiLinkHolder();

      for (int i = 0; i < srchEntries.length; i++) {
        try {
          wce = we.retrieveWikiCategoryEngine(srchEntries[i].getCategory());
          WikiLocal.setWikiCategoryEngine(wce);
          wlh2.setCategory(srchEntries[i].getCategory());
          WikiProvidedObject wikipage =
              wce.getIndexingManager().getWikiPageFromIndex(srchEntries[i].getPage());
          wlh2.setWikiPage(wikipage.getName());
          SyndEntry entry = new SyndEntryImpl();
          entry.setTitle(srchEntries[i].getCategory() + " - " + wikipage.getName());
          entry.setPublishedDate(wikipage.getDate());
          entry.setAuthor(wikipage.getAttribute(WikiConstants.ATTRIBUTE_AUTHOR));
          List links = new ArrayList();

          work(request, wlh2, "view", entry, links, true);
          work(request, wlh2, "pageinfo", entry, links, false);

          entry.setLinks(links);

          String author1 =
              StringUtils.nonNullString(wikipage.getAttribute(WikiConstants.ATTRIBUTE_AUTHOR), "-");
          tmplctx.put("score", decimalFormat.format(srchEntries[i].getScore()));
          tmplctx.put("wikipage", wikipage);
          tmplctx.put("author", author1);
          tmplctx.put(
              "comments",
              StringUtils.nonNullString(
                  wikipage.getAttribute(WikiConstants.ATTRIBUTE_COMMENTS), "-"));
          tmplctx.put(
              "message",
              i18n.str(
                  "actions.search.rss_change_summary",
                  new String[] {
                    String.valueOf(wikipage.getVersion()),
                    WikiUtils.getUserLink(author1),
                    author1,
                    dateformat.format(wikipage.getDate()),
                    WikiViewUtils.decipherURL(wlh2, "diff"),
                    WikiViewUtils.decipherURL(wlh2, "pageinfo")
                  }));
          if (includeLastChange) {
            int lcrev = wce.getPageProvider().getInitialVersion() - 1;
            tmplctx.put(
                "lastchange",
                wce.getPageProvider().getPageRevision(wikipage.getName(), lcrev, lcrev));
          }

          StringWriter stw = new StringWriter();
          we.getWikiTemplateFilesHandler().write("searchaction.rss.html", tmplctx, stw);
          SyndContent description = new SyndContentImpl();
          description.setType("text/html");
          description.setValue(stw.toString());
          entry.setDescription(description);
          entries.add(entry);
        } finally {
          WikiLocal.setWikiCategoryEngine(origwce);
        }
      }

      feed.setEntries(entries);

      request.setContentType("text/xml; charset=" + encoding);

      SyndFeedOutput output = new SyndFeedOutput();
      output.output(feed, request.getWriter());
    } else {

      Map model = new HashMap();
      // model.put("contents", contents);
      // model.put(WikiConstants.ATTRIBUTE_COMMENTS, comments);
      // model.put("pagename", pagename);
      // model.put("date0", date0);
      // model.put("date1", date1);
      model.put("all_categories", allcnames);
      model.put("categories", categories);
      model.put("encoding", encoding);
      model.put("searchresults", srch);
      model.put("dateformat", dateformat);
      model.put("decimalformat", decimalFormat);
      model.put("existing_tags", existingTagsArr);
      model.put(
          "dateformatpattern",
          ((dateformat instanceof SimpleDateFormat)
              ? ((SimpleDateFormat) dateformat).toPattern()
              : "-"));
      model.put("dateformatpatternexample", dateformat.format(new Date()));
      // put a new hashmap, 'cos on Tomcat, it seems that someone is indirectly changing the
      // parameter map
      model.put("request_parameter_map", new HashMap(request.getParameterMap()));

      wlh.setAttribute("wiki.model.search", model);
      showJSPView("search.jsp");
    }
    return RENDER_COMPLETED;
  }

  private static void work(
      WebInteractionContext request,
      WikiLinkHolder wlh2,
      String action,
      SyndEntry entry,
      List links,
      boolean doSetLink) {
    wlh2.setAction(action);
    String s = request.toURLString(wlh2, null);
    s = request.encodeURL(s, true);
    if (links != null) {
      SyndLinkImpl l = new SyndLinkImpl();
      l.setHref(s);
      l.setType("text/html");
      links.add(l);
    }
    if (doSetLink) {
      entry.setLink(s);
    }
  }
}
