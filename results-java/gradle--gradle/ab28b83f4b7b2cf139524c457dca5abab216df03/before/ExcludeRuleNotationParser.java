/*
 * Copyright 2009 the original author or authors.
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

package org.gradle.api.internal.artifacts;

import org.gradle.api.InvalidUserDataException;
import org.gradle.api.artifacts.ExcludeRule;
import org.gradle.api.internal.notations.parsers.MapNotationParser;

import java.util.Collection;
import java.util.Map;

/**
 * @author Rene Groeschke
 */
public class ExcludeRuleNotationParser<T extends ExcludeRule> extends MapNotationParser<T> {

    @Override
    public void describe(Collection<String> candidateFormats) {
        candidateFormats.add("Maps, e.g. [group: 'org.gradle', module:'gradle-core'].");
    }

    @Override
    protected T parseMap(Map<String, Object> values) {
        checkValidExcludeRuleMap(values);       //TODO move this kind of checks up to mapNotationParser to allow "either/or" required properties
        ExcludeRule excluderule = new DefaultExcludeRule();
        return (T) excluderule;
    }

    void checkValidExcludeRuleMap(Map<String, Object> ruleMap) throws InvalidUserDataException {
        final Object module = ruleMap.get(ExcludeRule.MODULE_KEY);
        final Object group = ruleMap.get(ExcludeRule.GROUP_KEY);
        if ((group == null || group.toString().equals("")) && (module == null || module.toString().equals(""))) {
            throw new InvalidUserDataException(
                    "Invalid format: '" + ruleMap + "'. Group or Module must not be empty or null. Correct example: "
                            + "group:'org.gradle', module:'gradle-core'");
        }
        //TODO Should we get stricter here to refuse any other map keys?
    }
}