/* <<< COPYRIGHT START >>>
 * Copyright 2006-Present OxygenSoftwareLibrary.com
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 *
 * @author: Ugorji Nwoke
 * <<< COPYRIGHT END >>>
 */

package net.ugorji.oxygen.wiki.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import net.ugorji.oxygen.util.CloseUtils;
import net.ugorji.oxygen.util.OxygenUtils;
import net.ugorji.oxygen.util.StringUtils;
import net.ugorji.oxygen.web.WebInteractionContext;
import net.ugorji.oxygen.web.WebLocal;
import net.ugorji.oxygen.wiki.WikiCategoryEngine;
import net.ugorji.oxygen.wiki.WikiException;
import net.ugorji.oxygen.wiki.WikiLinkHolder;
import net.ugorji.oxygen.wiki.WikiLocal;

// Consider not caching the context, so we can close it within the finally
// This is because an idle ldap connection can be closed by the LDAP server,
// and then we get just CommunicationException all the time
public class LdapFindAction extends GenericWikiWebAction {

  {
    setFlag(FLAG_REQUIRES_PAGENAME);
    setFlag(FLAG_MAKE_SHORTHAND);
  }

  public int render() throws Exception {
    DirContext ctx = null;
    try {
      String s = null;
      WebInteractionContext request = WebLocal.getWebInteractionContext();
      WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
      String username = request.getParameter("u");
      if (StringUtils.isBlank(username)) {
        username = wlh.getWikiPage();
      }
      WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
      Properties wcep = wce.getProperties();
      // ctx = (DirContext)wce.getAttribute(getClass().getName() + ".dircontext");
      // if(ctx == null) {
      String user = wcep.getProperty("net.ugorji.oxygen.wiki.ldapfind.user");
      String pw = wcep.getProperty("net.ugorji.oxygen.wiki.ldapfind.password");
      String url = wcep.getProperty("net.ugorji.oxygen.wiki.ldapfind.url");
      String timeout = wcep.getProperty("net.ugorji.oxygen.wiki.ldapfind.connect_timeout_ms", "20000");

      Hashtable env = new Hashtable();

      env.put(LdapContext.CONTROL_FACTORIES, "com.sun.jndi.ldap.ControlFactory");
      env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
      env.put("com.sun.jndi.ldap.connect.timeout", timeout); // timeout in 15 seconds
      env.put(Context.STATE_FACTORIES, "PersonStateFactory");
      env.put(Context.OBJECT_FACTORIES, "PersonObjectFactory");
      env.put(Context.PROVIDER_URL, url);
      if (!StringUtils.isBlank(user)) env.put(Context.SECURITY_PRINCIPAL, user);
      if (!StringUtils.isBlank(pw)) env.put(Context.SECURITY_CREDENTIALS, pw);

      // System.out.println("env: " + env);

      ctx = new InitialDirContext(env);

      String dnroot = wcep.getProperty("net.ugorji.oxygen.wiki.ldapfind.dn_root");

      Properties schemaOverrides = new Properties();
      OxygenUtils.extractProps(wcep, schemaOverrides, "net.ugorji.oxygen.wiki.ldapfind.schema.", true);

      // wce.setAttribute(getClass().getName() + ".dircontext", ctx);
      // }
      String usernameKey = schemaOverrides.getProperty("username", "username");
      String managerKey = schemaOverrides.getProperty("manager", "manager");
      String rdnKey = schemaOverrides.getProperty("rdn", "rdn");

      String searchRoot = "ou=people";
      Attributes matchAttrs = new BasicAttributes(true);
      NamingEnumeration answer = null;

      // Search for objects that have those matching attributes
      matchAttrs.put(new BasicAttribute(usernameKey, username));
      answer = ctx.search(searchRoot, matchAttrs);
      Attributes userattr = (Attributes) getAttributesResult(answer, true);
      matchAttrs.remove(usernameKey);

      if (userattr == null) {
        throw new WikiException(WebLocal.getI18n().str("actions.ldapfind.no_user_found", username));
      }

      String myrdn = (String) userattr.get(rdnKey).get();
      matchAttrs.put(new BasicAttribute(managerKey, rdnKey + "=" + myrdn + "," + dnroot));
      answer = ctx.search(searchRoot, matchAttrs, new String[] {"cn", usernameKey});
      List reports = (List) getAttributesResult(answer, false);
      matchAttrs.remove(managerKey);

      Attributes managerattr = null;
      Attribute tmpAttrs = userattr.get(managerKey);
      if (tmpAttrs != null) {
        Pattern p = Pattern.compile(rdnKey + "=(.+?),.*");
        Matcher m = p.matcher((String) tmpAttrs.get());
        if (m.matches()) {
          // System.out.println("Matched manager: " + m.group(1));
          matchAttrs.put(new BasicAttribute(rdnKey, m.group(1)));
          answer = ctx.search(searchRoot, matchAttrs, new String[] {"cn", usernameKey});
          managerattr = (Attributes) getAttributesResult(answer, true);
          matchAttrs.remove(rdnKey);
        }
      }

      Map model = new HashMap();
      model.put("rdn_key", rdnKey);
      model.put("dn_root", dnroot);
      model.put("username", username);
      model.put("user_attributes", userattr);
      model.put("manager_attributes", managerattr);
      model.put("reports_attributes", reports.toArray(new Attributes[0]));
      model.put("schema_overrides", schemaOverrides);

      wlh.setAttribute("wiki.model.ldapfind", model);

      showJSPView("ldapfind.jsp");
      return RENDER_COMPLETED;
    } finally {
      CloseUtils.close(ctx);
    }
  }

  private Object getAttributesResult(NamingEnumeration answer, boolean oneOnly) throws Exception {
    Attributes oneAttr = null;
    List list = new ArrayList();
    while (answer.hasMore()) {
      SearchResult sr = (SearchResult) answer.next();
      oneAttr = sr.getAttributes();
      if (oneOnly) {
        // answer.close();
        break;
      }
      list.add(oneAttr);
    }
    CloseUtils.close(answer);
    return (oneOnly ? (Object) oneAttr : (Object) list);
  }
}
