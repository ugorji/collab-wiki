/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.actions;

import java.util.HashMap;
import java.util.Map;
import net.ugorji.oxygen.util.OxygenUtils;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiConstants;
import net.ugorji.oxygen.wiki.WikiLinkHolder;
import net.ugorji.oxygen.wiki.WikiLocal;
import net.ugorji.oxygen.wiki.WikiProvidedObject;
import net.ugorji.oxygen.wiki.WikiUtils;

/**
 * Shows the page info - its subpages, references, versions. Usually, templates may depend on this
 * action to show the extra info and versions info. So this action depends on 2 things, either on
 * the request parameters or on the request attribute. - include.pageinfo.versions (true|false) -
 * include.pageinfo.extrainfo (true|false)
 *
 * @author ugorji
 */
public class PageInfoAction extends GenericWikiWebAction {

  {
    setFlag(FLAG_REQUIRES_PAGENAME);
    setFlag(FLAG_MAKE_SHORTHAND);
  }

  public void includeView() throws UnsupportedOperationException, Exception {
    preRender();
    includeJSPView("pageinfo.jsp");
  }

  public int render() throws Exception {
    preRender();
    showJSPView("pageinfo.jsp");
    return RENDER_COMPLETED;
  }

  private int preRender() throws Exception {
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    // WikiLinkHolder wlh2 = wlh.getClone();
    boolean showversions = "true".equals(WikiUtils.getString("include.pageinfo.versions", "true"));
    boolean showextrainfo =
        "true".equals(WikiUtils.getString("include.pageinfo.extrainfo", "true"));

    int latestver = -1;
    WikiProvidedObject wp = WikiUtils.getWikiPage();
    WikiProvidedObject[] wps = null;
    String pagename = WikiUtils.getString("pagename", null);
    if (pagename == null && wp != null) {
      pagename = wp.getName();
    }

    if (showversions) {
      wp =
          wce.getPageProvider()
              .getPage(pagename, WikiProvidedObject.VERSION_LATEST_DETAILS_UNNECESSARY);
      if (wps == null) {
        wps = wce.getPageProvider().getPageVersionHistory(pagename);
        // System.out.println("wps from wce.getPageProvider().getPageVersionHistory: " + pagename +
        // ": " + wps.length);
      }
      if (wps == null) {
        wps = new WikiProvidedObject[0];
      }
      OxygenUtils.reverseArray(wps);
      if (wps.length != 0) {
        latestver = wps[0].getVersion();
      }
    }

    String[] prefs = new String[0];
    String[] prefby = new String[0];
    String[] subpages = new String[0];
    if (showextrainfo) {
      prefs = wce.getIndexingManager().getPagesThatReference(pagename);
      prefby = wce.getIndexingManager().getPagesReferencedBy(pagename);
      subpages = wce.getIndexingManager().getAllReferersMatching(pagename + "/" + "[^/]+");
    }
    String parentpage = pagename;
    while (true) {
      int idx = parentpage.lastIndexOf("/");
      if (idx > 0) {
        parentpage = parentpage.substring(0, idx);
        if (wce.getIndexingManager().isAReferrer(parentpage)) {
          break;
        }
      } else {
        break;
      }
    }
    // System.out.println("Parent Page: " + parentpage);
    if ((parentpage == pagename) || !(wce.getIndexingManager().isAReferrer(parentpage))) {
      parentpage = null;
    }

    boolean delVersionSupported =
        "true".equals(wce.getProperty(WikiConstants.DELETE_VERSION_SUPPORTED_KEY));

    Map model = new HashMap();

    model.put("page_draft", wce.getPageDraft(pagename));
    model.put("showversions", Boolean.valueOf(showversions));
    model.put("showextrainfo", Boolean.valueOf(showextrainfo));
    model.put("latestver", new Integer(latestver));
    model.put("wikipage", wp);
    model.put("pageversions", wps);
    model.put("thispageisreferencedby", prefby);
    model.put("thispagereferences", prefs);
    model.put("subpages", subpages);
    model.put("pagename", pagename);
    model.put("parentpage", parentpage);
    model.put("del_versions_supported", Boolean.valueOf(delVersionSupported));
    wlh.setAttribute("wiki.model.pageinfo", model);

    // do this, so that we always show details for PageInfo
    wlh.setAttribute(WikiConstants.SHOW_DETAILS_BY_DEFAULT_KEY, "true");
    return ACTION_PROCESSING_COMPLETED;
  }
}
