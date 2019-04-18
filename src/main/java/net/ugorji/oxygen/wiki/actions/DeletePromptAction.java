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
import net.ugorji.oxygen.wiki.WikiException;
import net.ugorji.oxygen.wiki.WikiLinkHolder;
import net.ugorji.oxygen.wiki.WikiLocal;
import net.ugorji.oxygen.wiki.WikiUtils;

public class DeletePromptAction extends GenericWikiWebAction {

  {
    setFlag(FLAG_REQUIRES_PAGENAME);
    setFlag(FLAG_WRITE_ACTION);
    setFlag(FLAG_MAKE_SHORTHAND);
  }

  public int render() throws Exception {
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    // now actually do the delete
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    String pagename = WikiUtils.getString("pagename", null);
    String attachment = WikiUtils.getString("attachment", null);
    String deleteversions = request.getParameter("deleteversions");
    String[] versionsToDelete = request.getParameterValues("v");
    if (versionsToDelete == null || versionsToDelete.length == 0) {
      versionsToDelete = new String[0];
      deleteversions = "false";
    }
    if (pagename == null) {
      pagename = wlh.getWikiPage();
    } else {
      wlh.setWikiPage(pagename);
    }
    // do this, so that the deleteattachment link will work
    if (attachment != null) {
      wlh.setExtrainfo(attachment);
    }

    if (StringUtils.isBlank(pagename)) {
      throw new WikiException("No page or attachment name is provided");
    }

    Map model = new HashMap();
    model.put("pagename", pagename);
    if (!StringUtils.isBlank(attachment)) {
      model.put("attachment", attachment);
    }
    model.put("deleteversions", Boolean.valueOf(deleteversions));
    model.put("versionstodelete", versionsToDelete);
    wlh.setAttribute("wiki.model.deleteprompt", model);

    showJSPView("deleteprompt.jsp");
    return RENDER_COMPLETED;
  }
}
