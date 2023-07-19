<%@ page import="fr.paris.lutece.plugins.wiki.web.WikiDynamicInputs"%><%@ page errorPage="../../ErrorPagePortal.jsp" trimDirectiveWhitespaces="true" contentType="application/javascript; charset=UTF-8" %>

<jsp:useBean id="AutoSaveWiki" scope="request" class="fr.paris.lutece.plugins.wiki.web.WikiDynamicInputs" />

    <%
    if("autoSaveWiki".equals(request.getParameter("actionName")))
    {
        WikiDynamicInputs.autoSaveWiki( request );
    }
     else if("getPageHeadings".equals(request.getParameter("actionName")))
    {
        WikiDynamicInputs.getPageHeadings( request, response );
    }

    else if("lastOpenModify".equals(request.getParameter("actionName")))
    {
        WikiDynamicInputs.updateLastOpenModifyTopicPage( request );

    } else if("modifyPage".equals(request.getParameter("actionName")))
    {
         WikiDynamicInputs.modifyPage( request, response );
    }

    %>