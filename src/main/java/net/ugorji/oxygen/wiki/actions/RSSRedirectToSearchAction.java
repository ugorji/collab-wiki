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
import net.ugorji.oxygen.wiki.WikiLinkHolder;
import net.ugorji.oxygen.wiki.WikiLocal;
import net.ugorji.oxygen.wiki.WikiViewUtils;

public class RSSRedirectToSearchAction extends GenericWikiWebAction {

  {
    setFlag(FLAG_MAKE_SHORTHAND);
  }

  public int processAction() throws Exception {
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    String category = wlh.getCategory();
    Map params = new HashMap();
    params.put("wiki.submit", "true");
    params.put("cat", category);
    params.put("rss.format", "rss_2.0");
    params.put("rss", "true");
    params.put("rss_includelastchange", "true");
    params.put("LAST_MODIFIED", "1");

    String url = WikiViewUtils.decipherURL(wlh.getClone(), "search", params);
    request.sendRedirect(url);

    return ACTION_PROCESSING_COMPLETED | REDIRECT_EXTERNAL;
  }
}
