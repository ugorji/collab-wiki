<%@ include file="_topinclude.jspf" %><%
  WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
  Map model = (Map)wlh.getAttribute("wiki.model.pageinfo");
  WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
  boolean delVersionSupported = ((Boolean)model.get("del_versions_supported")).booleanValue();
  WikiLinkHolder wlh2 = wlh.getClone();
  boolean showversions = ((Boolean)model.get("showversions")).booleanValue();
  boolean showextrainfo = ((Boolean)model.get("showextrainfo")).booleanValue();
  boolean pageInfoInvisible = "true".equals(wlh.getAttribute("hide.pageinfo"));
  WikiProvidedObject wp = (WikiProvidedObject)model.get("wikipage");
  WikiProvidedObject[] wps = (WikiProvidedObject[])model.get("pageversions");
  String pagename = (String)model.get("pagename");
  String parentpage = (String)model.get("parentpage");
  Map pagedraft = (Map)model.get("page_draft");
  Map pageDrafts = null;
  if(pagedraft != null) {
    pageDrafts = new HashMap();
    pageDrafts.put(pagename, pagedraft);
  }
  String[] prefs = (String[])model.get("thispagereferences");
  String[] prefby = (String[])model.get("thispageisreferencedby");
  String[] subpages = (String[])model.get("subpages");
  String tmpstr = "";

  String attCommButtonsAnchor = String.valueOf(WikiViewUtils.nextRandomInt(99));
  int tabidx = 0;
%>

<div id="oxywiki_pageinfo" style="display:<%= WikiViewUtils.visibilityJS(!pageInfoInvisible) %>;">
<% tabidx = 0; %>
<table class="oxy-tabpane-div-tabs">
<tr >
<% tabidx++; %><td id="oxy-tabpane-pageinfo-tab-<%= tabidx %>" class="oxy-tabpane-tab-active"><a href="#<%= attCommButtonsAnchor %>"   onClick="javascript:oxy_select_tab(<%= tabidx %>, '-pageinfo');oxy_set_cookie('oxywiki-pageinfo-tab', <%= tabidx %>, null);"><%= wi18n.str("jspviews.pageinfo.versions") %></a></td>
<% tabidx++; %><td id="oxy-tabpane-pageinfo-tab-<%= tabidx %>" class="oxy-tabpane-tab-inactive"><a href="#<%= attCommButtonsAnchor %>" onClick="javascript:oxy_select_tab(<%= tabidx %>, '-pageinfo');oxy_set_cookie('oxywiki-pageinfo-tab', <%= tabidx %>, null);"><%= wi18n.str("jspviews.pageinfo.hierachy_et_al") %></a></td>
<% tabidx++; %><td id="oxy-tabpane-pageinfo-tab-<%= tabidx %>" class="oxy-tabpane-tab-inactive"><a href="#<%= attCommButtonsAnchor %>" onClick="javascript:oxy_select_tab(<%= tabidx %>, '-pageinfo');oxy_set_cookie('oxywiki-pageinfo-tab', <%= tabidx %>, null);"><%= wi18n.str("jspviews.pageinfo.attachments") %></a></td>
</tr>
</table>

<% tabidx = 0; %>
<div class="oxy-tabpane-div">

<% tabidx++; %><div id="oxy-tabpane-pageinfo-div-<%= tabidx %>" style="display:block" >
<% if(showversions) { %>
<SCRIPT LANGUAGE="JavaScript"><!--
function oxywiki_get_selected_value_button(radioObj) {
  var radioLength = radioObj.length;
  for(var i = 0; i < radioLength; i++) {
    if(radioObj[i].checked) {
      return radioObj[i].value;
    }
  }
  return "";
}
function oxywiki_update_diff_submit_button() {
  var r1val = oxy_get_selected_radio_value(document.oxywiki_pageinfo_form.r1);
  var r2val = oxy_get_selected_radio_value(document.oxywiki_pageinfo_form.r2);

  var pfx = '<%= wi18n.str("jspviews.pageinfo.diff") %>';
  document.oxywiki_pageinfo_form.diff.disabled = false;
  document.oxywiki_pageinfo_form.diff.value = pfx + ' ' + r1val + " - " + r2val;

  pfx = '<%= wi18n.str("jspviews.pageinfo.revert") %>';
  document.oxywiki_pageinfo_form.revert_1.disabled = false;
  document.oxywiki_pageinfo_form.revert_1.value = pfx + ' ' + r1val;
  document.oxywiki_pageinfo_form.revert_2.disabled = false;
  document.oxywiki_pageinfo_form.revert_2.value = pfx + ' ' + r2val;

}

function oxywiki_diff_submit() {
  document.oxywiki_pageinfo_form.action = '<%= WikiViewUtils.decipherURL(wlh2, "diff") %>';
  document.oxywiki_pageinfo_form.deleteversions.disabled = true;
  document.oxywiki_pageinfo_form.vv.disabled = true;
  document.oxywiki_pageinfo_form.submit();
}

function oxywiki_revert_submit(ii) {
  document.oxywiki_pageinfo_form.action = '<%= WikiViewUtils.decipherURL(wlh2, "revert") %>';
  document.oxywiki_pageinfo_form.deleteversions.disabled = true;
  document.oxywiki_pageinfo_form.vv.disabled = false;
  if(ii == 1) {
    document.oxywiki_pageinfo_form.vv.value = oxy_get_selected_radio_value(document.oxywiki_pageinfo_form.r1);
  } else if (ii == 2) {
    document.oxywiki_pageinfo_form.vv.value = oxy_get_selected_radio_value(document.oxywiki_pageinfo_form.r2);
  } else {
    return;
  }
  //alert("vv value is: " + document.oxywiki_pageinfo_form.vv.value);
  document.oxywiki_pageinfo_form.submit();
}
    
function oxywiki_delete_versions_submit() {
  document.oxywiki_pageinfo_form.action = '<%= WikiViewUtils.decipherURL(wlh2, "deleteprompt") %>';
  document.oxywiki_pageinfo_form.vv.disabled = true;
  document.oxywiki_pageinfo_form.deleteversions.disabled = false;
  document.oxywiki_pageinfo_form.deleteversions.value = 'true';
  document.oxywiki_pageinfo_form.submit();
}

// --></script>
<h3><%= wi18n.str("jspviews.pageinfo.versions_header", pagename) %></h3>
<form name="oxywiki_pageinfo_form" type="get" action="<%= WikiViewUtils.decipherURL(wlh2, "diff") %>">
<input type="hidden" name="deleteversions" value="true" />
<input type="hidden" name="vv" value="-1" />
<table border="1">
<tr>
<th>--</th>
<th><%= wi18n.str("jspviews.pageinfo.versions_diff") %></th>
<th colspan="8"><%= wi18n.str("jspviews.pageinfo.versions_metadata") %></th>
</tr>
<tr>
<th>--</th>
<th>--</th>
<th>--</th>
<th><%= wi18n.str("jspviews.pageinfo.modified") %></th>
<th><%= wi18n.str("jspviews.pageinfo.size") %></th>
<th><%= wi18n.str("jspviews.pageinfo.author") %></th>
<th><%= wi18n.str("jspviews.pageinfo.flags") %></th>
<th><%= wi18n.str("jspviews.pageinfo.tags") %></th>
<th><%= wi18n.str("jspviews.pageinfo.subscribers") %></th>
<th><%= wi18n.str("jspviews.pageinfo.comments") %></th>
</tr>

<%-- System.out.println("wps.length: " + wps.length); --%>
<% for(int i = 0; i < wps.length; i++) { 
     wlh2.setVersion(wps[i].getVersion());
     Properties wpprops = wps[i].getAttributes();
     if(wpprops == null) wpprops = new Properties(); 
     String author1 = StringUtils.nonNullString(wpprops.getProperty("author"), "-");
     //System.out.println("wpprops: " + i + ": " + wpprops);
%>
<tr>
<td valign="top">
<% if(i != 0 && i != (wps.length - 1)) { %>
<input type="checkbox" name="v" value="<%= wps[i].getVersion() %>" />
<% } else { %>&nbsp;<% } %>
<td valign="top">
<a href="<%= WikiViewUtils.decipherURL(wlh2, "view") %>"><%= wps[i].getVersion() %></a>
</td>
<td valign="top">
<% if(i == 0) { tmpstr = "checked"; } else { tmpstr = ""; } %>
<input type="radio" name="r1" value="<%= wps[i].getVersion() %>" onClick="oxywiki_update_diff_submit_button();" <%= tmpstr %> />
<input type="radio" name="r2" value="<%= wps[i].getVersion() %>" onClick="oxywiki_update_diff_submit_button();" <%= tmpstr %> />
</td>
<td valign="top"><%= df.format(wps[i].getDate()) %></td>
<td valign="top"><%= wps[i].getSize() %></td>
<td valign="top"><a href="<%= WikiUtils.getUserLink(author1) %>"><%= author1 %></a></td>
<td valign="top">
  &nbsp;<% if("true".equals(wpprops.getProperty("minor.edit"))) { %><%= wi18n.str("jspviews.pageinfo.minor_edit_flag_on") %><% } %>
</td>
<td valign="top"><%= StringUtils.nonNullString(wpprops.getProperty("tags")) %></td>
<td valign="top"><%= StringUtils.nonNullString(wpprops.getProperty("subscribers")) %></td>
<td valign="top"><%= StringUtils.nonNullString(wpprops.getProperty("comments")) %></td>
</tr>
<% } %>
</table>

<% if(delVersionSupported) { %><input type="button" name="del_sel_versions" value="<%= wi18n.str("jspviews.pageinfo.delete_selected_versions") %>" onClick="oxywiki_delete_versions_submit();" /><% } %>
<input type="button" name="diff" value="<%= wi18n.str("jspviews.pageinfo.diff") %> 1 - 1" disabled onClick="oxywiki_diff_submit();" />
<input type="button" name="revert_1" value="<%= wi18n.str("jspviews.pageinfo.revert") %> 1" disabled onClick="oxywiki_revert_submit(1);" />
<input type="button" name="revert_2" value="<%= wi18n.str("jspviews.pageinfo.revert") %> 1" disabled onClick="oxywiki_revert_submit(2);" />
<br/>
</form> 
<% } %>

</div>

<% tabidx++; %><div id="oxy-tabpane-pageinfo-div-<%= tabidx %>" style="display:none" >
<% if(showextrainfo) { %>
<%    wlh2.setVersion(WikiProvidedObject.VERSION_LATEST_DETAILS_UNNECESSARY); %>

<h3><%= wi18n.str("jspviews.pageinfo.extra_info_header", pagename) %></h3>
<b><%= wi18n.str("jspviews.pageinfo.parent_page") %></b>
<% if(parentpage != null && parentpage.length() > 0) { 
     wlh2.setWikiPage(parentpage); %>
<a href="<%= WikiViewUtils.decipherURL(wlh2, "view") %>"><%= parentpage %></a>
<% } else { %>
<%= wi18n.str("jspviews.pageinfo.parent_page_none") %>
<% } %>

<table border="1">
<tr>
<th><%= wi18n.str("jspviews.pageinfo.child_pages") %></th>
<th><%= wi18n.str("jspviews.pageinfo.ref_by") %></th>
<th><%= wi18n.str("jspviews.pageinfo.refs") %></th>
</tr>
<tr>
<td valign="top">
<% for(int i = 0; i < subpages.length; i++) { %>
<%   wlh2.setWikiPage(subpages[i]); %>
<a href="<%= WikiViewUtils.decipherURL(wlh2, "view") %>"><%= subpages[i] %></a><br>
<% } %>
</td>
<td valign="top">
<% for(int i = 0; i < prefs.length; i++) { %>
<%   wlh2.setWikiPage(prefs[i]); %>
<a href="<%= WikiViewUtils.decipherURL(wlh2, "view") %>"><%= prefs[i] %></a><br>
<% } %>
</td>
<td valign="top">
<% for(int i = 0; i < prefby.length; i++) { %>
<%   wlh2.setWikiPage(prefby[i]); %>
<a href="<%= WikiViewUtils.decipherURL(wlh2, "view") %>"><%= prefby[i] %></a><br>
<% } %>
</td>
</tr>
</table>

<%@ include file="pageindex.pagedrafts.jspf" %>

<% } %>
</div>

<% tabidx++; %><div id="oxy-tabpane-pageinfo-div-<%= tabidx %>" style="display:none" >
<% if(showextrainfo) { %>
<h3><%= wi18n.str("jspviews.pageinfo.attachments_header", pagename) %></h3>
    <% wlh.setAttribute("hide.attachments", "false"); %>
    <% wlh.setAttribute("hide.attachments.addform", "true"); %>    
    <% wlh.setAttribute("include.deleted.attachments", "true"); %>  
    <% out.flush(); %><% WikiViewUtils.includeView("attachments"); %>

<% } %>
</div>

</div>

