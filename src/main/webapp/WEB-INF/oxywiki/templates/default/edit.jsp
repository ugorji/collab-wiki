<%@ include file="_topinclude.jspf" %><%
  WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
  Map model = (Map)wlh.getAttribute("wiki.model.editable.edit");
  WikiCategoryEngine wcengine = WikiLocal.getWikiCategoryEngine();
  WikiProvidedObject wp = (WikiProvidedObject)model.get("wikipage");
  String text = (String)model.get(WikiConstants.PARAMETER_TEXT);
  String subscribers = (String)model.get("subscribers");
  String comments = (String)model.get(WikiConstants.ATTRIBUTE_COMMENTS);
  String tags = (String)model.get(WikiConstants.ATTRIBUTE_TAGS);
  
  boolean constrainTags = ((Boolean)model.get("constrain_tags")).booleanValue();
  Collection allowedTags = (Collection)model.get("allowed_tags");
  
  boolean minoreditflag = ((Boolean)model.get("minoreditflag")).booleanValue();
  boolean pageDraftSupported = ((Boolean)model.get("page_draft_supported")).booleanValue();
  Exception editerror = (Exception)model.get("exception");
  String pagetemplate = (String)model.get("pagetemplate");
  String[] pagetemplates = (String[])model.get("pagetemplates");
  String[] existingtags = (String[])model.get("existing_tags");
  boolean showPageTemplateUIControls = (pagetemplates != null && pagetemplates.length > 0);
  boolean allowPublish = ((Boolean)model.get("allowpublish")).booleanValue();
  String _posteditlink = WikiViewUtils.decipherURL(wlh, "editpost");
  String _editdiffLink = WikiViewUtils.decipherURL(wlh, "editdiff");
  String _previewLink = WikiViewUtils.decipherURL(wlh, "editpreview");
  String _savedraftLink = WikiViewUtils.decipherURL(wlh, "savedraft");
  String _rawLink = WikiViewUtils.decipherURL(wlh, "raw");
  //System.out.println(" ... " + _posteditlink + " ... " + _previewLink + " ... " + _rawLink);
  
  //do this, so that locks.jsp will be included well
  WikiEditLock[] elocks = wcengine.getWikiEditManager().getLocks(wp.getName());
  wlh.setAttribute("net.ugorji.oxygen.wiki.locks", elocks);
  long lockttl = wcengine.getWikiEditManager().getMaxTTL();
  
  String attCommButtonsAnchor = String.valueOf(WikiViewUtils.nextRandomInt(99));
%>
<%-- TBD: add a javascript onSubmit here, so it ensures the text field is empty --%>
<script LANGUAGE="JavaScript">
<!--
var oxywiki_timerID = 0;
var oxywiki_tEnd  = new Date();
oxywiki_tEnd.setTime(oxywiki_tEnd.getTime() + <%= lockttl %>);

function oxywiki_UpdateTimer() {
  var   tDate = new Date();
  var   tDiff = oxywiki_tEnd.getTime() - tDate.getTime();
  tDate.setTime(tDiff);
  document.oxywiki_timerform.timerfield.value = "" + tDate.getMinutes() + ":" + tDate.getSeconds();
  if(parseInt("" + (tDiff / 1000)) == 180) { // 180
    alert("<%= wi18n.str("jspviews.edit.alert_timeout_warning") %>");
    oxywiki_timerID = setTimeout("oxywiki_UpdateTimer()", 1000);
  } else if(parseInt("" + (tDiff / 1000)) <= 1) { // 1
    document.oxywiki_timerform.timerfield.value = "<%= wi18n.str("jspviews.edit.timeout") %>";
    //disabled prevents folks from copying the text, and readonly doesn't work ... bummer
    //document.oxywiki_timerform.text.disabled = true;
    document.oxywiki_editform.<%= WikiConstants.PARAMETER_TEXT %>.readonly = 'readonly';
    if(document.oxywiki_editform.postaction_usetemplate) {
      document.oxywiki_editform.postaction_usetemplate.disabled = true;
    }
    document.oxywiki_editform.postaction_save.disabled = true;
    document.oxywiki_editform.postaction_preview.disabled = true;
    alert("<%= wi18n.str("jspviews.edit.alert_timeout") %>");
    return;
  } 
  oxywiki_timerID = setTimeout("oxywiki_UpdateTimer()", 1000);
}

oxywiki_timerID = setTimeout("oxywiki_UpdateTimer()", 1000);

function oxywikiEditPreviewHTML() {
  oxywiki_tEnd.setTime(new Date().getTime() + <%= lockttl %>);
  var oxyurl = '<%= _previewLink %>';
  var oxypars = 'text=' + encodeURIComponent($('oxywiki-text').value);	
  var oxyAjax = new Ajax.Updater('oxy-tabpane-edit-div-2', oxyurl, {method: 'post', postBody: oxypars});
}

function oxywikiEditDiff() {
  oxywiki_tEnd.setTime(new Date().getTime() + <%= lockttl %>);
  var oxyurl = '<%= _editdiffLink %>';
  var oxypars = 'text=' + encodeURIComponent($('oxywiki-text').value);	
  var oxyAjax = new Ajax.Updater('oxy-tabpane-edit-div-3', oxyurl, {method: 'post', postBody: oxypars});
}

function oxywikiSaveDraft() {
  oxywiki_tEnd.setTime(new Date().getTime() + <%= lockttl %>);
  var oxyurl = '<%= _savedraftLink %>';
  var oxypars = 'text=' + encodeURIComponent($('oxywiki-text').value);	
  var oxyAjax = new Ajax.Updater('oxy-edit-status', oxyurl, {method: 'post', postBody: oxypars});
}
  
function oxywiki_submitpagetemplateform() {
  var b = true;
  if (document.oxywiki_editform.text.value.length > 0) {
    b = (confirm("<%= wi18n.str("jspviews.edit.overlay_template_prompt") %>"));
  } 
  if(b) {
    var oxyurl = '<%= _rawLink %>';
    var oxyselui = $('oxywiki-pagetemplate');
    var oxyseltempl = oxyselui.options[oxyselui.selectedIndex].value.split(':');
    var oxypars = 'page=' + oxyseltempl[1] + '&category=' + oxyseltempl[0];
    var oxyAjax = new Ajax.Request(oxyurl, {method: 'get', parameters: oxypars, onComplete: oxywikiInclTemplateContents} );
  }
}

function oxywikiInclTemplateContents(originalRequest) {
  $('oxywiki-text').value = originalRequest.responseText;
}

function oxywiki_EditSelectToAddTag() {
  var s = document.oxywiki_editform.elements["attribute.tags"].value;
  var s2 = " " + document.oxywiki_editform.select_to_add_tag.value + " ";
  if(s.indexOf(s2) == -1) {
    document.oxywiki_editform.elements["attribute.tags"].value = s + s2;
  }
}

// -->
</script>
<h3><%= wi18n.str("jspviews.edit.edit") %> <%= wp.getName() %></h3>
<jsp:include page="locks.jsp" />
<p />
<% if(editerror != null) { %>
<font color="red"><%= wi18n.str("jspviews.edit.error_message") %></font>
<% } %>

<form name="oxywiki_timerform">
<%= wi18n.str("jspviews.edit.timeout_desc") %>
<input type="text" disabled="true" name="timerfield" size="7" />
</form>
<p>

<form name="oxywiki_editform" action="<%= _posteditlink %>" method="POST" >
<blockquote id="oxy-edit-status">&nbsp;</blockquote>

<table class="oxy-tabpane-div-tabs">
<tr >
<td id="oxy-tabpane-edit-tab-1" class="oxy-tabpane-tab-active"><a href="#<%= attCommButtonsAnchor %>"   onClick="javascript:oxy_select_tab(1, '-edit');"><%= wi18n.str("jspviews.edit.edit_ui") %></a></td>
<td id="oxy-tabpane-edit-tab-2" class="oxy-tabpane-tab-inactive"><a href="#<%= attCommButtonsAnchor %>" onClick="javascript:oxywikiEditPreviewHTML();oxy_select_tab(2, '-edit');"><%= wi18n.str("jspviews.edit.preview") %></a></td>
<td id="oxy-tabpane-edit-tab-3" class="oxy-tabpane-tab-inactive"><a href="#<%= attCommButtonsAnchor %>" onClick="javascript:oxywikiEditDiff();oxy_select_tab(3, '-edit');"><%= wi18n.str("jspviews.edit.diff") %></a></td>
<td id="oxy-tabpane-edit-tab-4" class="oxy-tabpane-tab-inactive"><a href="#<%= attCommButtonsAnchor %>" onClick="javascript:oxy_select_tab(4, '-edit');"><%= wi18n.str("jspviews.edit.help") %></a></td>
</tr>
</table>

<div class="oxy-tabpane-div">

<div id="oxy-tabpane-edit-div-1" style="display:block" >
<textarea id="oxywiki-text" wrap="virtual" name="<%= WikiConstants.PARAMETER_TEXT %>" rows="20" cols="80" style="width:100%;" 
  ><% out.flush(); %><%= text  %></textarea>
<br />
<table border="1" width="100%">
<% if(showPageTemplateUIControls) { %>
<tr>
<td width="15">Page Template: </td>
<td>
<select id="oxywiki-pagetemplate" name="pagetemplate">
<% for(int i = 0; i < pagetemplates.length; i++) { %>
<option value="<%= pagetemplates[i] %>" <%-- if(pagetemplates[i].equals(pagetemplate)) { %>selected<% } --%> ><%= pagetemplates[i] %></option>
<% } %>
</select>
&nbsp;&nbsp;&nbsp;
<input type="button" name="postaction_usetemplate" value="<%= wi18n.str("jspviews.edit.use_sel_template") %>" onClick="oxywiki_submitpagetemplateform();" />
</td>
</tr>
<% } %>
<tr>
<td width="15"><nobr><%= wi18n.str("jspviews.edit.flags") %></nobr></td>
<td>
<input type="checkbox" name="attribute.<%= WikiEvent.MINOR_EDIT_FLAG_KEY %>" value="true" <% if(minoreditflag) { %>CHECKED<% } %> />
<%= wi18n.str("jspviews.edit.minor_edit_field") %>
<% if(allowPublish) { %>
&nbsp;&nbsp;
<input type="checkbox" name="attribute.published" value="true" CHECKED />
<%= wi18n.str("jspviews.edit.published_field") %>
<% } %>
</td>
</tr>
<tr>
<td width="15"><%= wi18n.str("jspviews.edit.comments_field") %></td>
<td><input type="text" name="attribute.comments" value="<%= comments %>" size="80" style="width:100%" /></td>
</tr>
<tr>
<td width="15"><%= wi18n.str("jspviews.edit.tags_field") %></td>
<td><input type="text" name="attribute.tags" value="<%= tags %>" size="80" style="width:100%" <% if(constrainTags) { %>readOnly="true"<% } %> />
<br/>
<i><%= wi18n.str("jspviews.edit.existing_tags") %>: <%= Arrays.asList(existingtags) %></i>
<% if(allowedTags.size() > 0) { %>
<br/>
<%= wi18n.str("jspviews.edit.select_to_add_tag") %>: 
<select name="select_to_add_tag" onchange="oxywiki_EditSelectToAddTag()">
  <option value="">----------</option>
<% for(Iterator itr = allowedTags.iterator(); itr.hasNext(); ) { 
     String currval = (String)itr.next(); %>
  <option value="<%= currval %>"><%= currval %></option>
<% } %>
</select>
<% } %>
</td>
</tr>
<tr>
<td width="15"><%= wi18n.str("jspviews.edit.subscribers_field") %></td>
<td><input type="text" name="attribute.subscribers" value="<%= subscribers %>" size="80" style="width:100%" /></td>
</tr>

<% if(WikiUtils.isCaptchaEnabled()) { %>
<tr>
<td width="15"><%= wi18n.str("jspviews.captcha") %></td>
<td>
  <img src="<%= WikiViewUtils.decipherURL(wlh, "captcha") %>" />
  <input type="text" name="j_captcha_response" value="" size="80" style="width:100%" /><br/>
  <%= wi18n.str("jspviews.captcha_message") %>
</td>
</tr>
<% } %>

</table>

</div>

<div id="oxy-tabpane-edit-div-2" style="display:none; background:fixed url(<%= wikiwebctx.getContextPath() %>/images/previewbg.gif);">
&nbsp; <br/>
&nbsp; <br/>
&nbsp; <br/>
</div>

<div id="oxy-tabpane-edit-div-3" style="display:none; background:fixed url(<%= wikiwebctx.getContextPath() %>/images/previewbg.gif);">
&nbsp; <br/>
&nbsp; <br/>
&nbsp; <br/>
</div>

<div id="oxy-tabpane-edit-div-4" style="display:none" >
<pre>
<i><a href="<%= WikiUtils.getCategoryURL("help", "view", "Edit") %>">
<%= wi18n.str("jspviews.edit.detailed_help_title") %></a></i>
<%= wi18n.str("jspviews.edit.edit_help") %>
</pre>
</div>

</div><%-- end oxy-tabpane-div --%>

<a name="<%= attCommButtonsAnchor %>" />
<table border="1" width="100%">
<tr>
<td width="15">...: </td>
<td>
<input type="hidden" name="postaction" value="true" />
<input type="submit" name="postaction_save" value="<%= wi18n.str("jspviews.edit.save") %>" />
&nbsp;
<% if(pageDraftSupported) { %>
<input type="button" value="<%= wi18n.str("jspviews.edit.save_draft") %>" onClick="oxywikiSaveDraft();"/>
&nbsp;
<% } %>
<input type="button" value="<%= wi18n.str("jspviews.edit.edit_ui") %>" onClick="oxy_select_tab(1, '-edit');"/>
&nbsp;
<input type="button" name="postaction_preview" value="<%= wi18n.str("jspviews.edit.preview") %>" onClick="oxywikiEditPreviewHTML();oxy_select_tab(2, '-edit');"/>
&nbsp;
<input type="button" value="<%= wi18n.str("jspviews.edit.diff") %>" onClick="oxywikiEditDiff();oxy_select_tab(3, '-edit');"/>
<%-- <input type="submit" name="postaction_preview" value="<%= wi18n.str("jspviews.edit.preview") %>" /> --%>
&nbsp;
<input type="button" name="postaction_help" value="<%= wi18n.str("jspviews.edit.help") %>" onClick="oxy_select_tab(4, '-edit');"/>
&nbsp;
<input type="submit" name="postaction_cancel" value="<%= wi18n.str("jspviews.edit.cancel") %>" />
</td>
</tr>
</table>

</form>

<% if(editerror != null) { %>
<pre><%= StringUtils.toHTMLEscape(StringUtils.toString(editerror), false, false) %></pre>
<% } %>



