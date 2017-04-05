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
package org.hibernate.envers.query;

import org.hibernate.envers.configuration.AuditConfiguration;
import org.hibernate.envers.query.projection.AuditProjection;
import org.hibernate.envers.query.criteria.AuditCriterion;
import org.hibernate.envers.query.criteria.RevisionTypeAuditExpression;
import org.hibernate.envers.tools.Triple;
import org.hibernate.envers.RevisionType;

/**
 * @author Adam Warski (adam at warski dot org)
 */
@SuppressWarnings({"JavaDoc"})
public class RevisionTypeProperty implements AuditProjection {
    private RevisionTypeProperty() { }

    /**
     * Apply a "equal" constraint on the revision type
     */
    public static AuditCriterion eq(RevisionType type) {
        return new RevisionTypeAuditExpression(type, "=");
    }

    /**
     * Apply a "not equal" constraint on the revision type
     */
    public static AuditCriterion ne(RevisionType type) {
        return new RevisionTypeAuditExpression(type, "<>");
    }

    /**
     * Projection on the revision type
     */
    public static AuditProjection revisionType() {
        return new RevisionTypeProperty();
    }

    public Triple<String, String, Boolean> getData(AuditConfiguration verCfg) {
        return Triple.make(null, verCfg.getAuditEntCfg().getRevisionTypePropName(), false);
    }
}