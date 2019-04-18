<%@ include file="_topinclude.jspf" %><%
  WikiLinkHolder wlh = WikiLocal.getWikiLinkHolder();
  boolean loginErrorOccurred = "true".equals(wikiwebctx.getParameter("loginerror"));
%>
<form name="loginform" method="post" action="j_security_check">

  <table cellpadding="5" cellspacing="0" border="0" align="center" >
    <tr> 
      <th colspan="2"><%= wi18n.str("jspviews.login.header_message") %></th>
    </tr>
    <% if(loginErrorOccurred) { %>
    <tr> 
      <td colspan="2" ><font color="red"><%= wi18n.str("jspviews.login.wrong_credentials_prompt") %></font></td>
    </tr>
    <% } %>
    <tr> 
      <td><%= wi18n.str("jspviews.login.username_prompt") %>: </td>
      <td><input name="j_username" size="17" maxlength="32" value=""></td>
    </tr>
    <tr> 
      <td><%= wi18n.str("jspviews.login.password_prompt") %>: </td>
      <td><input type="password" name="j_password" size="17" maxlength="32" value=""></td>
    </tr> 
    <tr> 
      <td>&nbsp; </td>
      <td><input type="submit" name="submit" value="Login"></td>
    </tr> 
  </table>
</form>

