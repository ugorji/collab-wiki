<%@ page import="net.ugorji.oxygen.wiki.*" %><% 
  // This is only used by the servlet.
  String url = request.getContextPath() + "/p" + 
    "/" + WikiConstants.ACTION_SECTIONS + 
    "/" + WikiConstants.BUILTIN_SECTION_NAME;
  response.sendRedirect(url); 
%>
