<%@ include file="_topinclude.jspf" %><%
  WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
  Map model = (Map)wlh.getAttribute("wiki.model.attachmentinfo");
  WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
  String pagename = (String)model.get("wikipagename");
  String attachment = (String)model.get("wikiattachmentname");
  WikiProvidedObject[] wps = (WikiProvidedObject[])model.get("attachmentversions");
%>
<SCRIPT LANGUAGE="JavaScript"><!--
function oxywiki_delete_attachment_versions_submit() {
  document.oxywiki_attach_info_form.action = '<%= WikiViewUtils.decipherURL(wlh, "deleteprompt") %>';
  document.oxywiki_attach_info_form.deleteversions.disabled = false;
  document.oxywiki_attach_info_form.deleteversions.value = 'true';
  document.oxywiki_attach_info_form.submit();
}
function oxywiki_revert_submit() {
  document.oxywiki_attach_info_form.action = '<%= WikiViewUtils.decipherURL(wlh, "revert") %>';
  document.oxywiki_attach_info_form.deleteversions.disabled = true;
  document.oxywiki_attach_info_form.submit();
}

// --></script>
<h3><%= wi18n.str("jspviews.attachmentinfo.versions_header", new String[]{attachment, pagename}) %></h3>
<form name="oxywiki_attach_info_form" type="get" action="<%= WikiViewUtils.decipherURL(wlh, "deleteprompt") %>">
<input type="hidden" name="deleteversions" value="true" />
<input type="hidden" name="attachment" value="<%= attachment %>" />
<table border="1">
<tr>
<th>--</th>
<th>--</th>
<th>--</th>
<th><%= wi18n.str("jspviews.attachmentinfo.size") %></th>
<th><%= wi18n.str("jspviews.attachmentinfo.modified") %></th>
<th><%= wi18n.str("jspviews.attachmentinfo.author") %></th>
<th><%= wi18n.str("jspviews.attachmentinfo.comments") %></th>
</tr>
<% for(int i = 0; i < wps.length; i++) { 
   Properties wpprops = wps[i].getAttributes();
   if(wpprops == null) wpprops = new Properties(); 
   String author1 = StringUtils.nonNullString(wpprops.getProperty("author"), "-");
%>
<tr>
<td valign="top">
<% if(i != 0 && i != (wps.length - 1)) { %>
<input type="checkbox" name="v" value="<%= wps[i].getVersion() %>" />
<% } else { %>&nbsp;<% } %>
</td>
<td valign="top">
<input type="radio" name="vv" value="<%= wps[i].getVersion() %>" />
</td>
<td valign="top">
<a href="<%= WikiViewUtils.decipherURL(wlh, "viewattachment", new String[]{WikiConstants.REQUEST_PARAM_VER_KEY, String.valueOf(wps[i].getVersion())}) %>"><%= wps[i].getVersion() %></a>
</td>
<td><%= wps[i].getSize() %></td>
<td><%= (wps[i].getDate() == null ? "-" : (df.format(wps[i].getDate()))) %></td>
<td valign="top"><a href="<%= WikiUtils.getUserLink(author1) %>"><%= author1 %></a></td>
<td valign="top"><%= StringUtils.nonNullString(wpprops.getProperty("comments")) %></td>
</tr>
<% } %>
</table>

<input type="button" name="ok" value="<%= wi18n.str("jspviews.attachmentinfo.delete_selected_versions") %>" onClick="oxywiki_delete_attachment_versions_submit();" />
<input type="button" name="revert" value="<%= wi18n.str("jspviews.pageinfo.revert") %>" onClick="oxywiki_revert_submit();" />
<p/>
<a href="<%= WikiViewUtils.decipherURL(wlh, "deleteprompt", new String[]{"attachment", attachment}) %>"><%= wi18n.str("jspviews.attachments.delete") %></a>
