/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import net.ugorji.oxygen.markup.MarkupLink;
import net.ugorji.oxygen.markup.MarkupUtils;
import net.ugorji.oxygen.web.ViewContext;
import net.ugorji.oxygen.web.WebAction;

/**
 * Helper object for a link representation. It holds state about a link, and the context in and
 * around a link (like attributes, parameters, etc).
 *
 * @author ugorji
 */
public class WikiLinkHolder extends ViewContext implements Cloneable, Serializable {

  private String text;
  private String text2;
  private String category;
  private String pagerep;
  private String contextPage;
  private String extrainfo;
  private String anchor;
  private String nonEvaluatedLink;
  private boolean extLink;
  private boolean explicitLink;
  private boolean aLink = true;
  private String evaluatedExtLink;
  private boolean wikiPageDoesNotExists = false;

  // these extra params are set for use, when you want to infer what a URL will look like
  private Map extraparams = new HashMap();
  private int version = WikiProvidedObject.VERSION_LATEST_DETAILS_UNNECESSARY;

  public WikiLinkHolder() {
    setAction(WikiConstants.ACTION_VIEW);
    setCategory(WikiConstants.BUILTIN_SECTION_NAME);
    setAction(WikiConstants.ACTION_SECTIONS);
  }

  public WikiLinkHolder getClone() {
    try {
      WikiLinkHolder wlh2 = (WikiLinkHolder) super.clone();
      if (extraparams != null) {
        wlh2.extraparams = new HashMap();
        wlh2.extraparams.putAll(extraparams);
      }
      return wlh2;
    } catch (Exception exc) {
      throw new RuntimeException("Unexpected exception getting copy of WikiLinkHolder", exc);
    }
  }

  public String getWikiURL() {
    return WikiUtils.getWikiURL(this, action, null);
  }

  public boolean isExtLink() {
    return extLink;
  }

  public String getURL() throws Exception {
    String url = null;
    if (!aLink) {
      url = nonEvaluatedLink;
    } else if (extLink) {
      url = getExtURL();
    } else {
      url = getWikiURL();
    }
    return url;
  }

  public String getExtURL() throws Exception {
    return evaluatedExtLink;
  }

  public void setExplicitLink(boolean b) {
    explicitLink = b;
  }

  public boolean getExplicitLink() {
    return explicitLink;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String s) {
    action = s;
  }

  public String getText2() {
    return text2;
  }

  public void setText2(String s) {
    text2 = s;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String s) {
    category = s;
  }

  public String getExtrainfo() {
    return extrainfo;
  }

  public void setExtrainfo(String s) {
    extrainfo = s;
  }

  public String getAnchor() {
    return anchor;
  }

  public void setAnchor(String s) {
    anchor = s;
  }

  public String getText() {
    return text;
  }

  public void setText(String s) {
    text = s;
  }

  public String getWikiPage() {
    // /*
    // * Note:
    // * Using USE_CONTEXT_PAGE_TO_RESOLVE_LINKS_KEY is not fully baked.
    // * There is no support for same sibling pages, or uncle/aunt pages
    // * ie ./ABC, ../ABC
    // */

    //     if(wce != null &&
    // "true".equals(wce.getProperty(WikiConstants.USE_CONTEXT_PAGE_TO_RESOLVE_LINKS_KEY))) {
    //       if(pagerep != null && pagerep.startsWith("/")) {
    //         return getCleanPageRep(pagerep);
    //       } else if(contextPage != null) {
    //         return getCleanPageRep(contextPage) + "/" + getCleanPageRep(pagerep);
    //       }
    //     }
    return getCleanPageRep(pagerep);
  }

  public void setWikiPage(String s) {
    pagerep = s;
  }

  public String getContextPage() {
    return contextPage;
  }

  public void setContextPage(String s) {
    contextPage = s;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int v) {
    version = v;
  }

  public WikiCategoryEngine getWikiCategoryEngine(boolean ensureRetrieved) {
    WikiCategoryEngine wce = null;
    if (wce == null) {
      wce = (WikiCategoryEngine) getAttribute(WikiCategoryEngine.class);
    }
    if (wce == null && category != null) {
      WikiEngine engine = WikiLocal.getWikiEngine();
      wce =
          (ensureRetrieved
              ? engine.retrieveWikiCategoryEngine(category)
              : engine.getWikiCategoryEngine(category));
    }
    return wce;
  }

  public WebAction getWikiWebAction() {
    return WikiLocal.getWikiEngine().getAction(action);
  }

  public boolean isALink() {
    return aLink;
  }

  public boolean isWikiPageDoesNotExists() {
    return wikiPageDoesNotExists;
  }

  //  public String getRequestContextPath() {
  //   WebInteractionContext wikiwebctx = WebLocal.getWebInteractionContext();
  //
  //    if(wikiwebctx != null) {
  //      return wikiwebctx.getContextPath();
  //    } else {
  //      return WikiLocal.getWikiEngine().getProperty(WikiConstants.CONTEXT_PATH_KEY);
  //    }
  //    //} else {
  //    //  return "/UNKNOWN_CTX_PATH";
  //    //}
  //  }

  public void parseWikiLinkstr(MarkupLink mlink) throws Exception {
    setExplicitLink(mlink.explicitLink);
    text = mlink.text;
    text2 = mlink.text2;
    parseURLPart(mlink.urlpart);
  }

  private void parseURLPart(String urlpart) throws Exception {
    extLink = false;
    aLink = true;
    int idx = urlpart.indexOf(":");
    if (idx >= 0) {
      nonEvaluatedLink = urlpart;
      String protocol = nonEvaluatedLink.substring(0, idx);
      String linkstr = nonEvaluatedLink.substring(idx + 1);
      if (WikiLocal.getWikiEngine().isCategoryNameRegistered(protocol)) {
        setCategory(protocol);
        doWikiLink(linkstr);
      } else {
        WikiCategoryEngine wce = getWikiCategoryEngine(false);
        if (wce != null && wce.getShorthandManager().hasKey(protocol)) {
          evaluatedExtLink = wce.getShorthandManager().getEvaluatedString(protocol, linkstr);
          // do not encodeURL ... e.g. encoding ymsgr:sendim?ugorji returns garbage
          // evaluatedExtLink = WebUtils.encodeURL(evaluatedExtLink);
          extLink = true;
        } else if (MarkupUtils.genericURLPattern.matcher(nonEvaluatedLink).matches()) {
          extLink = true;
          evaluatedExtLink = nonEvaluatedLink;
        } else {
          aLink = false;
        }
      }
    } else if (MarkupUtils.emailPattern.matcher(urlpart).matches()) {
      nonEvaluatedLink = urlpart;
      evaluatedExtLink = "mailto:" + urlpart;
      extLink = true;
    } else {
      doWikiLink(urlpart);
    }
  }

  private void doWikiLink(String urlpart) throws Exception {
    int idx = 0;
    // int idx2 = 0;
    while (urlpart.startsWith("/")) {
      // e.g. /////page2
      urlpart = urlpart.substring(1);
    }
    pagerep = urlpart;
    idx = urlpart.indexOf("#");
    if (idx >= 0) {
      anchor = urlpart.substring(idx + 1);
      pagerep = urlpart.substring(0, idx);
    } else {
      idx = urlpart.indexOf("^");
      if (idx >= 0) {
        action = WikiConstants.ACTION_VIEW_ATTACHMENT;
        extrainfo = urlpart.substring(idx + 1).trim();
        pagerep = urlpart.substring(0, idx);
      }
    }
    if (pagerep.length() == 0) {
      pagerep = contextPage;
    } else {
      // pagerep = WikiUtils.toCamelCase(pagerep);
      pagerep = WikiUtils.toUsablePageRep(pagerep);
    }
    // it is possible that the indexing manager is not yet initialized
    // like if it is calling this object ...
    WikiCategoryEngine wce = getWikiCategoryEngine(false);
    if (wce != null) {
      wikiPageDoesNotExists = !(wce.getIndexingManager().isAReferrer(pagerep));
    }
  }

  private static String getCleanPageRep(String s) {
    if (s != null) {
      s = s.trim();
      while (s.startsWith("/")) {
        s = s.substring(1);
      }
      while (s.endsWith("/")) {
        s = s.substring(0, s.length() - 1);
      }
    }
    return s;
  }
}

/**
 * Gets the full URL given a relative one. Since this object knows the context path, it can give the
 * appropriate absolute URL (without the host/port though) Note that we will need to call
 * wikiwebctx.encode(s) here (just in case we're in a portlet context)
 *
 * @param relativeURL
 * @return
 */
/*
public String getURLGivenRelative(String relativeURL) {
  StringBuffer buf = new StringBuffer();
  buf.append(getRequestContextPath());
  buf.append(relativeURL);
  String s = buf.toString();
  WebInteractionContext wikiwebctx = WebLocal.getWebInteractionContext();
  if(wikiwebctx != null) {
    s = wikiwebctx.encodeURL(s, false);
  }
  return s;
}
*/
