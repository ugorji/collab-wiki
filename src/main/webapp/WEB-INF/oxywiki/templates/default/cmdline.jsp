<%@ include file="_topinclude.jspf" %><%
  WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
  Map model = (Map)wlh.getAttribute("wiki.model.cmdline");
  int exitcode = ((Integer)model.get("exitcode")).intValue();
  String outerrstr = (String)model.get("outerrstr");
  String cmdline = (String)model.get("cmdline");
  String dir = (String)model.get("dir");
%>
<h3><%= wi18n.str("jspviews.cmdline.header") %></h3>
<form action="<%= WikiViewUtils.decipherURL(wlh, "cmdline") %>" method="get" >
<input type="hidden" name="<%= WikiConstants.SUBMIT_REQUEST_PARAMETER %>" value="true" />
<table border="1" width="100%">
<tr>
<th colspan="2"><%= wi18n.str("jspviews.cmdline.input") %></th>
</tr>
<tr>
<td width="15"><%= wi18n.str("jspviews.cmdline.dir") %></td>
<td><input type="text" name="dir" value="<%= dir %>" size="80" style="width:100%" /></td>
</tr>
<tr>
<td width="15"><%= wi18n.str("jspviews.cmdline.cmdline") %></td>
<td><textarea wrap="virtual" name="cmdline" rows="3" cols="80" style="width:100%;" ><%= cmdline %></textarea></td>
</tr>
<tr>
<td colspan="2">
<input type="hidden" name="postaction" value="true" />
<input type="submit" name="postaction_cmdline" value="<%= wi18n.str("jspviews.cmdline.execute") %>" />
</td>
</tr>
<tr>
<th colspan="2"><%= wi18n.str("jspviews.cmdline.output_header") %></th>
</tr>
<tr>
<td width="15"><%= wi18n.str("jspviews.cmdline.exitcode") %></td>
<td><%= exitcode %></td>
</tr>
<tr>
<td width="15"><%= wi18n.str("jspviews.cmdline.output") %></td>
<td><blockquote><%= StringUtils.toHTMLEscape(outerrstr, true, true) %></blockquote></td>
</tr>
</table>
</form>
