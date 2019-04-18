/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.actions;

import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiConstants;
import net.ugorji.oxygen.wiki.WikiLocal;

/**
 * Handles the attachments screen (and associated actions) - show all attachments in a list - allow
 * adding of a new attachment
 *
 * @author ugorji
 */
public class AttachmentsAction extends GenericWikiWebAction {

  {
    setFlag(FLAG_REQUIRES_PAGENAME);
    setFlag(FLAG_MAKE_SHORTHAND);
  }

  public void includeView() throws UnsupportedOperationException, Exception {
    includeJSPView("attachments.jsp");
  }

  public int render() throws Exception {
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    if (!wce.isActionSupported(WikiConstants.ACTION_ATTACHMENTS)) {
      showJSPView("editnotsupported.jsp");
      return RENDER_COMPLETED;
    }
    showJSPView("attachments.jsp");
    return RENDER_COMPLETED;
  }
}
