
<%@ include file="_topinclude.jspf" %><%
  WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
  Map model = (Map)wlh.getAttribute("wiki.model.userprefs");
  WikiCategoryEngine wcengine = WikiLocal.getWikiCategoryEngine();
  String username = (String)model.get("username");
  String emailaddress = (String)model.get("emailaddress");
  String subscriptions = (String)model.get("subscriptions");
  String locale = (String)model.get("locale");
  Locale[] locales = (Locale[])model.get("locales");
  String[] view_show = (String[])model.get("available_view_show");
  List user_view_show_list = (List)model.get("user_view_show");

%>
<h3><%= wi18n.str("jspviews.userprefs.header") %></h3>

<div>

<form action="<%= WikiViewUtils.decipherURL(wlh, "userprefspost") %>" method="get" >

<%= wi18n.str("jspviews.userprefs.pref_name") %><br/>
<input type="text" name="username" value="<%= username %>" size="80" maxlength="80" style="width:80%" 
<% if(!WikiUtils.isSetUsernameSupported()) { %> disabled="true" readonly="true" <% } %>
/><br/>
<%= wi18n.str("jspviews.userprefs.email") %><br/>
<input type="text" name="emailaddress" value="<%= emailaddress %>" size="80" maxlength="80" style="width:80%" /><br/>
<%= wi18n.str("jspviews.userprefs.subscriptions") %><br/>
<input type="text" name="subscriptions" value="<%= subscriptions %>" size="80" maxlength="80" style="width:80%" /><br/>
<%= wi18n.str("jspviews.userprefs.locale") %><br />
<select name="locale">
  <% for(int i = 0; i < locales.length; i++) { %>
  <option value="<%= locales[i].toString() %>" <% if(locales[i].toString().equals(locale)) { %>SELECTED<% } %>><%= locales[i].toString() %></option>
  <% } %>
</select>
<p>
<input type="submit" name="postuserprefsaction_save_all" value="<%= wi18n.str("jspviews.userprefs.save_all") %>" />
&nbsp;&nbsp;
<% if(WikiUtils.isSetUsernameSupported()) { %>
<input type="submit" name="postuserprefsaction_set_username" value="<%= wi18n.str("jspviews.userprefs.set_username") %>" />
&nbsp;&nbsp;
<% } %>
<input type="submit" name="postuserprefsaction_clear_username" value="<%= wi18n.str("jspviews.userprefs.clear_username") %>" />
&nbsp;&nbsp;
<input type="submit" name="postuserprefsaction_cancel" value="<%= wi18n.str("jspviews.userprefs.cancel") %>" />

<input type="hidden" name="postuserprefs" value="true" />
</form>

</div>

