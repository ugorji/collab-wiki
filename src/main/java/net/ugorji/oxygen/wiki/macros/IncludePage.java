/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.macros;

import java.io.Reader;
import java.io.Writer;
import net.ugorji.oxygen.markup.MarkupMacroParameters;
import net.ugorji.oxygen.markup.MarkupRenderContext;
import net.ugorji.oxygen.util.CloseUtils;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiLocal;
import net.ugorji.oxygen.wiki.WikiProvidedObject;

/**
 * Includes another page in this page IncludePage can include any page which exists (whether or not
 * it is published) Usage: {include:Examples/Random /}
 *
 * @author ugorji
 */
public class IncludePage extends GenericWikiMacro {

  {
    onlyExecuteOnRealPageView = true;
  }

  public void doExecute(Writer writer, MarkupRenderContext rc, MarkupMacroParameters params)
      throws Exception {
    Reader r = null;
    try {
      WebInteractionContext req = WebLocal.getWebInteractionContext();
      if (req != null) {
        WikiCategoryEngine engine = WikiLocal.getWikiCategoryEngine();
        String s = params.get(0).trim();
        int indexOfColon = s.indexOf(':');
        if (indexOfColon != -1) {
          engine = engine.getWikiEngine().retrieveWikiCategoryEngine(s.substring(0, indexOfColon));
          s = s.substring(indexOfColon + 1);
        }
        // if(engine.getIndexingManager().isAReferrer(s)) {
        if (engine.getPageProvider().pageExists(s)) {
          WikiProvidedObject wp = new WikiProvidedObject(s);
          r = engine.getPageProvider().getPageReader(wp);
          engine.writeHTML(r, writer, rc);
        }
      }
    } finally {
      CloseUtils.close(r);
    }
  }
}
