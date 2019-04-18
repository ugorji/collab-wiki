<%@ include file="_topinclude.jspf" %><%
  WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
  Map model = (Map)wlh.getAttribute("wiki.model.calendar");
  WikiLinkHolder wlh2 = wlh.getClone();
  WikiCategoryEngine wce = WikiLocal.getWikiCategoryEngine();
  WikiCalendarHelper wcal = (WikiCalendarHelper)model.get("wikicalendarhelper");
  Calendar cal2 = (Calendar)wcal.getCal().clone();
  //System.out.println("cal2: " + cal2.getTime());
  int firstDayOfWeek = cal2.getFirstDayOfWeek();
  String[] daysOfWeek = new String[7];
  SimpleDateFormat dwdf = new SimpleDateFormat("EE");
  Collection daysWithData = wcal.getDaysInCurrentMonthWithData();
%>
<table>
<tr>
<th colspan="7">
<% cal2.add(Calendar.YEAR, -1);
   dwdf.applyPattern("yyyy/MM"); 
   wlh2.setWikiPage(dwdf.format(cal2.getTime()));
%>
<a href="<%= WikiViewUtils.decipherURL(wlh2, "calendarview") %>">&lt;&lt;</a>
<%-- &nbsp;&nbsp; --%>
<% cal2.add(Calendar.YEAR, 1);
   cal2.add(Calendar.MONTH, -1);
   dwdf.applyPattern("yyyy/MM");
   wlh2.setWikiPage(dwdf.format(cal2.getTime()));
%>
<a href="<%= WikiViewUtils.decipherURL(wlh2, "calendarview") %>">&lt;</a>
<%-- &nbsp;&nbsp; --%>
<% cal2.add(Calendar.MONTH, 1);
   dwdf.applyPattern("yyyy/MM");
   wlh2.setWikiPage(dwdf.format(cal2.getTime()));
   dwdf.applyPattern("MMM");
%>
<a href="<%= WikiViewUtils.decipherURL(wlh2, "calendarview") %>"><%= dwdf.format(cal2.getTime()) %></a>
<%-- &nbsp;&nbsp; --%>
<% dwdf.applyPattern("yyyy");
   wlh2.setWikiPage(dwdf.format(cal2.getTime()));
   dwdf.applyPattern("yyyy");
%>
<a href="<%= WikiViewUtils.decipherURL(wlh2, "calendarview") %>"><%= dwdf.format(cal2.getTime()) %></a>
<%-- &nbsp;&nbsp; --%>
<% cal2.add(Calendar.MONTH, 1);
   dwdf.applyPattern("yyyy/MM");
   wlh2.setWikiPage(dwdf.format(cal2.getTime()));
%>
<a href="<%= WikiViewUtils.decipherURL(wlh2, "calendarview") %>">&gt;</a>
<%-- &nbsp;&nbsp; --%>
<% cal2.add(Calendar.MONTH, -1);
   cal2.add(Calendar.YEAR, 1);
   dwdf.applyPattern("yyyy/MM");
   wlh2.setWikiPage(dwdf.format(cal2.getTime()));
%>
<a href="<%= WikiViewUtils.decipherURL(wlh2, "calendarview") %>">&gt;&gt;</a>
</th>
</tr>
<tr>
<% cal2.setTime(wcal.getOrigdate());
   dwdf.applyPattern("EE");
   for(int i = 0; i < 7; i++) { 
     cal2.set(cal2.DAY_OF_WEEK, firstDayOfWeek + i);
%>
<th><%= dwdf.format(cal2.getTime()).substring(0, 1) %></th>
<% } %>
</tr>
<% for(Iterator itr = wcal.getWeeks().iterator(); itr.hasNext(); ) { 
     int[] week0 = (int[])itr.next(); 
%>
<tr>
<%   cal2.setTime(wcal.getOrigdate());
     dwdf.applyPattern("yyyy/MM/dd");
     for(int i = 0; i < week0.length; i++) { 
       if(week0[i] == -1) {
%>
<td>&nbsp;</td>
<%
       } else if(daysWithData.contains(new Integer(week0[i]))) {
         cal2.set(cal2.DAY_OF_MONTH, week0[i]);
         wlh2.setWikiPage(dwdf.format(cal2.getTime()));
%><td><b><a href="<%= WikiViewUtils.decipherURL(wlh2, "calendarview") %>"><%= week0[i] %></a></b></td>
<%
       } else {
%><td><%= week0[i] %></td>
<%     } 
     } 
%>
</tr>
<% } %>
</table>

<% cal2.setTime(wcal.getOrigdate()); %>
