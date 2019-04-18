<%@ include file="_topinclude.jspf" %><%@ page 
    import="net.ugorji.oxygen.wiki.actions.DiffAction" %><%
  WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
  Map model = (Map)wlh.getAttribute("wiki.model.diff");
  WikiProvidedObject wp = (WikiProvidedObject)model.get("wikipage");
  String _viewlink = WikiViewUtils.decipherURL(wlh, "view");
  String _pageinfolink = WikiViewUtils.decipherURL(wlh, "pageinfo");
  OxygenRevision wrev = (OxygenRevision)model.get("wikirevision");
  int deltasize = wrev.getSize();
%>
<b><%= wi18n.str("common.revision.before", String.valueOf(wrev.getOriginalVersion())) %></b>
<b><%= wi18n.str("common.revision.after", String.valueOf(wrev.getRevisedVersion())) %></b>

<%= wrev.toHTMLString() %>

<p>
<%= wi18n.str("jspviews.diff.back_to") %> <a href="<%= _viewlink %>"><%= wp.getName() %></a> | 
  <a href="<%= _pageinfolink %>"><%= wi18n.str("jspviews.diff.page_history") %></a>
<%--
<pre><%= rev.toString() %></pre>
<pre><%= rev.toRCSString() %></pre>
--%>
