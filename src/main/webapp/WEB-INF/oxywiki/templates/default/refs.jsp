<%@ include file="_topinclude.jspf" %><%
  WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
  WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
  WikiLinkHolder wlh2 = wlh.getClone();

  String[] nonref = wce.getIndexingManager().getNonReferencedPages();
  String[] nonexist = wce.getIndexingManager().getNonExistentPages();
%>
<h3><%= wi18n.str("jspviews.refs.non_ref_header") %></h3>
<table>
<tr>
<th><%= wi18n.str("jspviews.refs.pages") %></th>
</tr>
<% for(int i = 0; i < nonref.length; i++) { %>
<%   wlh2.setWikiPage(nonref[i]); %>
<tr>
<td><a href="<%= WikiViewUtils.decipherURL(wlh2, "view") %>"><%= nonref[i] %></a></td>
</tr>
<% } %>
</table>

<h3><%= wi18n.str("jspviews.refs.non_exist_header") %></h3>
<table border="1">
<tr>
<th><%= wi18n.str("jspviews.refs.pages") %></th>
<th><%= wi18n.str("jspviews.refs.ref_by") %></th>
</tr>
<% for(int i = 0; i < nonexist.length; i++) { %>
<%   wlh2.setWikiPage(nonexist[i]); %>
<td><a href="<%= WikiViewUtils.decipherURL(wlh2, "view") %>"><%= nonexist[i] %></a></td>
<td>
<%   String[] nonexistreferers = wce.getIndexingManager().getPagesThatReference(nonexist[i]);
     for(int j = 0; j < nonexistreferers.length; j++) { 
       wlh2.setWikiPage(nonexistreferers[j]); 
%><a href="<%= WikiViewUtils.decipherURL(wlh2, "view") %>"><%= nonexistreferers[j] %></a>
<%   } %>
</td>
</tr>
<% } %>
</table>

