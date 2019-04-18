/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.actions;

import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiLocal;

/**
 * Bogus action, that just says my name
 *
 * @author ugorji
 */
public class SayUgorjiAction extends GenericWikiWebAction {
  public int render() throws Exception {
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    request.setContentType("text/plain; charset=" + wce.getCharacterEncoding());
    request.getWriter().println("Ugorji Nwoke");
    request.getWriter().flush();
    return RENDER_COMPLETED;
  }
}
