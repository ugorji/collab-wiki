/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.actions;

import java.io.PrintWriter;
import java.io.StringReader;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiConstants;
import net.ugorji.oxygen.wiki.WikiLocal;
import net.ugorji.oxygen.wiki.WikiProvidedObject;
import net.ugorji.oxygen.wiki.WikiUtils;

public class MarkupToHTMLAction extends GenericWikiWebAction {

  {
    setFlag(FLAG_REQUIRES_PAGENAME);
  }

  public int render() throws Exception {
    WikiProvidedObject wp = WikiUtils.getWikiPage();
    WebInteractionContext wctx = WebLocal.getWebInteractionContext();
    PrintWriter w = wctx.getWriter();
    try {
      StringReader r = new StringReader(wctx.getParameter(WikiConstants.PARAMETER_TEXT));
      WikiLocal.getWikiCategoryEngine().writeHTML(wp, r, false);
    } catch (Exception exc) {
      w.print("<pre>");
      exc.printStackTrace(w);
      w.print("</pre>");
    } finally {
      w.flush();
    }
    return RENDER_COMPLETED;
  }
}
