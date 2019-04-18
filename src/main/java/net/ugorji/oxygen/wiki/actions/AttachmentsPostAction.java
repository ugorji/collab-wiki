/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.actions;

import java.io.File;
import java.util.Properties;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiAttachmentProvider;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiConstants;
import net.ugorji.oxygen.wiki.WikiLinkHolder;
import net.ugorji.oxygen.wiki.WikiLocal;
import net.ugorji.oxygen.wiki.WikiProvidedObject;
import net.ugorji.oxygen.wiki.WikiUtils;

public class AttachmentsPostAction extends GenericWikiWebAction {

  {
    setFlag(FLAG_REQUIRES_PAGENAME);
    setFlag(FLAG_WRITE_ACTION);
  }

  public int processAction() throws Exception {
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    checkAttachmentSupported();
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    WikiProvidedObject wp = WikiUtils.getWikiPage();
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    File[] files = request.getUploadedFiles();
    WikiAttachmentProvider prov = wce.getAttachmentProvider();
    String author = request.getUserName();
    boolean deleteAfterUpload0 = deleteAfterUpload(wce);
    if (files != null && files.length > 0) {
      for (int i = 0; i < files.length; i++) {
        File f = files[i];
        Properties p2 = new Properties();
        p2.setProperty(WikiConstants.ATTRIBUTE_AUTHOR, author);
        WikiUtils.extractProps(WikiConstants.REQUEST_PARAM_ATTRIBUTE_PREFIX, p2);
        prov.saveAttachment(wp.getName(), f.getName(), f, p2);
        if (deleteAfterUpload0) {
          f.delete();
        }
      }
    }
    wlh.setAction("attachments");
    // request.sendRedirect(wlh.getWikiURL());
    return ACTION_PROCESSING_COMPLETED | REDIRECT_AFTER_POST;
  }

  private boolean deleteAfterUpload(WikiCategoryEngine wce) throws Exception {
    return "true"
        .equals(wce.getProperty(WikiConstants.DELETE_TEMP_UPLOADED_ATTACHMENTS_AFTER_SAVE));
  }

  private void checkAttachmentSupported() throws Exception {
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    if (!wce.isActionSupported(WikiConstants.ACTION_ATTACHMENTS)) {
      throw new Exception("Attachment is not supported for this section");
    }
  }
}
