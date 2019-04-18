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
import java.util.Properties;
import net.ugorji.oxygen.util.OxygenIntRange;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiConstants;
import net.ugorji.oxygen.wiki.WikiLinkHolder;
import net.ugorji.oxygen.wiki.WikiLocal;
import net.ugorji.oxygen.wiki.WikiProvidedObject;
import net.ugorji.oxygen.wiki.WikiUtils;

public class DeleteAction extends GenericWikiWebAction {

  {
    setFlag(FLAG_REQUIRES_PAGENAME);
    setFlag(FLAG_WRITE_ACTION);
  }

  public int processAction() throws Exception {
    // now actually do the delete
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    WikiProvidedObject wp = WikiUtils.getWikiPage();
    // WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();

    WebInteractionContext request = WebLocal.getWebInteractionContext();
    boolean deleteVersions = "true".equals(request.getParameter("deleteversions"));
    String author = request.getUserName();
    Properties p2 = new Properties();
    p2.setProperty(WikiConstants.ATTRIBUTE_AUTHOR, author);
    WikiUtils.extractProps(WikiConstants.REQUEST_PARAM_ATTRIBUTE_PREFIX, p2);
    if (deleteVersions) {
      int[] vi = WikiUtils.getIntArray("v", new int[0]);
      if (vi.length > 0) {
        wce.getPageProvider().deletePageVersions(wp.getName(), p2, new OxygenIntRange(vi));
      }
    } else {
      wce.getPageProvider().deletePage(wp.getName(), p2);
    }
    return ACTION_PROCESSING_COMPLETED;
  }

  public int render() throws Exception {
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();

    String deleteversions = request.getParameter("deleteversions");
    String[] versionsToDelete = request.getParameterValues("v");
    if (versionsToDelete == null || versionsToDelete.length == 0) {
      versionsToDelete = new String[0];
      deleteversions = "false";
    }

    Map model = new HashMap();
    model.put("wikipage", WikiUtils.getWikiPage());
    model.put("deleteversions", Boolean.valueOf(deleteversions));
    model.put("versionstodelete", versionsToDelete);
    wlh.setAttribute("wiki.model.pagedeleted", model);

    showJSPView("pagedeleted.jsp");
    return RENDER_COMPLETED;
  }
}
