/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.wiki.rs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import fr.paris.lutece.plugins.mylutece.service.search.MyLuteceSearchUser;
import fr.paris.lutece.plugins.wiki.business.item.AbstractWikiItem;
import fr.paris.lutece.plugins.wiki.business.permission.WikiItemUserPermission;
import fr.paris.lutece.plugins.wiki.service.WikiItemService;
import fr.paris.lutece.plugins.wiki.service.permission.WikiPermissionService;
import fr.paris.lutece.plugins.wiki.service.security.WikiAccessControlService;
import fr.paris.lutece.plugins.wiki.service.user.ExternalUserSearchService;
import fr.paris.lutece.plugins.wiki.service.user.WikiUserAttributeValuesService;
import fr.paris.lutece.plugins.wiki.service.user.WikiUserDisplayName;
import fr.paris.lutece.plugins.wiki.web.WikiItemManagementXPage;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.service.security.SecurityTokenService;
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.util.ReferenceList;

/**
 * REST endpoints backing the user permission panel of the item modification page: searching the
 * user directory and granting a permission to several users at once, without leaving the page.
 *
 * Both endpoints require an authenticated user holding edit rights on the target item, and a valid
 * security token.
 *
 * The item, the permission type and the token travel in the query string, because the platform
 * token service reads the token from the request parameters and JAX-RS has already consumed the
 * request body by then. The payload itself travels in a form encoded body, so a bulk grant is not
 * bound by the length of a URL. A token may only be validated once, so every response carries a
 * fresh one for the next call.
 */
@Path( "wiki/permission" )
public class WikiUserPermissionRest
{
    /**
     * Token action, shared with the item modification form that carries the token.
     */
    private static final String ACTION_TOKEN = WikiItemManagementXPage.ACTION_UPDATE_ITEM;

    private static final String PARAMETER_CODE = "code";
    private static final String PARAMETER_PERMISSION_TYPE = "permission_type";
    private static final String PARAMETER_USER_GUID = "user_guid";
    private static final String PARAMETER_SEARCH_LASTNAME = "search_lastname";
    private static final String PARAMETER_SEARCH_GIVENNAME = "search_givenname";
    private static final String PARAMETER_SEARCH_EMAIL = "search_email";
    private static final String PREFIX_PROVIDER_ATTRIBUTE = "provider_attribute_";
    private static final String PARAMETER_ATTRIBUTE = "attribute";
    private static final String PARAMETER_VALUE = "value";
    private static final String PARAMETER_LABEL = "label";
    private static final String PARAMETER_QUERY = "q";
    private static final int SUGGESTION_LIMIT = 15;

    /**
     * How many users a search renders. Beyond that, naming them one by one is the wrong tool: the
     * caller is describing a population, and a rule on an attribute expresses it without going stale.
     */
    private static final int RESULT_LIMIT = 50;

    private static final String ERROR_NOT_AVAILABLE = "User search service is not available";
    private static final String ERROR_UNKNOWN_ITEM = "Unknown wiki item";
    private static final String ERROR_BAD_PERMISSION = "Unsupported permission type";
    private static final String ERROR_INVALID_TOKEN = "Invalid security token";
    private static final String ERROR_NO_CRITERIA = "At least one search criterion is required";
    private static final String ERROR_UNKNOWN_ATTRIBUTE_VALUE = "The directory holds no such attribute value";

    @Context
    private HttpServletRequest _request;

    /**
     * Suggests the values an attribute takes in the user directory, among those still possible given
     * what the other search fields already hold. The directory holds thousands of combinations once
     * every organisation level is mapped, so they stay on the server and only the matching values
     * travel.
     *
     * Read only, so no security token is required: an authenticated user holding edit rights on the
     * target item is enough.
     *
     * @return the matching values
     */
    @GET
    @Path( "attribute-values" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response attributeValues( )
    {
        AbstractWikiItem item = WikiItemService.findByCode( _request.getParameter( PARAMETER_CODE ) );
        Response refusal = checkReadAccess( item );
        if ( refusal != null )
        {
            return refusal;
        }

        Map<String, String> mapContext = new HashMap<>( );
        for ( Map.Entry<String, String [ ]> entry : _request.getParameterMap( ).entrySet( ) )
        {
            if ( entry.getKey( ).startsWith( PREFIX_PROVIDER_ATTRIBUTE ) && entry.getValue( ).length > 0 )
            {
                mapContext.put( entry.getKey( ).substring( PREFIX_PROVIDER_ATTRIBUTE.length( ) ).trim( ), entry.getValue( ) [0] );
            }
        }

        List<String> listValues = WikiUserAttributeValuesService.suggest( _request.getParameter( PARAMETER_ATTRIBUTE ),
                _request.getParameter( PARAMETER_QUERY ), mapContext, SUGGESTION_LIMIT );

        return Response.ok( new SuggestionDto( listValues ) ).build( );
    }

    /**
     * Resolves an attribute value to its directory spelling and says how many agents carry it right
     * now, so the caller can show what a population rule would cover before granting it. The count
     * runs on the settled value only, never on half-typed text: a number that does not match what
     * the rule would grant is worse than none.
     *
     * Read only, so no security token is required, for the same reason as the suggestions.
     *
     * @return the directory spelling and the matching agent count, the spelling null when unknown
     */
    @GET
    @Path( "population-count" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response populationCount( )
    {
        AbstractWikiItem item = WikiItemService.findByCode( _request.getParameter( PARAMETER_CODE ) );
        Response refusal = checkReadAccess( item );
        if ( refusal != null )
        {
            return refusal;
        }

        String strAttribute = _request.getParameter( PARAMETER_ATTRIBUTE );
        String strCanonical = WikiUserAttributeValuesService.canonicalValue( strAttribute, _request.getParameter( PARAMETER_VALUE ) );

        if ( strCanonical == null )
        {
            return Response.ok( new PopulationCountDto( null, 0 ) ).build( );
        }

        ReferenceList listCriteria = new ReferenceList( );
        ReferenceItem criterion = new ReferenceItem( );
        criterion.setName( strAttribute.trim( ) );
        criterion.setCode( strCanonical );
        listCriteria.add( criterion );

        int nCount = 0;
        for ( MyLuteceSearchUser found : ExternalUserSearchService.getInstance( ).searchUsers( null, null, null, listCriteria ) )
        {
            if ( found.getProviderUserId( ) != null && !found.getProviderUserId( ).isBlank( ) )
            {
                nCount++;
            }
        }

        return Response.ok( new PopulationCountDto( strCanonical, nCount ) ).build( );
    }

    /**
     * Searches the user directory for candidates to grant a permission to.
     *
     * @return the matching users, each flagged when they already hold the permission
     */
    @POST
    @Path( "search" )
    @Consumes( MediaType.APPLICATION_FORM_URLENCODED )
    @Produces( MediaType.APPLICATION_JSON )
    public Response search( MultivaluedMap<String, String> form )
    {
        AbstractWikiItem item = WikiItemService.findByCode( _request.getParameter( PARAMETER_CODE ) );
        Response refusal = checkAccess( item );
        if ( refusal != null )
        {
            return refusal;
        }

        MultivaluedMap<String, String> payload = form != null ? form : new MultivaluedHashMap<>( );

        if ( !hasCriteria( payload ) )
        {
            return badRequest( ERROR_NO_CRITERIA );
        }

        String strPermissionType = _request.getParameter( PARAMETER_PERMISSION_TYPE );

        Set<String> setAlreadyGranted = new HashSet<>( );
        for ( WikiItemUserPermission permission : WikiPermissionService.getUsersWithPermission( item, strPermissionType ) )
        {
            setAlreadyGranted.add( permission.getUserGuid( ) );
        }

        List<MyLuteceSearchUser> listFound = ExternalUserSearchService.getInstance( ).searchUsers( payload.getFirst( PARAMETER_SEARCH_LASTNAME ),
                payload.getFirst( PARAMETER_SEARCH_GIVENNAME ), payload.getFirst( PARAMETER_SEARCH_EMAIL ), readProviderAttributes( payload ) );

        List<UserDto> listResults = new ArrayList<>( );
        int nTotal = 0;

        for ( MyLuteceSearchUser found : listFound )
        {
            if ( found.getProviderUserId( ) != null && !found.getProviderUserId( ).isBlank( ) )
            {
                nTotal++;
                if ( listResults.size( ) < RESULT_LIMIT )
                {
                    listResults.add( new UserDto( found, setAlreadyGranted.contains( found.getProviderUserId( ) ) ) );
                }
            }
        }

        return Response.ok( new SearchResultDto( listResults, nTotal, nextToken( ) ) ).build( );
    }

    /**
     * Grants a permission to every user of the request, ignoring those already holding it.
     *
     * @return how many users were granted, and how many were rejected
     */
    @POST
    @Path( "grant" )
    @Consumes( MediaType.APPLICATION_FORM_URLENCODED )
    @Produces( MediaType.APPLICATION_JSON )
    public Response grant( MultivaluedMap<String, String> form )
    {
        AbstractWikiItem item = WikiItemService.findByCode( _request.getParameter( PARAMETER_CODE ) );
        Response refusal = checkAccess( item );
        if ( refusal != null )
        {
            return refusal;
        }

        MultivaluedMap<String, String> payload = form != null ? form : new MultivaluedHashMap<>( );
        String strPermissionType = _request.getParameter( PARAMETER_PERMISSION_TYPE );

        List<String> listUserGuids = payload.get( PARAMETER_USER_GUID );
        if ( listUserGuids == null || listUserGuids.isEmpty( ) )
        {
            return Response.ok( new GrantResultDto( 0, 0, nextToken( ) ) ).build( );
        }

        int nGranted = 0;
        int nRejected = 0;
        Set<String> setRequested = new HashSet<>( );

        for ( String strUserGuid : new HashSet<>( listUserGuids ) )
        {
            if ( strUserGuid == null || strUserGuid.isBlank( ) )
            {
                nRejected++;
            }
            else
            {
                setRequested.add( strUserGuid );
            }
        }

        List<MyLuteceSearchUser> listResolved = ExternalUserSearchService.getInstance( ).getUsersByProviderUserIds( new ArrayList<>( setRequested ) );

        for ( MyLuteceSearchUser externalUser : listResolved )
        {
            String strUserGuid = externalUser.getProviderUserId( );
            if ( strUserGuid != null && setRequested.remove( strUserGuid ) )
            {
                WikiPermissionService.grant( item, strUserGuid, WikiUserDisplayName.of( externalUser ), strPermissionType );
                nGranted++;
            }
        }

        nRejected += setRequested.size( );

        return Response.ok( new GrantResultDto( nGranted, nRejected, nextToken( ) ) ).build( );
    }

    /**
     * Refuses the request unless an authenticated user holds edit rights on the target item, the
     * permission type is supported, the directory is reachable and the security token is valid.
     * Checks are ordered from the cheapest to the most expensive. The refusal travels as a plain
     * response: the platform REST layer maps any thrown exception to a 500.
     *
     * @param item
     *            the target item, resolved once by the caller, null when unknown
     * @return the response to return to the caller, null when the request may proceed
     */
    private Response checkAccess( AbstractWikiItem item )
    {
        LuteceUser user = SecurityService.getInstance( ).getRegisteredUser( _request );
        if ( user == null )
        {
            return Response.status( Response.Status.UNAUTHORIZED ).build( );
        }

        if ( !SecurityTokenService.getInstance( ).validate( _request, ACTION_TOKEN ) )
        {
            return Response.status( Response.Status.FORBIDDEN ).entity( new ErrorDto( ERROR_INVALID_TOKEN ) ).build( );
        }

        if ( !WikiPermissionService.isValidPermissionType( _request.getParameter( PARAMETER_PERMISSION_TYPE ) ) )
        {
            return badRequest( ERROR_BAD_PERMISSION );
        }

        if ( !ExternalUserSearchService.getInstance( ).isAvailable( ) )
        {
            return Response.status( Response.Status.SERVICE_UNAVAILABLE ).entity( new ErrorDto( ERROR_NOT_AVAILABLE ) ).build( );
        }

        return checkItemEditRights( user, item );
    }

    /**
     * Grants a permission to everyone carrying an attribute value, rather than to the users listed on
     * screen. Habilitating a whole direction one name at a time would freeze the day someone joins it.
     *
     * @return the rule applied, or a refusal when the directory holds no such value
     */
    @POST
    @Path( "grant-attribute" )
    @Consumes( MediaType.APPLICATION_FORM_URLENCODED )
    @Produces( MediaType.APPLICATION_JSON )
    public Response grantAttribute( MultivaluedMap<String, String> form )
    {
        AbstractWikiItem item = WikiItemService.findByCode( _request.getParameter( PARAMETER_CODE ) );
        Response refusal = checkAccess( item );
        if ( refusal != null )
        {
            return refusal;
        }

        MultivaluedMap<String, String> payload = form != null ? form : new MultivaluedHashMap<>( );
        String strAttribute = payload.getFirst( PARAMETER_ATTRIBUTE );
        String strValue = payload.getFirst( PARAMETER_VALUE );
        String strPermissionType = _request.getParameter( PARAMETER_PERMISSION_TYPE );

        boolean bGranted = WikiPermissionService.grantToAttribute( item, strAttribute, strValue, payload.getFirst( PARAMETER_LABEL ), strPermissionType );

        if ( !bGranted )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( new ErrorDto( ERROR_UNKNOWN_ATTRIBUTE_VALUE ) ).build( );
        }

        return Response.ok( new TokenDto( nextToken( ) ) ).build( );
    }

    /**
     * Revokes a permission granted to the holders of an attribute value.
     *
     * @return the outcome
     */
    @POST
    @Path( "revoke-attribute" )
    @Consumes( MediaType.APPLICATION_FORM_URLENCODED )
    @Produces( MediaType.APPLICATION_JSON )
    public Response revokeAttribute( MultivaluedMap<String, String> form )
    {
        AbstractWikiItem item = WikiItemService.findByCode( _request.getParameter( PARAMETER_CODE ) );
        Response refusal = checkAccess( item );
        if ( refusal != null )
        {
            return refusal;
        }

        MultivaluedMap<String, String> payload = form != null ? form : new MultivaluedHashMap<>( );
        WikiPermissionService.revokeFromAttribute( item, payload.getFirst( PARAMETER_ATTRIBUTE ), payload.getFirst( PARAMETER_VALUE ),
                _request.getParameter( PARAMETER_PERMISSION_TYPE ) );

        return Response.ok( new TokenDto( nextToken( ) ) ).build( );
    }

    /**
     * Refuses a read request unless an authenticated user holds edit rights on the target item. No
     * token is checked: the request changes nothing, and a one-shot token would break on the
     * repeated calls a suggestion list makes.
     *
     * @param item
     *            the target item, resolved once by the caller, null when unknown
     * @return the response to return to the caller, null when the request may proceed
     */
    private Response checkReadAccess( AbstractWikiItem item )
    {
        LuteceUser user = SecurityService.getInstance( ).getRegisteredUser( _request );

        if ( user == null )
        {
            return Response.status( Response.Status.UNAUTHORIZED ).build( );
        }

        return checkItemEditRights( user, item );
    }

    /**
     * Refuses the request unless the user holds edit rights on a known target item. Shared tail of
     * every guard, so tightening the rule is done once.
     *
     * @param user
     *            the authenticated user
     * @param item
     *            the target item, null when unknown
     * @return the response to return to the caller, null when the request may proceed
     */
    private Response checkItemEditRights( LuteceUser user, AbstractWikiItem item )
    {
        if ( item == null )
        {
            return badRequest( ERROR_UNKNOWN_ITEM );
        }

        if ( !WikiAccessControlService.canEdit( user, item ) )
        {
            return Response.status( Response.Status.FORBIDDEN ).build( );
        }

        return null;
    }

    /**
     * Tells whether the request carries at least one search criterion, whichever it is. A search
     * without any criterion would walk the whole directory, so it is refused.
     *
     * @param form
     *            the request body
     * @return true when at least one criterion is filled in
     */
    private boolean hasCriteria( MultivaluedMap<String, String> form )
    {
        for ( String strParameterName : form.keySet( ) )
        {
            boolean bIsCriterion = PARAMETER_SEARCH_LASTNAME.equals( strParameterName ) || PARAMETER_SEARCH_GIVENNAME.equals( strParameterName )
                    || PARAMETER_SEARCH_EMAIL.equals( strParameterName ) || strParameterName.startsWith( PREFIX_PROVIDER_ATTRIBUTE );

            String strValue = form.getFirst( strParameterName );
            if ( bIsCriterion && strValue != null && !strValue.isBlank( ) )
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Collects the dynamic provider attribute criteria carried by the body. Which attributes are
     * searchable depends on the authentication provider configuration, so they are read by prefix
     * rather than declared.
     *
     * @param form
     *            the request body
     * @return the criteria, empty when none was filled in
     */
    private ReferenceList readProviderAttributes( MultivaluedMap<String, String> form )
    {
        ReferenceList listProviderAttributes = new ReferenceList( );

        for ( String strParameterName : form.keySet( ) )
        {
            if ( !strParameterName.startsWith( PREFIX_PROVIDER_ATTRIBUTE ) )
            {
                continue;
            }

            String strValue = form.getFirst( strParameterName );
            if ( strValue != null && !strValue.isBlank( ) )
            {
                ReferenceItem providerAttribute = new ReferenceItem( );
                providerAttribute.setName( strParameterName.substring( PREFIX_PROVIDER_ATTRIBUTE.length( ) ).trim( ) );
                providerAttribute.setCode( strValue.trim( ) );
                listProviderAttributes.add( providerAttribute );
            }
        }

        return listProviderAttributes;
    }

    /**
     * Issues the token the caller must send with its next call, the previous one having been spent.
     *
     * @return a fresh token
     */
    private String nextToken( )
    {
        return SecurityTokenService.getInstance( ).getToken( _request, ACTION_TOKEN );
    }

    /**
     * Builds a bad request response carrying a message.
     *
     * @param strMessage
     *            the message
     * @return the response
     */
    private Response badRequest( String strMessage )
    {
        return Response.status( Response.Status.BAD_REQUEST ).entity( new ErrorDto( strMessage ) ).build( );
    }

    /**
     * One candidate user of a search result.
     */
    public static class UserDto
    {
        private final String _strGuid;
        private final String _strDisplayName;
        private final String _strLastName;
        private final String _strFirstName;
        private final String _strEmail;
        private final boolean _bAlreadyGranted;

        /**
         * Builds the row of a found user.
         *
         * @param user
         *            the user found in the directory
         * @param bAlreadyGranted
         *            whether that user already holds the permission
         */
        UserDto( MyLuteceSearchUser user, boolean bAlreadyGranted )
        {
            _strGuid = user.getProviderUserId( );
            _strDisplayName = WikiUserDisplayName.of( user );
            _strLastName = user.getLastName( );
            _strFirstName = user.getGivenName( );
            _strEmail = user.getEmail( );
            _bAlreadyGranted = bAlreadyGranted;
        }

        /**
         * Returns the user identifier.
         *
         * @return the guid
         */
        public String getGuid( )
        {
            return _strGuid;
        }

        /**
         * Returns the name to display.
         *
         * @return the display name
         */
        public String getDisplayName( )
        {
            return _strDisplayName;
        }

        /**
         * Returns the family name.
         *
         * @return the last name
         */
        public String getLastName( )
        {
            return _strLastName;
        }

        /**
         * Returns the given name.
         *
         * @return the first name
         */
        public String getFirstName( )
        {
            return _strFirstName;
        }

        /**
         * Returns the user email.
         *
         * @return the email
         */
        public String getEmail( )
        {
            return _strEmail;
        }

        /**
         * Tells whether the user already holds the permission.
         *
         * @return true when already granted
         */
        public boolean isAlreadyGranted( )
        {
            return _bAlreadyGranted;
        }
    }

    /**
     * A search result: every user the directory matched.
     */
    public static class SearchResultDto
    {
        private final List<UserDto> _listResults;
        private final int _nTotal;
        private final String _strNextToken;

        /**
         * Builds the search result.
         *
         * @param listResults
         *            the users rendered, capped so a large population does not freeze the page
         * @param nTotal
         *            how many users the directory matched, told apart from what is rendered so the
         *            caller knows a rule on the criterion covers more people than the rows shown
         * @param strNextToken
         *            the token to send with the next call
         */
        SearchResultDto( List<UserDto> listResults, int nTotal, String strNextToken )
        {
            _listResults = listResults;
            _nTotal = nTotal;
            _strNextToken = strNextToken;
        }

        /**
         * Returns how many users the directory matched.
         *
         * @return the total
         */
        public int getTotal( )
        {
            return _nTotal;
        }

        /**
         * Returns the token to send with the next call.
         *
         * @return the next token
         */
        public String getNextToken( )
        {
            return _strNextToken;
        }

        /**
         * Returns the rows returned.
         *
         * @return the results
         */
        public List<UserDto> getResults( )
        {
            return _listResults;
        }

    }

    /**
     * The outcome of a bulk grant.
     */
    public static class GrantResultDto
    {
        private final int _nGranted;
        private final int _nRejected;
        private final String _strNextToken;

        /**
         * Builds the grant outcome.
         *
         * @param nGranted
         *            how many users were granted
         * @param nRejected
         *            how many were rejected because they are unknown to the directory
         * @param strNextToken
         *            the token to send with the next call
         */
        GrantResultDto( int nGranted, int nRejected, String strNextToken )
        {
            _nGranted = nGranted;
            _nRejected = nRejected;
            _strNextToken = strNextToken;
        }

        /**
         * Returns the token to send with the next call.
         *
         * @return the next token
         */
        public String getNextToken( )
        {
            return _strNextToken;
        }

        /**
         * Returns how many users were granted.
         *
         * @return the granted count
         */
        public int getGranted( )
        {
            return _nGranted;
        }

        /**
         * Returns how many users were rejected.
         *
         * @return the rejected count
         */
        public int getRejected( )
        {
            return _nRejected;
        }
    }

    /**
     * The outcome of a call whose only payload is the token of the next call.
     */
    public static class TokenDto
    {
        private final String _strNextToken;

        /**
         * Builds the outcome.
         *
         * @param strNextToken
         *            the token to send with the next call
         */
        TokenDto( String strNextToken )
        {
            _strNextToken = strNextToken;
        }

        /**
         * Returns the token to send with the next call.
         *
         * @return the next token
         */
        public String getNextToken( )
        {
            return _strNextToken;
        }
    }

    /**
     * A population measured against the directory: the spelling the directory uses and how many
     * agents carry the value today.
     */
    public static class PopulationCountDto
    {
        private final String _strCanonical;
        private final int _nCount;

        /**
         * Builds the measure.
         *
         * @param strCanonical
         *            the directory spelling, null when the directory holds no such value
         * @param nCount
         *            how many agents carry the value
         */
        PopulationCountDto( String strCanonical, int nCount )
        {
            _strCanonical = strCanonical;
            _nCount = nCount;
        }

        /**
         * Returns the directory spelling of the value.
         *
         * @return the spelling, null when unknown
         */
        public String getCanonical( )
        {
            return _strCanonical;
        }

        /**
         * Returns how many agents carry the value.
         *
         * @return the count
         */
        public int getCount( )
        {
            return _nCount;
        }
    }

    /**
     * The values suggested for one attribute.
     */
    public static class SuggestionDto
    {
        private final List<String> _listValues;

        /**
         * Builds the suggestion.
         *
         * @param listValues
         *            the matching values
         */
        SuggestionDto( List<String> listValues )
        {
            _listValues = listValues;
        }

        /**
         * Returns the matching values.
         *
         * @return the values
         */
        public List<String> getValues( )
        {
            return _listValues;
        }
    }

    /**
     * An error message returned to the caller.
     */
    public static class ErrorDto
    {
        private final String _strMessage;

        /**
         * Builds the error.
         *
         * @param strMessage
         *            the message
         */
        ErrorDto( String strMessage )
        {
            _strMessage = strMessage;
        }

        /**
         * Returns the message.
         *
         * @return the message
         */
        public String getMessage( )
        {
            return _strMessage;
        }
    }
}
