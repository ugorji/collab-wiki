/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.macros;

import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import net.ugorji.oxygen.markup.MarkupMacroParameters;
import net.ugorji.oxygen.markup.MarkupRenderContext;
import net.ugorji.oxygen.web.WebAction;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiConstants;
import net.ugorji.oxygen.wiki.WikiLinkHolder;
import net.ugorji.oxygen.wiki.WikiLocal;
import net.ugorji.oxygen.wiki.WikiProvidedObject;

/**
 * Includes the attachments page in here (thus showing all current attachments) Usage:
 * {attachments/} {attachments:
 *
 * @author ugorji
 */
public class WikiWebActionIncludeViewMacro extends GenericWikiMacro {

  {
    onlyExecuteOnRealPageView = true;
  }

  public void doExecute(Writer writer, MarkupRenderContext rc, MarkupMacroParameters params)
      throws Exception {
    WikiCategoryEngine engine = WikiLocal.getWikiCategoryEngine();
    WebInteractionContext req = WebLocal.getWebInteractionContext();
    if (req != null) {
      WikiLinkHolder lh0 = WikiLocal.getWikiLinkHolder();
      try {
        WikiProvidedObject wp0 = (WikiProvidedObject) rc.get(WikiConstants.PAGE_KEY);
        WikiLinkHolder lh1 = lh0.getClone();
        lh1.setWikiPage(wp0.getName());
        lh1.setVersion(wp0.getVersion());
        WikiLocal.setWikiLinkHolder(lh1);
        String action = params.get(0).trim();
        Map m = params.getParams();
        for (Iterator itr = m.entrySet().iterator(); itr.hasNext(); ) {
          Map.Entry me = (Map.Entry) itr.next();
          lh1.setAttribute(String.valueOf(me.getKey()), String.valueOf(me.getValue()));
        }
        WebAction waction = engine.getWikiEngine().getAction(action);
        waction.includeView();
      } catch (UnsupportedOperationException uexc) {
        // no - op
      } finally {
        WikiLocal.setWikiLinkHolder(lh0);
      }
    }
  }
}
