<%@ include file="_topinclude.jspf" %><%
  WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
  WikiLinkHolder wlh2 = wlh.getClone();
  WikiProvidedObject wp = WikiUtils.getWikiPage();
  WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
  WikiAttachmentProvider attprov = wce.getAttachmentProvider();
  //String _attachmentslink = wlh.getWikiURL();
  String _viewlink = WikiViewUtils.decipherURL(wlh, WikiConstants.ACTION_VIEW);
  boolean hideAttachFileForm = "true".equals(wlh.getAttribute("hide.attachments.addform"));
  boolean hideAttach = "true".equals(wlh.getAttribute("hide.attachments"));
  boolean includeDeleted = "true".equals(wlh.getAttribute("include.deleted.attachments"));
  
  String attCommToggleAnchor = String.valueOf(WikiViewUtils.nextRandomInt(99));
  WikiProvidedObject[] atts = WikiViewUtils.lookupAttachments(wce, wp.getName(), includeDeleted);
  if(atts == null) {
    atts = new WikiProvidedObject[0];
  }
  List allExistingAtts = Arrays.asList(wce.getIndexingManager().lookupAttachmentNames(wp.getName(), null, null));
  
  String _attachmentslink = WikiViewUtils.decipherURL(wlh, "attachmentspost");
  //String _showattachmentsdetailslink = "#";
  //if(!detailsNecessary) {
  //  wlh2.setExtraparams(new HashMap());
  //  wlh2.getExtraparams().put("details", "true");
  //  _showattachmentsdetailslink = WebLocal.getWebInteractionContext().toURLString(wlh2);
  //  wlh2.setExtraparams(new HashMap());
  //}
%>
<div id="oxywiki_attachments" style="display:<%= (hideAttach ? "none" : "block") %>">
<% if(atts.length > 0) { %>
<table border="1">
<tr>
<th>--</th>
<th>--</th>
<th><%= wi18n.str("jspviews.attachments.attachments") %></th>
<th><%= wi18n.str("jspviews.attachments.version") %></th>
<th><%= wi18n.str("jspviews.attachments.size") %></th>
<th><%= wi18n.str("jspviews.attachments.last_mod") %></th>
<th><%= wi18n.str("jspviews.attachments.author") %></th>
<th><%= wi18n.str("jspviews.attachments.comments") %></th>
</tr>
<% wlh2.setVersion(WikiProvidedObject.VERSION_LATEST_DETAILS_UNNECESSARY);
   for(int i = 0; i < atts.length; i++) { 
     String _attachmentname = atts[i].getName();
     wlh2.setExtrainfo(_attachmentname);
     //String _attachmentlink = WikiViewUtils.decipherURL(wlh2, WikiConstants.ACTION_VIEW_ATTACHMENT);
     wlh2.setAction(WikiConstants.ACTION_VIEW_ATTACHMENT);
     String _attachmentlink = wlh2.getWikiURL();
     String _deletepromptlink = WikiViewUtils.decipherURL(wlh2, "deleteprompt", new String[]{"attachment", _attachmentname});
     String _attachmentinfolink = WikiViewUtils.decipherURL(wlh2, "attachmentinfo");
     String _deleteattachmentlink = WikiViewUtils.decipherURL(wlh2, "deleteattachment");
     boolean _attExists = allExistingAtts.contains(_attachmentname);
%>
<tr>
<td><a href="<%= _attachmentinfolink %>"><%= wi18n.str("jspviews.attachments.info") %></a></td>
<td>
<% if(wce.isActionSupported(WikiConstants.ACTION_DELETE) && _attExists) { %>
<a href="<%= _deletepromptlink %>"><%= wi18n.str("jspviews.attachments.delete") %></a>
<% } else { %>&nbsp;<% } %>
</td>
<td>
<% if(_attExists) { %>
<a href="<%= _attachmentlink %>"><%= _attachmentname %></a>
<% } else { %>
<strike><%= _attachmentname %></strike>
<% } %>
</td>
<td><%= atts[i].getVersion() %></td>
<td><%= atts[i].getSize() %></td>
<td><%= (atts[i].getDate() == null ? "-" : (df.format(atts[i].getDate()))) %></td>
<%     String author1 = StringUtils.nonNullString(WikiViewUtils.getAuthor(atts[i]), "-"); %>
<td><a href="<%= WikiUtils.getUserLink(author1) %>"><%= author1 %></a></td>
<td><%= WikiViewUtils.getAttribute(atts[i], WikiConstants.ATTRIBUTE_COMMENTS, "-") %></td>
</tr>
<% } %>
</table>
<%-- [NO LONGER NEEDED] 
<% if(!detailsNecessary) { %><a href="<%= _showattachmentsdetailslink %>"> <%= wi18n.str("jspviews.attachments.details") %></a> <% } %> 
--%>
<% } %>

<p/>
</div>

<a name="<%= attCommToggleAnchor %>" />

<% if(wce.isActionSupported("attachmentspost")) { %>
<div id="oxywiki_add_attachment" style="display:<%= (hideAttachFileForm ? "none" : "block") %>">
<h3><%= wi18n.str("jspviews.attachments.attach_file_form_header") %></h3>
<form action="<%= _attachmentslink %>" method="POST" enctype="multipart/form-data" >
<input type="checkbox" name="attribute.<%= WikiEvent.MINOR_EDIT_FLAG_KEY %>" value="true" /><%= wi18n.str("jspviews.attachments.minor_edit_field") %><br />
<%= wi18n.str("jspviews.attachments.comments_field") %>
<input type="text" name="attribute.comments" value="..." style="width:100%" />
<input type="file" name="file0" size="80" style="width:100%" ><%-- maxLength="512" --%>
<input type="file" name="file1" size="80" style="width:100%" ><%-- maxLength="512" --%>
<input type="file" name="file2" size="80" style="width:100%" ><%-- maxLength="512" --%>
<input type="submit" name="ok" value="Attach File">
<input type="hidden" name="postattachments" value="true" />
</form>
<a href="<%= _viewlink %>"><%= wi18n.str("jspviews.attachments.return_to_page") %></a>
</div>

<% } %>

