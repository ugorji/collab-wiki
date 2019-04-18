<%@ include file="_topinclude.jspf" %><%
  WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
  String user1 = (String)wlh.getAttribute("wiki.model.user.username");
%>
<%= wi18n.str("jspviews.user.message", user1) %>
