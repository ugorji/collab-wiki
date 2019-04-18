<%@ include file="_topinclude.jspf" %><%
  WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
  WikiLinkHolder wlh2 = wlh.getClone();
  WikiEngine we = WikiLocal.getWikiEngine();
  String[] categories = we.getRegisteredWikiCategoryNames();
  Arrays.sort(categories);
  String _togglelongtermlocklink = WikiViewUtils.decipherURL(wlh, "togglelongtermlock");
  String _adminlink = null; 
  boolean isLockHeld = we.getLongTermLock().isHeld();
%>

<script LANGUAGE="JavaScript">
<!--
  function oxywiki_togglelongtermlock() {
    //alert('hello there');
    var oxypars = 'm=' + encodeURIComponent($('oxywiki_lock_message').value);	
    var oxyAjax = new Ajax.Request('<%= _togglelongtermlocklink %>', {method: 'get', parameters: oxypars, onComplete: oxywiki_reload_current_page });
  } 
// -->
</script>

<h2><%= wi18n.str("jspviews.admin.header") %></h2>
<p></p>

<a href="<%= WikiUtils.getCategoryURL(WikiConstants.BUILTIN_SECTION_NAME, "cmdline", null) %>"><%= wi18n.str("templates.default.cmdline") %></a>

<h3><%= wi18n.str("jspviews.admin.header_overall_engine") %></h3>

<form name="adminx" action="#">
<table border="1">
<tr>
<th align="left" colspan="2"><%= wi18n.str("jspviews.admin.title_overall_actions") %></th>
</tr>
<tr>
<td><a href="javascript:oxywiki_togglelongtermlock();"><%= wi18n.str("jspviews.admin.action_lock_engine") %> &plusmn; </a></td>
<td>
      <%= wi18n.str("jspviews.admin.enter_lock_message") %>: 
      <input id="oxywiki_lock_message" type="text" name="lock_message" value="<%= wi18n.str("general.engine_locked_long_term") %>" size="80" maxlength="80" style="width:100%" onFocus="this.select()" /><br/>
    <%= wi18n.str("jspviews.admin.description_lock_engine", String.valueOf(isLockHeld)) %>
</td>
</tr>
<tr>
<% _adminlink = WikiViewUtils.decipherURL(wlh, "admin", new String[]{"engine", "true", "reload", "true"}); %>
<td><a href="<%= _adminlink %>"><%= wi18n.str("jspviews.admin.action_reload_engine_metadata") %></a></td>
<td><%= wi18n.str("jspviews.admin.description_reload_engine_metadata") %></td>
</tr>
<tr>
<% _adminlink = WikiViewUtils.decipherURL(wlh, "admin", new String[]{"engine", "true", "reset", "true"}); %>
<td><a href="<%= _adminlink %>"><%= wi18n.str("jspviews.admin.action_reset_engine") %></a></td>
<td><%= wi18n.str("jspviews.admin.description_reset_engine") %></td>
</tr>
<tr>
<th align="left" colspan="2"><%= wi18n.str("jspviews.admin.title_engine_config_files") %></th>
</tr>
<tr>
<% _adminlink = WikiViewUtils.decipherURL(wlh, "editconfig", new String[]{"editconfig", "true", "configfile", "oxywiki.properties"}); %>
<td><a href="<%= _adminlink %>"><%= wi18n.str("jspviews.admin.edit_config") %></a></td>
<td><%= wi18n.str("jspviews.admin.description_edit_config") %></td>
</tr>
<tr>
<% _adminlink = WikiViewUtils.decipherURL(wlh, "editconfig", new String[]{"editconfig", "true", "configfile", "userpreferences.properties"}); %>
<td><a href="<%= _adminlink %>"><%= wi18n.str("jspviews.admin.edit_user_preferences") %></a></td>
<td><%= wi18n.str("jspviews.admin.description_edit_user_preferences") %></td>
</tr>
</table>
</form>

<%--
<form action="<%= _adminlink %>">
<input type="hidden" name="engine" value="true" />
<input type="hidden" name="changeconfigdir" value="true" />
<table border="1">
<tr>
<th align="left" colspan="2"><%= wi18n.str("jspviews.admin.title_change_config_dir") %></th>
</tr>
<tr>
<td><input type="text" name="configdir" size="80" style="width:100%" value="<%= we.getConfigDir() %>"/></input>
&nbsp;&nbsp;&nbsp;
<input type="submit" name="postaction_changeconfigdir" value="<%= wi18n.str("jspviews.admin.action_submit") %>" />
</td>
</tr>
</table>
</form>
--%>

<h3><%= wi18n.str("jspviews.admin.title_categories") %></h3>
<table border="1">
<tr>
<th><%= wi18n.str("jspviews.admin.title_col_category") %></th>
<th><%= wi18n.str("jspviews.admin.title_col_is_active") %></th>
<th><%= wi18n.str("jspviews.admin.title_col_num_pages") %></th>
<th><%= wi18n.str("jspviews.admin.title_col_num_locks") %></th>
<th><%= wi18n.str("jspviews.admin.title_col_actions") %></th>
</tr>
<% for(int i = 0; i < categories.length; i++) { 
     //do not show builtin section
     if(WikiConstants.BUILTIN_SECTION_NAME.equals(categories[i])) {
       continue;
     }
     WikiCategoryEngine wce = we.getWikiCategoryEngine(categories[i]);
     boolean isActive = (wce != null);
     int lockSize = ((isActive) ? wce.getWikiEditManager().getAllLocks().size() : 0);
     wlh2.setCategory(categories[i]);
     boolean isPageExist = (wce != null && wce.getIndexingManager().isAReferrer(wce.getEntryPage()));    
     boolean isWarning = (!isActive || wce.getLongTermLock().isHeld() || lockSize > 0);
%>
<tr <% if(isWarning) { %>class="oxy-warning"<% } %>>
<td>
<%--
<% if(isActive) { %>
  <%= WikiViewUtils.getLinkHTML(wlh2, false, true) %>
<% } else { %>
  <%= categories[i] %>
<% } %>

<% if(isActive && isPageExist) { %><a href="<%= WikiUtils.getCategoryURL(categories[i], WikiConstants.ACTION_VIEW, null) %>"><% } %>
<%= categories[i] %>
<% if(isActive) { %></a><% } %>
--%>
<% if(!isActive) { %>
<%= categories[i] %>
<% } else if(isPageExist) { %>
<a href="<%= WikiUtils.getCategoryURL(categories[i], WikiConstants.ACTION_VIEW, null) %>"><%= categories[i] %></a>
<% } else { %>
<a href="<%= WikiUtils.getCategoryURL(categories[i], WikiConstants.ACTION_EDIT, null) %>"><%= categories[i] %></a>
<% } %>
</td>
<td><%= isActive %></td>
<td>
<% if(isActive) { %>
<%= wce.getIndexingManager().getAllReferersMatching(null).length %>
<% } else { %>
-
<% } %>
</td>
<td>
<a href="javascript:oxy_set_cookie('oxywiki-sysinfo-tab', 7, null);location.href='<%= WikiViewUtils.decipherURL(wlh2, "sysinfo") %>';">
<% if(isActive && lockSize > 0) { %>
<b><%= lockSize %></b>
<% } else if(isActive) { %>
<%= lockSize %>
<% } else { %>
-
<% } %>
</a>
</td>
<td>

<% if(!isActive) { %>
<% _adminlink = WikiViewUtils.decipherURL(wlh, "admin", new String[]{"load", "true", "cat", categories[i] }); %>
<a href="<%= _adminlink %>"><%= wi18n.str("jspviews.admin.action_cat_load") %></a>
|
<% } %>
<% if(isActive) { %>
<% _adminlink = WikiViewUtils.decipherURL(wlh, "admin", new String[]{"unload", "true", "cat", categories[i] }); %>
<a href="<%= _adminlink %>"><%= wi18n.str("jspviews.admin.action_cat_unload") %></a>
|
<% _adminlink = WikiViewUtils.decipherURL(wlh, "admin", new String[]{"reload", "true", "cat", categories[i] }); %>
<a href="<%= _adminlink %>"><%= wi18n.str("jspviews.admin.action_cat_reload") %></a>
|
<%   String arr99 = (wce.getLongTermLock().isHeld() ? "unlock" : "lock"); 
     _adminlink = WikiViewUtils.decipherURL(wlh, "admin", new String[]{arr99, "true", "cat", categories[i] }); %>
<a href="<%= _adminlink %>"><%= wi18n.str("jspviews.admin.action_cat_" + arr99) %></a>
|
<% } %>
<% _adminlink = WikiViewUtils.decipherURL(wlh, "editconfig", new String[]{"editconfig", "true", "configfile", "oxywiki-" + categories[i] + ".properties"}); %>
<a href="<%= _adminlink %>">
<%= wi18n.str("jspviews.admin.action_cat_edit_config") %>
</a>

</td>
</tr>
<% } %>
</table>

