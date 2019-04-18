/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki;

import java.util.Locale;
import net.ugorji.oxygen.util.OxyLocal;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;

/**
 * This class is used to set things which are only available within the execution of a servlet.
 *
 * @author ugorjid
 */
public class WikiLocal {
  public static WikiCategoryEngine getWikiCategoryEngine() {
    return (WikiCategoryEngine) OxyLocal.get(WikiCategoryEngine.class);
  }

  public static void setWikiCategoryEngine(WikiCategoryEngine o) {
    OxyLocal.set(WikiCategoryEngine.class, o);
    if (o != null) {
      WebLocal.setProperties(o.getProperties());
    }
  }

  public static WikiLinkHolder getWikiLinkHolder() {
    return (WikiLinkHolder) WebLocal.getViewContext();
  }

  public static void setWikiLinkHolder(WikiLinkHolder o) {
    WebLocal.setViewContext(o);
  }

  public static WikiEngine getWikiEngine() {
    return (WikiEngine) OxyLocal.get(WikiEngine.class);
  }

  public static void setWikiEngine(WikiEngine engine) {
    OxyLocal.set(WikiEngine.class, engine);
    if (engine != null) {
      WebLocal.setProperties(engine.getProperties());
      if (engine.getI18nManager() != null) {
        WebInteractionContext wctx = WebLocal.getWebInteractionContext();
        Locale locale = (wctx == null) ? null : wctx.getLocale();
        WebLocal.setI18n(engine.getI18nManager().getI18n(locale));
      }
    }
  }

  public static WikiTemplateHandler getWikiTemplateHandler() {
    return (WikiTemplateHandler) WebLocal.getTemplateHandler();
  }

  public static WikiUserSession getWikiUserSession() {
    return (WikiUserSession) WebLocal.getWebUserSession();
  }
}
