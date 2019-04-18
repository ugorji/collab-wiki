package net.ugorji.oxygen.wiki.actions;

import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiLocal;
import net.ugorji.oxygen.wiki.WikiProvidedObject;
import net.ugorji.oxygen.wiki.WikiUtils;

public class SaveDraftAction extends GenericWikiWebAction {
  {
    setFlag(FLAG_REQUIRES_PAGENAME);
    setFlag(FLAG_WRITE_ACTION);
  }

  public int processAction() throws Exception {
    WikiProvidedObject wp = WikiUtils.getWikiPage();
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    WebInteractionContext wctx = WebLocal.getWebInteractionContext();
    String author = wctx.getUserName();
    wce.savePageDraft(wp.getName(), author, wctx.getParameter("text"));
    wce.getWikiEditManager().acquireLock(wp.getName(), author);
    return ACTION_PROCESSING_COMPLETED;
  }

  public int render() throws Exception {
    WebInteractionContext wctx = WebLocal.getWebInteractionContext();
    wctx.getWriter().write(WebLocal.getI18n().str("actions.savedraft.page_draft_saved"));
    return RENDER_COMPLETED;
  }
}
