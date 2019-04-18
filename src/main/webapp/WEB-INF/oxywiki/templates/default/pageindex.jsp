<%@ include file="_topinclude.jspf" %><%
  WikiProvidedObject wp = null;
  WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
  Map model = (Map)wlh.getAttribute("wiki.model.pageindex");
  WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();   
  WikiLinkHolder wlh2 = wlh.getClone();
  Map tags = (Map)model.get("tags");
  String[] pages = (String[])model.get("pages");
  String[] deletedpages = (String[])model.get("deletedpages");
  Map attachments = (Map)model.get("attachments");
  Map deletedattachments = (Map)model.get("deletedattachments");

  Map pageDrafts = (Map)model.get("pagedrafts");
  boolean pageDraftSupported = ((Boolean)model.get("page_draft_supported")).booleanValue();

  int pageversion = WikiProvidedObject.VERSION_LATEST_DETAILS_NECESSARY;
  
  String _listpagesfortaglink = WikiViewUtils.decipherURL(wlh, "listpagesfortag");
  String attCommButtonsAnchor = String.valueOf(WikiViewUtils.nextRandomInt(99));
  int tabidx = 0;
%>

<script LANGUAGE="JavaScript">
<!--
function oxywiki_pageindex_listfortag(mytag) {
  var oxyurl = '<%= _listpagesfortaglink %>';
  var oxypars = 'tag=' + mytag;
  var oxyAjax = new Ajax.Updater('oxy-pageindex-div-listpagesfortag', oxyurl, {method: 'get', parameters: oxypars});
}
// -->
</script>

<p/>

<% tabidx = 0; %>
<table class="oxy-tabpane-div-tabs">
<tr >
<% tabidx++; %><td id="oxy-tabpane-pageindex-tab-<%= tabidx %>" class="oxy-tabpane-tab-active"><a href="#<%= attCommButtonsAnchor %>"   onClick="javascript:oxy_select_tab(<%= tabidx %>, '-pageindex');oxy_set_cookie('oxywiki-pageindex-tab', <%= tabidx %>, null);"><%= wi18n.str("jspviews.pageindex.pages") %></a></td>
<% tabidx++; %><td id="oxy-tabpane-pageindex-tab-<%= tabidx %>" class="oxy-tabpane-tab-inactive"><a href="#<%= attCommButtonsAnchor %>" onClick="javascript:oxy_select_tab(<%= tabidx %>, '-pageindex');oxy_set_cookie('oxywiki-pageindex-tab', <%= tabidx %>, null);"><%= wi18n.str("jspviews.pageindex.deleted_pages") %></a></td>
<% tabidx++; %><td id="oxy-tabpane-pageindex-tab-<%= tabidx %>" class="oxy-tabpane-tab-inactive"><a href="#<%= attCommButtonsAnchor %>" onClick="javascript:oxy_select_tab(<%= tabidx %>, '-pageindex');oxy_set_cookie('oxywiki-pageindex-tab', <%= tabidx %>, null);"><%= wi18n.str("jspviews.pageindex.attachments") %></a></td>
<% tabidx++; %><td id="oxy-tabpane-pageindex-tab-<%= tabidx %>" class="oxy-tabpane-tab-inactive"><a href="#<%= attCommButtonsAnchor %>" onClick="javascript:oxy_select_tab(<%= tabidx %>, '-pageindex');oxy_set_cookie('oxywiki-pageindex-tab', <%= tabidx %>, null);"><%= wi18n.str("jspviews.pageindex.deleted_attachments") %></a></td>
<% tabidx++; %><td id="oxy-tabpane-pageindex-tab-<%= tabidx %>" class="oxy-tabpane-tab-inactive"><a href="#<%= attCommButtonsAnchor %>" onClick="javascript:oxy_select_tab(<%= tabidx %>, '-pageindex');oxy_set_cookie('oxywiki-pageindex-tab', <%= tabidx %>, null);"><%= wi18n.str("jspviews.pageindex.page_drafts") %></a></td>
<% tabidx++; %><td id="oxy-tabpane-pageindex-tab-<%= tabidx %>" class="oxy-tabpane-tab-inactive"><a href="#<%= attCommButtonsAnchor %>" onClick="javascript:oxy_select_tab(<%= tabidx %>, '-pageindex');oxy_set_cookie('oxywiki-pageindex-tab', <%= tabidx %>, null);"><%= wi18n.str("jspviews.pageindex.tags") %></a></td>
</tr>
</table>

<% tabidx = 0; %>
<div class="oxy-tabpane-div">

<% tabidx++; %><div id="oxy-tabpane-pageindex-div-<%= tabidx %>" style="display:block" >
<h2><%= wi18n.str("jspviews.pageindex.pages") %></h2>

<table>
<tr>
<th><%= wi18n.str("jspviews.pageindex.actions") %></th>
<th><%= wi18n.str("jspviews.pageindex.page_name") %></th>
<th><%= wi18n.str("jspviews.pageindex.date") %></th>
<th><%= wi18n.str("jspviews.pageindex.author") %></th>
<th><%= wi18n.str("jspviews.pageindex.size") %></th>
<th><%= wi18n.str("jspviews.pageindex.version") %></th>
</tr>
<% for(int j = 0; j < pages.length; j++) { 
     wlh2.setWikiPage(pages[j]); 
     wp = wce.getPageProvider().getPage(pages[j], pageversion);
     String author1 = StringUtils.nonNullString(wp.getAttribute("author"), "-");
%>
<tr>
<td><a href="<%= WikiViewUtils.decipherURL(wlh2, "pageinfo") %>"><%= wi18n.str("jspviews.pageindex.info") %></a></td>
<td><a href="<%= WikiViewUtils.decipherURL(wlh2, "view") %>"><%= pages[j] %></a></td>
<td><%= df.format(wp.getDate()) %></td>
<td><a href="<%= WikiUtils.getUserLink(author1) %>"><%= author1 %></a></td>
<td><%= wp.getSize() %></td>
<td><%= wp.getVersion() %></td>
<tr>
<% } %>
</table>
</div>

<% tabidx++; %><div id="oxy-tabpane-pageindex-div-<%= tabidx %>" style="display:none" >
<h2><%= wi18n.str("jspviews.pageindex.deleted_pages") %></h2>

<%= wi18n.str("jspviews.pageindex.deleted_pages.msg") %>
<p/>
<table>
<tr>
<th><%= wi18n.str("jspviews.pageindex.actions") %></th>
<th><%= wi18n.str("jspviews.pageindex.page_name") %></th>
<th><%= wi18n.str("jspviews.pageindex.date") %></th>
<th><%= wi18n.str("jspviews.pageindex.author") %></th>
<th><%= wi18n.str("jspviews.pageindex.size") %></th>
<th><%= wi18n.str("jspviews.pageindex.version") %></th>
</tr>
<% for(int j = 0; j < deletedpages.length; j++) { 
     wlh2.setWikiPage(deletedpages[j]);
     wp = wce.getPageProvider().getPage(deletedpages[j], WikiProvidedObject.VERSION_LATEST_DETAILS_NECESSARY);
     String author1 = StringUtils.nonNullString(wp.getAttribute("author"), "-");
%>
<tr>
<td><a href="<%= WikiViewUtils.decipherURL(wlh2, "pageinfo") %>"><%= wi18n.str("jspviews.pageindex.info") %></a></td>
<td><a href="<%= WikiViewUtils.decipherURL(wlh2, "edit") %>"><%= deletedpages[j] %></a></td>
<td><%= df.format(wp.getDate()) %></td>
<td><a href="<%= WikiUtils.getUserLink(author1) %>"><%= author1 %></a></td>
<td><%= wp.getSize() %></td>
<td><%= wp.getVersion() %></td>
<tr>
<% } %>
</table>
</div>

<% tabidx++; %><div id="oxy-tabpane-pageindex-div-<%= tabidx %>" style="display:none" >
<h2><%= wi18n.str("jspviews.pageindex.attachments") %></h2>

<table>
<tr>
<th><%= wi18n.str("jspviews.pageindex.page_source") %></th>
<th><%= wi18n.str("jspviews.pageindex.attachment") %></th>
</tr>
<% if(attachments != null) {
   for(Iterator itr = (new TreeSet(attachments.keySet())).iterator(); itr.hasNext(); ) { 
     String wpage = (String)itr.next();
     List atts = (List)attachments.get(wpage);
     String[] attsarr = (String[])((List)attachments.get(wpage)).toArray(new String[0]);
     Arrays.sort(attsarr);
%>
<tr>
<%   wlh2.setWikiPage(wpage); 
     wlh2.setExtrainfo(null);
     wlh2.setAction(WikiConstants.ACTION_VIEW);
%>
<td><a href="<%= wlh2.getWikiURL() %>"><%= wpage %></a></td>
<td>
<%   for(int i = 0; i < attsarr.length; i++) {
       wlh2.setExtrainfo(attsarr[i]); 
       wlh2.setAction("attachmentinfo"); 
%>
<a href="<%= wlh2.getWikiURL() %>"><%= wi18n.str("jspviews.pageindex.info") %></a> <b>|</b> 
<%     wlh2.setAction(WikiConstants.ACTION_VIEW_ATTACHMENT); %>
<a href="<%= wlh2.getWikiURL() %>"><%= attsarr[i] %></a>
<%     if(i != attsarr.length - 1) { %><br/><% } %>
<%   } %>
</td>
</tr>
<% } %>
<% } %>
</table>
</div>

<% tabidx++; %><div id="oxy-tabpane-pageindex-div-<%= tabidx %>" style="display:none" >
<h2><%= wi18n.str("jspviews.pageindex.deleted_attachments") %></h2>

<table>
<tr>
<th><%= wi18n.str("jspviews.pageindex.page_source") %></th>
<th><%= wi18n.str("jspviews.pageindex.attachment") %></th>
</tr>
<% if(deletedattachments != null) {
   for(Iterator itr = (new TreeSet(deletedattachments.keySet())).iterator(); itr.hasNext(); ) { 
     String wpage = (String)itr.next();
     List atts = (List)attachments.get(wpage);
     String[] attsarr = (String[])((List)deletedattachments.get(wpage)).toArray(new String[0]);
     Arrays.sort(attsarr);
%>
<tr>
<%   wlh2.setWikiPage(wpage); 
     wlh2.setExtrainfo(null);
     wlh2.setAction(WikiConstants.ACTION_VIEW);
%>
<td><a href="<%= wlh2.getWikiURL() %>"><%= wpage %></a></td>
<td>
<%   for(int i = 0; i < attsarr.length; i++) {
       wlh2.setExtrainfo(attsarr[i]); 
       wlh2.setAction("attachmentinfo");
%>
<a href="<%= wlh2.getWikiURL() %>"><%= wi18n.str("jspviews.pageindex.info") %></a> <b>|</b> 
<strike><%= attsarr[i] %></strike>
<%     if(i != attsarr.length - 1) { %><br/><% } %>
<%   } %>
</td>
</tr>
<% } %>
<% } %>
</table>
</div>

<% tabidx++; %><div id="oxy-tabpane-pageindex-div-<%= tabidx %>" style="display:none" >
<h2><%= wi18n.str("jspviews.pageindex.page_drafts") %></h2>

<%= wi18n.str("jspviews.pageindex.page_drafts.msg") %>
<p/>

<%@ include file="pageindex.pagedrafts.jspf" %>
</div>

<% tabidx++; %><div id="oxy-tabpane-pageindex-div-<%= tabidx %>" style="display:none" >
<% for(Iterator itr = tags.entrySet().iterator(); itr.hasNext(); ) { 
     Map.Entry me = (Map.Entry)itr.next();
     String tagx = (String)me.getKey();
     SimpleInt countx = (SimpleInt)me.getValue();
%>
<a href="javascript:oxywiki_pageindex_listfortag('<%= tagx %>')"><%= tagx %>(<%= countx %>)</a>
<% } %>

<div id="oxy-pageindex-div-listpagesfortag" >
&nbsp; <br/>
&nbsp; <br/>
&nbsp; <br/>
</div>

</div>
</div><%-- end oxy-tabpane-div --%>

<script language="javascript">
<!--
  var tabidx = oxy_get_cookie('oxywiki-pageindex-tab');
  if(tabidx != null) {
    oxy_select_tab(tabidx, '-pageindex');
  }
-->
</script>

