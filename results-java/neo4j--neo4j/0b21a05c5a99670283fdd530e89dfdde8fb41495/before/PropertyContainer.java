package org.neo4j.api.core;

/*
 * Copyright (c) 2002-2008 "Neo Technology,"
 *     Network Engine for Objects in Lund AB [http://neotechnology.com]
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
public interface PropertyContainer
{

	// Properties
	/**
	 * Returns <code>true</code> if this node has a property accessible
	 * through the given key, <code>false</code> otherwise. If key is
	 * <code>null</code>, this method returns <code>false</code>.
	 * @param key the property key
	 * @return <code>true</code> if this node has a property accessible
	 * through the given key, <code>false</code> otherwise
	 */
	public boolean hasProperty( String key );

	/**
	 * Returns the property value associated with the given key. The value is of
	 * one of the valid property types, i.e. a Java primitive, a
	 * {@link String String} or an array of any of the valid types. If there's
	 * no property associated with <code>key</code> an unchecked exception is
	 * raised.
	 * @param key the property key
	 * @return the property value associated with the given key
	 * @throws NotFoundException if there's no property associated with
	 * <code>key</code>
	 */
	// TODO: change exception type
	public Object getProperty( String key );

	/**
	 * Returns the property value associated with the given key, or a default
	 * value. The value is of one of the valid property types, i.e. a Java
	 * primitive, a {@link String String} or an array of any of the valid types.
	 * If <code>defaultValue</code> is not of a supported type, an unchecked
	 * exception is raised.
	 * @param key the property key
	 * @param defaultValue the default value to return if no property value was
	 * associated with the given key
	 * @return the property value associated with the given key
	 */
	public Object getProperty( String key, Object defaultValue );

	/**
	 * Sets the property value for the given key to <code>value</code>. The
	 * property value must be one of the valid property types, i.e:
	 * <ul>
	 * <li><code>boolean</code> or <code>boolean[]</code></li>
	 * <li><code>byte</code> or <code>byte[]</code></li>
	 * <li><code>short</code> or <code>short[]</code></li>
	 * <li><code>int</code> or <code>int[]</code></li>
	 * <li><code>long</code> or <code>long[]</code></li>
	 * <li><code>float</code> or <code>float[]</code></li>
	 * <li><code>double</code> or <code>double[]</code></li>
	 * <li><code>char</code> or <code>char[]</code></li>
	 * <li><code>java.lang.String</code> or <code>String[]</code></li>
	 * </ul>
	 * <p>
	 * This means that <code>null</code> is not an accepted property value.
	 * @param key the key with which the new property value will be associated
	 * @param value the new property value, of one of the valid property types
	 * @throws IllegalArgumentException if <code>value</code> is of an
	 * unsupported type (including <code>null</code>)
	 */
	public void setProperty( String key, Object value );

	/**
	 * Removes and returns the property associated with the given key. If
	 * there's no property associated with the key, then <code>null</code> is
	 * returned.
	 * @param key  the property key
	 * @return the property value that used to be associated with the given key
	 */
	public Object removeProperty( String key );

	/**
	 * Returns all currently valid property keys, or an empty iterable if this
	 * node has no properties.
	 * @return all property keys
	 */
	// TODO: figure out concurrency semantics
	public Iterable<String> getPropertyKeys();

	/**
	 * Returns all currently valid property values, or an empty iterable if this
	 * node has no properties. All values are of a supported property type, i.e.
	 * a Java primitive, a {@link String String} or an array of any of the
	 * supported types.
	 * @return all property values
	 */
	// TODO: figure out concurrency semantics
	public Iterable<Object> getPropertyValues();

}