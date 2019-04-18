<%@ include file="_topinclude.jspf" %><%
  WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
  WikiLinkHolder wlh2 = wlh.getClone();
  WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();   
  String[] tags = wikiwebctx.getParameterValues("tag");
  //System.out.println("listpagesfortag.jsp: tags: " + Arrays.asList(tags));
%>

<%
  for(int i = 0; i < tags.length; i++) { 
    String[] tagpages = wce.getIndexingManager().lookupPageNamesGivenTag(tags[i]);
    Arrays.sort(tagpages);
%>
<h3><%= wi18n.str("jspviews.pageindex.tags") %>: <%= tags[i] %></h3>
<%   for(int j = 0; j < tagpages.length; j++) { 
       wlh2.setWikiPage(tagpages[j]);
%>
<a href="<%= WikiViewUtils.decipherURL(wlh2, "view") %>"><%= tagpages[j] %></a><br />
<%   } %>
<% } %>
