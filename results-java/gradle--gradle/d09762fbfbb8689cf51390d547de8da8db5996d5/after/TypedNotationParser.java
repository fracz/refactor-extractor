/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.notations.parsers;

import org.gradle.api.internal.notations.api.NotationParser;

/**
 * by Szczepan Faber, created at: 11/9/11
 */
public abstract class TypedNotationParser<N, T> implements NotationParser<T> {

    private final Class<N> typeToken;

    public TypedNotationParser(Class<N> typeToken) {
        assert typeToken != null : "typeToken cannot be null";
        this.typeToken = typeToken;
    }

    public boolean canParse(Object notation) {
        return typeToken.isAssignableFrom(notation.getClass());
    }

    public T parseNotation(Object notation) {
        assert canParse(notation) : "This parser only parses instances of " + typeToken.getName();
        return parseType(typeToken.cast(notation));
    }

    abstract public T parseType(N notation);
}