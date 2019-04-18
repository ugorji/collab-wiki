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
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiLocal;

/**
 * outputs the value of a property onto the wiki page Usage is like: {property:wiki.name/}
 *
 * @author ugorji
 */
public class GetProperty extends GenericWikiMacro {

  {
    onlyExecuteOnRealPageView = true;
  }

  public void doExecute(Writer writer, MarkupRenderContext rc, MarkupMacroParameters params)
      throws Exception {
    WikiCategoryEngine engine = WikiLocal.getWikiCategoryEngine();
    String key = params.get(0).trim();
    String val = engine.getProperty(key);
    writer.write(val);
  }
}
