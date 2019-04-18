<%@ include file="_topinclude.jspf" %><%
  WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
  String action = WikiUtils.getString("previewtop.action", null);
  String _postlink = WikiViewUtils.decipherURL(wlh, action);
%>
<blockquote>
<form action="<%= _postlink %>" method="POST" >
<font color="red"><b><%= wi18n.str("jspviews.edit.preview_msg") %></b></font>
<input type="hidden" name="postaction" value="true" />
<input type="hidden" name="attribute.<%= WikiEvent.MINOR_EDIT_FLAG_KEY %>" value="<%= WikiUtils.getString("attribute." + WikiEvent.MINOR_EDIT_FLAG_KEY, "") %>" />
<input type="hidden" name="attribute.comments" value="<%= WikiUtils.getString("attribute.comments", "") %>" />
<input type="hidden" name="attribute.subscribers" value="<%= WikiUtils.getString("attribute.subscribers", "") %>" />
<input type="hidden" name="<%= WikiConstants.PARAMETER_TEXT %>" value="<%= StringUtils.toHTMLEscape(WikiUtils.getString(WikiConstants.PARAMETER_TEXT, ""), false, false) %>" />
<input type="submit" name="postaction_save" value="<%= wi18n.str("jspviews.edit.save") %>" />
&nbsp;&nbsp;&nbsp;
<input type="submit" name="postaction_reedit" value="<%= wi18n.str("jspviews.edit.re_edit") %>" />
&nbsp;&nbsp;&nbsp;
<input type="submit" name="postaction_cancel" value="<%= wi18n.str("jspviews.edit.cancel") %>" />
</form>
</blockquote>
<% out.flush(); %>
