<%@ include file="_topinclude.jspf" %><%
  WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
  Map model = (Map)wlh.getAttribute("wiki.model.recentchanges");
  WikiEngine we = WikiLocal.getWikiEngine();
  WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
  boolean detailsNecessary = ((Boolean)model.get("detailsNecessary")).booleanValue();
  WikiLinkHolder wlh2 = wlh.getClone();
  Date startdate = (Date)model.get("startdate");
  String[] categories = (String[])model.get("categories");
  Map wpsmap = (Map)model.get("wikipagesmap");
  WikiProvidedObject[] wps = null;
  int changeperiod = ((Integer)model.get("changeperiod")).intValue();
%>
<p>
<table border="1">
<tr>
<th colspan="6"><%= wi18n.str("jspviews.recentchanges.header", new String[]{df.format(startdate), String.valueOf(changeperiod)}) %></th>
</tr>
<tr>
<th><%= wi18n.str("jspviews.recentchanges.section") %></th>
<% if(detailsNecessary) { %>
<th><%= wi18n.str("jspviews.recentchanges.version") %></th>
<% } %>
<th><%= wi18n.str("jspviews.recentchanges.page") %></th>
<th><%= wi18n.str("jspviews.recentchanges.last_mod") %></th>
<% if(detailsNecessary) { %>
<th><%= wi18n.str("jspviews.recentchanges.author") %></th>
<th><%= wi18n.str("jspviews.recentchanges.comments") %></th>
<% } %>
</tr>
<% for(int i0 = 0; i0 < categories.length; i0++) { 
     //WikiLocal.setWikiCategoryEngine(we.getWikiCategoryEngine(categories[i0]));
     wlh2.setCategory(categories[i0]);
     wps = (WikiProvidedObject[])wpsmap.get(categories[i0]);
     for(int i = 0; i < wps.length; i++) { 
       wlh2.setWikiPage(wps[i].getName()); 
%>
<tr>
<th><%= categories[i0] %></th>
<% if(detailsNecessary) { %>
<td><%= wps[i].getVersion() %></td>
<% } %>
<td><a href="<%= WikiViewUtils.decipherURL(wlh2, "view") %>"><%= wps[i].getName() %></a></td>
<td><%= df.format(wps[i].getDate()) %></td>
<% if(detailsNecessary) { %>
<%   String author1 = StringUtils.nonNullString(WikiViewUtils.getAuthor(wps[i]), "-"); %>
<td><a href="<%= WikiUtils.getUserLink(author1) %>"><%= author1 %></a></td>
<td><%= WikiViewUtils.getAttribute(wps[i], WikiConstants.ATTRIBUTE_COMMENTS, "-") %></td>
<% } %>
</tr> 
<% } %>
<% } %>
</table>

