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
import net.ugorji.oxygen.util.OxygenIntRange;
import net.ugorji.oxygen.util.StringUtils;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiConstants;
import net.ugorji.oxygen.wiki.WikiException;
import net.ugorji.oxygen.wiki.WikiLinkHolder;
import net.ugorji.oxygen.wiki.WikiLocal;
import net.ugorji.oxygen.wiki.WikiUtils;

public class DeleteAttachmentAction extends GenericWikiWebAction {

  {
    setFlag(FLAG_REQUIRES_EXTRAINFO);
    setFlag(FLAG_REQUIRES_PAGENAME);
    setFlag(FLAG_WRITE_ACTION);
  }

  public int processAction() throws Exception {
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    // now actually do the delete
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    String pagename = WikiUtils.getString("pagename", null);
    String attachment = WikiUtils.getString("attachment", null);
    boolean deleteVersions = "true".equals(request.getParameter("deleteversions"));
    if (pagename == null) {
      pagename = wlh.getWikiPage();
    }
    if (attachment == null) {
      attachment = wlh.getExtrainfo();
    }
    if (StringUtils.isBlank(pagename) || StringUtils.isBlank(attachment)) {
      throw new WikiException("No page or attachment name is provided");
    }
    String author = request.getUserName();
    Properties p2 = new Properties();
    p2.setProperty(WikiConstants.ATTRIBUTE_AUTHOR, author);
    WikiUtils.extractProps(WikiConstants.REQUEST_PARAM_ATTRIBUTE_PREFIX, p2);
    if (deleteVersions) {
      int[] vi = WikiUtils.getIntArray("v", new int[0]);
      if (vi.length > 0) {
        wce.getAttachmentProvider()
            .deleteAttachmentVersions(pagename, attachment, p2, new OxygenIntRange(vi));
      }
      wlh.setAction(WikiConstants.ACTION_ATTACHMENTINFO);
    } else {
      wce.getAttachmentProvider().deleteAttachment(pagename, attachment, p2);
      wlh.setAction(WikiConstants.ACTION_ATTACHMENTS);
    }

    return ACTION_PROCESSING_COMPLETED | REDIRECT_AFTER_POST;
    // request.sendRedirect(WikiUtils.getWikiURL(wlh, WikiConstants.SERVLET_ACTION_ATTACHMENTS,
    // true, true, true));
    // return REQUEST_PROCESSING_COMPLETED;
  }
}
