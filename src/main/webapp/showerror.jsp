<%--
  WebLogic Server does not support using a servlet as both a content handler and an error handler.
  It's a weird problem going all the way back to WLS 7 timeframe.
  To workaround it, we use a jsp that internally forwards to the servlet.
--%><jsp:forward page="/p/showerror/builtin"/>