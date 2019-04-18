/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.actions;

import net.ugorji.oxygen.util.OxygenUtils;
import net.ugorji.oxygen.util.StringUtils;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiLinkHolder;
import net.ugorji.oxygen.wiki.WikiLocal;

public class UpdateUserSessionAction extends GenericWikiWebAction {

  {
    setFlag(FLAG_REQUIRES_PAGENAME);
  }

  public int processAction() throws Exception {
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    String s = request.getParameter("locale");
    if (!StringUtils.isBlank(s)) {
      WikiLocal.getWikiUserSession()
          .setLocale(
              WikiLocal.getWikiCategoryEngine().getName(), OxygenUtils.stringToLocale(s), true);
    }
    // redirect to the view
    // wlh.setAction(WikiConstants.SERVLET_ACTION_VIEW);
    // don't redirect. do don't blahAfterPost. This action is only called by AJAX, and then we'd do
    // a redirect with javascript.
    return ACTION_PROCESSING_COMPLETED;
  }
}
