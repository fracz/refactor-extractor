/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2011, Red Hat Inc. or third-party contributors as
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
package org.hibernate.metamodel.source.annotations.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.AccessType;

import com.fasterxml.classmate.ResolvedTypeWithMembers;
import com.fasterxml.classmate.members.HierarchicType;
import com.fasterxml.classmate.members.ResolvedMember;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;

import org.hibernate.AnnotationException;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.metamodel.source.annotations.JPADotNames;
import org.hibernate.metamodel.source.annotations.util.JandexHelper;
import org.hibernate.metamodel.source.annotations.util.ReflectionHelper;

/**
 * Base class for a configured entity, mapped super class or embeddable
 *
 * @author Hardy Ferentschik
 */
public class ConfiguredClass {
	/**
	 * The Jandex class info for this configured class. Provides access to the annotation defined on this configured class.
	 */
	private final ClassInfo classInfo;

	/**
	 * The actual java type.
	 */
	private final Class<?> clazz;

	/**
	 * The default access type for this entity
	 */
	private final AccessType classAccessType;

	/**
	 * The type of configured class, entity, mapped super class, embeddable, ...
	 */
	private final ConfiguredClassType configuredClassType;

	/**
	 * The attribute overrides defined on this entity
	 */
	private final List<AnnotationInstance> attributeOverrides;

	/**
	 * The association overrides defined on this entity;
	 */
	private final List<AnnotationInstance> associationOverrides;

	/**
	 * The mapped attributes for entity
	 */
	private final Map<String, MappedAttribute> mappedAttributes;

	/**
	 * The embedded classes for this entity
	 */
	private final Map<String, EmbeddedClass> embeddedClasses = new HashMap<String, EmbeddedClass>();

	private final Set<String> transientFieldNames = new HashSet<String>();
	private final Set<String> transientMethodNames = new HashSet<String>();

	private final AnnotationBindingContext context;

	public ConfiguredClass(ClassInfo classInfo,
						   AccessType defaultAccessType,
						   ResolvedTypeWithMembers resolvedType,
						   AnnotationBindingContext context) {
		this.context = context;
		this.classInfo = classInfo;
		this.clazz = context.classLoaderService().classForName( classInfo.toString() );
		this.configuredClassType = determineType();
		this.classAccessType = determineClassAccessType( defaultAccessType );

		this.attributeOverrides = findAttributeOverrides();
		this.associationOverrides = findAssociationOverrides();

		// find transient field and method names
		findTransientFieldAndMethodNames();

		List<MappedAttribute> simpleProps = collectAttributes( resolvedType );
		// make sure the properties are ordered by property name
		Collections.sort( simpleProps );
		Map<String, MappedAttribute> tmpMap = new LinkedHashMap<String, MappedAttribute>();
		for ( MappedAttribute property : simpleProps ) {
			tmpMap.put( property.getName(), property );
		}
		this.mappedAttributes = Collections.unmodifiableMap( tmpMap );
	}

	public String getName() {
		return clazz.getName();
	}

	public Class<?> getConfiguredClass() {
		return clazz;
	}

	public ClassInfo getClassInfo() {
		return classInfo;
	}

	public ConfiguredClassType getConfiguredClassType() {
		return configuredClassType;
	}

	public Iterable<MappedAttribute> getMappedAttributes() {
		return mappedAttributes.values();
	}

	public Iterable<EmbeddedClass> getEmbeddedClasses() {
		return embeddedClasses.values();
	}

	public MappedAttribute getMappedProperty(String propertyName) {
		return mappedAttributes.get( propertyName );
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "ConfiguredClass" );
		sb.append( "{clazz=" ).append( clazz );
		sb.append( ", classAccessType=" ).append( classAccessType );
		sb.append( ", configuredClassType=" ).append( configuredClassType );
		sb.append( ", mappedAttributes=" ).append( mappedAttributes );
		sb.append( ", transientFieldNames=" ).append( transientFieldNames );
		sb.append( ", transientMethodNames=" ).append( transientMethodNames );
		sb.append( '}' );
		return sb.toString();
	}

	private ConfiguredClassType determineType() {
		if ( classInfo.annotations().containsKey( JPADotNames.ENTITY ) ) {
			return ConfiguredClassType.ENTITY;
		}
		else if ( classInfo.annotations().containsKey( JPADotNames.MAPPED_SUPERCLASS ) ) {
			return ConfiguredClassType.MAPPED_SUPERCLASS;
		}
		else if ( classInfo.annotations().containsKey( JPADotNames.EMBEDDABLE ) ) {
			return ConfiguredClassType.EMBEDDABLE;
		}
		else {
			return ConfiguredClassType.NON_ENTITY;
		}
	}

	private AccessType determineClassAccessType(AccessType defaultAccessType) {
		// default to the hierarchy access type to start with
		AccessType accessType = defaultAccessType;

		AnnotationInstance accessAnnotation = JandexHelper.getSingleAnnotation( classInfo, JPADotNames.ACCESS );
		if ( accessAnnotation != null ) {
			accessType = JandexHelper.getValueAsEnum( accessAnnotation, "value", AccessType.class );
		}

		return accessType;
	}

	/**
	 * @param resolvedTypes the resolved types for the field/properties of this class
	 *
	 * @return A list of the persistent properties of this configured class
	 */
	private List<MappedAttribute> collectAttributes(ResolvedTypeWithMembers resolvedTypes) {
		// use the class mate library to generic types
		ResolvedTypeWithMembers resolvedType = null;
		for ( HierarchicType hierarchicType : resolvedTypes.allTypesAndOverrides() ) {
			if ( hierarchicType.getType().getErasedType().equals( clazz ) ) {
				resolvedType = ReflectionHelper.resolveMemberTypes( hierarchicType.getType() );
				break;
			}
		}

		if ( resolvedType == null ) {
			throw new AssertionFailure( "Unable to resolve types for " + clazz.getName() );
		}

		List<MappedAttribute> properties = new ArrayList<MappedAttribute>();
		Set<String> explicitlyConfiguredMemberNames = createExplicitlyConfiguredAccessProperties(
				properties, resolvedType
		);

		if ( AccessType.FIELD.equals( classAccessType ) ) {
			Field fields[] = clazz.getDeclaredFields();
			Field.setAccessible( fields, true );
			for ( Field field : fields ) {
				if ( isPersistentMember( transientFieldNames, explicitlyConfiguredMemberNames, field ) ) {
					properties.add( createMappedProperty( field, resolvedType ) );
				}
			}
		}
		else {
			Method[] methods = clazz.getDeclaredMethods();
			Method.setAccessible( methods, true );
			for ( Method method : methods ) {
				if ( isPersistentMember( transientMethodNames, explicitlyConfiguredMemberNames, method ) ) {
					properties.add( createMappedProperty( method, resolvedType ) );
				}
			}
		}
		return properties;
	}

	private boolean isPersistentMember(Set<String> transientNames, Set<String> explicitlyConfiguredMemberNames, Member member) {
		if ( !ReflectionHelper.isProperty( member ) ) {
			return false;
		}

		if ( transientNames.contains( member.getName() ) ) {
			return false;
		}

		if ( explicitlyConfiguredMemberNames.contains( member.getName() ) ) {
			return false;
		}

		return true;
	}

	/**
	 * Creates {@code MappedProperty} instances for the explicitly configured persistent properties
	 *
	 * @param mappedProperties list to which to add the explicitly configured mapped properties
	 * @param resolvedMembers the resolved type parameters for this class
	 *
	 * @return the property names of the explicitly configured class names in a set
	 */
	private Set<String> createExplicitlyConfiguredAccessProperties(List<MappedAttribute> mappedProperties, ResolvedTypeWithMembers resolvedMembers) {
		Set<String> explicitAccessMembers = new HashSet<String>();

		List<AnnotationInstance> accessAnnotations = classInfo.annotations().get( JPADotNames.ACCESS );
		if ( accessAnnotations == null ) {
			return explicitAccessMembers;
		}

		// iterate over all @Access annotations defined on the current class
		for ( AnnotationInstance accessAnnotation : accessAnnotations ) {
			// we are only interested at annotations defined on fields and methods
			AnnotationTarget annotationTarget = accessAnnotation.target();
			if ( !( annotationTarget.getClass().equals( MethodInfo.class ) || annotationTarget.getClass()
					.equals( FieldInfo.class ) ) ) {
				continue;
			}

			AccessType accessType = JandexHelper.getValueAsEnum( accessAnnotation, "value", AccessType.class );

			// when class access type is field
			// overriding access annotations must be placed on properties and have the access type PROPERTY
			if ( AccessType.FIELD.equals( classAccessType ) ) {
				if ( !( annotationTarget instanceof MethodInfo ) ) {
					// todo log warning !?
					continue;
				}

				if ( !AccessType.PROPERTY.equals( accessType ) ) {
					// todo log warning !?
					continue;
				}
			}

			// when class access type is property
			// overriding access annotations must be placed on fields and have the access type FIELD
			if ( AccessType.PROPERTY.equals( classAccessType ) ) {
				if ( !( annotationTarget instanceof FieldInfo ) ) {
					// todo log warning !?
					continue;
				}

				if ( !AccessType.FIELD.equals( accessType ) ) {
					// todo log warning !?
					continue;
				}
			}

			// the placement is correct, get the member
			Member member;
			if ( annotationTarget instanceof MethodInfo ) {
				Method m;
				try {
					m = clazz.getMethod( ( (MethodInfo) annotationTarget ).name() );
				}
				catch ( NoSuchMethodException e ) {
					throw new HibernateException(
							"Unable to load method "
									+ ( (MethodInfo) annotationTarget ).name()
									+ " of class " + clazz.getName()
					);
				}
				member = m;
			}
			else {
				Field f;
				try {
					f = clazz.getField( ( (FieldInfo) annotationTarget ).name() );
				}
				catch ( NoSuchFieldException e ) {
					throw new HibernateException(
							"Unable to load field "
									+ ( (FieldInfo) annotationTarget ).name()
									+ " of class " + clazz.getName()
					);
				}
				member = f;
			}
			if ( ReflectionHelper.isProperty( member ) ) {
				mappedProperties.add( createMappedProperty( member, resolvedMembers ) );
				explicitAccessMembers.add( member.getName() );
			}
		}
		return explicitAccessMembers;
	}

	private MappedAttribute createMappedProperty(Member member, ResolvedTypeWithMembers resolvedType) {
		final String attributeName = ReflectionHelper.getPropertyName( member );
		ResolvedMember[] resolvedMembers;
		if ( member instanceof Field ) {
			resolvedMembers = resolvedType.getMemberFields();
		}
		else {
			resolvedMembers = resolvedType.getMemberMethods();
		}
		final Class<?> type = (Class<?>) findResolvedType( member.getName(), resolvedMembers );
		final Map<DotName, List<AnnotationInstance>> annotations = JandexHelper.getMemberAnnotations(
				classInfo, member.getName()
		);

		MappedAttribute attribute;
		AttributeType attributeType = determineAttributeType( annotations );
		switch ( attributeType ) {
			case BASIC: {
				attribute = SimpleAttribute.createSimpleAttribute( attributeName, type.getName(), annotations );
				break;
			}
			case EMBEDDED: {
				ClassInfo embeddableClassInfo = context.getClassInfo( type.getName() );
				if ( classInfo == null ) {
					String msg = String.format(
							"Attribute %s of entity %s is annotated with @Embedded, but no embeddable configuration for type %s can be found.",
							attributeName,
							getName(),
							type.getName()
					);
					throw new AnnotationException( msg );
				}

				EmbeddedClass embeddedClass = new EmbeddedClass(
						embeddableClassInfo,
						classAccessType,
						context.resolveType( type.getName() ),
						attributeOverrides,
						associationOverrides,
						context
				);

				embeddedClasses.put( attributeName, embeddedClass );
			}
			// TODO handle the different association types
			default: {
				attribute = AssociationAttribute.createAssociationAttribute(
						attributeName, ( (Class) type ).getName(), attributeType, annotations
				);
			}
		}

		return attribute;
	}

	/**
	 * Given the annotations defined on a persistent attribute this methods determines the attribute type.
	 *
	 * @param annotations the annotations defined on the persistent attribute
	 *
	 * @return an instance of the {@code AttributeType} enum
	 */
	private AttributeType determineAttributeType(Map<DotName, List<AnnotationInstance>> annotations) {
		EnumMap<AttributeType, AnnotationInstance> discoveredAttributeTypes =
				new EnumMap<AttributeType, AnnotationInstance>( AttributeType.class );

		AnnotationInstance oneToOne = JandexHelper.getSingleAnnotation( annotations, JPADotNames.ONE_TO_ONE );
		if ( oneToOne != null ) {
			discoveredAttributeTypes.put( AttributeType.ONE_TO_ONE, oneToOne );
		}

		AnnotationInstance oneToMany = JandexHelper.getSingleAnnotation( annotations, JPADotNames.ONE_TO_MANY );
		if ( oneToMany != null ) {
			discoveredAttributeTypes.put( AttributeType.ONE_TO_MANY, oneToMany );
		}

		AnnotationInstance manyToOne = JandexHelper.getSingleAnnotation( annotations, JPADotNames.MANY_TO_ONE );
		if ( manyToOne != null ) {
			discoveredAttributeTypes.put( AttributeType.MANY_TO_ONE, manyToOne );
		}

		AnnotationInstance manyToMany = JandexHelper.getSingleAnnotation( annotations, JPADotNames.MANY_TO_MANY );
		if ( manyToMany != null ) {
			discoveredAttributeTypes.put( AttributeType.MANY_TO_MANY, manyToMany );
		}

		AnnotationInstance embedded = JandexHelper.getSingleAnnotation( annotations, JPADotNames.EMBEDDED );
		if ( embedded != null ) {
			discoveredAttributeTypes.put( AttributeType.EMBEDDED, embedded );
		}

		if ( discoveredAttributeTypes.size() == 0 ) {
			return AttributeType.BASIC;
		}
		else if ( discoveredAttributeTypes.size() == 1 ) {
			return discoveredAttributeTypes.keySet().iterator().next();
		}
		else {
			throw new AnnotationException( "More than one association type configured for property  " + getName() + " of class " + getName() );
		}
	}

	private Type findResolvedType(String name, ResolvedMember[] resolvedMembers) {
		for ( ResolvedMember resolvedMember : resolvedMembers ) {
			if ( resolvedMember.getName().equals( name ) ) {
				return resolvedMember.getType().getErasedType();
			}
		}
		// todo - what to do here
		return null;
	}

	/**
	 * Populates the sets of transient field and method names.
	 */
	private void findTransientFieldAndMethodNames() {
		List<AnnotationInstance> transientMembers = classInfo.annotations().get( JPADotNames.TRANSIENT );
		if ( transientMembers == null ) {
			return;
		}

		for ( AnnotationInstance transientMember : transientMembers ) {
			AnnotationTarget target = transientMember.target();
			if ( target instanceof FieldInfo ) {
				transientFieldNames.add( ( (FieldInfo) target ).name() );
			}
			else {
				transientMethodNames.add( ( (MethodInfo) target ).name() );
			}
		}
	}

	private List<AnnotationInstance> findAttributeOverrides() {
		List<AnnotationInstance> attributeOverrideList = new ArrayList<AnnotationInstance>();

		AnnotationInstance attributeOverrideAnnotation = JandexHelper.getSingleAnnotation(
				classInfo,
				JPADotNames.ATTRIBUTE_OVERRIDE
		);
		if ( attributeOverrideAnnotation != null ) {
			attributeOverrideList.add( attributeOverrideAnnotation );
		}

		AnnotationInstance attributeOverridesAnnotation = JandexHelper.getSingleAnnotation(
				classInfo,
				JPADotNames.ATTRIBUTE_OVERRIDES
		);
		if ( attributeOverrideAnnotation != null ) {
			AnnotationInstance[] attributeOverride = attributeOverridesAnnotation.value().asNestedArray();
			Collections.addAll( attributeOverrideList, attributeOverride );
		}

		return attributeOverrideList;
	}

	private List<AnnotationInstance> findAssociationOverrides() {
		List<AnnotationInstance> associationOverrideList = new ArrayList<AnnotationInstance>();

		AnnotationInstance associationOverrideAnnotation = JandexHelper.getSingleAnnotation(
				classInfo,
				JPADotNames.ASSOCIATION_OVERRIDE
		);
		if ( associationOverrideAnnotation != null ) {
			associationOverrideList.add( associationOverrideAnnotation );
		}

		AnnotationInstance associationOverridesAnnotation = JandexHelper.getSingleAnnotation(
				classInfo,
				JPADotNames.ASSOCIATION_OVERRIDES
		);
		if ( associationOverrideAnnotation != null ) {
			AnnotationInstance[] attributeOverride = associationOverridesAnnotation.value().asNestedArray();
			Collections.addAll( associationOverrideList, attributeOverride );
		}

		return associationOverrideList;
	}
}