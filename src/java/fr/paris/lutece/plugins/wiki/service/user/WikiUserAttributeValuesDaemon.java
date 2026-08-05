/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.wiki.service.user;

import fr.paris.lutece.portal.service.daemon.Daemon;

/**
 * Refreshes the values suggested for the searchable user attributes of the permission panel. Walking
 * the user directory takes seconds, so it happens here rather than on a page render.
 */
public class WikiUserAttributeValuesDaemon extends Daemon
{
    /**
     * {@inheritDoc}
     */
    @Override
    public void run( )
    {
        WikiUserAttributeValuesService.refresh( );
        setLastRunLogs( "Wiki user attribute values refreshed : " + WikiUserAttributeValuesService.countCombinations( ) + " combinations" );
    }
}
