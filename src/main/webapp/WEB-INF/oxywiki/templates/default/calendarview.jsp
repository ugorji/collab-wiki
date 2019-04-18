<%@ include file="_topinclude.jspf" %><%
  WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
  DateFormat dfcal = WikiViewUtils.getDateFormat(wlh.getLocale());
  Map model = (Map)wlh.getAttribute("wiki.model.calendarview");
  WikiLinkHolder wlh2 = wlh.getClone();
  WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
  WikiCalendarHelper wcal = (WikiCalendarHelper)model.get("wikicalendarhelper");
  WikiProvidedObject[] wps = wcal.getPages();
  Calendar cal2 = (Calendar)wcal.getCal().clone();
  Date lastcaldate = null;
  for(int i = 0; i < wps.length; i++) { 
    Date wpdate = wps[i].getDate();
    cal2.setTime(wpdate);
    cal2.set(cal2.HOUR, 1);
    cal2.set(cal2.MINUTE, 1);
    cal2.set(cal2.SECOND, 1);
    cal2.set(cal2.MILLISECOND, 1); 
    wpdate = cal2.getTime();
%>
<%  if(!(wpdate.equals(lastcaldate))) { %>
<div class="wikicalendardate"><%= dfcal.format(wpdate) %></div>
<%    
      lastcaldate = wpdate;
    } 
%>
<div class="wikicalendartitle"><%= wcal.getTitle(wps[i]) %></div>
<%
    out.flush();
    wcal.writeHTML(wps[i]);
    wlh2.setWikiPage(wps[i].getName());
%>
<div align="left" style="padding: 2px 4px 2px 4px;" ><i>
  <%   String author1 = StringUtils.nonNullString(WikiViewUtils.getAuthor(wps[i]), "-"); %>
<%= wi18n.str("templates.default.version") %> <%= wps[i].getVersion() %> <%= wi18n.str("templates.default.modified") %> <%= df.format(wps[i].getDate()) %> <%= wi18n.str("templates.default.by") %> <a href="<%= WikiUtils.getUserLink(author1) %>"><%= author1 %></a>
| 
<%= wi18n.str("templates.default.reviews") %> (<%= wce.getIndexingManager().lookupPageReviewNames(wps[i].getName(), null, null).length %>) 
| 
<a href="<%= WikiViewUtils.decipherURL(wlh2, "view") %>"><%= wi18n.str("general.more") %></a>
</i></div>
<% } %>

