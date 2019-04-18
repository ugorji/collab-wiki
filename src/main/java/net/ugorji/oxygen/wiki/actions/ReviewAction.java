/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.actions;

import java.util.Arrays;
import java.util.Map;
import net.ugorji.oxygen.util.StringUtils;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiConstants;
import net.ugorji.oxygen.wiki.WikiLinkHolder;
import net.ugorji.oxygen.wiki.WikiLocal;
import net.ugorji.oxygen.wiki.WikiProvidedObject;
import net.ugorji.oxygen.wiki.WikiUserSession;
import net.ugorji.oxygen.wiki.WikiUtils;

/**
 * Shows the review screen - allows you to save a review to a page
 *
 * @author ugorji
 */
public class ReviewAction extends GenericWikiWebAction {

  protected static String action = "review";
  protected static String jsppage = "reviews.jsp";

  {
    setFlag(FLAG_REQUIRES_PAGENAME);
    setFlag(FLAG_MAKE_SHORTHAND);
  }

  public void includeView() throws UnsupportedOperationException, Exception {
    reviewH2();
    includeJSPView(jsppage);
  }

  public int render() throws Exception {
    reviewH2();
    return EditableAction.render(action, jsppage, isSupported());
  }

  public static boolean isSupported() {
    return WikiLocal.getWikiCategoryEngine().isActionSupported(WikiConstants.ACTION_REVIEW);
  }

  public static void reviewH2() throws Exception {
    EditableAction.checkEditableSupported(isSupported(), action);
    EditableAction.handleEditableInit(EditableAction.getKey(action));

    WikiUserSession wus = WikiLocal.getWikiUserSession();
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    Map model = EditableAction.handleEditableInitModel(EditableAction.getKey(action));
    Exception pexc = (Exception) wlh.getAttribute("editable.exception");
    String text = (String) wlh.getAttribute("editable.text");
    text = StringUtils.toHTMLEscape(StringUtils.nonNullString(text), false, false);

    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    WikiProvidedObject wp = WikiUtils.getWikiPage();
    // System.out.println("ReviewAction: wp.getName(): " + wp.getName());
    String[] sarr = wce.getIndexingManager().lookupPageReviewNames(wp.getName(), null, null);
    Arrays.sort(sarr);
    // System.out.println("ReviewAction: sarr: " + Arrays.asList(sarr));
    WikiProvidedObject[] reviews = new WikiProvidedObject[sarr.length];
    int vv = WikiProvidedObject.VERSION_LATEST_DETAILS_NECESSARY;
    for (int i = 0; i < sarr.length; i++) {
      reviews[i] = wce.getPageReviewProvider().getPageReview(wp.getName(), sarr[i], vv);
    }

    model.put("reviews", reviews);
    model.put("exception", pexc);
    model.put(WikiConstants.PARAMETER_TEXT, text);
    wlh.setAttribute(EditableAction.getKey(action), model);
  }
}
