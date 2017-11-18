/*
 * Copyright 2014 the original author or authors.
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

package org.gradle.api.internal.artifacts.ivyservice.resolutionstrategy;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.InvalidActionClosureException;
import org.gradle.api.InvalidUserCodeException;
import org.gradle.api.RuleAction;
import org.gradle.api.artifacts.ComponentMetadata;
import org.gradle.api.artifacts.ComponentSelection;
import org.gradle.api.artifacts.ComponentSelectionRules;
import org.gradle.api.artifacts.ivy.IvyModuleDescriptor;
import org.gradle.api.internal.ClosureBackedRuleAction;
import org.gradle.api.internal.NoInputsRuleAction;
import org.gradle.api.internal.artifacts.ComponentSelectionRulesInternal;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class DefaultComponentSelectionRules implements ComponentSelectionRulesInternal {
    final Set<RuleAction<? super ComponentSelection>> rules = Sets.newLinkedHashSet();

    private final static List<Class<?>> VALID_INPUT_TYPES = Lists.newArrayList(ComponentMetadata.class, IvyModuleDescriptor.class);

    private static final String UNSUPPORTED_PARAMETER_TYPE_ERROR = "Unsupported parameter type for component selection rule: %s";

    public Collection<RuleAction<? super ComponentSelection>> getRules() {
        return rules;
    }

    public ComponentSelectionRules all(Action<? super ComponentSelection> selectionAction) {
        addRule(new NoInputsRuleAction<ComponentSelection>(selectionAction));
        return this;
    }

    public ComponentSelectionRules all(RuleAction<? super ComponentSelection> ruleAction) {
        addRule(validateInputTypes(ruleAction));
        return this;
    }

    public ComponentSelectionRules all(Closure<?> closure) {
        addRule(createRuleActionFromClosure(closure));
        return this;
    }

    private void addRule(RuleAction<? super ComponentSelection> ruleAction) {
        rules.add(ruleAction);
    }

    private RuleAction<? super ComponentSelection> createRuleActionFromClosure(Closure<?> closure) {
        try {
            return validateInputTypes(new ClosureBackedRuleAction<ComponentSelection>(ComponentSelection.class, closure));
        } catch (RuntimeException e) {
            throw new InvalidActionClosureException(String.format("The closure provided is not valid as a rule action for '%s'.", ComponentSelectionRules.class.getSimpleName()), closure, e);
        }
    }

    private RuleAction<? super ComponentSelection> validateInputTypes(RuleAction<? super ComponentSelection> ruleAction) {
        for (Class<?> inputType : ruleAction.getInputTypes()) {
            if (!VALID_INPUT_TYPES.contains(inputType)) {
                throw new InvalidUserCodeException(String.format(UNSUPPORTED_PARAMETER_TYPE_ERROR, inputType.getName()));
            }
        }
        return ruleAction;
    }
}