<%@ include file="_topinclude.jspf" %><%
  WebInteractionContext webctx = WebLocal.getWebInteractionContext();
  WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
  WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
  String wcename = wce.getName();
  WikiEngine we = WikiLocal.getWikiEngine();
  
  WikiProvidedObject wp = WikiUtils.getWikiPage();
  WikiLinkHolder wlh2 = wlh.getClone();
  String wlhpagename = wlh2.getWikiPage();
  String pagerep = wlhpagename;
  if(wp != null) {
    pagerep = wp.getName();
  } 
  String[] allcnames = we.getRegisteredAndLoadedWikiCategoryNames();
  Arrays.sort(allcnames);
  
  String misclink = wlh.getWikiURL();

  int selBoxSize = Math.min(6, allcnames.length);
  //wlh2.setAction("rss");
  //String rsslink = wlh2.getWikiURL();
  //wlh2.setAction("recentchanges");
  //String recentchangeslink = wlh2.getWikiURL();
  wlh2.setAction("copypage");
  String copylink = wlh2.getWikiURL();
  wlh2.setAction("search");
  String searchlink = wlh2.getWikiURL();
  String randomStr = "UGORJIISOKSOWHATISYOURPROBLEMWITHHIMTODAYIDONTLIKEYOUGOODBYEWHATEVER";
  String randomStr2 = "AKILAHISOKSOWHATISYOURPROBLEMWITHHIMTODAYIDONTLIKEYOUGOODBYEWHATEVER";
  
  wlh2.setWikiPage(randomStr);
  wlh2.setAction("view");
  String viewlink = wlh2.getWikiURL();
  wlh2.setAction("edit");
  String editlink = wlh2.getWikiURL();
  wlh2.setAction("pageinfo");
  String pageinfolink = wlh2.getWikiURL();
  wlh2.setExtrainfo(randomStr2);
  wlh2.setAction("viewattachment");
  String viewattachmentlink = wlh2.getWikiURL();
  wlh2.setAction("attachmentinfo");
  String attachmentinfolink = wlh2.getWikiURL();

  String pagetemplateparentpage = wce.getProperty(WikiConstants.PAGETEMPLATE_PARENTPAGE);
  String[] pagetemplates = ((pagetemplateparentpage == null) ? (new String[0])
                            : (wce.getIndexingManager().getAllReferersMatching(pagetemplateparentpage + "/" + ".+")));

  String leftmenu = WikiViewUtils.getDecorationEditLink(wce.getProperty(WikiConstants.PAGE_DECORATION_PREFIX + "left"));
  String rightmenu = WikiViewUtils.getDecorationEditLink(wce.getProperty(WikiConstants.PAGE_DECORATION_PREFIX + "right"));
  String topmenu = WikiViewUtils.getDecorationEditLink(wce.getProperty(WikiConstants.PAGE_DECORATION_PREFIX + "top"));
  String bottommenu = WikiViewUtils.getDecorationEditLink(wce.getProperty(WikiConstants.PAGE_DECORATION_PREFIX + "bottom"));

  String attCommButtonsAnchor = String.valueOf(WikiViewUtils.nextRandomInt(99));
  int idx = 0;
%>
<script LANGUAGE="JavaScript">
<!--
  function oxywiki_misc_ror_form_selectall_clicked() {
    var aCheckedValue = document.oxywiki_rss_or_recentchanges_form.f.checked;
    for(var i = 0; i < document.oxywiki_rss_or_recentchanges_form.cat.length; i++) {
      document.oxywiki_rss_or_recentchanges_form.cat[i].selected = aCheckedValue;
    }
  }
  function oxywiki_misc_ror_form_rss() {
    document.oxywiki_rss_or_recentchanges_form.action = '<%= searchlink %>';
    document.oxywiki_rss_or_recentchanges_form.rss.value = 'true';
    document.oxywiki_rss_or_recentchanges_form.submit();
  }
  function oxywiki_misc_ror_form_recentchanges() {
    document.oxywiki_rss_or_recentchanges_form.action = '<%= searchlink %>';
    document.oxywiki_rss_or_recentchanges_form.rss.value = 'false';
    document.oxywiki_rss_or_recentchanges_form.submit();
  }
  function oxywiki_misc_info() {
    oxywiki_misc_view_or_edit_or_info('<%= pageinfolink %>', '<%= attachmentinfolink %>');
  }
  function oxywiki_misc_view() {
    oxywiki_misc_view_or_edit_or_info('<%= viewlink %>', '<%= viewattachmentlink %>');
  }
  function oxywiki_misc_edit() {
    oxywiki_misc_view_or_edit_or_info('<%= editlink %>', '');
  }
  function oxywiki_misc_view_or_edit_or_info(aapagelink, aaattachlink) {
    var newlink = '';
    if(aaattachlink != '' && document.oxywiki_misc_free_for_all_form.page.value.length > 0 && document.oxywiki_misc_free_for_all_form.attachment.value.length > 0) {
      newlink = aaattachlink;
      newlink = newlink.replace(/<%= randomStr %>/g, document.oxywiki_misc_free_for_all_form.page.value);
      newlink = newlink.replace(/<%= randomStr2 %>/g, document.oxywiki_misc_free_for_all_form.attachment.value);
    } else if(document.oxywiki_misc_free_for_all_form.page.value.length > 0) {
      newlink = aapagelink;
      newlink = newlink.replace(/<%= randomStr %>/g, document.oxywiki_misc_free_for_all_form.page.value);
    } else {
      alert("<%= wi18n.str("jspviews.misc.no_page_entered") %>");
      return false;
    }
    document.oxywiki_misc_view_or_edit_form.action = newlink;
    document.oxywiki_misc_view_or_edit_form.submit();
  }
  function oxywiki_misc_template_edit() {
    var newpagename = document.oxywiki_misc_free_for_all_form.template_name.value;
    if(newpagename.length <= 0 && document.oxywiki_misc_free_for_all_form.existing_template) {
      newpagename = document.oxywiki_misc_free_for_all_form.existing_template.value;
    } else {
      newpagename = '<%= pagetemplateparentpage %>' + '/' + newpagename;
    }
    if(newpagename.length <= 0) {
      alert("<%= wi18n.str("jspviews.misc.no_page_entered") %>");
      return false;
    }
    var newlink = '<%= editlink %>';
    newlink = newlink.replace(/<%= randomStr %>/g, newpagename);
    document.oxywiki_misc_view_or_edit_form.action = newlink;
    document.oxywiki_misc_view_or_edit_form.submit();
  }
  
// -->
</script>

<h2><%= wi18n.str("jspviews.misc.header") %></h2>

<form name="oxywiki_misc_view_or_edit_form" action="#" method="get">
</form>

<table class="oxy-tabpane-div-tabs">
<tr>
<% idx++; %><td id="oxy-tabpane-misc-tab-<%= idx %>" class="oxy-tabpane-tab-active"><a href="#<%= attCommButtonsAnchor %>"   onClick="javascript:oxy_select_tab(<%= idx %>, '-misc');oxy_set_cookie('oxywiki-misc-tab', <%= idx %>, null);"><%= wi18n.str("templates.default.rss_or_recent_changes") %></a></td>
<% idx++; %><td id="oxy-tabpane-misc-tab-<%= idx %>" class="oxy-tabpane-tab-inactive"><a href="#<%= attCommButtonsAnchor %>"   onClick="javascript:oxy_select_tab(<%= idx %>, '-misc');oxy_set_cookie('oxywiki-misc-tab', <%= idx %>, null);"><%= wi18n.str("templates.default.goto_copy_page") %></a></td>
<% idx++; %><td id="oxy-tabpane-misc-tab-<%= idx %>" class="oxy-tabpane-tab-inactive"><a href="#<%= attCommButtonsAnchor %>"   onClick="javascript:oxy_select_tab(<%= idx %>, '-misc');oxy_set_cookie('oxywiki-misc-tab', <%= idx %>, null);"><%= wi18n.str("templates.default.goto_page") %></a></td>
<% idx++; %><td id="oxy-tabpane-misc-tab-<%= idx %>" class="oxy-tabpane-tab-inactive"><a href="#<%= attCommButtonsAnchor %>"   onClick="javascript:oxy_select_tab(<%= idx %>, '-misc');oxy_set_cookie('oxywiki-misc-tab', <%= idx %>, null);"><%= wi18n.str("templates.default.goto_page_template") %></a></td>
<% idx++; %><td id="oxy-tabpane-misc-tab-<%= idx %>" class="oxy-tabpane-tab-inactive"><a href="#<%= attCommButtonsAnchor %>"   onClick="javascript:oxy_select_tab(<%= idx %>, '-misc');oxy_set_cookie('oxywiki-misc-tab', <%= idx %>, null);"><%= wi18n.str("templates.default.goto_page_decorator") %></a></td>
<% idx++; %><td id="oxy-tabpane-misc-tab-<%= idx %>" class="oxy-tabpane-tab-inactive"><a href="#<%= attCommButtonsAnchor %>"   onClick="javascript:oxy_select_tab(<%= idx %>, '-misc');oxy_set_cookie('oxywiki-misc-tab', <%= idx %>, null);"><%= wi18n.str("templates.default.goto_user") %></a></td>
</tr>
</table>

<div class="oxy-tabpane-div">

<% idx = 0; %>

<% idx++; %><div id="oxy-tabpane-misc-div-<%= idx %>" style="display:block" >
<h4><%= wi18n.str("jspviews.misc.rss_or_recent_changes_header") %></h4>

<form name="oxywiki_rss_or_recentchanges_form" action="#" method="get">
<input type="checkbox" name="f" onClick="javascript:oxywiki_misc_ror_form_selectall_clicked()">:
<select name="cat" size="<%= selBoxSize %>" multiple >
<% for(int i = 0; i < allcnames.length; i++) { %>
<option value="<%= allcnames[i] %>" <% if(wcename.equals(allcnames[i])) { %>selected<% } %> ><%= allcnames[i] %></option>
<% } %>
</select>
<%-- <b>||</b> --%>
<br/>
<%= wi18n.str("jspviews.misc.rss_format") %>
<select name="rss.format" >
<option value="rss_1.0" ><%= wi18n.str("templates.default.rss_10") %></option>
<option value="rss_2.0" selected><%= wi18n.str("templates.default.rss_20") %></option>
<option value="atom_1.0" ><%= wi18n.str("templates.default.atom_10") %></option>
</select>
<br/>
<input type="checkbox" name="rss_includelastchange" value="true" checked><%= wi18n.str("jspviews.search.rss_include_last_change") %>
<br/>
<%-- COMMENT THIS OLD ONE FOR NOW (Ugorji) 
<nobr><%= wi18n.str("jspviews.misc.change_period") %></nobr><input type="text" name="changeperiod" value="1" size="80" style="width:80%" />
--%>
<nobr><%= wi18n.str("jspviews.misc.change_period") %></nobr><input type="text" name="<%= WikiConstants.SEARCH_INDEX_LAST_MODIFIED %>" value="1" size="80" style="width:80%" />
<br/>
<INPUT type="button" name="ok" value="<%= wi18n.str("jspviews.misc.rss") %>" onClick="oxywiki_misc_ror_form_rss();">
<INPUT type="button" name="ok" value="<%= wi18n.str("jspviews.misc.recent_changes") %>" onClick="oxywiki_misc_ror_form_recentchanges();">

<input type="hidden" name="rss" value="">
<input type="hidden" name="wiki.submit" value="true">
</form>

<p/>
</div>

<% idx++; %><div id="oxy-tabpane-misc-div-<%= idx %>" style="display:none" >
<h4><%= wi18n.str("jspviews.misc.copy_header") %></h4>
<form name="oxywiki_misc_copy_form" action="<%= copylink %>" method="post">
<%= wi18n.str("jspviews.misc.copy_message") %>
<table border="1" width="100%">
<tr>
<td width="15"><%= wi18n.str("jspviews.edit.comments_field") %></td>
<td><input type="text" name="attribute.comments" value="" style="width:80%" /></td>
</tr>
<tr>
<td><%= wi18n.str("jspviews.misc.copy_source") %></td>
<td><input type="text" name="source" value="" style="width:80%" /></td>
</tr>
<tr>
<td><%= wi18n.str("jspviews.misc.copy_destination") %></td>
<td><input type="text" name="dest" value="" style="width:80%" /></td>
</tr>
<tr>
<td>&nbsp;</td>
<td><input type="checkbox" name="delete" value="true" /><%= wi18n.str("jspviews.misc.copy_delete_source") %></td>
</tr>
<tr>
<td>&nbsp;</td>
<td>
<% if(WikiUtils.isCaptchaEnabled()) { %>
  <%= wi18n.str("jspviews.captcha") %><br/>
  <img src="<%= WikiViewUtils.decipherURL(wlh, "captcha") %>" />
  <input type="text" name="j_captcha_response" value="" size="80" style="width:100%" /><br/>
  <%= wi18n.str("jspviews.captcha_message") %><br/>
<% } %>
<INPUT type="button" name="ok" value="<%= wi18n.str("jspviews.misc.copy_page") %>" onClick="document.oxywiki_misc_copy_form.submit();">
</td>
</tr>
</table>
</form>
</div>

<form name="oxywiki_misc_free_for_all_form" action="#">

<% idx++; %><div id="oxy-tabpane-misc-div-<%= idx %>" style="display:none" >

<h4><%= wi18n.str("jspviews.misc.page_or_attachment_header") %></h4>
<%= wi18n.str("jspviews.misc.page_or_attachment_message") %>
<p/>
<%= wi18n.str("jspviews.misc.page") %>: <input type="text" name="<%= WikiConstants.REQUEST_PARAM_PAGE_KEY %>" value="<%= pagerep %>" size="80" style="width:80%" />
<br/>
<%= wi18n.str("jspviews.misc.attachment") %>: <input type="text" name="attachment" value="" size="80" style="width:80%" />
<br/>
<INPUT type="button" name="ok" value="<%= wi18n.str("jspviews.misc.info") %>" onClick="oxywiki_misc_info();">
<INPUT type="button" name="ok" value="<%= wi18n.str("jspviews.misc.view") %>" onClick="oxywiki_misc_view();">
<INPUT type="button" name="ok" value="<%= wi18n.str("jspviews.misc.edit") %>" onClick="oxywiki_misc_edit();">
<br/>
</div>


<% idx++; %><div id="oxy-tabpane-misc-div-<%= idx %>" style="display:none" >
<h4><%= wi18n.str("jspviews.misc.edit_template") %></h4>
<%= wi18n.str("jspviews.misc.edit_template_msg") %><br/>
<% if(pagetemplates.length > 0) { %>
<select id="oxywiki-pagetemplate" name="existing_template">
<% for(int i = 0; i < pagetemplates.length; i++) { %>
<option value="<%= pagetemplates[i] %>" ><%= pagetemplates[i] %></option>
<% } %>
</select>
<b>||</b>
<% } %>
<%= pagetemplateparentpage %>/
<input type="text" name="template_name" value="" /><br/>
<INPUT type="button" name="ok" value="<%= wi18n.str("jspviews.misc.edit") %>" onClick="oxywiki_misc_template_edit();">
<br/>

</div>

</form>

<% idx++; %><div id="oxy-tabpane-misc-div-<%= idx %>" style="display:none" >
<h4><%= wi18n.str("jspviews.misc.decorated_pages_header") %></h4>
<%= wi18n.str("jspviews.misc.decorated_pages_description") %><br/>
<a href="<%= leftmenu %>"><%= wi18n.str("jspviews.misc.decoration.left") %></a> 
| 
<a href="<%= rightmenu %>"><%= wi18n.str("jspviews.misc.decoration.right") %></a> 
| 
<a href="<%= topmenu %>"><%= wi18n.str("jspviews.misc.decoration.top") %></a> 
| 
<a href="<%= bottommenu %>"><%= wi18n.str("jspviews.misc.decoration.bottom") %></a> 
</div>

<% idx++; %><div id="oxy-tabpane-misc-div-<%= idx %>" style="display:none" >
<h4><%= wi18n.str("jspviews.misc.user_header") %></h4>
<form name="oxywiki_misc_user_form" action="<%= misclink %>" method="get">
<%= wi18n.str("jspviews.misc.user_message") %>
<p/>
<%= wi18n.str("jspviews.misc.user") %>: <input type="text" name="u" value="<%= webctx.getUserName() %>" size="20" style="width:80%" />
<br/>
<INPUT type="button" name="ok" value="<%= wi18n.str("jspviews.misc.find_user") %>" onClick="document.oxywiki_misc_user_form.submit();">
<br/>
</form>
</div>


</div> <!-- end of tabpane -->

<script language="javascript">
<!--
  oxy_autoselect_tab(null, 'oxywiki-misc-tab', '-misc');
-->
</script>

