<%@ include file="_topinclude.jspf" %><%
  WikiCategoryEngine wcengine = WikiLocal.getWikiCategoryEngine();
  WikiProvidedObject wp = WikiUtils.getWikiPage();
  WikiEditManager editManager = wcengine.getWikiEditManager();
  WikiEditLock[] elocks = editManager.getLocks(wp.getName());
  WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
  wlh.setAttribute("oxygen.wiki.locks", elocks);
%>
<p>
<%= wi18n.str("general.cannot_edit_lock_cannot_be_acquired") %>

<p>
<jsp:include page="locks.jsp" />
