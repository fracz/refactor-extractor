/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.server.security.enterprise.auth;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.CachingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Initializable;

import java.util.Collection;
import java.util.Map;

import org.neo4j.kernel.api.security.AuthSubject;
import org.neo4j.kernel.api.security.AuthenticationResult;
import org.neo4j.kernel.api.security.exception.InvalidAuthTokenException;

public class MultiRealmAuthManager implements EnterpriseAuthManager
{
    private final EnterpriseUserManager userManager;
    private final Collection<Realm> realms;
    private final SecurityManager securityManager;
    private final EhCacheManager cacheManager;

    public MultiRealmAuthManager( EnterpriseUserManager userManager, Collection<Realm> realms )
    {
        this.userManager = userManager;
        this.realms = realms;
        securityManager = new DefaultSecurityManager( realms );

        // TODO: This is a bit big for our current needs.
        // Maybe MemoryConstrainedCacheManager is good enough if we do not need timeToLiveSeconds?
        cacheManager = new EhCacheManager();
    }

    @Override
    public AuthSubject login( Map<String,Object> authToken ) throws InvalidAuthTokenException
    {
        Subject subject = new Subject.Builder( securityManager ).buildSubject();

        ShiroAuthToken token = new ShiroAuthToken( authToken );

        AuthenticationResult result = AuthenticationResult.FAILURE;

        try
        {
            subject.login( token );
            result = AuthenticationResult.SUCCESS;
        }
        catch ( UnsupportedTokenException e )
        {
            throw new InvalidAuthTokenException( e.getCause().getMessage() );
        }
        catch ( AuthenticationException e )
        {
            result = AuthenticationResult.FAILURE;
        }

        return new ShiroAuthSubject( this, subject, result );
    }

    @Override
    public void init() throws Throwable
    {
        cacheManager.init();

        for ( Realm realm : realms )
        {
            if ( realm instanceof Initializable )
            {
                ((Initializable) realm).init();
            }
            if ( realm instanceof CachingRealm )
            {
                ((CachingRealm) realm).setCacheManager( cacheManager );
            }
            if ( realm instanceof NeoLifecycleRealm )
            {
                ((NeoLifecycleRealm) realm).initialize();
            }
        }
    }

    @Override
    public void start() throws Throwable
    {
        for ( Realm realm : realms )
        {
            if ( realm instanceof NeoLifecycleRealm )
            {
                ((NeoLifecycleRealm) realm).start();
            }
        }
    }

    @Override
    public void stop() throws Throwable
    {
        for ( Realm realm : realms )
        {
            if ( realm instanceof NeoLifecycleRealm )
            {
                ((NeoLifecycleRealm) realm).stop();
            }
        }
    }

    @Override
    public void shutdown() throws Throwable
    {
        for ( Realm realm : realms )
        {
            if ( realm instanceof CachingRealm )
            {
                ((CachingRealm) realm).setCacheManager( null );
            }
            if ( realm instanceof NeoLifecycleRealm )
            {
                ((NeoLifecycleRealm) realm).shutdown();
            }
        }
        cacheManager.destroy();
    }

    @Override
    public EnterpriseUserManager getUserManager()
    {
        return userManager;
    }
}