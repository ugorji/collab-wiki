package net.ugorji.oxygen.wiki.actions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.util.Properties;
import net.ugorji.oxygen.util.OxygenUtils;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiAttachmentProvider;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiLinkHolder;
import net.ugorji.oxygen.wiki.WikiLocal;
import net.ugorji.oxygen.wiki.WikiPageProvider;
import net.ugorji.oxygen.wiki.WikiProvidedObject;
import net.ugorji.oxygen.wiki.WikiUtils;

public class RevertAction extends GenericWikiWebAction {
  {
    setFlag(FLAG_REQUIRES_PAGENAME);
    setFlag(FLAG_WRITE_ACTION);
  }

  public int processAction() throws Exception {
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    String attach = request.getParameter("attachment");
    int version = Integer.parseInt(request.getParameter("vv"));
    WikiProvidedObject wp = WikiUtils.getWikiPage();
    if (attach == null) {
      WikiPageProvider prov = wce.getPageProvider();
      wce.removePageDraft(wp.getName());
      if (version >= prov.getInitialVersion()) {
        WikiProvidedObject wp2 = prov.getPage(wp.getName(), version);
        Properties p = new Properties(wp2.getAttributes());
        p.put("author", request.getUserName());
        StringWriter stw = new StringWriter();
        OxygenUtils.copyStreams(prov.getPageReader(wp2), stw, true);
        prov.savePage(wp.getName(), stw.toString(), p);
      }
      wlh.setAction("pageinfo");
    } else {
      WikiAttachmentProvider prov = wce.getAttachmentProvider();
      WikiProvidedObject wp2 = prov.getAttachment(wp.getName(), attach, version);
      File tmpfile = File.createTempFile("oxywiki-revert-att", ".dat");
      OxygenUtils.copyStreams(
          prov.getAttachmentInputStream(wp.getName(), wp2), new FileOutputStream(tmpfile), true);
      Properties p = new Properties(wp2.getAttributes());
      p.put("author", request.getUserName());
      prov.saveAttachment(wp.getName(), attach, tmpfile, p);
      tmpfile.delete();
      wlh.setExtrainfo(attach);
      wlh.setAction("attachmentinfo");
    }
    return ACTION_PROCESSING_COMPLETED | REDIRECT_AFTER_POST;
  }
}
