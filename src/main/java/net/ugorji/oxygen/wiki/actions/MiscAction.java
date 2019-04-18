/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.actions;

import net.ugorji.oxygen.util.StringUtils;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiUtils;

/**
 * Handles the misc screen (and associated actions) - viewing an arbitrary page
 *
 * @author ugorji
 */
public class MiscAction extends GenericWikiWebAction {

  {
    setFlag(FLAG_REQUIRES_PAGENAME);
  }

  public int processAction() throws Exception {
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    String user = request.getParameter("u");
    if (!StringUtils.isBlank(user)) {
      request.sendRedirect(WikiUtils.getUserLink(user));
      return ACTION_PROCESSING_COMPLETED | REDIRECT_EXTERNAL;
    }
    return ACTION_PROCESSING_COMPLETED;
  }

  public int render() throws Exception {
    showJSPView("misc.jsp");
    return RENDER_COMPLETED;
  }
}
