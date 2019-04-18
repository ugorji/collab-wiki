/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.actions;

import java.util.Properties;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiConstants;
import net.ugorji.oxygen.wiki.WikiEngine;
import net.ugorji.oxygen.wiki.WikiLinkHolder;
import net.ugorji.oxygen.wiki.WikiLocal;

public class EditConfigPostAction extends GenericWikiWebAction {

  {
    setFlag(FLAG_ADMIN_ACTION);
  }

  public int processAction() throws Exception {
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    // System.out.println("handlePostEditConfig called");
    WikiEngine we = WikiLocal.getWikiEngine();
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    boolean save = (request.getParameter("posteditconfigaction_save") != null);
    if (save) {
      // System.out.println("handlePostEditConfig save called");
      String text = request.getParameter("configtext");
      String configfile = request.getParameter("configfile");
      Properties atts = new Properties();
      atts.setProperty(WikiConstants.ATTRIBUTE_AUTHOR, request.getUserName());
      atts.setProperty(WikiConstants.ATTRIBUTE_COMMENTS, "config file updated");
      we.getWikiRuntimePersistenceManager().store(configfile, text, atts);
    }
    wlh.setAction(WikiConstants.ACTION_ADMIN);
    // request.sendRedirect(WikiUtils.getWikiURL(wlh, WikiConstants.SERVLET_ACTION_ADMIN, false,
    // true, true));
    return ACTION_PROCESSING_COMPLETED | REDIRECT_AFTER_POST;
  }
}
