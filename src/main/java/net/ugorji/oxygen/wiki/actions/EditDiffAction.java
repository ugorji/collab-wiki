package net.ugorji.oxygen.wiki.actions;

import java.io.StringWriter;
import java.io.Writer;
import net.ugorji.oxygen.util.OxygenRevision;
import net.ugorji.oxygen.util.OxygenUtils;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiLocal;
import net.ugorji.oxygen.wiki.WikiProvidedObject;
import net.ugorji.oxygen.wiki.WikiUtils;

public class EditDiffAction extends GenericWikiWebAction {
  {
    setFlag(FLAG_REQUIRES_PAGENAME);
  }

  public int render() throws Exception {
    WebInteractionContext wctx = WebLocal.getWebInteractionContext();
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    WikiProvidedObject wp = WikiUtils.getWikiPage();
    StringWriter stw = new StringWriter();
    OxygenUtils.copyStreams(wce.getPageProvider().getPageReader(wp), stw, true);
    String text1 = stw.toString();
    String text2 = wctx.getParameter("text");
    OxygenRevision rev = OxygenRevision.getDiff(text1, text2, WebLocal.getI18n());
    Writer w = wctx.getWriter();
    rev.writeHTML(w);
    w.flush();
    return RENDER_COMPLETED;
  }
}
