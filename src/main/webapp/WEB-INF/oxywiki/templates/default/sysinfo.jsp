<%@ include file="_topinclude.jspf" %><%
  WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
  WikiEngine we = WikiLocal.getWikiEngine();
  WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
  //String[] cnames = we.getRegisteredAndLoadedWikiCategoryNames();
  //Arrays.sort(cnames);
  Properties xwl = wce.getShorthandManager().getAll();
  String[] xwlnames = (String[])(OxygenUtils.toList(xwl.propertyNames())).toArray(new String[0]);
  Arrays.sort(xwlnames);
  //do this, so we can include locks.jsp
  WikiEditLock[] elocks = (WikiEditLock[])wce.getWikiEditManager().getAllLocks().toArray(new WikiEditLock[0]);
  wlh.setAttribute("net.ugorji.oxygen.wiki.locks", elocks);
  Map tags = wce.getIndexingManager().lookupExistingTagsWithCount();
  String attCommButtonsAnchor = String.valueOf(WikiViewUtils.nextRandomInt(99));
  int idx = 0;
%>
<h3><%= wi18n.str("jspviews.sysinfo.header") %></h3>
<%= wi18n.str("jspviews.sysinfo.desc") %>

<table class="oxy-tabpane-div-tabs">
<tr >
<% idx++; %><td id="oxy-tabpane-sysinfo-tab-<%= idx %>" class="oxy-tabpane-tab-active"><a href="#<%= attCommButtonsAnchor %>"   onClick="javascript:oxy_select_tab(<%= idx %>, '-sysinfo');oxy_set_cookie('oxywiki-sysinfo-tab', <%= idx %>, null);"><%= wi18n.str("jspviews.sysinfo.general_info") %></a></td>
<% idx++; %><td id="oxy-tabpane-sysinfo-tab-<%= idx %>" class="oxy-tabpane-tab-inactive"><a href="#<%= attCommButtonsAnchor %>"   onClick="javascript:oxy_select_tab(<%= idx %>, '-sysinfo');oxy_set_cookie('oxywiki-sysinfo-tab', <%= idx %>, null);"><%= wi18n.str("jspviews.sysinfo.tags") %></a></td>
<% idx++; %><td id="oxy-tabpane-sysinfo-tab-<%= idx %>" class="oxy-tabpane-tab-inactive"><a href="#<%= attCommButtonsAnchor %>"   onClick="javascript:oxy_select_tab(<%= idx %>, '-sysinfo');oxy_set_cookie('oxywiki-sysinfo-tab', <%= idx %>, null);"><%= wi18n.str("jspviews.sysinfo.providers") %></a></td>
<% idx++; %><td id="oxy-tabpane-sysinfo-tab-<%= idx %>" class="oxy-tabpane-tab-inactive"><a href="#<%= attCommButtonsAnchor %>"   onClick="javascript:oxy_select_tab(<%= idx %>, '-sysinfo');oxy_set_cookie('oxywiki-sysinfo-tab', <%= idx %>, null);"><%= wi18n.str("jspviews.sysinfo.important_properties") %></a></td>
<% idx++; %><td id="oxy-tabpane-sysinfo-tab-<%= idx %>" class="oxy-tabpane-tab-inactive"><a href="#<%= attCommButtonsAnchor %>"   onClick="javascript:oxy_select_tab(<%= idx %>, '-sysinfo');oxy_set_cookie('oxywiki-sysinfo-tab', <%= idx %>, null);"><%= wi18n.str("jspviews.sysinfo.ext_wiki_links") %></a></td>
<% idx++; %><td id="oxy-tabpane-sysinfo-tab-<%= idx %>" class="oxy-tabpane-tab-inactive"><a href="#<%= attCommButtonsAnchor %>"   onClick="javascript:oxy_select_tab(<%= idx %>, '-sysinfo');oxy_set_cookie('oxywiki-sysinfo-tab', <%= idx %>, null);"><%= wi18n.str("jspviews.sysinfo.refs") %></a></td>
<% idx++; %><td id="oxy-tabpane-sysinfo-tab-<%= idx %>" class="oxy-tabpane-tab-inactive"><a href="#<%= attCommButtonsAnchor %>"   onClick="javascript:oxy_select_tab(<%= idx %>, '-sysinfo');oxy_set_cookie('oxywiki-sysinfo-tab', <%= idx %>, null);"><%= wi18n.str("jspviews.sysinfo.page_locks") %></a></td>
</tr>
</table>

<div class="oxy-tabpane-div">

<% idx = 0; %>
<% idx++; %><div id="oxy-tabpane-sysinfo-div-<%= idx %>" style="display:block" >
<h4><%= wi18n.str("jspviews.sysinfo.general_info") %></h4>

<table border="1">
<tr>
<th><%= wi18n.str("jspviews.sysinfo.home_page") %></th>
<td><b><a href="<%= WikiUtils.getCategoryURL(wce.getName(), "view", null) %>"><%= wce.getEntryPage() %></a></b></td>
</tr>
<tr>
<th><%= wi18n.str("jspviews.sysinfo.name") %></th>
<td><%= wce.getProperty(WikiConstants.ENGINE_NAME_KEY) %></td>
</tr>
<%--
<tr>
<th><%= wi18n.str("jspviews.sysinfo.sections_list") %></th>
<td><%= Arrays.asList(cnames) %></td>
</tr>
--%>
<tr>
<th><%= wi18n.str("jspviews.sysinfo.num_pages") %></th>
<td><%= wce.getIndexingManager().lookupPageNames(null, null).length %></td>
</tr>
<tr>
<th><%= wi18n.str("jspviews.sysinfo.num_attachments") %></th>
<td><%= wce.getIndexingManager().lookupAttachmentNames(null, null, null).length %></td>
</tr>
<tr>
<th><%= wi18n.str("jspviews.sysinfo.num_reviews") %></th>
<td><%= wce.getIndexingManager().lookupPageReviewNames(null, null, null).length %></td>
</tr>
</table>
</div>

<% idx++; %><div id="oxy-tabpane-sysinfo-div-<%= idx %>" style="display:none" >
<h4><%= wi18n.str("jspviews.sysinfo.tags") %></h4>
<blockquote>
<% if(tags.size() > 0) { %><i><%= StringUtils.mapToStringWithValuesInBrackets(tags) %></i><% } %>
</blockquote>
</div>


<% idx++; %><div id="oxy-tabpane-sysinfo-div-<%= idx %>" style="display:none" >
<h4><%= wi18n.str("jspviews.sysinfo.providers") %></h4>

<table border="1">
<tr>
<td><%= wi18n.str("jspviews.sysinfo.page_provider") %></td>
<td><%= wce.getProperty(WikiConstants.PAGE_PROVIDER_KEY) %></td>
</tr>
<tr>
<td><%= wi18n.str("jspviews.sysinfo.attach_provider") %></td>
<td><%= wce.getProperty(WikiConstants.ATTACHMENT_PROVIDER_KEY) %></td>
</tr>
<tr>
<td><%= wi18n.str("jspviews.sysinfo.review_provider") %></td>
<td><%= wce.getProperty(WikiConstants.PAGE_REVIEW_PROVIDER_KEY) %></td>
</tr>
<tr>
<td><%= wi18n.str("jspviews.sysinfo.filesystem_helper") %></td>
<td><%= wce.getProperty(WikiConstants.PROVIDER_FILESYSTEM_HELPER_CLASS_KEY) %></td>
</tr>
<tr>
<td><%= wi18n.str("jspviews.sysinfo.cache_manager") %></td>
<td><%= wce.getProperty(WikiConstants.ENGINE_CACHE_MANAGER_KEY) %></td>
</tr>
</table>
</div>

<% idx++; %><div id="oxy-tabpane-sysinfo-div-<%= idx %>" style="display:none" >
<h4><%= wi18n.str("jspviews.sysinfo.important_properties") %></h4>

<table border="1">
<% String[] propkeys = new String[]
     {"net.ugorji.oxygen.wiki.show.details",
      "net.ugorji.oxygen.wiki.index.details", 
      "net.ugorji.oxygen.wiki.actions_not_supported"}; 
   for(int i = 0; i < propkeys.length; i++) { 
%>
<tr>
<td><%= propkeys[i] %></td>
<td><%= wce.getProperty(propkeys[i]) %></td>
</tr>
<% } %>
</table>
</div>

<% idx++; %><div id="oxy-tabpane-sysinfo-div-<%= idx %>" style="display:none" >
<h4><%= wi18n.str("jspviews.sysinfo.ext_wiki_links") %></h4>

<table border="1">
<%
  for(int i = 0; i < xwlnames.length; i++) {
%>
<tr>
<td><%= xwlnames[i] %></td>
<td><%= xwl.getProperty(xwlnames[i]) %></td>
</tr>
<% } %>
</table>
</div>

<% idx++; %><div id="oxy-tabpane-sysinfo-div-<%= idx %>" style="display:none" >
<h4><%= wi18n.str("jspviews.sysinfo.refs") %></h4>
<% out.flush(); %>
<% WikiViewUtils.includeView("refs"); %>
</div>

<% idx++; %><% int locksidx = idx; %><div id="oxy-tabpane-sysinfo-div-<%= idx %>" style="display:none" >
<a name="sysinfo.locks" />
<h4><%= wi18n.str("jspviews.sysinfo.page_locks") %></h4>
<jsp:include page="locks.jsp" />
</div>

</div> <!-- end of tabpane -->

<script language="javascript">
<!--
  oxy_autoselect_tab(new Array("", "", "", "", "", "", "sysinfo.locks"), 'oxywiki-sysinfo-tab', '-sysinfo');
-->
</script>


