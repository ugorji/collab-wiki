/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.actions;

import net.ugorji.oxygen.util.I18n;
import net.ugorji.oxygen.web.TemplateHandler;
import net.ugorji.oxygen.web.ViewContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiConstants;
import net.ugorji.oxygen.wiki.WikiLinkHolder;
import net.ugorji.oxygen.wiki.WikiLocal;
import net.ugorji.oxygen.wiki.WikiPageNotFoundException;
import net.ugorji.oxygen.wiki.WikiProvidedObject;
import net.ugorji.oxygen.wiki.WikiUtils;

/**
 * Shows a view page (with borders and decorations)
 *
 * @author ugorji
 */
public class ViewAction extends GenericWikiWebAction {
  protected boolean showBorders = true;
  protected boolean realPageView = true;

  public ViewAction() {
    setFlag(FLAG_REQUIRES_PAGENAME);
    setFlag(FLAG_HONORS_VERSION);
    setFlag(FLAG_MAKE_SHORTHAND);
  }

  // Ensure we work well, even if a page is deleted, we can still see its past versions
  public int render() throws Exception {
    final WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    final WikiProvidedObject wp = WikiUtils.getWikiPage();
    WikiLocal.getWikiUserSession().addViewTrail();
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    TemplateHandler thdlr = wce.getWikiTemplateHandler();
    ViewContext tctx = WebLocal.getViewContext();
    tctx.setAttribute(WikiConstants.TEMPLATE_SHOWBORDERS_KEY, Boolean.valueOf(showBorders));

    // if(wce.getPageProvider().pageExists(wp.getName())) {
    // Convenience check, so we can throw a 404 if an innocent view of a non-existent page
    if (wp.getVersion() == WikiProvidedObject.VERSION_LATEST_DETAILS_UNNECESSARY
        && !wce.getIndexingManager().isAReferrer(wp.getName())) {
      // Now, throw an exception, if page does not exist. So it can be interpreted for a 404.
      I18n wi18n = WebLocal.getI18n();
      throw new WikiPageNotFoundException(
          wi18n.str("jspviews.pagenotexist.msg", wp.getName()), wp.getName(), wlh.getCategory());
    } else {
      tctx.setAttribute(WikiConstants.TEMPLATE_WIKIPAGE_KEY, wp);
      tctx.setAttribute(WikiConstants.TEMPLATE_REALPAGEVIEW_KEY, Boolean.valueOf(realPageView));
      tctx.setAttribute(WikiConstants.TEMPLATE_JSPPAGE_KEY, "includejsp.jsp");
    }
    thdlr.render();
    return RENDER_COMPLETED;
  }
}
