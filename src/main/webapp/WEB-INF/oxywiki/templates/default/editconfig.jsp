<%@ include file="_topinclude.jspf" %><%
  WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
  Map model = (Map)wlh.getAttribute("wiki.model.editconfig");
  String configfile = (String)model.get("configfile");
  String configtext = (String)model.get("configtext");
  
  WikiCategoryEngine wcengine = WikiLocal.getWikiCategoryEngine();
  WikiEngine we = wcengine.getWikiEngine();

  String _adminlink = WikiViewUtils.decipherURL(wlh, "editconfigpost");
%>
<h3><%= wi18n.str("jspviews.editconfig.edit") %> <%= configfile %></h3>
<form action="<%= _adminlink %>" method="POST" >
<p>
<textarea wrap="virtual" name="configtext" rows="25" cols="80" style="width:100%;" 
  ><%= configtext %></textarea>
<br />
<p>
<input type="hidden" name="posteditconfig" value="true" />
<input type="hidden" name="configfile" value="<%= configfile %>" />
<input type="submit" name="posteditconfigaction_save" value="<%= wi18n.str("jspviews.editconfig.save") %>" />
&nbsp;&nbsp;&nbsp;&nbsp;
<input type="submit" name="posteditconfigaction_cancel" value="<%= wi18n.str("jspviews.editconfig.cancel") %>" />
</form>

