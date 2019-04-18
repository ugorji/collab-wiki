<%@ include file="_topinclude.jspf" %><%
  WebInteractionContext webctx = WebLocal.getWebInteractionContext();
  WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
  WikiLinkHolder wlh2 = new WikiLinkHolder();
  wlh2.setCategory(wlh.getCategory());
  
  String src = request.getParameter("source");
  String dest = request.getParameter("dest");
  boolean del = "true".equals(request.getParameter("delete"));
  String[] srcdest = new String[]{src, dest};
%>
<dl>
<dd><%= wi18n.str("jspviews.copypage.page_copied", srcdest) %></dd>
<% if(del) { %>
<dd><%= wi18n.str("jspviews.copypage.page_deleted", src) %></dd>
<% } %>
</dl>

<table>
<% for(int i = 0; i < srcdest.length; i++) { 
     wlh2.setWikiPage(srcdest[i]);
%>
<tr>
<td>
<% wlh2.setAction("pageinfo"); %>
<a href="<%= wikiwebctx.toURLString(wlh2, null) %>"><%= wi18n.str("jspviews.copypage.info") %></a>
<b> | </b>
<% wlh2.setAction("view"); %>
<a href="<%= wikiwebctx.toURLString(wlh2, null) %>"><%= wi18n.str("jspviews.copypage.view") %></a>
</td>
<td>
<%= srcdest[i] %>
</td>
</tr>
<% } %>
<table>
