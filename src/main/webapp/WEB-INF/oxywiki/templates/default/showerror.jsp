<%-- 
  This is used as the error page for the whole webapp.
  When an error bubbles to the top, or an error response code is thrown,
  this page gets called. 
  This is actually the view page of the ShowErrorAction.
--%>
<%@ page isErrorPage="true" 
    import="java.io.*,
	    java.util.*,
	    java.text.DateFormat,
	    oxygen.util.*,
	    oxygen.wiki.*,
            oxygen.web.*,
            oxygen.markup.MarkupUtils" %><%
   DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.FULL); 
   I18n wi18n = WebLocal.getI18n();
   WikiEngine wengine = WikiLocal.getWikiEngine();
   boolean showMessageOnly = "true".equals(wengine.getProperty(WebConstants.SHOW_ERROR_MESSAGE_ONLY_KEY));
   
   boolean pageNotFound = false;
   boolean unauthorizedAccess = false;
   boolean internalServerError = false;
   String errRequestURI = (String) request.getAttribute("javax.servlet.error.request_uri");
   Integer errStatusCodeINT = (Integer) request.getAttribute("javax.servlet.error.status_code");
   int errStatusCode = -1;
   if (errStatusCodeINT != null) {
     errStatusCode = errStatusCodeINT.intValue ();
   }
   String errTopic = wi18n.str("common.error.topic_other_prefix") + errStatusCodeINT;
   String errMessage = (String)request.getAttribute ("javax.servlet.error.message");
   if(errStatusCode == 404 || errStatusCode == 403 || errStatusCode == 500) {
     errTopic = wi18n.str("common.error.topic_" + errStatusCode);
   } 
   if(StringUtils.isBlank(errMessage)) {
     if(errStatusCode == 404 || errStatusCode == 403 || errStatusCode == 500) {
       errMessage = wi18n.str("common.error.message_" + errStatusCode);
     } else {
       errMessage = "";
     }
   }
   String excstacktrace = null;
   String excMessage = null;
   if(exception != null) {
     excMessage = exception.getMessage();
     excstacktrace = StringUtils.toHTMLEscape(StringUtils.toString(exception), false, false);
   }
   errMessage = StringUtils.nonNullString(errMessage, "");
   excMessage = StringUtils.nonNullString(excMessage, "");
  
   errMessage = StringUtils.toHTMLEscape(errMessage, true, false);
   excMessage = StringUtils.toHTMLEscape(excMessage, true, true);
   
   Throwable owexc = (Throwable)request.getAttribute(WebConstants.THROWABLE_ATTRIBUTE_KEY);
   String editpagelinkforpnfe = null;
   WikiPageNotFoundException wpnfe = null;
   
   //treat WikiPageNotFoundException specially (show create link for it)
   if(owexc != null && owexc instanceof WikiPageNotFoundException) {
     wpnfe = (WikiPageNotFoundException)owexc;
     WikiCategoryEngine wce99 = wengine.retrieveWikiCategoryEngine(wpnfe.getCategory());
     if(wce99.isActionSupported(WikiConstants.ACTION_EDIT)) {
       WikiLinkHolder wlhOwexc = new WikiLinkHolder();
       wlhOwexc.setAction("edit");
       wlhOwexc.setCategory(wpnfe.getCategory());
       wlhOwexc.setWikiPage(wpnfe.getPagename());
       editpagelinkforpnfe = wlhOwexc.getURL();
     }
   }
%>

<b><i><%= wi18n.str("common.error.contact_admin_for_more_info", df.format(new Date())) %></i></b>
<p/>
<% if(showMessageOnly) { %>

<blockquote>
<font color="red">
[<%= errStatusCode %>]<br/>
<%= errMessage %><br/>
<%= excMessage %>
</font>
</blockquote>

<% } else { %>
<table border="1">
<tr>
<th colspan="2">
<%= wi18n.str("common.error.intercepted") %>
</th>
</tr>
<tr>
<td><%= wi18n.str("common.error.request_uri") %></td>
<td><%= errRequestURI %></td>
</tr>
<tr>
<td><%= wi18n.str("common.error.status_code") %></td>
<td><%= errStatusCode %></td>
</tr>
<tr>
<td><%= wi18n.str("common.error.topic") %></td>
<td><%= errTopic %></td>
</tr>
<tr>
<td><%= wi18n.str("common.error.message") %></td>
<td><%= errMessage %></td>
</tr>
<% if(excstacktrace != null) { %>
<tr>
<th colspan="2"><%= wi18n.str("common.error.exception_info") %></th>
</tr>
<tr>
<td><%= wi18n.str("common.error.exception_message") %></td>
<td><span class="pre"><%= excMessage %></span></td>
</tr>
<tr>
<td colspan=2><pre><%= excstacktrace %></pre></td>
</tr>
<% } %>
</table>
 
<% } %>

<% if(wpnfe != null) { %>
<font color="red"><b><i>
<%= wi18n.str("jspviews.pagenotexist.msg", wpnfe.getPagename()) %>
<% if(editpagelinkforpnfe != null) { %>
<p>
<a href="<%= editpagelinkforpnfe %>"><%= wi18n.str("jspviews.pagenotexist.create_it") %></a>
<% } %>
</i></b></font>

<% } %>

