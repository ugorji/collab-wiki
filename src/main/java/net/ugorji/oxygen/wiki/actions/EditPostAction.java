/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.actions;

import java.util.List;
import java.util.Properties;
import net.ugorji.oxygen.util.StringUtils;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiConstants;
import net.ugorji.oxygen.wiki.WikiEditLock;
import net.ugorji.oxygen.wiki.WikiException;
import net.ugorji.oxygen.wiki.WikiLinkHolder;
import net.ugorji.oxygen.wiki.WikiLocal;
import net.ugorji.oxygen.wiki.WikiProvidedObject;
import net.ugorji.oxygen.wiki.WikiUtils;

public class EditPostAction extends GenericWikiWebAction {

  {
    setFlag(FLAG_REQUIRES_PAGENAME);
    setFlag(FLAG_WRITE_ACTION);
  }

  public int processAction() throws Exception {
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    // EditAction.editH2();
    boolean save = (request.getParameter("postaction_save") != null);
    boolean cancel = (request.getParameter("postaction_cancel") != null);
    if (save) {
      EditableAction.checkEditableSupported(EditAction.isSupported(), EditAction.action);
      if (WikiUtils.isCaptchaEnabled()) {
        WebLocal.getWebUserSession().checkCaptchaChallenge();
      }
      return handlePostEditableSave();
    } else if (cancel) {
      return handlePostEditableCancel();
    } else {
      throw new Exception("Only Save or Cancel is supported");
    }
  }

  protected static int handlePostEditableSave() throws Exception {
    String attkey = EditableAction.getKey(EditAction.action);
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    handlePostEditableSaveCheck();
    String text = request.getParameter(WikiConstants.PARAMETER_TEXT);
    WikiProvidedObject wp = WikiUtils.getWikiPage();
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    EditableAction.ensureNoErrorInEnteredText();

    handlePostEditableSavePersist();
    // wlh.getExtraparams().clear();

    return ACTION_PROCESSING_COMPLETED | REDIRECT_AFTER_POST;
  }

  protected static void handlePostEditableSaveCheck() throws Exception {
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    final WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    WikiProvidedObject wp = WikiUtils.getWikiPage();
    String author = request.getUserName();
    if (wce.getWikiEditManager().isLockExclusive()) {
      WikiEditLock elock = wce.getWikiEditManager().getLock(wp.getName(), author);
      if (elock == null) {
        // System.out.println("No lock exist for: " + wp.getName());
        throw new WikiException(WebLocal.getI18n().str("actions.edit.lock_not_exist"));
      }
    }
  }

  protected static void handlePostEditableSavePersist() throws Exception {
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    String text = request.getParameter(WikiConstants.PARAMETER_TEXT);
    WikiProvidedObject wp = WikiUtils.getWikiPage();
    Properties atts = EditableAction.getAttributesForSave();

    // if constrainTags, ensure that only selected tags are allowed
    boolean constrainTags = "true".equals(wce.getProperty(WikiConstants.CONSTRAIN_TAGS_KEY));
    if (constrainTags) {
      // System.out.println("atts: " + atts);
      List allowedTags =
          StringUtils.tokens(
              StringUtils.nonNullString(wce.getProperty(WikiConstants.ALLOWED_TAGS_KEY)),
              " ",
              true,
              true);
      List currTags = StringUtils.tokens(atts.getProperty("tags"), " ", true, true);
      currTags.retainAll(allowedTags);
      atts.setProperty("tags", StringUtils.toString(currTags, " "));
    }

    try {
      wce.getPageProvider().savePage(wp.getName(), text, atts);
    } finally {
      wce.getWikiEditManager().releaseLock(wp.getName(), request.getUserName());
    }
    wlh.setAction(WikiConstants.ACTION_VIEW);
    // request.sendRedirect(WikiUtils.getWikiURL(wlh, WikiConstants.SERVLET_ACTION_VIEW, true, true,
    // true));
  }

  protected int handlePostEditableCancel() throws Exception {
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    WikiProvidedObject wp = WikiUtils.getWikiPage();
    wce.getWikiEditManager().releaseLock(wp.getName(), request.getUserName());
    EditableAction.handlePostEditableCancel();
    return ACTION_PROCESSING_COMPLETED | REDIRECT_AFTER_POST;
  }
}
