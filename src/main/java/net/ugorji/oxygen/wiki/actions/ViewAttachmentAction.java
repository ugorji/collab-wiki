/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.actions;

import java.io.InputStream;
import java.io.OutputStream;
import net.ugorji.oxygen.util.CloseUtils;
import net.ugorji.oxygen.util.StringUtils;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiAttachmentProvider;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiEngine;
import net.ugorji.oxygen.wiki.WikiException;
import net.ugorji.oxygen.wiki.WikiLinkHolder;
import net.ugorji.oxygen.wiki.WikiLocal;
import net.ugorji.oxygen.wiki.WikiProvidedObject;

/**
 * Handles viewing an attachment screen
 *
 * @author ugorji
 */
public class ViewAttachmentAction extends GenericWikiWebAction {

  {
    setFlag(FLAG_REQUIRES_EXTRAINFO);
    setFlag(FLAG_REQUIRES_PAGENAME);
    setFlag(FLAG_HONORS_VERSION);
    setFlag(FLAG_NOT_HANDLED_BY_PORTLET);
    setFlag(FLAG_MAKE_SHORTHAND);
  }

  public int render() throws Exception {
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    InputStream in = null;
    OutputStream out = null;
    try {
      WikiEngine we = WikiLocal.getWikiEngine();
      WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
      // WikiProvidedObject wp = WikiUtils.getWikiPage();
      WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
      // WikiTemplateHandler thdlr = wce.getTemplateHandler();

      WikiAttachmentProvider prov = wce.getAttachmentProvider();
      String attname = wlh.getExtrainfo();
      if (StringUtils.isBlank(attname)) {
        throw new WikiException("No attachment name is provided");
      }
      WikiProvidedObject att = new WikiProvidedObject(attname);
      att.setVersion(wlh.getVersion());
      String mimetype = request.getMimeType(att.getName());
      if (mimetype == null) {
        mimetype = "application/binary";
      }
      request.setContentType(mimetype);
      request.setHeader("Content-Disposition", "inline; filename=\"" + att.getName() + "\";");
      in = prov.getAttachmentInputStream(wlh.getWikiPage(), att);
      out = request.getOutputStream();
      int read = 0;
      byte buffer[] = new byte[8192];
      while ((read = in.read(buffer)) > -1) {
        out.write(buffer, 0, read);
      }
      return RENDER_COMPLETED;
    } finally {
      CloseUtils.close(in);
      CloseUtils.close(out);
    }
  }
}
