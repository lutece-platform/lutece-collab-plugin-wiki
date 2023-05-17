<%@ page import="fr.paris.lutece.plugins.wiki.web.WikiDynamicInputs"%><%@ page errorPage="../../ErrorPagePortal.jsp" trimDirectiveWhitespaces="true" contentType="application/javascript; charset=UTF-8" %>

<jsp:useBean id="AutoSaveWiki" scope="request" class="fr.paris.lutece.plugins.wiki.web.WikiDynamicInputs" />

    <%
    if("saveWiki".equals(request.getParameter("actionName")))
    {
        WikiDynamicInputs.saveWiki( request );
    }
    else if("lastOpenModify".equals(request.getParameter("actionName")))
    {
        WikiDynamicInputs.updateLastOpenModifyTopicPage( request );
    }
    %>