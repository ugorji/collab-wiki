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
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiLinkHolder;
import net.ugorji.oxygen.wiki.WikiLocal;
import net.ugorji.oxygen.wiki.WikiUtils;
import net.ugorji.oxygen.wiki.WikiViewUtils;

/**
 * Handles the diff screen (and associated actions)
 *
 * @author ugorji
 */
public class DiffAction extends GenericWikiWebAction {

  {
    setFlag(FLAG_REQUIRES_PAGENAME);
    setFlag(FLAG_MAKE_SHORTHAND);
  }

  public void includeView() throws UnsupportedOperationException, Exception {
    preRender();
    includeJSPView("diff.jsp");
  }

  public int render() throws Exception {
    preRender();
    showJSPView("diff.jsp");
    return RENDER_COMPLETED;
  }

  private void preRender() throws Exception {
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    Map model = new HashMap();
    model.put("wikipage", WikiUtils.getWikiPage());

    model.put("wikirevision", WikiViewUtils.extractDiffInfo(wce, null));
    wlh.setAttribute("wiki.model.diff", model);
  }
}
