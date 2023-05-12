<%@ page errorPage="../../ErrorPagePortal.jsp" trimDirectiveWhitespaces="true" contentType="application/javascript; charset=UTF-8" %>

<jsp:useBean id="AutoSaveWiki" scope="request" class="fr.paris.lutece.plugins.wiki.web.AutoSaveWiki" />

<%= AutoSaveWiki.save( request ) %>