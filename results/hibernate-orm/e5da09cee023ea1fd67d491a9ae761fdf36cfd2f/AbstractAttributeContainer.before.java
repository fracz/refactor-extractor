/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2010, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
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
package org.hibernate.metamodel.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Convenient base class for {@link AttributeContainer}.  Because in our model all
 * {@link AttributeContainer AttributeContainers} are also {@link Hierarchical} we also implement that here
 * as well.
 *
 * @author Steve Ebersole
 */
public abstract class AbstractAttributeContainer implements AttributeContainer, Hierarchical  {
	private final String name;
	private final Hierarchical superType;
	private LinkedHashSet<Attribute> attributeSet = new LinkedHashSet<Attribute>();
	private HashMap<String,Attribute> attributeMap = new HashMap<String,Attribute>();

	public AbstractAttributeContainer(String name, Hierarchical superType) {
		this.name = name;
		this.superType = superType;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Hierarchical getSuperType() {
		return superType;
	}

	@Override
	public Set<Attribute> getAttributes() {
		return Collections.unmodifiableSet( attributeSet );
	}

	@Override
	public Attribute getAttribute(String name) {
		return attributeMap.get( name );
	}

	@Override
	public SingularAttribute getOrCreateSingularAttribute(String name) {
		SingularAttribute attribute = (SingularAttribute) getAttribute( name );
		if ( attribute == null ) {
			attribute = new SingularAttributeImpl( name, this );
			addAttribute( attribute );
		}
		return attribute;
	}

	@Override
	public PluralAttribute getOrCreateBag(String name) {
		return getOrCreatePluralAttribute( name, PluralAttributeNature.BAG );
	}

	@Override
	public PluralAttribute getOrCreateSet(String name) {
		return getOrCreatePluralAttribute( name, PluralAttributeNature.SET );
	}

	@Override
	public IndexedPluralAttribute getOrCreateList(String name) {
		return (IndexedPluralAttribute) getOrCreatePluralAttribute( name, PluralAttributeNature.LIST );
	}

	@Override
	public IndexedPluralAttribute getOrCreateMap(String name) {
		return (IndexedPluralAttribute) getOrCreatePluralAttribute( name, PluralAttributeNature.MAP );
	}

	@Override
	public PluralAttribute getOrCreatePluralAttribute(String name, PluralAttributeNature nature) {
		PluralAttribute attribute = (PluralAttribute) getAttribute( name );
		if ( attribute == null ) {
			attribute = nature.isIndexed()
					? new IndexedPluralAttributeImpl( name, nature, this )
					: new PluralAttributeImpl( name, nature, this );
			addAttribute( attribute );
		}
		return attribute;
	}

	protected void addAttribute(Attribute attribute) {
		// todo : how to best "secure" this?
		if ( attributeMap.put( attribute.getName(), attribute ) != null ) {
			throw new IllegalArgumentException( "Attrtibute with name [" + attribute.getName() + "] already registered" );
		}
		attributeSet.add( attribute );
	}

	// todo : inner classes for now..

	public static class SingularAttributeImpl implements SingularAttribute {
		private final AttributeContainer attributeContainer;
		private final String name;

		public SingularAttributeImpl(String name, AttributeContainer attributeContainer) {
			this.name = name;
			this.attributeContainer = attributeContainer;
		}

		@Override
		public Type getSingularAttributeType() {
			return null;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public AttributeContainer getAttributeContainer() {
			return attributeContainer;
		}

		@Override
		public boolean isSingular() {
			return true;
		}
	}

	public static class PluralAttributeImpl implements PluralAttribute {
		private final AttributeContainer attributeContainer;
		private final PluralAttributeNature nature;
		private final String name;

		private Type elementType;

		public PluralAttributeImpl(String name,  PluralAttributeNature nature, AttributeContainer attributeContainer) {
			this.name = name;
			this.nature = nature;
			this.attributeContainer = attributeContainer;
		}

		@Override
		public AttributeContainer getAttributeContainer() {
			return attributeContainer;
		}

		@Override
		public boolean isSingular() {
			return false;
		}

		@Override
		public PluralAttributeNature getNature() {
			return nature;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Type getElementType() {
			return elementType;
		}

		@Override
		public void setElementType(Type elementType) {
			this.elementType = elementType;
		}
	}

	public static class IndexedPluralAttributeImpl extends PluralAttributeImpl implements IndexedPluralAttribute {
		private Type indexType;

		public IndexedPluralAttributeImpl(String name, PluralAttributeNature nature, AttributeContainer attributeContainer) {
			super( name, nature, attributeContainer );
		}

		@Override
		public Type getIndexType() {
			return indexType;
		}

		@Override
		public void setIndexType(Type indexType) {
			this.indexType = indexType;
		}
	}
}