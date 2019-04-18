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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import net.ugorji.oxygen.markup.MarkupConstants;
import net.ugorji.oxygen.markup.MarkupMacroParameters;
import net.ugorji.oxygen.markup.MarkupParser;
import net.ugorji.oxygen.markup.MarkupRenderContext;
import net.ugorji.oxygen.util.I18n;
import net.ugorji.oxygen.util.StringUtils;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiConstants;
import net.ugorji.oxygen.wiki.WikiLocal;
import net.ugorji.oxygen.wiki.WikiParser2Base;
import net.ugorji.oxygen.wiki.WikiProvidedObject;
import net.ugorji.oxygen.wiki.WikiRenderContext;

/** @author ugorji */
public class TOC extends GenericWikiMacro {
  private static Pattern htmltagpattern = Pattern.compile("<.*?>");

  {
    onlyExecuteOnRealPageView = true;
  }

  public void doExecute(Writer writer, MarkupRenderContext rc, MarkupMacroParameters params)
      throws Exception {
    final WikiCategoryEngine engine = WikiLocal.getWikiCategoryEngine();
    String s = null;
    String s2 = null;
    WebInteractionContext req = WebLocal.getWebInteractionContext();
    I18n i18n = WebLocal.getI18n();
    WikiProvidedObject wp0 = (WikiProvidedObject) rc.get(WikiConstants.PAGE_KEY);
    boolean realPageView = ((Boolean) rc.get(MarkupConstants.REAL_PAGE_VIEW_KEY)).booleanValue();
    boolean singlePage = ((Boolean) rc.get(WikiConstants.SINGLE_PAGE_KEY)).booleanValue();
    if (realPageView && singlePage && req != null && wp0 != null) {
      // if(true)throw new RuntimeException("What is it");
      // if(true)throw new IOException("What is it");
      Reader r = engine.getPageProvider().getPageReader(wp0);
      TOCWikiParserBase tocparser = new TOCWikiParserBase(engine, wp0.getName(), r);

      Map m0 = new HashMap();

      m0.put("tocTitle", i18n.str("macro.toc.toc_title"));
      m0.put("tocShowHide", i18n.str("macro.toc.toc_show_hide"));
      s2 = params.get("show");
      s = "block";
      if (!StringUtils.isBlank(s2)) {
        s = (("true".equals(s2)) ? "block" : "none");
      }
      m0.put("displayValue", s);
      m0.put("tocHeaderLevels", tocparser.tocHeaderLevels);
      m0.put("tocHeaderStrings", tocparser.tocHeaderStrings);
      m0.put("anchorprefix", WikiConstants.IMPLICIT_PAGE_HEADER_ANCHOR_PREFIX);
      m0.put("jump", "jump");

      WikiLocal.getWikiEngine().getWikiTemplateFilesHandler().write("macro.toc.html", m0, writer);
    }
  }

  private class TOCWikiParserBase extends WikiParser2Base {
    public List tocHeaderLevels = new ArrayList();
    public List tocHeaderStrings = new ArrayList();

    public TOCWikiParserBase(WikiCategoryEngine engine, String pagename, Reader r)
        throws Exception {
      super();
      setRenderContext(new WikiRenderContext(engine, new WikiProvidedObject(pagename)));
      setRenderEngine(engine.getRenderEngine());
      MarkupParser mp = engine.getMarkupParserFactory().newMarkupParser(r);
      mp.setMarkupParserBase(this);
      mp.markupToHTML();
    }

    public void headerstart(int level) {
      super.headerstart(level);
      addToMemoryBuffer = true;
      membuf = new StringBuffer();
    }

    public void headerend() {
      tocHeaderLevels.add(new Integer(currentHeaderLevel));
      String s = membuf.toString();
      s = htmltagpattern.matcher(s).replaceAll("");
      tocHeaderStrings.add(s);
      addToMemoryBuffer = false;
      membuf = new StringBuffer(2);
      super.headerend();
    }
  }
}
