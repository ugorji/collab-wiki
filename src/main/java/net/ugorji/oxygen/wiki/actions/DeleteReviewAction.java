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
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiConstants;
import net.ugorji.oxygen.wiki.WikiLinkHolder;
import net.ugorji.oxygen.wiki.WikiLocal;
import net.ugorji.oxygen.wiki.WikiUtils;

public class DeleteReviewAction extends GenericWikiWebAction {
  {
    setFlag(FLAG_REQUIRES_PAGENAME);
    setFlag(FLAG_WRITE_ACTION);
  }

  public int processAction() throws Exception {
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    String pagename = WikiUtils.getString("pagename", null);
    String reviewname = WikiUtils.getString("reviewname", null);
    if (pagename == null) {
      pagename = wlh.getWikiPage();
    }
    if (reviewname == null) {
      reviewname = wlh.getExtrainfo();
    }
    String author = request.getUserName();
    Properties p2 = new Properties();
    p2.setProperty(WikiConstants.ATTRIBUTE_AUTHOR, author);
    WikiUtils.extractProps(WikiConstants.REQUEST_PARAM_ATTRIBUTE_PREFIX, p2);
    wce.getPageReviewProvider().deletePageReview(pagename, reviewname, p2);
    wlh.setAction(WikiConstants.ACTION_REVIEW);

    return ACTION_PROCESSING_COMPLETED | REDIRECT_AFTER_POST;
  }
}
