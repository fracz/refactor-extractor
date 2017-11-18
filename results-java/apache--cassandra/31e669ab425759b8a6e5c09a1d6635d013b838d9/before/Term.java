/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cassandra.cql3;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.BytesType;
import org.apache.cassandra.db.marshal.MarshalException;
import org.apache.cassandra.exceptions.InvalidRequestException;

/** A term parsed from a CQL statement. */
public class Term
{
    private static final Logger logger = LoggerFactory.getLogger(Term.class);

    public enum Type
    {
        STRING, INTEGER, UUID, FLOAT, BOOLEAN, HEX, QMARK;

        static Type forInt(int type)
        {
            if (type == CqlParser.STRING_LITERAL)
                return STRING;
            else if (type == CqlParser.INTEGER)
                return INTEGER;
            else if (type == CqlParser.UUID)
                return UUID;
            else if (type == CqlParser.FLOAT)
                return FLOAT;
            else if (type == CqlParser.BOOLEAN)
                return BOOLEAN;
            else if (type == CqlParser.HEXNUMBER)
                return HEX;
            else if (type == CqlParser.QMARK)
                return QMARK;

            throw new AssertionError();
        }
    }

    private final String text;
    private final Type type;
    public final int bindIndex;
    public final boolean isToken;

    // For transition post-5198, see below
    private static volatile boolean stringAsBlobWarningLogged = false;

    // This is a hack for the timeuuid functions (minTimeuuid, maxTimeuuid, now) because instead of handling them as
    // true function we let the TimeUUID.fromString() method handle it. We should probably clean that up someday
    private final boolean skipTypeValidation;

    private Term(String text, Type type, int bindIndex, boolean isToken, boolean skipTypeValidation)
    {
        this.text = text;
        this.type = type;
        this.bindIndex = bindIndex;
        this.isToken = isToken;
        this.skipTypeValidation = skipTypeValidation;
    }

    public Term(String text, Type type, boolean skipTypeValidation)
    {
        this(text, type, -1, false, skipTypeValidation);
    }

    public Term(String text, Type type)
    {
        this(text, type, -1, false, false);
    }

    /**
     * Create new Term instance from a string, and an integer that corresponds
     * with the token ID from CQLParser.
     *
     * @param text the text representation of the term.
     * @param type the term's type as an integer token ID.
     */
    public Term(String text, int type)
    {
        this(text, Type.forInt(type));
    }

    public Term(long value, Type type)
    {
        this(String.valueOf(value), type);
    }

    public Term(String text, int type, int index)
    {
        this(text, Type.forInt(type), index, false, false);
    }

    public static Term tokenOf(Term t)
    {
        return new Term(t.text, t.type, t.bindIndex, true, false);
    }

    /**
     * Returns the text parsed to create this term.
     *
     * @return the string term acquired from a CQL statement.
     */
    public String getText()
    {
        return isToken ? "token(" + text + ")" : text;
    }

    /**
     * Returns the typed value, serialized to a ByteBuffer according to a
     * comparator/validator.
     *
     * @return a ByteBuffer of the value.
     * @throws InvalidRequestException if unable to coerce the string to its type.
     */
    public ByteBuffer getByteBuffer(AbstractType<?> validator, List<ByteBuffer> variables) throws InvalidRequestException
    {
        try
        {
            if (!isBindMarker())
            {
                // BytesType doesn't want it's input prefixed by '0x'.
                if (type == Type.HEX && validator instanceof BytesType)
                    return validator.fromString(text.substring(2));
                return validator.fromString(text);
            }

            // must be a marker term so check for a CqlBindValue stored in the term
            if (bindIndex == -1)
                throw new AssertionError("a marker Term was encountered with no index value");

            ByteBuffer value = variables.get(bindIndex);
            // We don't yet support null values in prepared statements
            if (value == null)
                throw new InvalidRequestException("Invalid null value for prepared variable " + bindIndex);
            validator.validate(value);
            return value;
        }
        catch (MarshalException e)
        {
            throw new InvalidRequestException(e.getMessage());
        }
    }

    public void validateType(String identifier, AbstractType<?> validator) throws InvalidRequestException
    {
        if (skipTypeValidation)
            return;

        Set<Type> supported = validator.supportedCQL3Constants();
        // Treat null specially as this mean "I don't have a supportedCQL3Type method"
        if (supported == null)
            return;

        if (!supported.contains(type))
        {
            // Blobs should now be inputed as hexadecimal constants. However, to allow people to upgrade, we still allow
            // blob-as-strings, even though it is deprecated (see #5198).
            if (type == Type.STRING && validator instanceof BytesType)
            {
                if (!stringAsBlobWarningLogged)
                {
                    stringAsBlobWarningLogged = true;
                    logger.warn("Inputing CLQ3 blobs as strings (like %s = '%s') is now deprecated and will be removed in a future version. "
                              + "You should convert client code to use a blob constant (%s = %s) instead (see http://cassandra.apache.org/doc/cql3/CQL.html changelog section for more info).",
                              identifier, text, identifier, "0x" + text);
                }
                return;
            }

            // TODO: Ideallly we'd keep the declared CQL3 type of columns and use that in the following message, instead of the AbstracType class name.
            throw new InvalidRequestException(String.format("Invalid %s constant for %s of type %s", type, identifier, validator.asCQL3Type()));
        }
    }

    /**
     * Obtain the term's type.
     *
     * @return the type
     */
    public Type getType()
    {
        return type;
    }

    public boolean isBindMarker()
    {
        return type == Type.QMARK;
    }

    public List<Term> asList()
    {
        return Collections.singletonList(this);
    }

    @Override
    public String toString()
    {
        return String.format("Term(%s, type=%s%s)", getText(), type, isToken ? ", isToken" : "");
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1 + (isToken ? 1 : 0);
        result = prime * result + ((text == null) ? 0 : text.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Term other = (Term) obj;
        if (type==Type.QMARK) return false; // markers are never equal
        if (text == null)
        {
            if (other.text != null)
                return false;
        } else if (!text.equals(other.text))
            return false;
        if (type != other.type)
            return false;
        if (isToken != other.isToken)
            return false;
        return true;
    }
}