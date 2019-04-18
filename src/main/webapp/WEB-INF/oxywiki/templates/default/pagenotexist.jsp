<%@ include file="_topinclude.jspf" %><%
  WikiProvidedObject wp = WikiUtils.getWikiPage();
  WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
  WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
%>
<p>
<font color="red"><b><i>
<%= wi18n.str("jspviews.pagenotexist.msg", wp.getName()) %>
<% if(wce.isActionSupported(WikiConstants.ACTION_EDIT)) { %>
<p>
<a href="<%= WikiViewUtils.decipherURL(wlh, "edit") %>"><%= wi18n.str("jspviews.pagenotexist.create_it") %></a>
<% } %>
</i></b></font>
