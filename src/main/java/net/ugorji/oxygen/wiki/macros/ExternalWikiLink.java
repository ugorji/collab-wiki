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
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiLocal;

/**
 * Support for external wiki links. Known external wiki's are stored configured in the
 * oxywiki.properties file. Usage is like: [[extwiki jspwiki RecentChanges "JSPWiki recent
 * changes"]]
 *
 * @author ugorji
 */
public class ExternalWikiLink extends GenericWikiMacro {

  {
    onlyExecuteOnRealPageView = true;
  }

  public void doExecute(Writer writer, MarkupRenderContext rc, MarkupMacroParameters params)
      throws Exception {
    WikiCategoryEngine engine = WikiLocal.getWikiCategoryEngine();
    String name = null;
    String page = null;
    String text = null;
    if (params.getLength() > 0) {
      name = params.get(0).trim();
    }
    if (params.getLength() > 1) {
      page = params.get(1).trim();
      text = page;
    }
    if (params.getLength() > 2) {
      text = params.get(2);
    }
    if (page == null) {
      throw new IllegalArgumentException(
          WebLocal.getI18n().str("macros.extwiki.page_not_specified"));
    }
    String url = engine.getShorthandManager().getEvaluatedString(name, page);
    // do not encodeURL ... e.g. encoding ymsgr:sendim?ugorji returns garbage
    // url = WebUtils.encodeURL(url);
    writer.write("<a href=\"" + url + "\" >" + text + "</a>");
  }
}
