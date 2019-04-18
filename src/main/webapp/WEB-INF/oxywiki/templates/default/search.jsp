<%@ include file="_topinclude.jspf" %><%
  WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
  WikiLinkHolder wlh2 = wlh.getClone();
  WikiEngine we = WikiLocal.getWikiEngine();
  WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
  WikiProvidedObject wikipage = null;

  Map model = (Map)wlh.getAttribute("wiki.model.search");
  Map reqParams = (Map)model.get("request_parameter_map");
  //System.out.println("reqParams: " + reqParams);
  String[] cnames  = (String[])model.get("categories");
  OxygenSearchResults searchresults = (OxygenSearchResults)model.get("searchresults");
  OxygenSearchResults.Entry[] searchresultentries = searchresults.getResults();
  DecimalFormat dfmt = (DecimalFormat)model.get("decimalformat");
  DateFormat datefmt = (DateFormat)model.get("dateformat");
  String[] existingtags = (String[])model.get("existing_tags");
  String[] allcnames = (String[])model.get("all_categories");
  
  String datefmtPattern = (String)model.get("dateformatpattern");
  String datefmtPatternExample = (String)model.get("dateformatpatternexample");
  int randint = WikiViewUtils.nextRandomInt(99);

  List cnamesList = Arrays.asList(cnames);
  int selBoxSize = Math.min(6, allcnames.length);
%>
<h3><%= wi18n.str("jspviews.search.header") %></h3>

<table border="1">
<tr>
<th colspan="7"><%= wi18n.str("jspviews.search.search_results_header") %></th>
</tr>
<tr>
<th><%= wi18n.str("jspviews.search.category") %></th>
<th><%= wi18n.str("jspviews.search.page") %></th>
<th><%= wi18n.str("jspviews.search.score") %></th>
<th colspan="4"><%= wi18n.str("jspviews.search.last_change") %></th>
</tr>
<tr>
<th colspan="3">&nbsp;</th>
<th><%= wi18n.str("jspviews.search.last_change.version") %></th>
<th><%= wi18n.str("jspviews.search.last_change.date") %></th>
<th><%= wi18n.str("jspviews.search.last_change.author") %></th>
<th><%= wi18n.str("jspviews.search.last_change.comments") %></th>
</tr>

<% for(int i = 0; i < searchresultentries.length; i++) { 
     wlh2.setCategory(searchresultentries[i].getCategory());
     wlh2.setWikiPage(searchresultentries[i].getPage());
     wikipage = we.retrieveWikiCategoryEngine(searchresultentries[i].getCategory()).getIndexingManager().getWikiPageFromIndex(searchresultentries[i].getPage());
     String author1 = StringUtils.nonNullString(wikipage.getAttribute(WikiConstants.ATTRIBUTE_AUTHOR), "-");
%>
<tr>
<td>&nbsp;<a href="<%= WikiUtils.getCategoryURL(searchresultentries[i].getCategory(), WikiConstants.ACTION_VIEW, null) %>"><%= searchresultentries[i].getCategory() %></a>&nbsp;</td>
<td><a href="<%= WikiViewUtils.decipherURL(wlh2, "view") %>"><%= searchresultentries[i].getPage() %></a></td>
<td><%= dfmt.format(searchresultentries[i].getScore()) %></td>
<td><%= wikipage.getVersion() %></td>
<td><a href="<%= WikiViewUtils.decipherURL(wlh2, "diff") %>"><%= datefmt.format(wikipage.getDate()) %></a></td>
<td><a href="<%= WikiUtils.getUserLink(author1) %>"><%= author1 %></a></td>
<td><%= StringUtils.nonNullString(wikipage.getAttribute(WikiConstants.ATTRIBUTE_COMMENTS), "&nbsp;") %></td>
</tr>
<% } %>
</table>

<hr />

<script LANGUAGE="JavaScript">
<!--
  function oxywiki_searchform_selectall_clicked() {
    var aCheckedValue = document.oxywiki_searchform.form_selectall.checked;
    for(var i = 0; i < document.oxywiki_searchform.cat.length; i++) {
      document.oxywiki_searchform.cat[i].selected = aCheckedValue;
    }
  }
  function oxywiki_form_show_simple_search() {
    document.oxywiki_searchform.<%= WikiConstants.SEARCH_INDEX_SIMPLE_SEARCH_KEY %>.value = document.oxywiki_searchform.t_t.value;
    document.getElementById('oxywiki_simple_search').style.display = "block";
    document.getElementById('oxywiki_advanced_search').style.display = "none";
  }
  function oxywiki_form_show_advanced_search() {
    document.oxywiki_searchform.t_t.value = document.oxywiki_searchform.<%= WikiConstants.SEARCH_INDEX_SIMPLE_SEARCH_KEY %>.value;
    document.oxywiki_searchform.<%= WikiConstants.SEARCH_INDEX_SIMPLE_SEARCH_KEY %>.value = "";
    document.getElementById('oxywiki_simple_search').style.display = "none";
    document.getElementById('oxywiki_advanced_search').style.display = "block";
  }  
  function oxywiki_search_find() {
    document.oxywiki_searchform.rss.value = 'false';
    document.oxywiki_searchform.submit();
  }
  function oxywiki_search_rss() {
    document.oxywiki_searchform.rss.value = 'true';
    document.oxywiki_searchform.submit();
  }
  
// -->
</script>

<form name="oxywiki_searchform" action="<%= WikiViewUtils.decipherURL(wlh, "search") %>" method="get" >
<input type="hidden" name="<%= WikiConstants.SUBMIT_REQUEST_PARAMETER %>" value="true" />
<input type="hidden" name="t_t" value="" />
<a name="<%= randint %>" />
<a href="#<%= randint %>" onClick="javascript:oxywiki_form_show_simple_search()"><%= wi18n.str("jspviews.search.simple_search") %></a> | 
<a href="#<%= randint %>" onClick="javascript:oxywiki_form_show_advanced_search()"><%= wi18n.str("jspviews.search.advanced_search") %></a>
<br/>

<!-- ####################################### -->
<div id="oxywiki_simple_search" style="display: block;">
<input type="text" name="<%= WikiConstants.SEARCH_INDEX_SIMPLE_SEARCH_KEY %>" value="<%= WikiViewUtils.oneReqParam(reqParams, WikiConstants.SEARCH_INDEX_SIMPLE_SEARCH_KEY) %>" size="80" maxlength="80" style="width:80%" />
</div>

<!-- ####################################### -->

<div id="oxywiki_advanced_search" style="display: none;">
<table width="80%" border="0">
<tr>
<th colspan="2">
<%= wi18n.str("jspviews.search.search_form_header") %>
</th>
</tr>
<tr>
<td>
<%= wi18n.str("jspviews.search.pagename") %>
</td>
<td>
<input type="text" name="<%= WikiConstants.SEARCH_INDEX_PAGENAME %>" value="<%= WikiViewUtils.oneReqParam(reqParams, WikiConstants.SEARCH_INDEX_PAGENAME) %>" size="80" maxlength="80" style="width:80%" />
</td>
</tr>

<tr>
<td>
<%= wi18n.str("jspviews.search.contents") %>
</td>
<td>
<input type="text" name="<%= WikiConstants.SEARCH_INDEX_CONTENTS %>" value="<%= WikiViewUtils.oneReqParam(reqParams, WikiConstants.SEARCH_INDEX_CONTENTS) %>" size="80" maxlength="80" style="width:80%" />
</td>
</tr>

<tr>
<td>
<%= wi18n.str("jspviews.search.tags") %>
</td>
<td>
<input type="text" name="<%= WikiConstants.SEARCH_INDEX_TAGS %>" value="<%= WikiViewUtils.oneReqParam(reqParams, WikiConstants.SEARCH_INDEX_TAGS) %>" size="80" maxlength="80" style="width:80%" />
<br/>
<i><%= wi18n.str("jspviews.search.existing_tags") %>: <%= Arrays.asList(existingtags) %></i>
</td>
</tr>

<tr>
<td>
<%= wi18n.str("jspviews.search.author") %>
</td>
<td>
<input type="text" name="<%= WikiConstants.SEARCH_INDEX_AUTHOR %>" value="<%= WikiViewUtils.oneReqParam(reqParams, WikiConstants.SEARCH_INDEX_AUTHOR) %>" size="80" maxlength="80" style="width:80%" />
</td>
</tr>

<tr>
<td>
<%= wi18n.str("jspviews.search.attachment_name") %>
</td>
<td>
<input type="text" name="<%= WikiConstants.SEARCH_INDEX_ATTACHMENT_NAME %>" value="<%= WikiViewUtils.oneReqParam(reqParams, WikiConstants.SEARCH_INDEX_ATTACHMENT_NAME) %>" size="80" maxlength="80" style="width:80%" />
</td>
</tr>

<tr>
<td>
<%= wi18n.str("jspviews.search.comments") %>
</td>
<td>
<input type="text" name="<%= WikiConstants.SEARCH_INDEX_COMMENTS %>" value="<%= WikiViewUtils.oneReqParam(reqParams, WikiConstants.SEARCH_INDEX_COMMENTS) %>" size="80" maxlength="80" style="width:80%" />
</td>
</tr>

<tr>
<td>
<%= wi18n.str("jspviews.search.modified") %>
</td>
<td>
<%= wi18n.str("jspviews.search.modified_last_X_days") %>
<input type="text" name="<%= WikiConstants.SEARCH_INDEX_LAST_MODIFIED %>" value="<%= WikiViewUtils.oneReqParam(reqParams, WikiConstants.SEARCH_INDEX_LAST_MODIFIED) %>" size="4" />
<br/><b><i><%= wi18n.str("general.or") %></i></b><br/>
<%= wi18n.str("jspviews.search.modified_between_X_and_Y", new String[]{datefmtPattern, datefmtPatternExample}) %>
<input type="text" name="<%= WikiConstants.SEARCH_INDEX_LAST_MODIFIED %>.0" value="<%= WikiViewUtils.oneReqParam(reqParams, WikiConstants.SEARCH_INDEX_LAST_MODIFIED + ".0") %>" size="20" />
<input type="text" name="<%= WikiConstants.SEARCH_INDEX_LAST_MODIFIED %>.1" value="<%= WikiViewUtils.oneReqParam(reqParams, WikiConstants.SEARCH_INDEX_LAST_MODIFIED + ".1") %>" size="20" />
</td>
</tr>

</table>
</div>
<!-- ####################################### -->

<%= wi18n.str("jspviews.search.categories") %> <input type="checkbox" name="form_selectall" onClick="javascript:oxywiki_searchform_selectall_clicked()">:
<select name="cat" size="<%= selBoxSize %>" multiple >
<% for(int i = 0; i < allcnames.length; i++) { %>
<option value="<%= allcnames[i] %>" 
<% if(cnamesList.contains(allcnames[i])) { %>selected<% } %> ><%= allcnames[i] %>
</option>
<%-- <% if(((i + 1) % 4) == 0) { %><br /><% } %> --%>
<% } %>
</select>
<br/>
<INPUT type="button" name="ok" value="<%= wi18n.str("jspviews.search.find") %>" onclick="oxywiki_search_find();" >
<br/>
<b><i><%= wi18n.str("general.or") %></i></b>
<br/>
<select name="rss.format" >
<option value="rss_1.0" ><%= wi18n.str("templates.default.rss_10") %></option>
<option value="rss_2.0" selected><%= wi18n.str("templates.default.rss_20") %></option>
<option value="atom_1.0" ><%= wi18n.str("templates.default.atom_10") %></option>
</select>
<input type="checkbox" name="rss_includelastchange" value="true" checked><%= wi18n.str("jspviews.search.rss_include_last_change") %>
<INPUT type="button" name="ok" value="<%= wi18n.str("jspviews.search.rss") %>" onclick="oxywiki_search_rss();" >
<input type="hidden" name="rss" value="">
</form>

<hr />
<a href="<%= WikiUtils.getCategoryURL("help", "view", "Search") %>"><%= wi18n.str("jspviews.search.help_page_link") %></a> <br />
<%= wi18n.str("jspviews.search.inline_help") %>

