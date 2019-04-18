
<%@ include file="_topinclude.jspf" %><%
  WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
  //Map m = wlh.getExtraparams();
  Map m = wikiwebctx.getParameterMap();
  //System.out.println(" >>>> '" + request.getQueryString() + "'");
%>
<table>
<% for(Iterator itr = m.entrySet().iterator(); itr.hasNext(); ) {
     Map.Entry me = (Map.Entry)itr.next();
%>
<tr><td><%= me.getKey() %></td><td><%= Arrays.asList((String[])me.getValue()) %></td></tr>
<% } %>
</table>

<form action="<%= WikiViewUtils.decipherURL(wlh, "test", m) %>" method="post" enctype="multipart/form-data" >
<input type="hidden" name="h1" value="h1.1" />
<input type="hidden" name="h1" value="h1.2" />
<input type="text" name="ab" value="hello" /><br/>
<input type="file" name="file0" ><br/>
<input type="hidden" name="postattachments" value="true" />
<input type="submit" name="ok" value="Go">
</form>

