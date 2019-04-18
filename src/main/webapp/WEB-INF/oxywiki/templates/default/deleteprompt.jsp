<%@ include file="_topinclude.jspf" %><%
  Map model = (Map)WikiLocal.getWikiLinkHolder().getAttribute("wiki.model.deleteprompt");
  String pagename = (String)model.get("pagename");
  String attachment = (String)model.get("attachment");
  WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
  boolean deleteversions = ((Boolean)model.get("deleteversions")).booleanValue();
  List versionsToDeleteList = Arrays.asList((String[])model.get("versionstodelete"));
  Map deleteversionsparams2pass = null;
  if(deleteversions) {
    deleteversionsparams2pass = new HashMap(wikiwebctx.getParameterMap());
    deleteversionsparams2pass.remove("attachment");
    deleteversionsparams2pass.put("deleteversions", "true");
  } 
%>

<p/>
<% if(deleteversions) { %>
  <% if(attachment == null) { %>

<%= wi18n.str("jspviews.deleteprompt.page_versions_prompt", new String[]{pagename, versionsToDeleteList.toString()}) %>
<br/>
<a href="<%= WikiViewUtils.decipherURL(wlh, "delete", deleteversionsparams2pass) %>"><%= wi18n.str("templates.default.delete") %></a>
&nbsp; | &nbsp;
<a href="<%= WikiViewUtils.decipherURL(wlh, "pageinfo") %>"><%= wi18n.str("templates.default.cancel") %></a>

  <% } else { %>

<%= wi18n.str("jspviews.deleteprompt.attachment_versions_prompt", new String[]{pagename, attachment, versionsToDeleteList.toString()}) %>
<br/>
<a href="<%= WikiViewUtils.decipherURL(wlh, "deleteattachment", deleteversionsparams2pass) %>"><%= wi18n.str("templates.default.delete") %></a>
&nbsp; | &nbsp;
<a href="<%= WikiViewUtils.decipherURL(wlh, "attachmentinfo") %>"><%= wi18n.str("templates.default.cancel") %></a>

  <% } %>
<% } else { %>
  <% if(attachment == null) { %>

<%= wi18n.str("jspviews.deleteprompt.page_prompt", pagename) %>
<br/>
<a href="<%= WikiViewUtils.decipherURL(wlh, "delete") %>"><%= wi18n.str("templates.default.delete") %></a>
&nbsp; | &nbsp;
<a href="<%= WikiViewUtils.decipherURL(wlh, "view") %>"><%= wi18n.str("templates.default.cancel") %></a>

  <% } else { %>

<%= wi18n.str("jspviews.deleteprompt.attachment_prompt", new String[]{pagename, attachment}) %>
<br/>
<a href="<%= WikiViewUtils.decipherURL(wlh, "deleteattachment") %>"><%= wi18n.str("templates.default.delete") %></a>
&nbsp; | &nbsp;
<a href="<%= WikiViewUtils.decipherURL(wlh, "attachments") %>"><%= wi18n.str("templates.default.cancel") %></a>

  <% } %>
<% } %>

