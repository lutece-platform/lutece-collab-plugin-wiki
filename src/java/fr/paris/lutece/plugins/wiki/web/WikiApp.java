/*
 * Copyright (c) 2002-2018, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.wiki.web;

import fr.paris.lutece.plugins.avatar.service.AvatarService;
import fr.paris.lutece.plugins.wiki.business.Image;
import fr.paris.lutece.plugins.wiki.business.ImageHome;
import fr.paris.lutece.plugins.wiki.business.Topic;
import fr.paris.lutece.plugins.wiki.business.TopicHome;
import fr.paris.lutece.plugins.wiki.business.TopicVersion;
import fr.paris.lutece.plugins.wiki.business.TopicVersionHome;
import fr.paris.lutece.plugins.wiki.business.WikiContent;
import fr.paris.lutece.plugins.wiki.service.DiffService;
import fr.paris.lutece.plugins.wiki.service.WikiLocaleService;
import fr.paris.lutece.plugins.wiki.service.WikiService;
import fr.paris.lutece.plugins.wiki.service.WikiUtils;
import fr.paris.lutece.plugins.wiki.utils.auth.WikiAnonymousUser;
import fr.paris.lutece.portal.business.page.Page;
import fr.paris.lutece.portal.business.role.RoleHome;
import fr.paris.lutece.portal.service.content.XPageAppService;
import fr.paris.lutece.portal.service.datastore.DatastoreService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.message.SiteMessage;
import fr.paris.lutece.portal.service.message.SiteMessageException;
import fr.paris.lutece.portal.service.message.SiteMessageService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.prefs.UserPreferencesService;
import fr.paris.lutece.portal.service.search.SearchEngine;
import fr.paris.lutece.portal.service.search.SearchResult;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.service.security.UserNotSignedException;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.portal.util.mvc.xpage.MVCApplication;
import fr.paris.lutece.portal.util.mvc.xpage.annotations.Controller;
import fr.paris.lutece.portal.web.l10n.LocaleService;
import fr.paris.lutece.portal.web.resource.ExtendableResourcePluginActionManager;
import fr.paris.lutece.portal.web.upload.MultipartHttpServletRequest;
import fr.paris.lutece.portal.web.xpages.XPage;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.html.Paginator;
import fr.paris.lutece.util.url.UrlItem;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.i18n.LocaleString;

/**
 * This class provides a simple implementation of an XPage
 */
@Controller( xpageName = "wiki", pageTitleProperty = "wiki.pageTitle", pagePathProperty = "wiki.pagePathLabel" )
public class WikiApp extends MVCApplication
{
    public static final int ACTION_VIEW = 1;
    private static final String TEMPLATE_MODIFY_WIKI = "skin/plugins/wiki/modify_page.html";
    private static final String TEMPLATE_VIEW_WIKI = "skin/plugins/wiki/view_page.html";
    private static final String TEMPLATE_VIEW_HISTORY_WIKI = "skin/plugins/wiki/history_page.html";
    private static final String TEMPLATE_VIEW_DIFF_TOPIC_WIKI = "skin/plugins/wiki/diff_topic.html";
    private static final String TEMPLATE_LIST_WIKI = "skin/plugins/wiki/list_wiki.html";
    private static final String TEMPLATE_SEARCH_WIKI = "skin/plugins/wiki/search_wiki.html";
    private static final String BEAN_SEARCH_ENGINE = "wiki.wikiSearchEngine";
    private static final String PROPERTY_PAGE_PATH = "wiki.pagePathLabel";
    private static final String PROPERTY_PAGE_TITLE = "wiki.pageTitle";
    private static final String PROPERTY_DEFAULT_RESULT_PER_PAGE = "wiki.search_wiki.itemsPerPage";
    private static final String PROPERTY_PATH_LIST = "wiki.path.list";
    private static final String PROPERTY_TITLE_LIST = "wiki.title.list";
    private static final String PROPERTY_PATH_SEARCH = "wiki.path.search";
    private static final String PROPERTY_TITLE_SEARCH = "wiki.title.search";
    private static final String PROPERTY_PATH_MODIFY = "wiki.path.labelModify";
    private static final String PROPERTY_PATH_HISTORY = "wiki.path.labelHistory";
    private static final String PROPERTY_PATH_DIFF = "wiki.path.labelDiff";
    private static final String MARK_TOPIC = "topic";
    private static final String MARK_LIST_TOPIC = "list_topic";
    private static final String MARK_LATEST_VERSION = "lastVersion";
    private static final String MARK_DIFF = "diff";
    private static final String MARK_RESULT = "result";
    private static final String MARK_LIST_TOPIC_VERSION = "listTopicVersion";
    private static final String MARK_PAGE_ROLES_LIST = "page_roles_list";
    private static final String MARK_QUERY = "query";
    private static final String MARK_PAGINATOR = "paginator";
    private static final String MARK_NB_ITEMS_PER_PAGE = "nb_items_per_page";
    private static final String MARK_EDIT_ROLE = "has_edit_role";
    private static final String MARK_ADMIN_ROLE = "has_admin_role";
    private static final String MARK_EXTEND = "isExtendInstalled";
    private static final String MARK_LANGUAGES_LIST = "languages_list";
    private static final String MARK_CURRENT_LANGUAGE = "current_language";
    private static final String PARAMETER_LANGUAGE = "language";
    private static final String VIEW_HOME = "home";
    private static final String VIEW_LIST = "list";
    private static final String VIEW_PAGE = "page";
    private static final String VIEW_MODIFY_PAGE = "modifyPage";
    private static final String VIEW_HISTORY = "history";
    private static final String VIEW_SEARCH = "search";
    private static final String VIEW_DIFF = "diff";
    private static final String VIEW_LIST_IMAGES = "listImages";
    private static final String ACTION_NEW_PAGE = "newPage";
    private static final String ACTION_EDIT_PAGE = "editPage";
    private static final String ACTION_MODIFY_PAGE = "modifyPage";
    private static final String ACTION_DELETE_PAGE = "deletePage";
    private static final String ACTION_REMOVE_IMAGE = "removeImage";
    private static final String ACTION_CONFIRM_REMOVE_IMAGE = "confirmRemoveImage";
    private static final String ACTION_REMOVE_VERSION = "removeVersion";
    private static final String ACTION_CONFIRM_REMOVE_VERSION = "confirmRemoveVersion";
    private static final String ACTION_UPLOAD_IMAGE = "uploadImage";
    private static final String ACTION_CHANGE_LANGUAGE = "changeLanguage";
    private static final String MESSAGE_IMAGE_REMOVED = "wiki.message.image.removed";
    private static final String MESSAGE_CONFIRM_REMOVE_IMAGE = "wiki.message.confirmRemoveImage";
    private static final String MESSAGE_NAME_MANDATORY = "wiki.message.error.name.notNull";
    private static final String MESSAGE_FILE_MANDATORY = "wiki.message.error.file.notNull";
    private static final String MESSAGE_CONFIRM_REMOVE_VERSION = "wiki.message.confirmRemoveVersion";
    private static final String MESSAGE_VERSION_REMOVED = "wiki.message.version.removed";
    private static final String MESSAGE_AUTHENTICATION_REQUIRED = "wiki.message.authenticationRequired";
    private static final String ANCHOR_IMAGES = "#images";
    private static final String DSKEY_WIKI_ROOT_LABEL = "wiki.site_property.path.rootLabel";
    private static final String DSKEY_WIKI_ROOT_PAGENAME = "wiki.site_property.path.rootPageName";
    private static final String DSKEY_ROLE_ADMIN = "wiki.site_property.role.admin";
    private static final String DSKEY_ROLE_EDIT = "wiki.site_property.role.edit";
    private static final String DSKEY_ROLE_VIEW = "wiki.site_property.role.view";
    private static final String DEFAULT_ROLE_ADMIN = "wiki_admin";
    private static final String DEFAULT_ROLE_EDIT = Page.ROLE_NONE;
    private static final String DEFAULT_ROLE_VIEW = Page.ROLE_NONE;
    private static final String URL_DEFAULT = "page=wiki";
    private static final String URL_VIEW_PAGE = "page=wiki&amp;view=page&amp;page_name=";
    private static final String PLUGIN_EXTEND = "extend";
    private static final int MODE_VIEW = 0;
    private static final int MODE_EDIT = 1;

    // private fields
    private String _strCurrentPageIndex;
    private int _nDefaultItemsPerPage;
    private int _nItemsPerPage;

    /**
     * Gets the Home page
     *
     * @param request
     *            The HTTP request
     * @return The XPage
     */
    @View( value = VIEW_HOME, defaultView = true )
    public XPage home( HttpServletRequest request )
    {
        return redirectWikiRoot( request );
    }

    /**
     * View List of all pages
     * @param request The HTTP request
     * @return The page
     */
    @View( VIEW_LIST )
    public XPage getTopicsList( HttpServletRequest request )
    {
        XPage page = getTopicsListPage( request );
        page.setTitle( getPageTitle( I18nService.getLocalizedString( PROPERTY_TITLE_LIST, LocaleService.getContextUserLocale( request ) ) ) );
        page.setExtendedPathLabel( getExtendedPath( I18nService.getLocalizedString( PROPERTY_PATH_LIST, LocaleService.getContextUserLocale( request ) ), null ) );

        return page;
    }

    /**
     * Search page
     *
     * @param request
     *            The request
     * @return Thye XPage
     */
    @View( VIEW_SEARCH )
    public XPage search( HttpServletRequest request )
    {
        String strQuery = request.getParameter( Constants.PARAMETER_QUERY );
        String strPortalUrl = AppPathService.getPortalUrl( );

        UrlItem urlWikiXpage = new UrlItem( strPortalUrl );
        urlWikiXpage.addParameter( XPageAppService.PARAM_XPAGE_APP, Constants.PLUGIN_NAME );
        urlWikiXpage.addParameter( Constants.PARAMETER_ACTION, Constants.PARAMETER_ACTION_SEARCH );

        _strCurrentPageIndex = Paginator.getPageIndex( request, Paginator.PARAMETER_PAGE_INDEX, _strCurrentPageIndex );
        _nDefaultItemsPerPage = AppPropertiesService.getPropertyInt( PROPERTY_DEFAULT_RESULT_PER_PAGE, 10 );
        _nItemsPerPage = Paginator.getItemsPerPage( request, Paginator.PARAMETER_ITEMS_PER_PAGE, _nItemsPerPage, _nDefaultItemsPerPage );

        SearchEngine engine = (SearchEngine) SpringContextService.getBean( BEAN_SEARCH_ENGINE );
        List<SearchResult> listResults = engine.getSearchResults( strQuery, request );

        Paginator paginator = new Paginator( listResults, _nItemsPerPage, urlWikiXpage.getUrl( ), Constants.PARAMETER_PAGE_INDEX, _strCurrentPageIndex );

        Map<String, Object> model = getModel( );
        model.put( MARK_RESULT, paginator.getPageItems( ) );
        model.put( MARK_QUERY, strQuery );
        model.put( MARK_PAGINATOR, paginator );
        model.put( MARK_NB_ITEMS_PER_PAGE, String.valueOf( _nItemsPerPage ) );

        XPage page = getXPage( TEMPLATE_SEARCH_WIKI, getLocale( request ), model );
        page.setTitle( getPageTitle( I18nService.getLocalizedString( PROPERTY_TITLE_SEARCH, getLocale( request ) ) ) );
        page.setExtendedPathLabel( getExtendedPath( I18nService.getLocalizedString( PROPERTY_PATH_SEARCH, getLocale( request ) ), null ) );

        return page;
    }

    /**
     * Display all pages
     *
     * @param request
     *            The HTTP request
     * @return The page
     */
    private XPage getTopicsListPage( HttpServletRequest request )
    {
        List<Topic> listTopic = getTopicsForUser( request );

        Map<String, Object> model = getModel( );
        model.put( MARK_LIST_TOPIC, listTopic );

        return getXPage( TEMPLATE_LIST_WIKI, getLocale( request ), model );
    }

    /**
     * Display the view page
     *
     * @param request
     *            The HTTP request
     * @return The XPage
     * @throws SiteMessageException
     *             if an error occurs
     */
    @View( VIEW_PAGE )
    public XPage view( HttpServletRequest request ) throws SiteMessageException
    {
        if ( !SecurityService.isAuthenticationEnable( ) )
        {
            SiteMessageService.setMessage( request, MESSAGE_AUTHENTICATION_REQUIRED, SiteMessage.TYPE_ERROR );
        }
        String strPageName = request.getParameter( Constants.PARAMETER_PAGE_NAME );
        Topic topic = getTopic( request, strPageName, MODE_VIEW );
        TopicVersion version = TopicVersionHome.findLastVersion( topic.getIdTopic( ) );
        if ( version == null )
        {
            UrlItem url = new UrlItem( AppPathService.getBaseUrl( request ) + AppPathService.getPortalUrl( ) );
            url.addParameter( Constants.PARAMETER_PAGE, Constants.PLUGIN_NAME );
            url.addParameter( Constants.PARAMETER_ACTION, ACTION_NEW_PAGE );
            url.addParameter( Constants.PARAMETER_PAGE_NAME, strPageName );
            return redirect( request, url.getUrl( ) );
        }
        fillUserData( version );
        String strWikiPage = WikiService.instance( ).getWikiPage( strPageName, version, getPageUrl( request ), getLanguage( request ) );
        Map<String, Object> model = getModel( );
        model.put( MARK_RESULT, strWikiPage );
        model.put( MARK_TOPIC, topic );
        model.put( MARK_LATEST_VERSION, version );
        model.put( MARK_EDIT_ROLE, hasEditRole( request, topic ) );
        model.put( MARK_ADMIN_ROLE, hasAdminRole( request ) );
        model.put( MARK_EXTEND, isExtend( ) );
        model.put( MARK_LANGUAGES_LIST, WikiLocaleService.getLanguages( ) );
        model.put( MARK_CURRENT_LANGUAGE, getLanguage( request ) );
        XPage page = getXPage( TEMPLATE_VIEW_WIKI, getLocale( request ), model );
        page.setTitle( getPageTitle( getTopicTitle( request, topic ) ) );
        page.setExtendedPathLabel( getExtendedPath( getTopicTitle( request, topic ), strPageName ) );

        return page;
    }

    /**
     * Create a new Page
     *
     * @param request
     *            The request
     * @return The XPage
     * @throws UserNotSignedException
     *             if the user is not signed
     * @throws java.io.UnsupportedEncodingException
     *             if an encoding exception occurs
     */
    @Action( ACTION_NEW_PAGE )
    public XPage newPage( HttpServletRequest request ) throws UserNotSignedException, UnsupportedEncodingException
    {
        checkUser( request );

        String strPageName = request.getParameter( Constants.PARAMETER_PAGE_NAME );
        String strPageTitle = strPageName;
        strPageName = WikiUtils.normalize( strPageName );

        Topic topic = TopicHome.findByPrimaryKey( strPageName );
        if ( topic == null )
        {
            topic = new Topic( );
            topic.setPageName( strPageName );
            topic.setViewRole( DatastoreService.getDataValue( DSKEY_ROLE_VIEW, DEFAULT_ROLE_VIEW ) );
            topic.setEditRole( DatastoreService.getDataValue( DSKEY_ROLE_EDIT, DEFAULT_ROLE_EDIT ) );

            TopicHome.create( topic );
        }

        Map<String, String> mapParameters = new ConcurrentHashMap<String, String>( );
        mapParameters.put( Constants.PARAMETER_PAGE_NAME, strPageName );
        mapParameters.put( Constants.PARAMETER_PAGE_TITLE, URLEncoder.encode( strPageTitle, "UTF-8" ) );

        return redirect( request, VIEW_MODIFY_PAGE, mapParameters );
    }

    /**
     * Display the Edit page
     * 
     * @param request
     *            The HTTP request
     * @return The page
     * @throws UserNotSignedException
     *             if the user is not signed
     * @throws java.io.UnsupportedEncodingException
     *             if an encoding exception occurs
     */
    @Action( ACTION_EDIT_PAGE )
    public XPage edit( HttpServletRequest request ) throws UserNotSignedException, UnsupportedEncodingException
    {
        checkUser( request );

        String strPageName = request.getParameter( Constants.PARAMETER_PAGE_NAME );
        Topic topic = TopicHome.findByPrimaryKey( strPageName );

        if ( topic == null )
        {
            return redirect( request, VIEW_HOME );
        }
        Map<String, String> mapParameters = new ConcurrentHashMap<String, String>( );
        mapParameters.put( Constants.PARAMETER_PAGE_NAME, URLEncoder.encode( strPageName, "UTF-8" ) );

        if ( !hasEditRole( request, topic ) )
        {
            return redirect( request, VIEW_PAGE, mapParameters );
        }
        return redirect( request, VIEW_MODIFY_PAGE, mapParameters );

    }

    /**
     * Display the edit mode
     *
     * @param request
     *            The HTTP request
     * @return The XPage
     * @throws SiteMessageException
     *             if an exception occurs
     */
    @View( VIEW_MODIFY_PAGE )
    public XPage modify( HttpServletRequest request ) throws SiteMessageException
    {
        String strPageName = request.getParameter( Constants.PARAMETER_PAGE_NAME );
        Topic topic = getTopic( request, strPageName, MODE_EDIT );
        TopicVersion topicVersion = TopicVersionHome.findLastVersion( topic.getIdTopic( ) );
        if ( topicVersion != null )
        {
            String strLanguage = getLanguage( request );
            WikiContent content = topicVersion.getWikiContent( strLanguage );
            content.setWikiContent( WikiService.renderEditor( topicVersion, strLanguage ) );
        }
        Map<String, Object> model = getModel( );
        model.put( MARK_TOPIC, topic );
        model.put( MARK_LATEST_VERSION, topicVersion );
        model.put( MARK_PAGE_ROLES_LIST, RoleHome.getRolesList( ) );
        model.put( MARK_EDIT_ROLE, hasEditRole( request, topic ) );
        model.put( MARK_ADMIN_ROLE, hasAdminRole( request ) );
        model.put( MARK_LANGUAGES_LIST, WikiLocaleService.getLanguages( ) );

        ExtendableResourcePluginActionManager.fillModel( request, null, model, Integer.toString( topic.getIdTopic( ) ), Topic.RESOURCE_TYPE );

        XPage page = getXPage( TEMPLATE_MODIFY_WIKI, request.getLocale( ), model );
        page.setTitle( getPageTitle( getTopicTitle( request, topic ) ) );

        String strPath = getTopicTitle( request, topic ) + I18nService.getLocalizedString( PROPERTY_PATH_MODIFY, request.getLocale( ) );
        page.setExtendedPathLabel( getExtendedPath( strPath, strPageName ) );

        return page;
    }

    /**
     * Modify a wikipage
     *
     * @param request
     *            The HTTP Request
     * @return The redirect URL
     * @throws UserNotSignedException
     *             If user not connected
     */
    @Action( ACTION_MODIFY_PAGE )
    public XPage doModifyPage( HttpServletRequest request ) throws UserNotSignedException
    {
        LuteceUser user = checkUser( request );

        String strPageName = request.getParameter( Constants.PARAMETER_PAGE_NAME );

        Topic topic = TopicHome.findByPrimaryKey( strPageName );

        if ( hasEditRole( request, topic ) )
        {
            String strPreviousVersionId = request.getParameter( Constants.PARAMETER_PREVIOUS_VERSION_ID );
            String strTopicId = request.getParameter( Constants.PARAMETER_TOPIC_ID );
            String strComment = request.getParameter( Constants.PARAMETER_MODIFICATION_COMMENT );
            String strViewRole = request.getParameter( Constants.PARAMETER_VIEW_ROLE );
            String strEditRole = request.getParameter( Constants.PARAMETER_EDIT_ROLE );
            int nPreviousVersionId = Integer.parseInt( strPreviousVersionId );
            int nTopicId = Integer.parseInt( strTopicId );
            TopicVersion topicVersion = new TopicVersion( );
            topicVersion.setIdTopic( nTopicId );
            topicVersion.setUserName( user.getName( ) );
            topicVersion.setEditComment( strComment );
            topicVersion.setIdTopicVersionPrevious( nPreviousVersionId );
            for ( String strLanguage : WikiLocaleService.getLanguages( ) )
            {
                String strPageTitle = request.getParameter( Constants.PARAMETER_PAGE_TITLE + "_" + strLanguage );
                String strContent = request.getParameter( Constants.PARAMETER_CONTENT + "_" + strLanguage );
                WikiContent content = new WikiContent( );
                content.setPageTitle( strPageTitle );
                content.setWikiContent( strContent );
                topicVersion.addLocalizedWikiContent( strLanguage, content );
            }

            TopicVersionHome.addTopicVersion( topicVersion );
            topic.setViewRole( strViewRole );
            topic.setEditRole( strEditRole );
            TopicHome.update( topic );
        }

        Map<String, String> mapParameters = new ConcurrentHashMap<String, String>( );
        mapParameters.put( Constants.PARAMETER_PAGE_NAME, strPageName );

        return redirect( request, VIEW_PAGE, mapParameters );
    }

    /**
     * Display the history page
     *
     * @param request
     *            The HTTP request
     * @return The XPage
     * @throws SiteMessageException
     *             if an error occurs
     */
    @View( VIEW_HISTORY )
    public XPage viewHistory( HttpServletRequest request ) throws SiteMessageException
    {
        String strPageName = request.getParameter( Constants.PARAMETER_PAGE_NAME );
        Topic topic = getTopic( request, strPageName, MODE_VIEW );
        Map<String, Object> model = getModel( );
        Collection<TopicVersion> listTopicVersions = TopicVersionHome.findAllVersions( topic.getIdTopic( ) );

        fillUsersData( listTopicVersions );
        model.put( MARK_LIST_TOPIC_VERSION, listTopicVersions );
        model.put( MARK_TOPIC, topic );
        model.put( MARK_EDIT_ROLE, hasEditRole( request, topic ) );
        model.put( MARK_ADMIN_ROLE, hasAdminRole( request ) );

        XPage page = getXPage( TEMPLATE_VIEW_HISTORY_WIKI, request.getLocale( ), model );
        page.setTitle( getPageTitle( getTopicTitle( request, topic ) ) );
        String strPath = getTopicTitle( request, topic ) + I18nService.getLocalizedString( PROPERTY_PATH_HISTORY, request.getLocale( ) );
        page.setExtendedPathLabel( getExtendedPath( strPath, strPageName ) );

        return page;
    }

    /**
     * Display the diff page
     *
     * @param request
     *            The HTTP request
     * @return The XPage
     * @throws SiteMessageException
     *             if an error occurs
     */
    @View( VIEW_DIFF )
    public XPage viewDiff( HttpServletRequest request ) throws SiteMessageException
    {
        String strPageName = request.getParameter( Constants.PARAMETER_PAGE_NAME );
        Topic topic = getTopic( request, strPageName, MODE_VIEW );
        String strNewVersion = request.getParameter( Constants.PARAMETER_NEW_VERSION );
        String strOldVersion = request.getParameter( Constants.PARAMETER_OLD_VERSION );
        int nNewTopicVersion = Integer.parseInt( strNewVersion );
        int nOldTopicVersion = Integer.parseInt( strOldVersion );

        XPage page = new XPage( );
        page.setContent( viewTopicDiff( request, strPageName, topic, nNewTopicVersion, nOldTopicVersion ) );
        page.setTitle( getPageTitle( getTopicTitle( request, topic ) ) );
        String strPath = getTopicTitle( request, topic ) + I18nService.getLocalizedString( PROPERTY_PATH_DIFF, request.getLocale( ) );
        page.setExtendedPathLabel( getExtendedPath( strPath, strPageName ) );

        return page;
    }

    /**
     * View Diff
     *
     * @param request
     *            The HTTP request
     * @param strPageName 
     *            The page name 
     * @param topic
     *            The topic
     * @param nNewTopicVersion
     *            The new version
     * @param nOldTopicVersion
     *            The old version
     * @return The page
     */
    private String viewTopicDiff( HttpServletRequest request, String strPageName, Topic topic, int nNewTopicVersion, int nOldTopicVersion )
    {
        int nPrevTopicVersion = ( nOldTopicVersion == 0 ) ? nNewTopicVersion : nOldTopicVersion;
        TopicVersion newTopicVersion = TopicVersionHome.findByPrimaryKey( nNewTopicVersion );
        TopicVersion oldTopicVersion = TopicVersionHome.findByPrimaryKey( nPrevTopicVersion );

        String strNewHtml = WikiService.instance( ).getWikiPage( strPageName, newTopicVersion, getLanguage( request ) );
        String strOldHtml = WikiService.instance( ).getWikiPage( strPageName, oldTopicVersion, getLanguage( request ) );
        String strDiff = DiffService.getDiff( strOldHtml, strNewHtml );

        Map<String, Object> model = getModel( );
        model.put( MARK_DIFF, strDiff );
        model.put( MARK_TOPIC, topic );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_VIEW_DIFF_TOPIC_WIKI, request.getLocale( ), model );

        return template.getHtml( );
    }

    /**
     * Delete a wikipage
     *
     * @param request
     *            The HTTP Request
     * @return The redirect URL
     * @throws UserNotSignedException
     *             if user not connected
     */
    @Action( ACTION_DELETE_PAGE )
    public XPage doDeletePage( HttpServletRequest request ) throws UserNotSignedException
    {
        checkUser( request );

        String strPageName = request.getParameter( Constants.PARAMETER_PAGE_NAME );
        Topic topic = TopicHome.findByPrimaryKey( strPageName );

        // Requires Admin role
        if ( hasAdminRole( request ) )
        {
            TopicHome.remove( topic.getIdTopic( ) );
        }

        return redirectWikiRoot( request );
    }

    /**
     * Upload an image
     * 
     * @param request
     *            The HTTP request
     * @return The XPage
     * @throws UserNotSignedException
     *             if the user is not signed
     */
    @Action( ACTION_UPLOAD_IMAGE )
    public XPage doUploadImage( HttpServletRequest request ) throws UserNotSignedException
    {
        checkUser( request );
        String strPageName = request.getParameter( Constants.PARAMETER_PAGE_NAME );
        String strName = request.getParameter( Constants.PARAMETER_IMAGE_NAME );
        String strTopicId = request.getParameter( Constants.PARAMETER_TOPIC_ID );
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        FileItem fileItem = multipartRequest.getFile( Constants.PARAMETER_IMAGE_FILE );
        Image image = new Image( );
        boolean bError = false;

        if ( ( fileItem == null ) || ( fileItem.getName( ) == null ) || "".equals( fileItem.getName( ) ) )
        {
            bError = true;
            addError( MESSAGE_FILE_MANDATORY, request.getLocale( ) );
        }

        if ( ( strName == null ) || strName.trim( ).equals( "" ) )
        {
            bError = true;
            addError( MESSAGE_NAME_MANDATORY, request.getLocale( ) );
        }

        if ( !bError )
        {
            image.setName( strName );
            image.setTopicId( Integer.parseInt( strTopicId ) );

            if ( ( fileItem != null ) && ( fileItem.getName( ) != null ) && !"".equals( fileItem.getName( ) ) )
            {
                image.setValue( fileItem.get( ) );
                image.setMimeType( fileItem.getContentType( ) );
            }
            else
            {
                image.setValue( null );
            }

            image.setWidth( 500 );
            image.setHeight( 500 );

            ImageHome.create( image );
        }

        Map<String, String> mapParameters = new ConcurrentHashMap<String, String>( );
        mapParameters.put( Constants.PARAMETER_PAGE_NAME, strPageName + ANCHOR_IMAGES );

        return redirect( request, VIEW_MODIFY_PAGE, mapParameters );
    }

    /**
     * Manages the removal form of a image whose identifier is in the http request
     *
     * @param request
     *            The Http request
     * @return the html code to confirm
     * @throws SiteMessageException A site message 
     */
    @Action( ACTION_CONFIRM_REMOVE_IMAGE )
    public XPage getConfirmRemoveImage( HttpServletRequest request ) throws SiteMessageException
    {
        int nId = Integer.parseInt( request.getParameter( Constants.PARAMETER_IMAGE_ID ) );
        UrlItem url = new UrlItem( AppPathService.getPortalUrl( ) );
        url.addParameter( Constants.PARAMETER_PAGE, Constants.PLUGIN_NAME );
        url.addParameter( Constants.PARAMETER_PAGE_NAME, request.getParameter( Constants.PARAMETER_PAGE_NAME ) );
        url.addParameter( Constants.PARAMETER_ACTION, ACTION_REMOVE_IMAGE );
        url.addParameter( Constants.PARAMETER_IMAGE_ID, nId );

        SiteMessageService.setMessage( request, MESSAGE_CONFIRM_REMOVE_IMAGE, SiteMessage.TYPE_CONFIRMATION, url.getUrl( ) );

        return null;
    }

    /**
     * Handles the removal form of a image
     *
     * @param request
     *            The Http request
     * @return the jsp URL to display the form to manage images
     * @throws UserNotSignedException
     *             if the user is not signed
     */
    @Action( ACTION_REMOVE_IMAGE )
    public XPage doRemoveImage( HttpServletRequest request ) throws UserNotSignedException
    {
        checkUser( request );
        int nId = Integer.parseInt( request.getParameter( Constants.PARAMETER_IMAGE_ID ) );
        ImageHome.remove( nId );
        addInfo( MESSAGE_IMAGE_REMOVED, getLocale( request ) );

        Map<String, String> mapParameters = new ConcurrentHashMap<String, String>( );
        mapParameters.put( Constants.PARAMETER_PAGE_NAME, request.getParameter( Constants.PARAMETER_PAGE_NAME ) + ANCHOR_IMAGES );

        return redirect( request, VIEW_MODIFY_PAGE, mapParameters );
    }

    /**
     * Manages the removal form of a image whose identifier is in the http request
     *
     * @param request
     *            The Http request
     * @return the html code to confirm
     * @throws SiteMessageException A site message 
     */
    @Action( ACTION_CONFIRM_REMOVE_VERSION )
    public XPage getConfirmRemoveVersion( HttpServletRequest request ) throws SiteMessageException
    {
        int nId = Integer.parseInt( request.getParameter( Constants.PARAMETER_TOPIC_VERSION_ID ) );
        UrlItem url = new UrlItem( AppPathService.getPortalUrl( ) );
        url.addParameter( Constants.PARAMETER_PAGE, Constants.PLUGIN_NAME );
        url.addParameter( Constants.PARAMETER_PAGE_NAME, request.getParameter( Constants.PARAMETER_PAGE_NAME ) );
        url.addParameter( Constants.PARAMETER_ACTION, ACTION_REMOVE_VERSION );
        url.addParameter( Constants.PARAMETER_TOPIC_VERSION_ID, nId );

        SiteMessageService.setMessage( request, MESSAGE_CONFIRM_REMOVE_VERSION, SiteMessage.TYPE_CONFIRMATION, url.getUrl( ) );

        return null;
    }

    /**
     * Handles the removal form of a image
     *
     * @param request
     *            The Http request
     * @return the jsp URL to display the form to manage images
     * @throws UserNotSignedException
     *             if the user is not signed
     */
    @Action( ACTION_REMOVE_VERSION )
    public XPage doRemoveVersion( HttpServletRequest request ) throws UserNotSignedException
    {
        checkUser( request );

        // requires admin role
        if ( hasAdminRole( request ) )
        {
            int nId = Integer.parseInt( request.getParameter( Constants.PARAMETER_TOPIC_VERSION_ID ) );
            TopicVersionHome.remove( nId );
            addInfo( MESSAGE_VERSION_REMOVED, getLocale( request ) );
        }

        Map<String, String> mapParameters = new ConcurrentHashMap<String, String>( );
        mapParameters.put( Constants.PARAMETER_PAGE_NAME, request.getParameter( Constants.PARAMETER_PAGE_NAME ) );

        return redirect( request, VIEW_HISTORY, mapParameters );
    }

    /**
     * Returns the image list as JSON
     * @param request The HTTP request
     * @return A JSON flow
     */
    @View( VIEW_LIST_IMAGES )
    public XPage viewListImages( HttpServletRequest request )
    {
        String strTopicId = request.getParameter( Constants.PARAMETER_TOPIC_ID );
        JSONArray array = new JSONArray( );

        if ( strTopicId != null )
        {
            int nTopicId = Integer.parseInt( strTopicId );
            List<Image> list = ImageHome.findByTopic( nTopicId );
            for ( Image image : list )
            {
                JSONObject jsonImage = new JSONObject( );
                jsonImage.accumulate( "id", image.getId( ) );
                jsonImage.accumulate( "name", image.getName( ) );
                array.add( jsonImage );
            }
        }
        return responseJSON( array.toString( ) );

    }

    /**
     * Change Language
     * 
     * @param request
     *            The HTTP request
     * @return The page
     * @throws UserNotSignedException
     */
    @Action( ACTION_CHANGE_LANGUAGE )
    public XPage doChangeLanguage( HttpServletRequest request ) throws UserNotSignedException
    {
        String strLanguage = request.getParameter( PARAMETER_LANGUAGE );
        String strPageName = request.getParameter( Constants.PARAMETER_PAGE_NAME );
        setLanguage( request, strLanguage );
        Map<String, String> mapParameters = new ConcurrentHashMap<String, String>( );
        mapParameters.put( Constants.PARAMETER_PAGE_NAME, strPageName );

        return redirect( request, VIEW_PAGE, mapParameters );

    }

    // /////////////////// Utils ////////////////////////////
    /**
     * Check the connected user
     *
     * @param request
     *            The HTTP request
     * @return The user
     * @throws UserNotSignedException
     *             if user not connected
     */
    private LuteceUser checkUser( HttpServletRequest request ) throws UserNotSignedException
    {
        LuteceUser user;

        if ( SecurityService.isAuthenticationEnable( ) )
        {
            user = SecurityService.getInstance( ).getRemoteUser( request );

            if ( user == null )
            {
                throw new UserNotSignedException( );
            }
        }
        else
        {
            user = new WikiAnonymousUser( );
        }

        return user;
    }

    /**
     * Gets the topic corresponding to the given page name
     *
     * @param request
     *            The HTTP request
     * @param strPageName
     *            The page name
     * @param nMode
     *            The mode VIEW vs EDIT
     * @return The topic
     * @throws SiteMessageException
     *             If an error occurs
     */
    private Topic getTopic( HttpServletRequest request, String strPageName, int nMode ) throws SiteMessageException
    {
        Topic topic = TopicHome.findByPrimaryKey( strPageName );

        if ( topic == null )
        {
            SiteMessageService.setMessage( request, Constants.MESSAGE_PAGE_NOT_EXISTS, SiteMessage.TYPE_STOP );
        }
        else
        {
            String strRole;
            String strMessage;
            switch( nMode )
            {
                case MODE_EDIT:
                    strRole = topic.getEditRole( );
                    strMessage = Constants.MESSAGE_USER_NOT_IN_ROLE;
                    break;
                case MODE_VIEW:
                default:
                    strRole = topic.getViewRole( );
                    strMessage = Constants.MESSAGE_USER_NOT_IN_ROLE;
                    break;
            }

            if ( SecurityService.isAuthenticationEnable( ) && ( !Page.ROLE_NONE.equals( strRole ) ) )
            {
                if ( !SecurityService.getInstance( ).isUserInRole( request, strRole ) )
                {
                    SiteMessageService.setMessage( request, strMessage, SiteMessage.TYPE_STOP );
                }
            }
        }

        return topic;
    }

    /**
     * Gets topics visible for a given user
     *
     * @param request
     *            The HTTP request
     * @return The list of topics
     */
    private List<Topic> getTopicsForUser( HttpServletRequest request )
    {
        Collection<Topic> listTopicAll = TopicHome.getTopicsList( );
        List<Topic> listTopic;

        if ( SecurityService.isAuthenticationEnable( ) )
        {
            listTopic = new ArrayList<Topic>( );

            for ( Topic topic : listTopicAll )
            {
                String strRole = topic.getViewRole( );

                if ( !Page.ROLE_NONE.equals( strRole ) )
                {
                    if ( SecurityService.getInstance( ).isUserInRole( request, strRole ) )
                    {
                        listTopic.add( topic );
                    }
                }
                else
                {
                    listTopic.add( topic );
                }
            }
        }
        else
        {
            listTopic = new ArrayList<Topic>( listTopicAll );
        }

        return listTopic;
    }

    /**
     * Build the page title
     *
     * @param strPageName
     *            The page name
     * @return The page title
     */
    private String getPageTitle( String strPageName )
    {
        StringBuilder sbPath = new StringBuilder( );
        sbPath.append( AppPropertiesService.getProperty( PROPERTY_PAGE_TITLE ) ).append( ' ' ).append( strPageName );

        return sbPath.toString( );
    }

    /**
     * Build the extended path
     *
     * @param strPageTitle
     *            The page title
     * @param strPageName
     *            The page name
     * @return The list that content path info
     */
    private ReferenceList getExtendedPath( String strPageTitle, String strPageName )
    {
        String strWikiRootLabel = DatastoreService.getDataValue( DSKEY_WIKI_ROOT_LABEL, AppPropertiesService.getProperty( PROPERTY_PAGE_PATH ) );
        String strWikiRootPageName = DatastoreService.getDataValue( DSKEY_WIKI_ROOT_PAGENAME, AppPropertiesService.getProperty( URL_DEFAULT ) );

        ReferenceList list = new ReferenceList( );
        if ( strPageName == null || !strPageName.equals( strWikiRootPageName ) )
        {
            String strWikiRootUrl = URL_VIEW_PAGE + strWikiRootPageName;
            list.addItem( strWikiRootLabel, strWikiRootUrl );
        }
        list.addItem( strPageTitle, "" );
        return list;
    }

    /**
     * Fill all versions with users infos
     * 
     * @param listVersions
     *            The version
     */
    private void fillUsersData( Collection<TopicVersion> listVersions )
    {
        for ( TopicVersion version : listVersions )
        {
            fillUserData( version );
        }
    }

    /**
     * Fill the version with users infos
     * 
     * @param version
     *            The version
     */
    private void fillUserData( TopicVersion version )
    {
        String strUserId = version.getLuteceUserId( );

        LuteceUser user = SecurityService.getInstance( ).getUser( strUserId );

        if ( user != null )
        {
            version.setUserName( user.getUserInfo( LuteceUser.NAME_GIVEN ) + " " + user.getUserInfo( LuteceUser.NAME_FAMILY ) );
            version.setUserAvatarUrl( AvatarService.getAvatarUrl( user.getEmail( ) ) );
        }
        else
        {
            version.setUserAvatarUrl( AvatarService.getAvatarUrl( strUserId ) );
        }

        version.setUserPseudo( UserPreferencesService.instance( ).getNickname( strUserId ) );
    }

    /**
     * Gets the current page URL from the request
     * 
     * @param request
     *            The request
     * @return The URL
     */
    private String getPageUrl( HttpServletRequest request )
    {
        return request.getRequestURI( ).substring( request.getContextPath( ).length( ) + 1 ) + "?" + request.getQueryString( );
    }

    /**
     * Checks if the user has the edit role for the given topic
     * 
     * @param request
     *            The request
     * @param topic
     *            The topic
     * @return true if he has otherwise false
     */
    private boolean hasEditRole( HttpServletRequest request, Topic topic )
    {
        if ( SecurityService.isAuthenticationEnable( ) )
        {
            if ( !Page.ROLE_NONE.equals( topic.getEditRole( ) ) )
            {
                return SecurityService.getInstance( ).isUserInRole( request, topic.getEditRole( ) );
            }
        }
        return true;
    }

    /**
     * Checks if the user has the admin role
     * 
     * @param request
     *            The HTTP request
     * @return true if he has otherwise false
     */
    private boolean hasAdminRole( HttpServletRequest request )
    {
        if ( SecurityService.isAuthenticationEnable( ) )
        {
            String strAdminRole = DatastoreService.getDataValue( DSKEY_ROLE_ADMIN, DEFAULT_ROLE_ADMIN );
            return SecurityService.getInstance( ).isUserInRole( request, strAdminRole );
        }
        return false;
    }

    /**
     * Redirect to wiki's root URL
     * 
     * @param request
     *            The HTTP request
     * @return The XPage
     */
    private XPage redirectWikiRoot( HttpServletRequest request )
    {
        String strWikiRootPageName = DatastoreService.getDataValue( DSKEY_WIKI_ROOT_PAGENAME, "" );
        if ( !"".equals( strWikiRootPageName ) )
        {
            Map<String, String> mapParameters = new ConcurrentHashMap<String, String>( );
            mapParameters.put( Constants.PARAMETER_PAGE_NAME, strWikiRootPageName );

            return redirect( request, VIEW_PAGE, mapParameters );
        }
        return redirectView( request, VIEW_LIST );
    }

    /**
     * Return if the plugin Extend is available
     * 
     * @return true if extend is installed and activated otherwise false
     */
    private boolean isExtend( )
    {
        Plugin plugin = PluginService.getPlugin( PLUGIN_EXTEND );
        return ( ( plugin != null ) && plugin.isInstalled( ) );
    }

    /**
     * Store the current selected language in the user's session
     * 
     * @param request
     *            The request
     * @param strLanguage
     *            The language
     */
    private void setLanguage( HttpServletRequest request, String strLanguage )
    {
        Locale userSelectedLocale = new Locale( strLanguage );
        if ( LocaleService.isSupported( userSelectedLocale ) ) LocaleService.setUserSelectedLocale( request, userSelectedLocale );
        
    }

    /**
     * Retrieve the current selected language from the user's session
     * 
     * @param request
     *            The request
     * @return The Language
     */
    private String getLanguage( HttpServletRequest request )
    {
        String strLanguage = null;
        HttpSession session = request.getSession( );
        
        if ( request.getParameter( PARAMETER_LANGUAGE) != null)
        {
            // consider the language parameter in the URL if exists
            strLanguage = request.getParameter( PARAMETER_LANGUAGE );
            setLanguage( request, strLanguage);
        }
        
        return LocaleService.getContextUserLocale( request ).getLanguage( );
    }

    /**
     * Return a topic title
     * 
     * @param topic
     *            The topic
     * @param strLanguage
     *            The language
     * @return The title
     */
    private String getTopicTitle( Topic topic, String strLanguage )
    {
        TopicVersion version = TopicVersionHome.findLastVersion( topic.getIdTopic( ) );
        if ( version !=null ) 
        {
            return version.getWikiContent( strLanguage ).getPageTitle( );
        }
        else
        {
            return topic.getPageName( );
        }
        
    }

    /**
     * Return a topic title
     * 
     * @param request
     *            The HTTP request
     * @param topic
     *            The topic
     * @return The title
     */
    private String getTopicTitle( HttpServletRequest request, Topic topic )
    {
        return getTopicTitle( topic, getLanguage( request ) );
    }
}
