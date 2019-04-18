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
import net.ugorji.oxygen.wiki.WikiProvidedObject;
import net.ugorji.oxygen.wiki.WikiUtils;

/**
 * Shows the raw text of the wiki page, as a text/plain
 *
 * @author ugorji
 */
public class RawAction extends GenericWikiWebAction {

  {
    setFlag(FLAG_REQUIRES_PAGENAME);
    setFlag(FLAG_HONORS_VERSION);
    setFlag(FLAG_NOT_HANDLED_BY_PORTLET);
    setFlag(FLAG_MAKE_SHORTHAND);
  }

  public int render() throws Exception {
    // System.out.println("Hi from RawAction");
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    request.setContentType("text/plain; charset=" + wce.getCharacterEncoding());
    WikiProvidedObject wp = WikiUtils.getWikiPage();
    wce.writeText(wp);
    return RENDER_COMPLETED;
  }
}
