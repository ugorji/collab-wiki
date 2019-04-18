<%@ include file="_topinclude.jspf" %><%
  WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
  WikiLinkHolder wlh2 = wlh.getClone();
  WikiCategoryEngine wcengine = WikiLocal.getWikiCategoryEngine();
  WikiEditManager editManager = wcengine.getWikiEditManager();
  WikiEditLock[] elocks = (WikiEditLock[])wlh.getAttribute("net.ugorji.oxygen.wiki.locks");
  long lockttl = wcengine.getWikiEditManager().getMaxTTL();
%>
<% if(elocks.length > 0) { %>
<%= wi18n.str("jspviews.locks.header") %><br/>
<table border="1">
<tr>
<th><%= wi18n.str("jspviews.locks.page") %></th>
<th><%= wi18n.str("jspviews.locks.user") %></th>
<th><%= wi18n.str("jspviews.locks.since") %></th>
<th><%= wi18n.str("jspviews.locks.expires") %></th>
<th><%= wi18n.str("jspviews.locks.time_left") %></th>
</tr>
<%  
    for(int i = 0; i < elocks.length; i++) { 
      wlh2.setWikiPage(elocks[i].getPagename());
      
%>
<tr>
<td><a href="<%= WikiViewUtils.decipherURL(wlh2, "view") %>"><%= elocks[i].getPagename() %></a></td>
<td><a href="<%= WikiUtils.getUserLink(elocks[i].getUsername()) %>"><%= elocks[i].getUsername() %></a></td>
<td><%= df.format(new Date(elocks[i].getLocktime())) %></td>
<td><%= df.format(new Date(editManager.getTTL(elocks[i]) + System.currentTimeMillis())) %></td>
<td><%= editManager.getFormattedTTL(elocks[i]) %></td>
</tr>
<% } %>
</table>
<% } else { %>
<%= wi18n.str("jspviews.locks.no_locks_msg") %>
<% } %>

