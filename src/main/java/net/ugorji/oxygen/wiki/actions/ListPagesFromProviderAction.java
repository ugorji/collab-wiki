/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.ugorji.oxygen.util.StringUtils;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiLinkHolder;
import net.ugorji.oxygen.wiki.WikiLocal;

public class ListPagesFromProviderAction extends GenericWikiWebAction {

  {
    setFlag(FLAG_MAKE_SHORTHAND);
  }

  public int render() throws Exception {
    preRender();
    showJSPView("listpages.jsp");
    return RENDER_COMPLETED;
  }

  private void preRender() throws Exception {
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    String[] allpages = wce.getPageProvider().getPageNames("/", Integer.MAX_VALUE, false);
    String[] allpublishedpages = wce.getIndexingManager().getAllReferersMatching(null);
    String[] pages = allpages;
    String s = request.getParameter("published");
    if (!StringUtils.isBlank(s)) {
      if ("true".equals(s)) {
        pages = allpublishedpages;
      } else {
        Collection col = new ArrayList(Arrays.asList(allpages));
        col.removeAll(Arrays.asList(allpublishedpages));
        pages = (String[]) col.toArray(new String[0]);
      }
    }
    Set allfirstchars = new HashSet();
    for (int i = 0; i < pages.length; i++) {
      allfirstchars.add(pages[i].substring(0, 1));
    }
    String[] allfirstcharsarr = (String[]) allfirstchars.toArray(new String[0]);
    Arrays.sort(allfirstcharsarr);

    Map model = new HashMap();
    model.put("firstcharsofpages", allfirstcharsarr);
    model.put("pages", pages);
    wlh.setAttribute("wiki.model.listpages", model);
  }
}
