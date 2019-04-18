/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki;

import net.ugorji.oxygen.markup.DefaultMarkupRenderContext;
import net.ugorji.oxygen.markup.MarkupMacro;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;

/**
 * Holds information which is used to control rendering. An instance of this is passed to every
 * macro.
 *
 * @author ugorjid
 */
public class WikiRenderContext extends DefaultMarkupRenderContext {
  private WikiCategoryEngine wce;

  public WikiRenderContext(WikiCategoryEngine wce0) throws Exception {
    this(wce0, null, false);
  }

  public WikiRenderContext(WikiCategoryEngine wce0, WikiProvidedObject wp, boolean realPageView)
      throws Exception {
    wce = wce0;
    if (wp == null) {
      wp =
          new WikiProvidedObject(
              (wce == null) ? WikiConstants.DEFAULT_ENTRY_PAGE : wce.getEntryPage());
    }
    if (wce != null) {
      setEmoticonsMgr(wce.getEmoticonsManager());
      setCensoredMgr(wce.getCensoredWordManager());
      setShorthandMgr(wce.getShorthandManager());
      setProps(wce.getProperties());
    }
    set(WikiConstants.SINGLE_PAGE_KEY, Boolean.TRUE);
    if (wce == null) {
      set(WikiConstants.ESCAPE_HTML_KEY, Boolean.TRUE);
      set(WikiConstants.FREELINK_SUPPORTED_KEY, Boolean.TRUE);
      set(WikiConstants.CAMEL_CASE_IS_LINK_KEY, Boolean.TRUE);
      set(WikiConstants.SLASH_SEPARATED_IS_LINK_KEY, Boolean.TRUE);
      set(WikiConstants.DECORATE_EXTERNAL_LINKS_KEY, Boolean.TRUE);
      set(WikiConstants.AS_IS_SUPPORTED_KEY, Boolean.TRUE);
      set(WikiConstants.INLINE_REVIEWS_KEY, Boolean.TRUE);
    } else {
      set(
          WikiConstants.ESCAPE_HTML_KEY,
          Boolean.valueOf("true".equals(wce.getProperty(WikiConstants.ESCAPE_HTML_KEY))));
      set(
          WikiConstants.FREELINK_SUPPORTED_KEY,
          Boolean.valueOf("true".equals(wce.getProperty(WikiConstants.FREELINK_SUPPORTED_KEY))));
      set(
          WikiConstants.CAMEL_CASE_IS_LINK_KEY,
          Boolean.valueOf("true".equals(wce.getProperty(WikiConstants.CAMEL_CASE_IS_LINK_KEY))));
      set(
          WikiConstants.SLASH_SEPARATED_IS_LINK_KEY,
          Boolean.valueOf(
              "true".equals(wce.getProperty(WikiConstants.SLASH_SEPARATED_IS_LINK_KEY))));
      set(
          WikiConstants.DECORATE_EXTERNAL_LINKS_KEY,
          Boolean.valueOf(
              "true".equals(wce.getProperty(WikiConstants.DECORATE_EXTERNAL_LINKS_KEY))));
      set(
          WikiConstants.AS_IS_SUPPORTED_KEY,
          Boolean.valueOf("true".equals(wce.getProperty(WikiConstants.AS_IS_SUPPORTED_KEY))));
      set(
          WikiConstants.INLINE_REVIEWS_KEY,
          Boolean.valueOf("true".equals(wce.getProperty(WikiConstants.INLINE_REVIEWS_KEY))));
      // System.out.println("wce.getProperty(WikiConstants.SLASH_SEPARATED_IS_LINK_KEY): '" +
      // wce.getProperty(WikiConstants.SLASH_SEPARATED_IS_LINK_KEY) + "'");
      // System.out.println(Boolean.valueOf("true".equals(wce.getProperty(WikiConstants.SLASH_SEPARATED_IS_LINK_KEY))));
      // System.out.println(get(WikiConstants.SLASH_SEPARATED_IS_LINK_KEY));
    }
    if (wp != null) {
      set(WikiConstants.PAGE_KEY, wp);
    }
    set(WikiConstants.REAL_PAGE_VIEW_KEY, Boolean.valueOf(realPageView));
  }

  public WikiRenderContext(WikiCategoryEngine engine, WikiProvidedObject wp) throws Exception {
    this(engine, wp, false);
  }

  public String getContentUnavailableString() {
    return WebLocal.getI18n().str("general.content_unavailable");
  }

  public MarkupMacro getMacro(String command) {
    if (wce != null) {
      return wce.getMacro(command);
    }
    return null;
  }

  public boolean isReferenced(String s) {
    if (wce != null) {
      return wce.getIndexingManager().isReferenced(s);
    }
    return false;
  }

  public String getEmoticonLink(String s) {
    WebInteractionContext wikiwebctx = WebLocal.getWebInteractionContext();
    String s2 = super.getEmoticonLink(s);
    if (s2 != null && wikiwebctx != null && !(s2.equals(s))) {
      s2 = wikiwebctx.encodeURL(s2, false);
    }
    return s2;
  }

  public boolean isHTMLTagsSupported() {
    if (wce != null) {
      return ("true".equals(wce.getProperty(WikiConstants.HTML_TAGS_SUPPORTED_KEY)));
    }
    return false;
  }

  public WikiCategoryEngine getWikiCategoryEngine() {
    return wce;
  }
}
