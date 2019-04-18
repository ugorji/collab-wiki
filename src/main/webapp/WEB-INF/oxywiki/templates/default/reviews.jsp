<%@ include file="_topinclude.jspf" %><%
  WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
  Map model = (Map)wlh.getAttribute("wiki.model.editable.review");
  WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
  WikiProvidedObject wp = (WikiProvidedObject)model.get("wikipage");
  WikiProvidedObject[] reviews = (WikiProvidedObject[])model.get("reviews");
  
  String attCommToggleAnchor = String.valueOf(WikiViewUtils.nextRandomInt(99));
  String attCommButtonsAnchor = String.valueOf(WikiViewUtils.nextRandomInt(99));

  Exception editerror = (Exception)model.get("exception");
  String text = (String)model.get(WikiConstants.PARAMETER_TEXT);

  WikiPageReviewProvider wprp = wce.getPageReviewProvider();
  
  boolean hideAttachFileForm = "true".equals(wlh.getAttribute("hide.reviews.addform"));
  boolean hideAttach = "true".equals(wlh.getAttribute("hide.reviews"));
  String _markupToHTMLLink = WikiViewUtils.decipherURL(wlh, "markuptohtml");
%>
<script LANGUAGE="JavaScript">
<!--
function oxywikiReviewPreviewHTML()
{
  var oxyurl = '<%= _markupToHTMLLink %>';
  var oxypars = 'text=' + encodeURIComponent($('oxywiki-text').value);	
  var oxyAjax = new Ajax.Updater('oxy-tabpane-review-div-2', oxyurl, {method: 'post', postBody: oxypars});
}
// -->
</script>


<a name="<%= attCommToggleAnchor %>" />
<% if(wce.isActionSupported("reviewpost")) { %>
<div id="oxywiki_add_review" style="display:<%= (hideAttachFileForm ? "none" : "block") %>">

<% if(editerror != null) { %>
<font color="red"><%= wi18n.str("jspviews.reviews.error_msg") %></font>
<% } %>

<h2><%= wi18n.str("jspviews.reviews.header") %></h2>

<form name="viewpageform" action="<%= WikiViewUtils.decipherURL(wlh, "reviewpost") %>" method="post" >

<table class="oxy-tabpane-div-tabs">
<tr >
<td id="oxy-tabpane-review-tab-1" class="oxy-tabpane-tab-active"><a href="#<%= attCommButtonsAnchor %>"   onClick="javascript:oxy_select_tab(1, '-review');"><%= wi18n.str("jspviews.edit.edit_ui") %></a></td>
<td id="oxy-tabpane-review-tab-2" class="oxy-tabpane-tab-inactive"><a href="#<%= attCommButtonsAnchor %>" onClick="javascript:oxywikiReviewPreviewHTML();oxy_select_tab(2, '-review');"><%= wi18n.str("jspviews.edit.preview") %></a></td>
<td id="oxy-tabpane-review-tab-3" class="oxy-tabpane-tab-inactive"><a href="#<%= attCommButtonsAnchor %>" onClick="javascript:oxy_select_tab(3, '-review');"><%= wi18n.str("jspviews.edit.help") %></a></td>
</tr>
</table>

<div class="oxy-tabpane-div">

<div id="oxy-tabpane-review-div-1" style="display:block" >
<input type="hidden" name="postaction" value="true" />
<textarea id="oxywiki-text" wrap="virtual" name="<%= WikiConstants.PARAMETER_TEXT %>" rows="10" cols="80" style="width:100%;" 
><% out.flush(); %><%= text  %></textarea>
<input type="checkbox" name="attribute.<%= WikiEvent.MINOR_EDIT_FLAG_KEY %>" value="true" /><%= wi18n.str("jspviews.reviews.minor_edit") %>
<br />

<% if(WikiUtils.isCaptchaEnabled()) { %>
  <img src="<%= WikiViewUtils.decipherURL(wlh, "captcha") %>" />
  <input type="text" name="j_captcha_response" value="" size="80" style="width:100%" /><br/>
  <%= wi18n.str("jspviews.captcha_message") %><br/>
<% } %>

</div>

<div id="oxy-tabpane-review-div-2" style="display:none; background:fixed url(<%= wikiwebctx.getContextPath() %>/images/previewbg.gif);">
&nbsp; <br/>
&nbsp; <br/>
&nbsp; <br/>
</div>

<div id="oxy-tabpane-review-div-3" style="display:none" >
<pre>
<i><a href="<%= WikiUtils.getCategoryURL("help", "view", "Edit") %>">
<%= wi18n.str("jspviews.edit.detailed_help_title") %></a></i>
<%= wi18n.str("jspviews.edit.edit_help") %>
</pre>
</div>

<a name="<%= attCommButtonsAnchor %>" />
<INPUT type="submit" name="postaction_save" value="<%= wi18n.str("jspviews.reviews.add_review") %>" />
&nbsp;&nbsp;
<input type="button" value="<%= wi18n.str("jspviews.edit.edit_ui") %>" onClick="oxy_select_tab(1, '-review');"/>
&nbsp;&nbsp;
<INPUT type="button" name="postaction_preview" value="<%= wi18n.str("jspviews.reviews.preview") %>" onClick="oxywikiReviewPreviewHTML();oxy_select_tab(2, '-review');"/>
&nbsp;&nbsp;
<input type="button" value="<%= wi18n.str("jspviews.edit.help") %>" onClick="oxy_select_tab(3, '-review');"/>
&nbsp;&nbsp;
<INPUT type="submit" name="postaction_cancel" value="<%= wi18n.str("jspviews.reviews.cancel") %>" />

</form>

<% if(editerror != null) { %>
<pre><%= StringUtils.toHTMLEscape(StringUtils.toString(editerror), false, false) %></pre>
<% } %>

</div><%-- close "oxy-tabpane-div" --%>

</div><%-- close "oxywiki_add_review" --%>

<% } %>

<div id="oxywiki_reviews" class="wikireview" style="display:<%= (hideAttach ? "none" : "block") %>">
<% if(reviews.length > 0) { %>
<h4><%= wi18n.str("jspviews.reviews.reviews") %></h4>
<% for(int i = 0; i < reviews.length; i++) { %>
<p>
<u><%= wi18n.str("jspviews.reviews.posted") %> 
<%   String author1 = StringUtils.nonNullString(WikiViewUtils.getAuthor(reviews[i]), "-"); %>
<%= wi18n.str("jspviews.reviews.by") %>: <i><a href="<%= WikiUtils.getUserLink(author1) %>"><%= author1 %></a></i> <%= wi18n.str("jspviews.reviews.at") %> <i><%= df.format(reviews[i].getDate()) %></i>
(<a href="<%= WikiViewUtils.decipherURL(wlh, "deletereview", new String[]{"reviewname", reviews[i].getName()}) %>"><i><%= wi18n.str("jspviews.reviews.delete") %></i></a>)
</u>
<br />
<% 
   out.flush(); 
   Reader r99 = wprp.getPageReviewReader(wp.getName(), reviews[i]); 
   wce.writeHTML(wp, r99, true); 
   try {r99.close();} finally{}; 
%>
<%-- <hr width="50%" align="left"> --%>
<% } %>

<% } %>

</div>
