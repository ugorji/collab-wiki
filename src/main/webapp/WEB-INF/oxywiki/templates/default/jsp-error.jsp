<%--
  This is the error page which is used by jsp pages. It mainly gets called when we 
  include a jsp page, which throws an exception. Consequently, it is the jsp page 
  defined as the error page of all our view pages.
--%>
<%@ page isErrorPage="true" 
    import="net.ugorji.oxygen.web.WebErrorModel,
            net.ugorji.oxygen.web.WebLocal,
	        net.ugorji.oxygen.wiki.WikiLocal" %><%
   try {	    
   WebErrorModel.includeErrorView(exception, 
     WebLocal.getWebInteractionContext().getWriter(), 
     WikiLocal.getWikiEngine().getWikiTemplateFilesHandler());
   } catch(Throwable thr) {
     oxygen.util.OxygenUtils.error(thr);
   }

%>
