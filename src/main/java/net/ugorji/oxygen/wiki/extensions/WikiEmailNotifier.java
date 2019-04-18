/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.extensions;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import net.ugorji.oxygen.manager.UserPreferencesManager;
import net.ugorji.oxygen.util.I18n;
import net.ugorji.oxygen.util.OxygenIntRange;
import net.ugorji.oxygen.util.OxygenRevision;
import net.ugorji.oxygen.util.OxygenUtils;
import net.ugorji.oxygen.util.SendMail;
import net.ugorji.oxygen.util.StringUtils;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiConstants;
import net.ugorji.oxygen.wiki.WikiEvent;
import net.ugorji.oxygen.wiki.WikiEventListener;
import net.ugorji.oxygen.wiki.WikiLinkHolder;
import net.ugorji.oxygen.wiki.WikiLocal;
import net.ugorji.oxygen.wiki.WikiProvidedObject;

/**
 * This handles email notifications. It has callbacks, which are called when a wikipage is saved,
 * deleted or a review is added to the page. This must be completed while the request context is
 * still in place
 *
 * @author ugorji
 */
public class WikiEmailNotifier implements WikiEventListener {

  private Map patternCache;
  private WikiCategoryEngine wce;

  public void prepare(WikiCategoryEngine _wce) throws Exception {
    wce = _wce;
    patternCache = new HashMap();
  }

  public void close() {
    patternCache = null;
  }

  public void handleWikiEvent(WikiEvent we) throws Exception {
    // System.out.println("WikiEmailNotifier called");
    int type = we.getType();
    if (!isEmailNotificationSupported() || isMinorEdit(we)) {
      return;
    }
    // System.out.println("WikiEmailNotifier passed supported check");
    WENModel wenmodel = new WENModel(we, wce);
    Map atts = we.getAttributes();
    String pagename = wenmodel.getPagename();
    String recipients = getRecipients(pagename);
    if (recipients == null || recipients.trim().length() == 0) {
      return;
    }
    // System.out.println("WikiEmailNotifier has some recipients: " + recipients);
    boolean doHTML = "html".equals(wce.getProperty(WikiConstants.EMAIL_FORMAT_KEY));
    String tmplLoc = "emailnotification.txt";
    if (doHTML) {
      tmplLoc = "emailnotification.html";
    }

    Map tmplctx = new HashMap();
    tmplctx.put("hdlr", wenmodel);
    if (type == WikiEvent.PAGE_SAVED) {
      String originalpagetext = (String) atts.get(WikiEvent.PAGE_TEXT_ORIGINAL_KEY);
      String pagetext = (String) atts.get(WikiEvent.PAGE_TEXT_KEY);
      OxygenRevision wrev = OxygenRevision.getDiff(originalpagetext, pagetext, WebLocal.getI18n());
      tmplctx.put("wrev", wrev);
    }

    StringWriter stw = new StringWriter();
    WikiLocal.getWikiEngine().getWikiTemplateFilesHandler().write(tmplLoc, tmplctx, stw);

    String emailText = stw.toString();

    // for testing
    // FileWriter fw2 = new FileWriter("c:/tmp/emailnotif.html");
    // fw2.write(emailText);
    // CloseUtils.close(fw2);

    SendMail.EmailInfoHolder email = new SendMail.EmailInfoHolder();
    email.recipients = recipients.trim();
    email.sender = wce.getProperty(WikiConstants.ENGINE_EMAIL_SENDER);
    email.subject =
        wenmodel.getI18n(
            "email_subject",
            new String[] {wce.getName(), wenmodel.eventToI18n(we.getType()), pagename});

    if (doHTML) {
      email.headers.put("Content-Type", "text/html; charset=\"us-ascii\"");
    }
    email.text = emailText;
    email.properties.put("mail.smtp.host", wce.getProperty(WikiConstants.ENGINE_EMAIL_SMTP_HOST));

    // System.out.println("Sending mail: " + wce.getProperty(WikiConstants.ENGINE_EMAIL_SMTP_HOST) +
    // ": to: " + email.recipients);
    // email.write(System.out);
    SendMail.sendMail(email);
  }

  private boolean isEmailNotificationSupported() {
    // System.out.println("WikiConstants.ENGINE_EMAIL_SUPPORTED_KEY: " +
    // wce.getProperty(WikiConstants.ENGINE_EMAIL_SUPPORTED_KEY));
    return "true".equals(wce.getProperty(WikiConstants.ENGINE_EMAIL_SUPPORTED_KEY));
  }

  // read the file ... and get the recipients ... and write it out
  private String getRecipients(String pagename) throws Exception {
    // System.out.println("trying to get recipients");
    WikiProvidedObject wp =
        wce.getPageProvider()
            .getPage(pagename, WikiProvidedObject.VERSION_LATEST_DETAILS_NECESSARY);
    // get recipients per page
    HashSet hs = new HashSet();
    String subsc = wp.getAttribute("subscribers");
    if (subsc != null) {
      StringTokenizer stz = new StringTokenizer(subsc, " ;,\n");
      while (stz.hasMoreTokens()) {
        hs.add(stz.nextToken().trim());
      }
    }

    UserPreferencesManager prefmgr = wce.getWikiEngine().getUserPreferencesManager();
    Map p = prefmgr.getForKey("subscriptions");

    OxygenUtils.debug("WikiEmailNotifier: subscription properties: " + p);
    // support scoping the page name to a category
    String fullyQualName = wce.getName() + ":" + pagename;
    for (Iterator itr = p.entrySet().iterator(); itr.hasNext(); ) {
      Map.Entry entry = (Map.Entry) itr.next();
      String username = (String) entry.getKey();
      String subscriptionRegex = StringUtils.getSingleValue(entry.getValue());
      // put a try/catch here, in case someone puts in a bad regex in the first place
      try {
        if (subscriptionRegex != null
            && getPattern(subscriptionRegex).matcher(fullyQualName).matches()) {
          hs.add(StringUtils.getSingleValue(prefmgr.getForUser(username, "emailaddress")));
        }
      } catch (Exception exc) {
        OxygenUtils.debug(exc);
      }
    }

    OxygenUtils.debug("WikiEmailNotifier: recipients hashset: " + hs);
    String recipients = StringUtils.toString(hs, ",");
    OxygenUtils.debug("WikiEmailNotifier: recipients: " + recipients);
    return recipients;
  }

  private Pattern getPattern(String s) {
    Pattern p = (Pattern) patternCache.get(s);
    if (p == null) {
      synchronized (patternCache) {
        p = Pattern.compile(s);
        patternCache.put(s, p);
      }
    }
    return p;
  }

  private boolean isMinorEdit(WikiEvent we) {
    boolean b = false;
    Boolean bobj = (Boolean) we.getAttribute(WikiEvent.MINOR_EDIT_FLAG_KEY);
    if (bobj != null) {
      b = bobj.booleanValue();
    }
    return b;
  }

  public static class WENModel {
    private WikiEvent we;
    private WikiCategoryEngine wce;
    private I18n i18n;

    public WENModel(WikiEvent _we, WikiCategoryEngine _wce) {
      we = _we;
      wce = _wce;
      i18n = WebLocal.getI18n();
    }

    public WikiEvent getWikiEvent() {
      return we;
    }

    public String getPagename() {
      return (String) we.getAttributes().get(WikiEvent.PAGE_NAME_KEY);
    }

    public String getAttachmentname() {
      return (String) we.getAttributes().get(WikiEvent.ATTACHMENT_NAME_KEY);
    }

    public String getEventText() {
      String s = null;
      if (s == null) {
        s = (String) we.getAttribute(WikiEvent.PAGE_TEXT_KEY);
      }
      if (s == null) {
        s = (String) we.getAttribute(WikiEvent.REVIEW_TEXT_KEY);
      }
      return s;
    }

    public Map getEventAttributes() {
      Map atts = null;
      if (atts == null) {
        atts = (Map) we.getAttribute(WikiEvent.PAGE_ATTRIBUTES_KEY);
      }
      if (atts == null) {
        atts = (Map) we.getAttribute(WikiEvent.ATTACHMENT_ATTRIBUTES_KEY);
      }
      if (atts == null) {
        atts = (Map) we.getAttribute(WikiEvent.REVIEW_ATTRIBUTES_KEY);
      }
      return atts;
    }

    public OxygenIntRange getVersionsDeleted() {
      return (OxygenIntRange) we.getAttribute(WikiEvent.VERSIONS_KEY);
    }

    public String getUrl() throws Exception {
      WebInteractionContext wctx = WebLocal.getWebInteractionContext();
      // if(wce == null || wctx == null) {
      //  return "-- UNKNOWN URL --";
      // }
      WikiLinkHolder wlh = new WikiLinkHolder();
      wlh.setAction(WikiConstants.ACTION_VIEW);
      wlh.setCategory(wce.getName());
      wlh.setWikiPage(getPagename());

      String s = wctx.toURLString(wlh, null);
      return s;
    }

    public I18n getI18n() {
      return i18n;
    }

    public String getI18n(String s) {
      return i18n.str("listeners.emailnotification." + s);
    }

    public String getI18n(String s, String[] args) {
      return i18n.str("listeners.emailnotification." + s, args);
    }

    public String eventToI18n(int type0) {
      return i18n.str("listeners.emailnotification.event." + OxygenUtils.getFlagPosition(type0));
    }

    public boolean isEvent(int type0) {
      return (OxygenUtils.isFlagSet(we.getType(), type0));
    }

    public static String propsToString(Properties p) throws Exception {
      return StringUtils.propsToString(p);
    }

    public static String getOriginal(OxygenRevision wrev, int i) {
      return StringUtils.toHTMLEscape(
          wrev.getOriginal(i, null, "- ", "\n").toString(), true, false);
    }

    public static String getRevised(OxygenRevision wrev, int i) {
      return StringUtils.toHTMLEscape(wrev.getRevised(i, null, "+ ", "\n").toString(), true, false);
    }
  }
}
