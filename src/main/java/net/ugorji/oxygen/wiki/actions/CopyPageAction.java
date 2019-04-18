/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.actions;

import java.util.Properties;
import net.ugorji.oxygen.util.StringUtils;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiConstants;
import net.ugorji.oxygen.wiki.WikiLocal;
import net.ugorji.oxygen.wiki.WikiPageProvider;
import net.ugorji.oxygen.wiki.WikiProvidedObject;
import net.ugorji.oxygen.wiki.WikiUtils;

public class CopyPageAction extends GenericWikiWebAction {

  {
    setFlag(FLAG_WRITE_ACTION);
  }

  public int processAction() throws Exception {
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    String src = request.getParameter("source");
    String dest = request.getParameter("dest");
    boolean del = "true".equals(request.getParameter("delete"));

    if (StringUtils.isBlank(src) || StringUtils.isBlank(dest)) {
      throw new NullPointerException();
    }

    String author = request.getUserName();
    Properties p2 = null;

    WikiPageProvider prov = wce.getPageProvider();

    if (WikiUtils.isCaptchaEnabled()) {
      WebLocal.getWebUserSession().checkCaptchaChallenge();
    }

    WikiProvidedObject wp = prov.getPage(src, WikiProvidedObject.VERSION_LATEST_DETAILS_NECESSARY);

    p2 = new Properties();
    p2.putAll(wp.getAttributes());
    p2.setProperty(WikiConstants.ATTRIBUTE_AUTHOR, author);
    WikiUtils.extractProps(WikiConstants.REQUEST_PARAM_ATTRIBUTE_PREFIX, p2);

    String pagetext = StringUtils.readerToString(prov.getPageReader(wp));
    prov.savePage(dest, pagetext, p2);

    // do the delete
    if (del) {
      p2 = new Properties();
      p2.setProperty(WikiConstants.ATTRIBUTE_AUTHOR, author);
      WikiUtils.extractProps(WikiConstants.REQUEST_PARAM_ATTRIBUTE_PREFIX, p2);

      prov.deletePage(src, p2);
    }
    return ACTION_PROCESSING_COMPLETED;
  }

  public int render() throws Exception {
    showJSPView("copypage.jsp");
    return RENDER_COMPLETED;
  }
}
