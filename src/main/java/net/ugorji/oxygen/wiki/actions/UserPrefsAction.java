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
import net.ugorji.oxygen.util.StringUtils;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiLinkHolder;
import net.ugorji.oxygen.wiki.WikiLocal;

/**
 * Shows user preferences screen
 *
 * @author ugorji
 */
public class UserPrefsAction extends GenericWikiWebAction {

  {
    setFlag(FLAG_MAKE_SHORTHAND);
  }

  public int render() throws Exception {
    handleUserPrefs();
    showJSPView("userprefs.jsp");
    return RENDER_COMPLETED;
  }

  private void handleUserPrefs() throws Exception {
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    // fill up the model, and put on the request attributes

    String username = request.getUserName();
    Map m = wce.getWikiEngine().getUserPreferencesManager().getForUser(username);
    String emailaddress =
        StringUtils.nonNullString(StringUtils.getSingleValue(m.get("emailaddress")));
    String subscriptions =
        StringUtils.nonNullString(StringUtils.getSingleValue(m.get("subscriptions")));
    String locale = StringUtils.nonNullString(StringUtils.getSingleValue(m.get("locale")));

    Map model = new HashMap();
    model.put("username", username);
    model.put("emailaddress", emailaddress);
    model.put("subscriptions", subscriptions);
    model.put("locale", locale);
    model.put("locales", wce.getSupportedUILocales());
    wlh.setAttribute("wiki.model.userprefs", model);
  }
}
