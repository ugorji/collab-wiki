/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.actions;

import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiEngine;
import net.ugorji.oxygen.wiki.WikiLinkHolder;
import net.ugorji.oxygen.wiki.WikiLocal;

/**
 * Handles the admin screen (and associated actions)
 *
 * @author ugorji
 */
public class AdminAction extends GenericWikiWebAction {

  {
    setFlag(FLAG_ADMIN_ACTION);
    setFlag(FLAG_MAKE_SHORTHAND);
  }

  public int render() throws Exception {
    showJSPView("admin.jsp");
    return RENDER_COMPLETED;
  }

  public int processAction() throws Exception {
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    // String postAdminAction = request.getParameter("postadminaction");
    String[] categoryNames = request.getParameterValues("cat");
    boolean engine = (request.getParameter("engine") != null);

    WikiEngine we = WikiLocal.getWikiEngine();
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    if (categoryNames != null) {
      boolean load = (request.getParameter("load") != null);
      boolean unload = (request.getParameter("unload") != null);
      boolean reload = (request.getParameter("reload") != null);
      boolean lock = (request.getParameter("lock") != null);
      boolean unlock = (request.getParameter("unlock") != null);
      for (int i = 0; i < categoryNames.length; i++) {
        if (load) {
          we.loadWikiCategoryEngine(categoryNames[i]);
        } else if (unload) {
          we.unloadWikiCategoryEngine(categoryNames[i]);
        } else if (reload) {
          we.reloadWikiCategoryEngine(categoryNames[i]);
        } else if (lock) {
          String mmsg = WebLocal.getI18n().str("general.category_engine_locked", categoryNames[i]);
          we.getWikiCategoryEngine(categoryNames[i]).getLongTermLock().hold(mmsg);
        } else if (unlock) {
          we.getWikiCategoryEngine(categoryNames[i]).getLongTermLock().release();
        }
      }
    }
    if (engine) {
      boolean reload = (request.getParameter("reload") != null);
      boolean reset = (request.getParameter("reset") != null);
      boolean changeconfigdir = (request.getParameter("changeconfigdir") != null);
      if (reset) {
        we.reset();
        // reset the wiki category engine, else we're in trouble
        WikiLocal.setWikiCategoryEngine(WikiLocal.getWikiLinkHolder().getWikiCategoryEngine(true));
      } else if (reload) {
        we.reloadEngineMetadata();
      } else if (changeconfigdir) {
        // String newCfgDir = request.getParameter("configdir");
        // if(newCfgDir != null && newCfgDir.trim().length() > 0) {
        //  Properties pp = we.getInitProps();
        //  pp.setProperty(WikiConstants.ENGINE_CONFIG_DIR_KEY, newCfgDir);
        //  we.reset(pp);
        // }
      }
    }
    // wlh = request.toWikiLinkHolder();
    // System.out.println("wlh.getWikiURL: " + wlh.getWikiURL(WikiConstants.SERVLET_ACTION_ADMIN,
    // false));
    // request.sendRedirect(WikiUtils.getWikiURL(wlh, WikiConstants.SERVLET_ACTION_ADMIN, false,
    // true, true));

    // You want to do a redirectAfterPost is some actions were done
    // else U just want to show the page
    return ((categoryNames == null && !engine)
        ? (ACTION_PROCESSING_COMPLETED)
        : (ACTION_PROCESSING_COMPLETED | REDIRECT_AFTER_POST));
  }
}
