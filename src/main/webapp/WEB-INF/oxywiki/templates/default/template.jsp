<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/DTD/strict.dtd">
<!-- this ensures we use standards compliant CSS mode (http://en.wikipedia.org/wiki/Quirks_mode) -->

<%@ include file="incl_imports_and_variables.jspf" %>
<% if(mb_tmplViewFullpage) { %>
<html>
<head>
<%@ include file="incl_html_head_generic_info.jspf" %>
<% } %>

<link rel="stylesheet" media="screen,projection,print" type="text/css" href="<%= ms_ctxpath %>/stylesheets/stylesheet.css" />
<link rel="stylesheet" media="screen,projection,print" type="text/css" href="<%= ms_ctxpath %>/stylesheets/oxywiki.css" />
<LINK rel="stylesheet" media="screen,projection,print" type="text/css" href="<%= ms_ctxpath %>/stylesheets/stylesheet-view3.css" />
<%@ include file="incl_stylesheets_definer.jspf" %>

<script LANGUAGE="JavaScript" SRC="<%= ms_ctxpath %>/util/prototype.js" ></SCRIPT>
<%@ include file="incl_javascript_functions.jspf" %>
<SCRIPT LANGUAGE="JavaScript" SRC="<%= ms_ctxpath %>/util/js-common.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="<%= ms_ctxpath %>/util/view3-menu.js"></SCRIPT>

<%@ include file="incl_layout.jspf" %>

<% if(mb_tmplViewFullpage) { %>
</head>
<body>
<% } %>

<a name="oxy_top" ></a>
<% if(m_wlh.getAction().equals("printableview")) { %>
  <a href="<%= ms_viewlink %>"><%= m_wi18n.str("templates.default.go_back_to_view") %></a><br/>
<% } %>

<div class="pagelayout" >
<!-- top -->

<% if(mb_showBorder && mb_topPageExists) { %>
  <div CLASS="breadcrumbs" > 
  <% out.flush(); %>
  <% WikiViewUtils.includeDecorationPage(ms_topmenu); %>
  </div>
<% } %>

<div><!-- wrap left, right and middle -->
<!-- put left menu here -->
<% if(mb_showBorder && (mb_leftPageExists || mb_showCalendarInLeft)) { %>
<div id="oxywiki_leftmenu" CLASS="leftmenu">
<% out.flush(); %>
<% if(mb_showCalendarInLeft) WikiViewUtils.includeView("calendar"); %>
<% if(mb_leftPageExists) WikiViewUtils.includeDecorationPage(ms_leftmenu); %>
</div>
<% } %>

<!-- put right menu here -->
<% if(mb_showBorder && (mb_rightPageExists || mb_showCalendarInRight)) { %>
<div id="oxywiki_rightmenu" CLASS="rightmenu" >
<% out.flush(); %>
<% if(mb_showCalendarInRight) WikiViewUtils.includeView("calendar"); %>
<% if(mb_rightPageExists) WikiViewUtils.includeDecorationPage(ms_rightmenu); %>
</div>
<% } %>

<!-- center (main body) here -->
<div id="oxywiki_mainwikipage" CLASS="mainwikipage" WIDTH="100%" VALIGN="top">
<%@ include file="incl_menu.jspf" %>
<%@ include file="incl_trail.jspf" %>

<% if(mb_showNameInPage) { %>
<H1 CLASS="pagename"><%= ms_pageName %>&nbsp;</H1>
<% } %>
<% out.flush(); %><% WikiLocal.getWikiTemplateHandler().includeView(ms_jsppage); %>
<%@ include file="incl_attachments_and_comments.jspf" %>
</div>

</div><!-- wrap left, right and middle -->

<div style='clear: both;'></div>

<!-- put bottom menu here -->
<% if(mb_showBorder && mb_bottomPageExists) { %>
  <div CLASS="breadcrumbs" > 
  <% out.flush(); %><% WikiViewUtils.includeDecorationPage(ms_bottommenu); %>
  </div>
<% } %>

<p/>

<a name="oxy_bottom"></a>
<div class="oxy-credits">
<%= m_wi18n.str("general.credits") %>. <a href="<%= ms_aboutlink %>"><%= m_wi18n.str("general.oxywiki_powered") %></a>. 
&copy; <a href="http://www.oxygensoftwarelibrary.com/">oxygensoftwarelibrary.com</a>
</div>

</div><!-- end pagelayout -->
<%--
<a href="javascript:oxy_alert('hello: ' + document.getElementById('oxywiki_leftmenu').offsetWidth);oxy_alert_update();">offsetWidth</a> | 
<a href="javascript:oxy_get_css_rule('mainwikipage');oxy_alert_update();">css rule</a> | 
<pre id="pre_oxy_message_to_box"><br/><br/></pre>
--%>

<% if(mb_tmplViewFullpage) { %>
</body>

</html>
<% } %>


