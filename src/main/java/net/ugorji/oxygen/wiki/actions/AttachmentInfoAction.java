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
import net.ugorji.oxygen.util.OxygenUtils;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiLinkHolder;
import net.ugorji.oxygen.wiki.WikiLocal;
import net.ugorji.oxygen.wiki.WikiProvidedObject;
import net.ugorji.oxygen.wiki.WikiUtils;

/**
 * Shows the attachment info - its versions.
 *
 * @author ugorji
 */
public class AttachmentInfoAction extends GenericWikiWebAction {

  {
    setFlag(FLAG_REQUIRES_EXTRAINFO);
    setFlag(FLAG_REQUIRES_PAGENAME);
    setFlag(FLAG_MAKE_SHORTHAND);
  }

  public void includeView() throws UnsupportedOperationException, Exception {
    preRender();
    includeJSPView("attachmentinfo.jsp");
  }

  public int render() throws Exception {
    preRender();
    showJSPView("attachmentinfo.jsp");
    return RENDER_COMPLETED;
  }

  private void preRender() throws Exception {
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    String pagename = WikiUtils.getString("pagename", null);
    String attachment = WikiUtils.getString("attachment", null);
    if (pagename == null) {
      pagename = wlh.getWikiPage();
    }
    if (attachment == null) {
      attachment = wlh.getExtrainfo();
    }
    WikiProvidedObject[] wps = null;
    if (wps == null) {
      wps = wce.getAttachmentProvider().getAttachmentVersionHistory(pagename, attachment);
    }
    if (wps == null) {
      wps = new WikiProvidedObject[0];
    }
    OxygenUtils.reverseArray(wps);
    Map model = new HashMap();

    model.put("wikipagename", pagename);
    model.put("wikiattachmentname", attachment);
    model.put("attachmentversions", wps);
    wlh.setAttribute("wiki.model.attachmentinfo", model);
  }
}
