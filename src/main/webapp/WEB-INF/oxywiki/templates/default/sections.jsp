<%@ include file="_topinclude.jspf" %><%
  WebInteractionContext wctx = WebLocal.getWebInteractionContext();
  WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
  
  Map model = (Map)wlh.getAttribute("wiki.model.engine_details");
  long numpages = ((Long)model.get("total_num_pages")).longValue();
  long numattach = ((Long)model.get("total_num_attachments")).longValue();
  long numreviews = ((Long)model.get("total_num_reviews")).longValue();
  long numsections = ((Long)model.get("total_num_sections")).longValue();
  long numlocks = ((Long)model.get("total_num_locks")).longValue();
  long numsessions = ((Long)model.get("total_num_sessions")).longValue();
  Map tags = (Map)model.get("tags");
  OxygenTimeElapsed x = (OxygenTimeElapsed)model.get("uptime");
  String[] elapsedArgs = new String[]{String.valueOf(x.days), String.valueOf(x.hours), String.valueOf(x.minutes)};
  String attCommButtonsAnchor = String.valueOf(WikiViewUtils.nextRandomInt(99));
%>


<table class="oxy-tabpane-div-tabs">
<tr >
<td id="oxy-tabpane-sections-tab-1" class="oxy-tabpane-tab-active"><a href="#<%= attCommButtonsAnchor %>"   onClick="javascript:oxy_select_tab(1, '-sections');oxy_set_cookie('oxywiki-sections-tab', 1, null);"><%= wi18n.str("jspviews.engine_details.header") %></a></td>
<td id="oxy-tabpane-sections-tab-2" class="oxy-tabpane-tab-inactive"><a href="#<%= attCommButtonsAnchor %>" onClick="javascript:oxy_select_tab(2, '-sections');oxy_set_cookie('oxywiki-sections-tab', 2, null);"><%= wi18n.str("jspviews.sections.header") %></a></td>
</tr>
</table>

<div class="oxy-tabpane-div">

<div id="oxy-tabpane-sections-div-1" style="display:block" >
<h3><%= wi18n.str("jspviews.engine_details.header") %></h3>
<table>
<tr>
<th><%= wi18n.str("jspviews.engine_details.uptime") %></th>
<td><%= wi18n.str("jspviews.engine_details.uptime_time_spent", elapsedArgs) %></td>
</tr>
<tr>
<th><%= wi18n.str("jspviews.engine_details.total_num_pages") %></th>
<td><%= numpages %></td>
</tr>
<tr>
<th><%= wi18n.str("jspviews.engine_details.total_num_attachments") %></th>
<td><%= numattach %></td>
</tr>
<tr>
<th><%= wi18n.str("jspviews.engine_details.total_num_reviews") %></th>
<td><%= numreviews %></td>
</tr>
<tr>
<th><%= wi18n.str("jspviews.engine_details.total_num_sections") %></th>
<td><%= numsections %></td>
</tr>
<tr>
<th><%= wi18n.str("jspviews.engine_details.total_num_locks") %></th>
<td><%= numlocks %></td>
</tr>
<tr>
<th><%= wi18n.str("jspviews.engine_details.total_num_sessions") %></th>
<td><%= numsessions %></td>
</tr>
</table>

<h4><%= wi18n.str("jspviews.engine_details.tags") %></h4>
<blockquote>
<% if(tags.size() > 0) { %><i><%= StringUtils.mapToStringWithValuesInBrackets(tags) %></i><% } %>
</blockquote>

</div>

<%
  model = (Map)wlh.getAttribute("wiki.model.sections");
  OxyTable tbl = (OxyTable)model.get("table");
  int numCols = tbl.getColumnCount();
  int numRows = tbl.getRowCount();
  Map m = new HashMap();
%>
<div id="oxy-tabpane-sections-div-2" style="display:none" >
<h3><%= wi18n.str("jspviews.sections.header") %></h3>
<table border="1">
<tr>
<th><% m.clear(); m.put("sort", "0"); %><a href="<%= wctx.toURLString(wlh, m) %>"><%= wi18n.str("jspviews.sections.name") %></a></th>
<th><% m.clear(); m.put("sort", "1"); %><a href="<%= wctx.toURLString(wlh, m) %>"><%= wi18n.str("jspviews.sections.num_pages") %></a></th>
<th><% m.clear(); m.put("sort", "2"); %><a href="<%= wctx.toURLString(wlh, m) %>"><%= wi18n.str("jspviews.sections.num_locks") %></a></th>
<th><% m.clear(); m.put("sort", "3"); %><a href="<%= wctx.toURLString(wlh, m) %>"><%= wi18n.str("jspviews.sections.last_updated") %></a></th>
<th><% m.clear(); m.put("sort", "4"); %><a href="<%= wctx.toURLString(wlh, m) %>"><%= wi18n.str("jspviews.sections.desc") %></a></th>
</tr>
<%
  for(int i = 0; i < numRows; i++) {
     String cname = (String)tbl.getValueAt(i, 0);
     String lastupdatestr = "-";
     if(tbl.getValueAt(i, 3) != null) {
       lastupdatestr = df.format((Date)tbl.getValueAt(i, 3));
     }
     String desc = (String)tbl.getValueAt(i, 4);
     if(desc == null) {
       desc = "-";
     }
%>
<tr>
<td><a href="<%= WikiUtils.getCategoryURL(cname, "sysinfo", null) %>"><%= cname %></a></td>
<td><%= tbl.getValueAt(i, 1) %></td>
<td><%= tbl.getValueAt(i, 2) %></td>
<td><%= lastupdatestr %></td>
<td><%= desc %></td>
</tr>
<% } %>
</table>

</div>

</div> <!-- end of tabpane -->

<script language="javascript">
<!--
  oxy_autoselect_tab(null, 'oxywiki-sections-tab', '-sections');
-->
</script>

