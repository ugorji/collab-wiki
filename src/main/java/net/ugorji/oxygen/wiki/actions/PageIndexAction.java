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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import net.ugorji.oxygen.util.OxygenUtils;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiLinkHolder;
import net.ugorji.oxygen.wiki.WikiLocal;
import net.ugorji.oxygen.wiki.WikiUtils;

/**
 * Shows the page index (all pages in the wiki)
 *
 * @author ugorji
 */
public class PageIndexAction extends GenericWikiWebAction {

  {
    setFlag(FLAG_MAKE_SHORTHAND);
  }

  public void includeView() throws UnsupportedOperationException, Exception {
    preRender();
    includeJSPView("pageindex.jsp");
  }

  public int render() throws Exception {
    preRender();
    showJSPView("pageindex.jsp");
    return RENDER_COMPLETED;
  }

  private void preRender() throws Exception {
    Collection al = null;
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    WikiLinkHolder wlh2 = wlh.getClone();
    String regex = WikiUtils.getString("regex", null);
    OxygenUtils.debug("pageindex.jsp: regex: " + regex);
    String[] pages = wce.getIndexingManager().getAllReferersMatching(regex);
    OxygenUtils.debug("pageindex.jsp: pages: " + Arrays.asList(pages));
    List allFromProv =
        Arrays.asList(wce.getPageProvider().getPageNames("", Integer.MAX_VALUE, true));
    // List allWithoutDeletedFromProv = Arrays.asList(wce.getPageProvider().getPageNames("",
    // Integer.MAX_VALUE, false));
    al = new HashSet();
    al.addAll(allFromProv);
    // System.out.println("PageIndex: al: " + al);
    // al.removeAll(allWithoutDeletedFromProv);
    al.removeAll(Arrays.asList(pages));
    String[] deletedpages = (String[]) al.toArray(new String[0]);
    Arrays.sort(deletedpages);

    Map pageDrafts = wce.getPageDrafts();

    Map tags = wce.getIndexingManager().lookupExistingTagsWithCount();

    Map model = new HashMap();
    model.put("pagedrafts", pageDrafts);
    model.put("page_draft_supported", Boolean.valueOf(wce.isPageDraftSupported()));
    model.put("tags", tags);
    model.put("pages", pages);
    model.put("deletedpages", deletedpages);
    model.put(
        "attachments",
        wce.getAttachmentProvider().getAllAttachmentNames(null, Integer.MAX_VALUE - 20, false));
    model.put(
        "deletedattachments",
        wce.getAttachmentProvider().getAllAttachmentNames(null, Integer.MAX_VALUE - 20, true));
    wlh.setAttribute("wiki.model.pageindex", model);
  }
}

/*
    final Map attachments = new HashMap();
    HitsHandler myhithdlr = new HitsHandler() {
        public void handleHits(Hits hits) throws Exception {
          int numhits = hits.length();
          for(int i = 0; i < numhits; i++) {
            Document doc = hits.doc(i);
            String[] atts = doc.getValues(WikiConstants.SEARCH_INDEX_ATTACHMENT_NAME);
            Arrays.sort(atts);
            attachments.put(doc.get(WikiConstants.SEARCH_INDEX_PAGENAME), Arrays.asList(atts));
          }
        }
      };

    Comparator comp = new Comparator() {
        public int compare(Object o1, Object o2) {
          return ((((String[])o1)[0]).compareTo(((String[])o2)[0]));
        }
      };

    Map querymap = new HashMap();
    querymap.put(WikiConstants.SEARCH_INDEX_INDEX_TYPE, WikiConstants.SEARCH_INDEX_ATTACHMENT_NAME);
    wce.getIndexingManager().search(querymap, true, myhithdlr);

*/
