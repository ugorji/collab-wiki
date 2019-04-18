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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.bsf.BSFManager;
import net.ugorji.oxygen.markup.MarkupMacroParameters;
import net.ugorji.oxygen.markup.MarkupRenderContext;
import net.ugorji.oxygen.util.I18n;
import net.ugorji.oxygen.util.StringUtils;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiConstants;
import net.ugorji.oxygen.wiki.WikiEngine;
import net.ugorji.oxygen.wiki.WikiLocal;
import net.ugorji.oxygen.wiki.WikiProvidedObject;

public class Script extends GenericWikiMacro {

  {
    onlyExecuteOnRealPageView = true;
  }

  public void doExecute(Writer writer, MarkupRenderContext rc, MarkupMacroParameters params)
      throws Exception {
    I18n wikii18n = WebLocal.getI18n();
    WebInteractionContext req = WebLocal.getWebInteractionContext();
    WikiEngine we = WikiLocal.getWikiEngine();
    WikiCategoryEngine engine = WikiLocal.getWikiCategoryEngine();
    WikiProvidedObject wp = (WikiProvidedObject) rc.get(WikiConstants.PAGE_KEY);

    if (req != null) {
      String content = params.getContent();
      int paramLength = params.getLength();
      String lang = "beanshell";
      boolean userAccessAllowed = true;
      if (userAccessAllowed) {
        List groups = new ArrayList();
        groups.addAll(
            StringUtils.tokens(
                engine.getProperty("net.ugorji.oxygen.wiki.scriptmacro.groups"), ", ", true, true));
        if (paramLength > 0) {
          lang = params.get(0).trim();
        }
        if (paramLength > 1) {
          groups.addAll(StringUtils.tokens(params.get(1).trim(), ", ", true, true));
        }
        if (groups.size() > 0) {
          for (Iterator itr = groups.iterator(); itr.hasNext(); ) {
            String group0 = ((String) itr.next()).trim();
            if (!req.isUserInRole(group0)) {
              userAccessAllowed = false;
              writer.write(wikii18n.str("general.content_unavailable"));
              break;
            }
          }
        }
      }
      if (userAccessAllowed) {
        String prefixpage = engine.getProperty("net.ugorji.oxygen.wiki.scriptmacro.pageprefix");
        if (!StringUtils.isBlank(prefixpage) && !wp.getName().startsWith(prefixpage)) {
          writer.write(wikii18n.str("general.content_unavailable"));
          userAccessAllowed = false;
        }
      }
      if (userAccessAllowed) {
        BSFManager manager = new BSFManager();
        manager.declareBean("wikiEngine", we, we.getClass());
        manager.declareBean("wikiCategoryEngine", engine, engine.getClass());
        manager.declareBean("wikiWriter", writer, writer.getClass());
        manager.declareBean("wikiRenderContext", rc, rc.getClass());
        manager.declareBean("wikiMacroParameters", params, params.getClass());
        manager.exec(lang, getClass().getName(), 0, 0, content);
      }
    }
  }
}
