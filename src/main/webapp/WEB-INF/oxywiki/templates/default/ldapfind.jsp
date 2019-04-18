<%@ include file="_topinclude.jspf" %><%@ page 
  import="javax.naming.directory.Attributes,
          javax.naming.directory.Attribute"
%><%!
  private String v(Attributes as, Properties overrides, String key) throws Exception {
    key = overrides.getProperty(key, key);
    Attribute a = as.get(key);
    return ((a == null) ? "-" : StringUtils.nonNullString((String)a.get(), "-"));
  }
  private String[] va(Attributes as, Properties overrides, String key) throws Exception {
    key = overrides.getProperty(key, key);
    String[] sa = new String[0];
    Attribute a = as.get(key);
    if(a != null) {
      sa = new String[a.size()];
      for(int i = 0; i < sa.length; i++) {
        sa[i] = (String)a.get(i);
      }
    }
    return sa;
  }
  
%>
<%
  WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
  WikiLinkHolder wlh2 = wlh.getClone();
  Map model = (Map)wlh.getAttribute("wiki.model.ldapfind");
  Properties som = (Properties)model.get("schema_overrides");
  String username_key = (String)som.getProperty("username", "username");
  String username = (String)model.get("username");
  String dn_root = (String)model.get("dn_root");
  Attributes attrs = (Attributes)model.get("user_attributes");
  Attributes mgrAttrs = (Attributes)model.get("manager_attributes");
  Attributes[] reportsAttrs  = (Attributes[])model.get("reports_attributes");
  String uid = null;
  String ufull = null;
  String email = null;
%>
<h2><%= v(attrs, som, "cn") %></h2>

<p/>
<%= wi18n.str("jspviews.ldapfind.message") %>

<p/>
<table>
<tr>
<th valign="top"><%= wi18n.str("jspviews.ldapfind.username") %></th>
<td valign="top"><%= v(attrs, som, username_key) %></td>
</tr>
<tr>
<th valign="top"><%= wi18n.str("jspviews.ldapfind.telephone") %></th>
<td valign="top"><%= v(attrs, som, "telephoneNumber") %></td>
</tr>
<tr>
<th valign="top"><%= wi18n.str("jspviews.ldapfind.email") %></th>
<td valign="top">
  <% email = (String)v(attrs, som, "mail"); %>
  <a href="mailto:<%= email %>"><%= email %></a><br/>
  <% email = (String)v(attrs, som, "mailAlternateAddress"); %>
  <a href="mailto:<%= email %>"><%= email %></a><br/>
</td>
</tr>
<tr>
<th valign="top"><%= wi18n.str("jspviews.ldapfind.title") %></th>
<td valign="top"><%= v(attrs, som, "title") %></td>
</tr>
<tr>
<th valign="top"><%= wi18n.str("jspviews.ldapfind.id") %></th>
<td valign="top"><%= v(attrs, som, "id") %></td>
</tr>
<tr>
<th valign="top"><%= wi18n.str("jspviews.ldapfind.department") %></th>
<td valign="top">
  <%= v(attrs, som, "department") %>
  (<%= v(attrs, som, "departmentNumber") %>)
</td>
</tr>
<tr>
<th valign="top"><%= wi18n.str("jspviews.ldapfind.address") %></th>
<td valign="top">
<%= v(attrs, som, "organization") %><br/>
<% String[] a1 = va(attrs, som, "street");
   for(int i = 0; i < a1.length; i++) { %>
<%= a1[i] %><br/>
<% } %>
<%= v(attrs, som, "city") %>, 
<%= v(attrs, som, "st") %> <%= v(attrs, som, "postalCode") %><br/>
<%= v(attrs, som, "co") %>
</td>
</tr>
</table>

<p/>

<dl>
<dt><%= wi18n.str("jspviews.ldapfind.manager") %></dt>
<% if(mgrAttrs != null) { 
     uid = (String)v(mgrAttrs, som, username_key); 
     ufull = (String)v(mgrAttrs, som, "cn"); 
     wlh2.setWikiPage(uid);
%>
<dd><a href="<%= wlh2.getURL() %>"><%= ufull %></a></dd>
<% } %>
<dd>&nbsp;</dd>
<dt><%= wi18n.str("jspviews.ldapfind.reports") %></dt>
<% if(reportsAttrs != null && reportsAttrs.length > 0) { 
     for(int i = 0; i < reportsAttrs.length; i++) {
       uid = v(reportsAttrs[i], som, username_key); 
       ufull = v(reportsAttrs[i], som, "cn");
       wlh2.setWikiPage(uid);
%>
<dd><a href="<%= wlh2.getURL() %>"><%= ufull %></a></dd>
<% } } %>
<dd>&nbsp;</dd>
</dl>

<p/>

<form name="oxywiki_ldapfind_form" action="<%= wlh.getURL() %>" method="get">
<input type="text" name="<%= WikiConstants.REQUEST_PARAM_PAGE_KEY %>" value="<%= username %>" size="20">
<input type="button" value="<%= wi18n.str("jspviews.ldapfind.search") %>" onClick="javascript:document.oxywiki_ldapfind_form.submit();" >
</form>
