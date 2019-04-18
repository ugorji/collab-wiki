/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import net.ugorji.oxygen.util.OxygenUtils;
import net.ugorji.oxygen.util.StringUtils;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.web.WebUserSession;

public class WikiUserSession extends WebUserSession {

  private Map locales = new HashMap();
  private Map viewTrail = new Hashtable();

  private Date lastCalendarDate;

  public void addViewTrail() throws Exception {
    WebInteractionContext req = WebLocal.getWebInteractionContext();
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    WikiProvidedObject wp = WikiUtils.getWikiPage();
    Vector vec = (Vector) viewTrail.get(wce.getName());
    if (vec == null) {
      vec = new Vector();
    }
    if (wp != null && wp.getName() != null) {
      vec.remove(wp.getName());
      vec.add(wp.getName());
    }
    viewTrail.put(wce.getName(), vec);
  }

  public String[] getViewTrail(int max) {
    WebInteractionContext req = WebLocal.getWebInteractionContext();
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    List list = (List) viewTrail.get(wce.getName());
    if (list == null) {
      list = new ArrayList(2);
    }
    int sz = list.size();
    if (sz > max) {
      list = list.subList(sz - max, sz);
    }
    // subList(from, to)
    return (String[]) list.toArray(new String[0]);
  }

  public Locale getLocale(String section) throws Exception {
    WikiCategoryEngine wce = wce(section);
    WebInteractionContext req = WebLocal.getWebInteractionContext();
    Locale locale = null;
    locale = (Locale) locales.get(section);
    // if(locale == null) {
    //  locale = defaultLocale;
    // }
    if (locale == null || wce == null || !(wce.isSupportedUILocale(locale))) {
      String username = req.getUserName();
      String s =
          StringUtils.getSingleValue(
              WikiLocal.getWikiEngine().getUserPreferencesManager().getForUser(username, "locale"));
      locale = OxygenUtils.stringToLocale(s);
    }
    if (locale == null || wce == null || !(wce.isSupportedUILocale(locale))) {
      // locale = req.getLocale();
      locale = WikiUtils.extractLocaleFromRequest(req);
    }
    if (locale == null || (wce != null && !(wce.isSupportedUILocale(locale)))) {
      locale = wce.getDefaultLocale();
    }
    setLocale(section, locale, false);
    return locale;
  }

  public void setLocale(String section, Locale locale0, boolean addToMap) {
    Locale locale = (Locale) locales.get(section);
    if (locale0 == null || locale0.equals(locale)) {
      return;
    }
    locale = locale0;
    if (addToMap) {
      locales.put(section, locale);
    }
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    wlh.setLocale(locale);
    WebLocal.setI18n(WikiLocal.getWikiEngine().getI18nManager().getI18n(locale));
  }

  public Date getLastCalendarDate() {
    return lastCalendarDate;
  }

  public void setLastCalendarDate(Date lastCalendarDate) {
    this.lastCalendarDate = lastCalendarDate;
  }

  // public Locale getDefaultLocale() {
  //  return defaultLocale;
  // }

  // public void setDefaultLocale(Locale defaultLocale) {
  //  this.defaultLocale = defaultLocale;
  // }

  private WikiCategoryEngine wce(String section) {
    WikiEngine we = WikiLocal.getWikiEngine();
    WikiCategoryEngine wce = we.getWikiCategoryEngine(section);
    return wce;
  }
}
