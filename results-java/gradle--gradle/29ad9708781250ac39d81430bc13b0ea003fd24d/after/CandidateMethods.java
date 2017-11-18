/*
 * Copyright 2015 the original author or authors.
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

package org.gradle.model.internal.manage.schema.extract;

import com.google.common.base.Equivalence;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.*;
import org.gradle.api.Nullable;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

public class CandidateMethods {

    private final Map<String, Map<Equivalence.Wrapper<Method>, Collection<Method>>> candidates;

    public CandidateMethods(Map<String, Map<Equivalence.Wrapper<Method>, Collection<Method>>> candidates) {
        this.candidates = candidates;
    }

    /**
     * @return TRUE if no candidate methods, FALSE otherwise
     */
    public boolean isEmpty() {
        return candidates.isEmpty();
    }

    /**
     * @return All candidate methods names
     */
    public Iterable<String> methodNames() {
        return candidates.keySet();
    }

    /**
     * @return All candidate methods indexed by signature equivalence
     */
    public Map<Equivalence.Wrapper<Method>, Collection<Method>> allMethods() {
        ImmutableMap.Builder<Equivalence.Wrapper<Method>, Collection<Method>> builder = ImmutableMap.builder();
        for (Map<Equivalence.Wrapper<Method>, Collection<Method>> eachMethodName : candidates.values()) {
            builder.putAll(eachMethodName);
        }
        return builder.build();
    }

    /**
     * @param methodName Method name
     * @return Candidate methods named {@literal methodName} or {@literal null} if none
     */
    @Nullable
    public Map<Equivalence.Wrapper<Method>, Collection<Method>> methodsNamed(String methodName) {
        if (candidates.containsKey(methodName)) {
            return candidates.get(methodName);
        }
        return null;
    }

    /**
     * @param methodName Method name
     * @return Overriden candidate methods named {@literal methodName} indexed by signature equivalence or
     * {@literal null} if none
     */
    @Nullable
    public Map<Equivalence.Wrapper<Method>, Collection<Method>> overridenMethodsNamed(String methodName) {
        if (candidates.containsKey(methodName)) {
            Map<Equivalence.Wrapper<Method>, Collection<Method>> result = Maps.filterValues(candidates.get(methodName), new Predicate<Collection<Method>>() {
                @Override
                public boolean apply(Collection<Method> equivalentMethods) {
                    return equivalentMethods.size() > 1;
                }
            });
            if (!result.isEmpty()) {
                return result;
            }
        }
        return null;
    }

    /**
     * @param methodName Method name
     * @return Overloaded candidate methods named {@literal methodName} indexed by signature equivalence or
     * {@literal null} if none
     */
    @Nullable
    public Map<Equivalence.Wrapper<Method>, Collection<Method>> overloadedMethodsNamed(String methodName) {
        if (candidates.containsKey(methodName)) {
            Map<Equivalence.Wrapper<Method>, Collection<Method>> overloadeds = candidates.get(methodName);
            if (overloadeds.size() > 1) {
                return overloadeds;
            }
        }
        return null;
    }

    /**
     * @param methodName Method name
     * @param excludes Signature equivalences to exclude from the returned index
     * @return Overloaded candidate methods named {@literal methodName} indexed by signature equivalence except thoses
     * matching any of the signature equivalence provided in {@literal excludes} or {@literal null} if none
     */
    @Nullable
    public Map<Equivalence.Wrapper<Method>, Collection<Method>> overloadedMethodsNamed(String methodName, Collection<Equivalence.Wrapper<Method>> excludes) {
        Map<Equivalence.Wrapper<Method>, Collection<Method>> allOverloaded = overloadedMethodsNamed(methodName);
        if (allOverloaded != null) {
            Map<Equivalence.Wrapper<Method>, Collection<Method>> filteredOverloaded = Maps.filterKeys(allOverloaded, Predicates.not(Predicates.in(excludes)));
            if (!filteredOverloaded.isEmpty()) {
                return filteredOverloaded;
            }
        }
        return null;
    }
}