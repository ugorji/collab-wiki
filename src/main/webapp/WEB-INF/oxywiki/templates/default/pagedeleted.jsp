<%@ include file="_topinclude.jspf" %><%
  Map model = (Map)WikiLocal.getWikiLinkHolder().getAttribute("wiki.model.pagedeleted");
  WikiProvidedObject wp = (WikiProvidedObject)model.get("wikipage");
  boolean deleteversions = ((Boolean)model.get("deleteversions")).booleanValue();
  List versionsToDeleteList = Arrays.asList((String[])model.get("versionstodelete"));
%>
<p>
<% if(deleteversions) { %>
<%= wi18n.str("jspviews.pagedeleted.versions_msg", new String[]{wp.getName(), versionsToDeleteList.toString()}) %>
<% } else { %>
<%= wi18n.str("jspviews.pagedeleted.msg", wp.getName()) %>
<% } %>
