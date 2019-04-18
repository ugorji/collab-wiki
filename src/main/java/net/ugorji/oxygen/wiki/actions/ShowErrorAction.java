/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.actions;

import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiConstants;

/**
 * Shows the error when it occurs
 *
 * @author ugorji
 */
public class ShowErrorAction extends GenericWikiWebAction {
  public void includeView() throws UnsupportedOperationException, Exception {
    includeJSPView("showerror.jsp");
  }

  public int render() throws Exception {
    // Since error page might call builtin, we don't want to show borders et al.
    WebLocal.getViewContext().setAttribute(WikiConstants.TEMPLATE_SHOWBORDERS_KEY, Boolean.FALSE);
    showJSPView("showerror.jsp");
    return RENDER_COMPLETED;
  }
}
