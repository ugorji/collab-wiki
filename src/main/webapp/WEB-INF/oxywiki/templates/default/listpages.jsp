<%@ include file="_topinclude.jspf" %><%
  WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
  Map model = (Map)wlh.getAttribute("wiki.model.listpages");
  WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();   
  WikiLinkHolder wlh2 = wlh.getClone();
  String[] pages = (String[])model.get("pages");
  String[] allfirstcharsarr = (String[])model.get("firstcharsofpages");
  char currFirstChar = '\0';
  List attachments = (List)model.get("attachments");
  
  String allpageslink = WikiViewUtils.decipherURL(wlh2, "listpages", new String[0]);
  String publpageslink = WikiViewUtils.decipherURL(wlh2, "listpages", new String[]{"published", "true"});
  String nonpublpageslink = WikiViewUtils.decipherURL(wlh2, "listpages", new String[]{"published", "false"});
  
%>
<% currFirstChar = '\0'; %>

<%= wi18n.str("jspviews.listpages.summary") %>

<p/>

<%-- put links here --%>
<a href="<%= allpageslink %>"><%= wi18n.str("jspviews.listpages.all_pages") %></a> |
<a href="<%= publpageslink %>"><%= wi18n.str("jspviews.listpages.published_pages") %></a> |
<a href="<%= nonpublpageslink %>"><%= wi18n.str("jspviews.listpages.non_published_pages") %></a> |
<br/>

<a name="PAGES" />
<h2><%= wi18n.str("jspviews.pageindex.pages") %></h2>
<% for(int i = 0; i < allfirstcharsarr.length; i++) { %>
<a href="#pages-<%= allfirstcharsarr[i] %>"><%= allfirstcharsarr[i] %></a> - 
<% } %>
<hr />

<% for(int j = 0; j < pages.length; j++) { %>
<%   if(pages[j].charAt(0) != currFirstChar) { %>
<a name="pages-<%= pages[j].charAt(0) %>" /><br/>
<%     currFirstChar = pages[j].charAt(0); %>
<%   } %>
<%   wlh2.setWikiPage(pages[j]); %>
<a href="<%= WikiViewUtils.decipherURL(wlh2, "edit") %>"><%= pages[j] %></a><br />
<% } %>

