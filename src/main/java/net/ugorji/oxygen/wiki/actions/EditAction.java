/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.actions;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import net.ugorji.oxygen.util.StringUtils;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiConstants;
import net.ugorji.oxygen.wiki.WikiEvent;
import net.ugorji.oxygen.wiki.WikiLinkHolder;
import net.ugorji.oxygen.wiki.WikiLocal;
import net.ugorji.oxygen.wiki.WikiProvidedObject;
import net.ugorji.oxygen.wiki.WikiUtils;

/**
 * Handles the edit screen (and associated actions) - show edit screen - handle save / preview, etc
 *
 * @author ugorji
 */
public class EditAction extends GenericWikiWebAction {

  protected static String action = "edit";
  protected static String jsppage = "edit.jsp";

  {
    setFlag(FLAG_REQUIRES_PAGENAME);
    setFlag(FLAG_WRITE_ACTION);
    setFlag(FLAG_MAKE_SHORTHAND);
  }

  public void includeView() throws UnsupportedOperationException, Exception {
    throw new UnsupportedOperationException();
  }

  public int render() throws Exception {
    editH2();
    return EditableAction.render(action, jsppage, isSupported());
  }

  public static boolean isSupported() {
    return WikiLocal.getWikiCategoryEngine().isActionSupported(WikiConstants.ACTION_EDIT);
  }

  protected static void editH2() throws Exception {
    EditableAction.checkEditableSupported(isSupported(), action);
    WebInteractionContext request = WebLocal.getWebInteractionContext();
    WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
    WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
    WikiProvidedObject wp = WikiUtils.getWikiPage();
    String author = request.getUserName();
    Map model = EditableAction.handleEditableInitModel(EditableAction.getKey(action));
    String s = null;
    ArrayList alist = null;
    boolean pageDraftSupported = wce.isPageDraftSupported();

    if (wce.getWikiEditManager().canAcquireLock(wp.getName(), author)) {
      wce.getWikiEditManager().acquireLock(wp.getName(), author);
      Exception pexc = (Exception) wlh.getAttribute("editable.exception");
      String text = (String) wlh.getAttribute("editable.text");
      if (text == null) {
        text =
            request.getParameter(
                WikiConstants
                    .PARAMETER_TEXT); // WikiUtils.getString(WikiConstants.PARAMETER_TEXT, null);
      }
      if (text == null && pageDraftSupported) {
        Map m = wce.getPageDraft(wp.getName());
        if (m != null) {
          text = (String) m.get("text");
        }
      }
      if (text == null) {
        boolean pageExists = wce.getPageProvider().pageExists(wp.getName());
        if (pageExists) {
          wp =
              wce.getPageProvider()
                  .getPage(wp.getName(), WikiProvidedObject.VERSION_LATEST_DETAILS_NECESSARY);
          Reader r = wce.getPageProvider().getPageReader(wp);
          text = StringUtils.readerToString(r);
        }
      }
      text = StringUtils.toHTMLEscape(StringUtils.nonNullString(text), false, false);
      String subscribers = WikiUtils.getString("attribute.subscribers", null);
      if (subscribers == null) {
        subscribers = wp.getAttribute("subscribers");
      }
      subscribers = StringUtils.nonNullString(subscribers);
      String tags = WikiUtils.getString("attribute.tags", null);
      if (tags == null) {
        tags = wp.getAttribute(WikiConstants.ATTRIBUTE_TAGS);
      }
      tags = StringUtils.nonNullString(tags);

      String minoreditflagstr =
          WikiUtils.getString("attribute." + WikiEvent.MINOR_EDIT_FLAG_KEY, null);
      Boolean minoreditflag = Boolean.valueOf("true".equals(minoreditflagstr));
      String comments = WikiUtils.getString("attribute.comments", "...");
      String pagetemplate = WikiUtils.getString("pagetemplate", null);
      String pagetemplateparentpage = wce.getProperty(WikiConstants.PAGETEMPLATE_PARENTPAGE);
      alist = new ArrayList();
      if (pagetemplateparentpage != null) {
        StringTokenizer stz = new StringTokenizer(pagetemplateparentpage, " ,");
        while (stz.hasMoreTokens()) {
          s = stz.nextToken();
          String sCatName = wce.getName();
          if (StringUtils.isBlank(s)) {
            continue;
          }
          int idxOfColon = s.indexOf(':');
          WikiCategoryEngine wce2 = wce;
          if (idxOfColon != -1) {
            sCatName = s.substring(0, idxOfColon).trim();
            wce2 = wce.getWikiEngine().getWikiCategoryEngine(sCatName);
            s = s.substring(idxOfColon + 1).trim();
          }
          // System.out.println("wce2, s: " + wce2.getName() + " SSSS " + s);
          String[] sArr =
              wce2.getIndexingManager()
                  .getAllReferersMatching(s + "|" + s + "/" + ".+"); // "[^/]+");
          for (int i = 0; i < sArr.length; i++) {
            alist.add(WikiUtils.fullQualifiedWikiName(sCatName, sArr[i]));
          }
        }
      }
      // System.out.println("pagetemplateparentpage: " + pagetemplateparentpage);
      // System.out.println("alist: " + alist);
      String[] pagetemplates = (String[]) alist.toArray(new String[0]);

      s = StringUtils.nonNullString(wce.getProperty(WikiConstants.ALLOWED_TAGS_KEY));
      List allowedTags = StringUtils.tokens(s, " ", true, true);
      boolean constrainTags = "true".equals(wce.getProperty(WikiConstants.CONSTRAIN_TAGS_KEY));

      model.put("wikipage", wp);

      model.put(WikiConstants.PARAMETER_TEXT, text);
      model.put("subscribers", subscribers);
      model.put(WikiConstants.ATTRIBUTE_COMMENTS, comments);
      model.put(WikiConstants.ATTRIBUTE_TAGS, tags);
      model.put("existing_tags", wce.getIndexingManager().lookupExistingTags());
      model.put("minoreditflag", minoreditflag);
      model.put("exception", pexc);
      model.put("pagetemplates", pagetemplates);
      model.put("pagetemplate", pagetemplate);
      model.put("allowpublish", Boolean.valueOf(WikiUtils.allowPublish()));
      model.put("allowed_tags", allowedTags);
      model.put("constrain_tags", Boolean.valueOf(constrainTags));
      model.put("page_draft_supported", Boolean.valueOf(pageDraftSupported));

      wlh.setAttribute(EditableAction.getKey(action), model);
      wlh.setAttribute(EditableAction.LOCK_NOT_ACQUIRED_KEY, Boolean.FALSE);
    } else {
      wlh.setAttribute(EditableAction.LOCK_NOT_ACQUIRED_KEY, Boolean.TRUE);
    }
  }
}
