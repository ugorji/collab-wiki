/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.actions;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import net.ugorji.oxygen.markup.MarkupRenderContext;
import net.ugorji.oxygen.markup.MarkupRenderEngine;
import net.ugorji.oxygen.util.NullWriter;
import net.ugorji.oxygen.util.StringUtils;
import net.ugorji.oxygen.web.TemplateHandler;
import net.ugorji.oxygen.web.ViewContext;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiConstants;
import net.ugorji.oxygen.wiki.WikiLinkHolder;
import net.ugorji.oxygen.wiki.WikiLocal;
import net.ugorji.oxygen.wiki.WikiProvidedObject;
import net.ugorji.oxygen.wiki.WikiRenderContext;
import net.ugorji.oxygen.wiki.WikiUtils;

public class EditableAction {
  protected static final String SHOW_PREVIEW_KEY = "show.preview";
  protected static final String LOCK_NOT_ACQUIRED_KEY = "lock.not.acquired";

  public static int render(String action, String jsppage, boolean isSupported) throws Exception {
    String attkey = getKey(action);
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    Map model = (Map) wlh.getAttribute(attkey);
    final WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    final WikiProvidedObject wp = WikiUtils.getWikiPage();
    final String text = (String) model.get(WikiConstants.PARAMETER_TEXT);
    if (!isSupported) {
      GenericWikiWebAction.showJSPView("editnotsupported.jsp");
      return GenericWikiWebAction.RENDER_COMPLETED;
    }
    TemplateHandler thdlr = wce.getWikiTemplateHandler();
    ViewContext tctx = WebLocal.getViewContext();
    if (Boolean.TRUE.equals(wlh.getAttribute(LOCK_NOT_ACQUIRED_KEY))) {
      GenericWikiWebAction.showJSPView("locknotacquired.jsp");
    } else {
      tctx.setAttribute(WikiConstants.TEMPLATE_SHOWBORDERS_KEY, Boolean.valueOf(false));
      GenericWikiWebAction.showJSPView(jsppage);
    }
    return GenericWikiWebAction.RENDER_COMPLETED;
  }

  protected static Map handleEditableInitModel(String attkey) throws Exception {
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    Map model = (Map) wlh.getAttribute(attkey);
    if (model == null) {
      model = new HashMap();
      WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
      WikiProvidedObject wp = WikiUtils.getWikiPage();

      model.put("wikipage", wp);

      wlh.setAttribute(attkey, model);
    }
    return model;
  }

  protected static void handleEditableInit(String attkey) throws Exception {
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    Map model = handleEditableInitModel(attkey);
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    Exception pexc = (Exception) wlh.getAttribute("editable.exception");
    String text = request.getParameter(WikiConstants.PARAMETER_TEXT);
    model.put(WikiConstants.PARAMETER_TEXT, StringUtils.nonNullString(text));
    model.put("exception", pexc);
  }

  protected static void checkEditableSupported(boolean isSupported, String action)
      throws Exception {
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    if (!isSupported) {
      throw new Exception(action + " is not supported for this section");
    }
  }

  protected static void handlePostEditableCancel() throws Exception {
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    wlh.setAction(WikiConstants.ACTION_VIEW);
    // request.sendRedirect(WikiUtils.getWikiURL(wlh, WikiConstants.SERVLET_ACTION_VIEW, true, true,
    // true));
    // wlh.getExtraparams().clear();
  }

  protected static void ensureNoErrorInEnteredText() throws Exception {
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    String text = request.getParameter(WikiConstants.PARAMETER_TEXT);
    WikiProvidedObject wp = WikiUtils.getWikiPage();
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    MarkupRenderEngine re = wce.getRenderEngine();
    MarkupRenderContext rc = new WikiRenderContext(wce, wp, false);
    re.render(new NullWriter(), new StringReader(text), rc, Integer.MAX_VALUE);
  }

  protected static Properties getAttributesForSave() throws Exception {
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    String author = request.getUserName();
    Properties atts = new Properties();
    atts.setProperty(WikiConstants.ATTRIBUTE_AUTHOR, author);
    WikiUtils.extractProps(WikiConstants.REQUEST_PARAM_ATTRIBUTE_PREFIX, atts);
    if (!WikiUtils.allowPublish()) {
      atts.remove("published");
    }
    return atts;
  }

  protected static String getKey(String action) {
    return "wiki.model.editable." + action;
  }
}

  /*
  private static boolean handleIfErrorInEnteredText(String attkey) throws Exception {
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    String text = request.getParameter(WikiConstants.PARAMETER_TEXT);
    WikiProvidedObject wp = WikiUtils.getWikiPage();
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    final WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    Exception pexc = null;
    try {
      MarkupRenderEngine re = wce.getRenderEngine();
      MarkupRenderContext rc = new WikiRenderContext(wce, wp, false);
      re.render(new NullWriter(), new StringReader(text), rc, Integer.MAX_VALUE);
    } catch(Exception exc) {
      pexc = exc;
    }
    if(pexc != null) {
      wlh.setAttribute("editable.text", text);
      wlh.setAttribute("editable.exception", pexc);
    }
    return (pexc != null);
  }
  */
