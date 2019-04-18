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
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiCalendarHelper;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiConstants;
import net.ugorji.oxygen.wiki.WikiLinkHolder;
import net.ugorji.oxygen.wiki.WikiLocal;
import net.ugorji.oxygen.wiki.WikiProvidedObject;
import net.ugorji.oxygen.wiki.WikiUtils;

public class WikiCalendarViewAction extends GenericWikiWebAction {

  {
    setFlag(FLAG_REQUIRES_PAGENAME);
    setFlag(FLAG_MAKE_SHORTHAND);
  }

  public int render() throws Exception {
    preRender();
    showJSPView("calendarview.jsp");
    return RENDER_COMPLETED;
  }

  private void preRender() throws Exception {
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    WikiUtils.ensureCalendarBrowseCanBeDone(wce);
    WebInteractionContext ctx = WebLocal.getWebInteractionContext();
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    final WikiProvidedObject wp = WikiUtils.getWikiPage();
    String datestr = wp.getName();
    // set datestr in the context
    WikiCalendarHelper wch = new WikiCalendarHelper(datestr);
    wlh.setAttribute(WikiConstants.WIKI_CALENDAR_LAST_DATE_KEY, wch.getOrigdate());
    // System.out.println("WikiCalendarViewAction: d: " + wch.origdate);
    Map model = new HashMap();

    model.put("wikicalendarhelper", wch);
    wlh.setAttribute("wiki.model.calendarview", model);
  }
}
