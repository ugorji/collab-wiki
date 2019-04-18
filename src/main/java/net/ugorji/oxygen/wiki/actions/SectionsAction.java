/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.actions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import net.ugorji.oxygen.util.OxyTable;
import net.ugorji.oxygen.util.OxygenTimeElapsed;
import net.ugorji.oxygen.util.SimpleInt;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiEngine;
import net.ugorji.oxygen.wiki.WikiIndexingManager;
import net.ugorji.oxygen.wiki.WikiLinkHolder;
import net.ugorji.oxygen.wiki.WikiLocal;

/**
 * Shows the sections screen - all available categories - how many pages they have, and last update
 * time
 *
 * @author ugorji
 */
public class SectionsAction extends GenericWikiWebAction {

  {
    setFlag(FLAG_MAKE_SHORTHAND);
  }

  public void includeView() throws UnsupportedOperationException, Exception {
    preRender();
    includeJSPView("sections.jsp");
  }

  public int render() throws Exception {
    preRender();
    showJSPView("sections.jsp");
    return RENDER_COMPLETED;
  }

  private void preRender() throws Exception {
    String[] headers = new String[] {"0", "1", "2", "3", "4"};
    OxyTable tbl = new OxyTable(headers);
    WikiEngine we = WikiLocal.getWikiEngine();
    String[] cnames = we.getRegisteredAndLoadedWikiCategoryNames();
    Arrays.sort(cnames);

    int numPages = 0, numAttachments = 0, numReviews = 0, numLocks = 0;
    Map tags = new TreeMap(); // so it's sorted
    for (int i = 0; i < cnames.length; i++) {
      WikiCategoryEngine wce = we.getWikiCategoryEngine(cnames[i]);
      WikiIndexingManager imgr = wce.getIndexingManager();
      // String lastupdatestr = "-";
      // if(wce.getLastUpdateDate() != null) {
      //  lastupdatestr = df.format(wce.getLastUpdateDate());
      // }
      String desc = wce.getProperty("net.ugorji.oxygen.wiki.description");
      // if(desc == null) {
      //  desc = "-";
      // }
      int wceNumLocks = wce.getWikiEditManager().getAllLocks().size();
      tbl.addRow(
          new Object[] {
            cnames[i],
            new Integer(wce.getIndexingManager().getAllReferersMatching(null).length),
            new Integer(wceNumLocks),
            wce.getLastUpdateDate(),
            desc
          });
      tags = mergeTagsMaps(tags, imgr.lookupExistingTagsWithCount());
      numPages += imgr.getAllReferersMatching(null).length;
      numAttachments += imgr.lookupAttachmentNames(null, null, null).length;
      numReviews += imgr.lookupPageReviewNames(null, null, null).length;
      numLocks += wceNumLocks;
    }

    WebInteractionContext wctx = WebLocal.getWebInteractionContext();
    String sortParam = wctx.getParameter("sort");
    if (sortParam != null) {
      tbl.sort(Integer.parseInt(sortParam));
    }

    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    Map model = new HashMap();
    model.put("table", tbl);
    wlh.setAttribute("wiki.model.sections", model);

    model = new HashMap();
    OxygenTimeElapsed timepast = new OxygenTimeElapsed();
    timepast.reset(we.getStartTime(), System.currentTimeMillis(), OxygenTimeElapsed.DAYS_PRECISION);
    // System.out.println("Time Past: " + ((System.currentTimeMillis() - we.getStartTime())/1000) +
    // " seconds");

    model.put("uptime", timepast);
    model.put("tags", tags);
    model.put("total_num_pages", new Long(numPages));
    model.put("total_num_attachments", new Long(numAttachments));
    model.put("total_num_reviews", new Long(numReviews));
    model.put("total_num_sections", new Long(cnames.length));
    model.put("total_num_locks", new Long(numLocks));
    model.put("total_num_sessions", new Long(we.getNumOpenSessions()));
    wlh.setAttribute("wiki.model.engine_details", model);
  }

  private Map mergeTagsMaps(Map m0, Map m1) {
    Set s0 = m0.keySet();
    Set s1 = m1.keySet();
    Set s2 = new HashSet();
    for (Iterator itr = s0.iterator(); itr.hasNext(); ) {
      String s = (String) itr.next();
      if (s1.contains(s)) {
        s2.add(s);
      }
    }
    TreeMap m2 = new TreeMap();
    m2.putAll(m0);
    m2.putAll(m1);
    for (Iterator itr = s2.iterator(); itr.hasNext(); ) {
      String s = (String) itr.next();
      int i = ((SimpleInt) m0.get(s)).get() + ((SimpleInt) m1.get(s)).get();
      SimpleInt si = new SimpleInt();
      si.set(i);
      m2.put(s, si);
    }
    return m2;
  }
}
