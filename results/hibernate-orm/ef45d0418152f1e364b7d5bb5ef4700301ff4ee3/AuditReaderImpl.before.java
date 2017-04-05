/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2008, Red Hat Middleware LLC or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Middleware LLC.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.envers.reader;

import java.util.Date;
import java.util.List;
import javax.persistence.NoResultException;

import org.hibernate.envers.configuration.AuditConfiguration;
import org.hibernate.envers.exception.NotAuditedException;
import org.hibernate.envers.exception.RevisionDoesNotExistException;
import org.hibernate.envers.exception.AuditException;
import org.hibernate.envers.query.RevisionProperty;
import org.hibernate.envers.query.AuditRestrictions;
import static org.hibernate.envers.tools.ArgumentsTools.checkNotNull;
import static org.hibernate.envers.tools.ArgumentsTools.checkPositive;

import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.engine.SessionImplementor;
import org.jboss.envers.query.VersionsQueryCreator;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class AuditReaderImpl implements AuditReaderImplementor {
    private final AuditConfiguration verCfg;
    private final SessionImplementor sessionImplementor;
    private final Session session;
    private final FirstLevelCache firstLevelCache;

    public AuditReaderImpl(AuditConfiguration verCfg, Session session,
                              SessionImplementor sessionImplementor) {
        this.verCfg = verCfg;
        this.sessionImplementor = sessionImplementor;
        this.session = session;

        firstLevelCache = new FirstLevelCache();
    }

    private void checkSession() {
        if (!session.isOpen()) {
            throw new IllegalStateException("The associated entity manager is closed!");
        }
    }

    public SessionImplementor getSessionImplementor() {
        return sessionImplementor;
    }

    public Session getSession() {
        return session;
    }

    public FirstLevelCache getFirstLevelCache() {
        return firstLevelCache;
    }

    @SuppressWarnings({"unchecked"})
    public <T> T find(Class<T> cls, Object primaryKey, Number revision) throws
            IllegalArgumentException, NotAuditedException, IllegalStateException {
        checkNotNull(cls, "Entity class");
        checkNotNull(primaryKey, "Primary key");
        checkNotNull(revision, "Entity revision");
        checkPositive(revision, "Entity revision");
        checkSession();

        String entityName = cls.getName();

        if (!verCfg.getEntCfg().isVersioned(entityName)) {
            throw new NotAuditedException(entityName, entityName + " is not versioned!");
        }

        if (firstLevelCache.contains(entityName, revision, primaryKey)) {
            return (T) firstLevelCache.get(entityName, revision, primaryKey);
        }

        Object result;
        try {
            // The result is put into the cache by the entity instantiator called from the query
            result = createQuery().forEntitiesAtRevision(cls, revision)
                .add(AuditRestrictions.idEq(primaryKey)).getSingleResult();
        } catch (NoResultException e) {
            result = null;
        } catch (NonUniqueResultException e) {
            throw new AuditException(e);
        }

        return (T) result;
    }

    @SuppressWarnings({"unchecked"})
    public List<Number> getRevisions(Class<?> cls, Object primaryKey)
            throws IllegalArgumentException, NotAuditedException, IllegalStateException {
        // todo: if a class is not versioned from the beginning, there's a missing ADD rev - what then?
        checkNotNull(cls, "Entity class");
        checkNotNull(primaryKey, "Primary key");
        checkSession();

        String entityName = cls.getName();

        if (!verCfg.getEntCfg().isVersioned(entityName)) {
            throw new NotAuditedException(entityName, entityName + " is not versioned!");
        }

        return createQuery().forRevisionsOfEntity(cls, false, true)
                .addProjection(RevisionProperty.revisionNumber())
                .add(AuditRestrictions.idEq(primaryKey))
                .getResultList();
    }

    public Date getRevisionDate(Number revision) throws IllegalArgumentException, RevisionDoesNotExistException,
            IllegalStateException{
        checkNotNull(revision, "Entity revision");
        checkPositive(revision, "Entity revision");
        checkSession();

        Query query = verCfg.getRevisionInfoQueryCreator().getRevisionDateQuery(session, revision);

        try {
            Long timestamp = (Long) query.uniqueResult();
            if (timestamp == null) {
                throw new RevisionDoesNotExistException(revision);
            }

            return new Date(timestamp);
        } catch (NonUniqueResultException e) {
            throw new AuditException(e);
        }
    }

    public Number getRevisionNumberForDate(Date date) {
        checkNotNull(date, "Date of revision");
        checkSession();

        Query query = verCfg.getRevisionInfoQueryCreator().getRevisionNumberForDateQuery(session, date);

        try {
            Number res = (Number) query.uniqueResult();
            if (res == null) {
                throw new RevisionDoesNotExistException(date);
            }

            return res;
        } catch (NonUniqueResultException e) {
            throw new AuditException(e);
        }
    }

    @SuppressWarnings({"unchecked"})
    public <T> T findRevision(Class<T> revisionEntityClass, Number revision) throws IllegalArgumentException,
            RevisionDoesNotExistException, IllegalStateException {
        checkNotNull(revision, "Entity revision");
        checkPositive(revision, "Entity revision");
        checkSession();

        Query query = verCfg.getRevisionInfoQueryCreator().getRevisionQuery(session, revision);

        try {
            T revisionData = (T) query.uniqueResult();

            if (revisionData == null) {
                throw new RevisionDoesNotExistException(revision);
            }

            return revisionData;
        } catch (NonUniqueResultException e) {
            throw new AuditException(e);
        }
    }

    public VersionsQueryCreator createQuery() {
        return new VersionsQueryCreator(verCfg, this);
    }
}