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
import net.ugorji.oxygen.wiki.WikiProvidedObject;
import net.ugorji.oxygen.wiki.WikiUtils;

public class ReviewPostAction extends GenericWikiWebAction {

  {
    setFlag(FLAG_REQUIRES_PAGENAME);
    setFlag(FLAG_WRITE_ACTION);
  }

  public int processAction() throws Exception {
    String attkey = EditableAction.getKey(ReviewAction.action);
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    EditableAction.checkEditableSupported(ReviewAction.isSupported(), ReviewAction.action);
    if (WikiUtils.isCaptchaEnabled()) {
      WebLocal.getWebUserSession().checkCaptchaChallenge();
    }
    // ReviewAction.reviewH2();
    boolean save = (request.getParameter("postaction_save") != null);
    boolean cancel = (request.getParameter("postaction_cancel") != null);
    if (save) {
      return handlePostEditableSave();
    } else if (cancel) {
      return handlePostEditableCancel();
    } else {
      throw new Exception("Only Save or Cancel is supported");
    }
  }

  protected static int handlePostEditableSave() throws Exception {
    String attkey = EditableAction.getKey(ReviewAction.action);
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    // handlePostEditableSaveCheck();
    String text = request.getParameter(WikiConstants.PARAMETER_TEXT);

    WikiProvidedObject wp = WikiUtils.getWikiPage();
    EditableAction.ensureNoErrorInEnteredText();
    handlePostEditableSavePersist();
    // wlh.getExtraparams().clear();
    wlh.setAction("view");

    return ACTION_PROCESSING_COMPLETED | REDIRECT_AFTER_POST;
  }

  private static void handlePostEditableSavePersist() throws Exception {
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    String text = request.getParameter(WikiConstants.PARAMETER_TEXT);
    WikiProvidedObject wp = WikiUtils.getWikiPage();
    Properties atts = EditableAction.getAttributesForSave();
    // WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    // System.out.println("In ReviewPostAction.handlePostEditableSavePersist(): text: " + text);
    wce.getPageReviewProvider().savePageReview(wp.getName(), text, atts);
    // request.sendRedirect(wlh.getWikiURL());
  }

  protected int handlePostEditableCancel() throws Exception {
    EditableAction.handlePostEditableCancel();
    return ACTION_PROCESSING_COMPLETED | REDIRECT_AFTER_POST;
  }
}
