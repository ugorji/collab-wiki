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
import net.ugorji.oxygen.web.ViewContext;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiConstants;
import net.ugorji.oxygen.wiki.WikiEngine;
import net.ugorji.oxygen.wiki.WikiLinkHolder;
import net.ugorji.oxygen.wiki.WikiLocal;

/** @author ugorji */
public class EditConfigAction extends GenericWikiWebAction {

  {
    setFlag(FLAG_ADMIN_ACTION);
  }

  public int render() throws Exception {
    handleEditConfig();
    ViewContext tctx = WebLocal.getViewContext();
    tctx.setAttribute(WikiConstants.TEMPLATE_SHOWBORDERS_KEY, Boolean.valueOf(false));
    showJSPView("editconfig.jsp");
    return RENDER_COMPLETED;
  }

  private void handleEditConfig() throws Exception {
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    WikiLinkHolder lh = WikiLocal.getWikiLinkHolder();
    WikiCategoryEngine wcengine = WikiLocal.getWikiCategoryEngine();
    WikiEngine we = wcengine.getWikiEngine();
    String configfile = request.getParameter("configfile");
    String configtext = we.getWikiRuntimePersistenceManager().load(configfile);
    if (configtext == null) {
      configtext = "";
    }
    Map model = new HashMap();

    model.put("configfile", configfile);
    model.put("configtext", configtext);
    lh.setAttribute("wiki.model.editconfig", model);
  }
}
