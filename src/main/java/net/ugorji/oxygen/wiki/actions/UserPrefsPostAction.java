/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.actions;

import java.util.Enumeration;
import java.util.Properties;
import net.ugorji.oxygen.manager.UserPreferencesManager;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiConstants;
import net.ugorji.oxygen.wiki.WikiLinkHolder;
import net.ugorji.oxygen.wiki.WikiLocal;
import net.ugorji.oxygen.wiki.WikiUtils;

public class UserPrefsPostAction extends GenericWikiWebAction {

  {
    setFlag(FLAG_ADMIN_ACTION);
  }

  public int processAction() throws Exception {
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    // final WikiProvidedObject wp = WikiUtils.getWikiPage();
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    // WikiTemplateHandler thdlr = wce.getTemplateHandler();
    boolean saveAll = (request.getParameter("postuserprefsaction_save_all") != null);
    boolean setUsername = (request.getParameter("postuserprefsaction_set_username") != null);
    boolean clearUsername = (request.getParameter("postuserprefsaction_clear_username") != null);
    // boolean cancel = (request.getParameter("postuserprefsaction_cancel") != null);
    String s = null;
    if (saveAll || setUsername) {
      String username = request.getUserName();
      String username2 = request.getParameter("username");
      if (WikiUtils.isSetUsernameSupported()
          && username2 != null
          && username2.trim().length() > 0) {
        request.setUserName(username2);
        username = username2;
      }
      if (saveAll) {
        Properties atts = new Properties();
        atts.setProperty(WikiConstants.ATTRIBUTE_AUTHOR, username);
        atts.setProperty(WikiConstants.ATTRIBUTE_COMMENTS, "config file updated");

        UserPreferencesManager prefmgr = wce.getWikiEngine().getUserPreferencesManager();

        for (Enumeration enum0 = request.getParameterNames(); enum0.hasMoreElements(); ) {
          String key = (String) enum0.nextElement();
          if (key.startsWith("post")) {
            continue;
          }
          String[] val = request.getParameterValues(key);
          prefmgr.setForUser(username, key, val);
        }
        prefmgr.save(atts);

        // set the user's locale in their session
        // WikiLocal.getWikiUserSession().setDefaultLocale(OxygenUtils.stringToLocale(request.getParameter("locale")));
      }
    } else if (clearUsername) {
      request.removeUserName();
    }
    wlh.setAction("userprefs");
    // request.sendRedirect(WikiUtils.getWikiURL(wlh, wlh.getAction(), false, true, true));
    return ACTION_PROCESSING_COMPLETED | REDIRECT_AFTER_POST;
  }
}
