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
import net.ugorji.oxygen.markup.MarkupMacroParameters;
import net.ugorji.oxygen.markup.MarkupRenderContext;
import net.ugorji.oxygen.web.WebAction;
import net.ugorji.oxygen.wiki.WikiLocal;

public class IncludeActionView extends GenericWikiMacro {

  public void doExecute(Writer writer, MarkupRenderContext rc, MarkupMacroParameters params)
      throws Exception {
    String action = params.get(0).trim();
    WebAction waction = WikiLocal.getWikiEngine().getAction(action);
    waction.includeView();
  }
}
