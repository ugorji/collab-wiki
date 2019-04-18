/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki;

import java.util.List;
import net.ugorji.oxygen.util.StringUtils;
import net.ugorji.oxygen.web.JSPTemplateHelper;
import net.ugorji.oxygen.web.TemplateHandler;
import net.ugorji.oxygen.web.ViewContext;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;

public class WikiTemplateHandler extends JSPTemplateHelper implements TemplateHandler {
  private String basefile;

  public WikiTemplateHandler(WikiCategoryEngine wce) {
    basefile = wce.getProperty(WikiConstants.TEMPLATE_BASEFILE_KEY);
    List l =
        StringUtils.tokens(wce.getProperty(WikiConstants.TEMPLATE_TEMPLATES_KEY), ", ", true, true);
    init("/WEB-INF/oxywiki/templates/", (String[]) l.toArray(new String[0]));
  }

  public void render() throws Exception {
    WebInteractionContext req = WebLocal.getWebInteractionContext();
    WikiCategoryEngine engine = WikiLocal.getWikiCategoryEngine();
    ViewContext tctx = WebLocal.getViewContext();
    Boolean showBorders = (Boolean) tctx.getAttribute(WikiConstants.TEMPLATE_SHOWBORDERS_KEY);
    if (showBorders == null) {
      showBorders = Boolean.TRUE;
    }
    tctx.setAttribute(WikiConstants.TEMPLATE_SHOWBORDERS_KEY, showBorders);
    req.include(findResourcePath(basefile));
  }

  /**
   * Shows a JSP view page, by delegating to the template handler for this category, or just
   * inserting directly if none exists.
   *
   * @param request
   * @param response
   * @param jsppage
   * @throws Exception
   */
  public void showView(final String jsppage) throws Exception {
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    ViewContext tctx = WebLocal.getViewContext();
    tctx.setAttribute(WikiConstants.TEMPLATE_JSPPAGE_KEY, jsppage);
    render();
    // WikiProvidedObject wp = WikiUtils.getWikiPage();
    // WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
  }

  /**
   * Include a JSP view page, at the current location
   *
   * @param request
   * @param response
   * @param jsppage
   * @throws Exception
   */
  public void includeView(final String jsppage) throws Exception {
    WebLocal.getWebInteractionContext().include(findResourcePath(jsppage));
  }
}
