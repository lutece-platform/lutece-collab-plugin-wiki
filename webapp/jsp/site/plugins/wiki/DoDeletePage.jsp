
<%@ page errorPage="ErrorPagePortal.jsp" %>

<jsp:useBean id="wiki" scope="session" class="fr.paris.lutece.plugins.wiki.web.WikiJspBean" />

<%
    response.sendRedirect( wiki.doDeletePage( request ) );
%>

