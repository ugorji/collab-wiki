/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.actions;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiCalendarHelper;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiConstants;
import net.ugorji.oxygen.wiki.WikiLinkHolder;
import net.ugorji.oxygen.wiki.WikiLocal;
import net.ugorji.oxygen.wiki.WikiUtils;

public class WikiCalendarAction extends GenericWikiWebAction {

  {
    setFlag(FLAG_REQUIRES_PAGENAME);
  }

  public void includeView() throws UnsupportedOperationException, Exception {
    preRender();
    includeJSPView("calendar.jsp");
  }

  public int render() throws Exception {
    preRender();
    showJSPView("calendar.jsp");
    return RENDER_COMPLETED;
  }

  private void preRender() throws Exception {
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    WikiUtils.ensureCalendarBrowseCanBeDone(wce);
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    Date d = (Date) wlh.getAttribute(WikiConstants.WIKI_CALENDAR_LAST_DATE_KEY);
    // System.out.println("WikiCalendar: d: " + d);
    if (d == null) {
      d = new Date();
    }
    WikiCalendarHelper wch = new WikiCalendarHelper(d);
    // get datestr from the context
    Map model = new HashMap();

    model.put("wikicalendarhelper", wch);
    wlh.setAttribute("wiki.model.calendar", model);
  }
}
