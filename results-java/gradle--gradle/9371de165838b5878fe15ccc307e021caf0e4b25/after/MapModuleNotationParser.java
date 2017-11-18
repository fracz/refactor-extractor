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
package org.gradle.api.internal.artifacts.dsl.dependencies;

import org.gradle.api.GradleException;
import org.gradle.api.artifacts.ExternalDependency;
import org.gradle.util.ReflectionUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Hans Dockter
 */
class MapModuleNotationParser {
    public ExternalDependency createDependency(Class<? extends ExternalDependency> dependencyType, Map<String, Object> map) {
        Map<String, Object> args = new HashMap<String, Object>(map);
        String group = getAndRemove(args, "group");
        String name = getAndRemove(args, "name");
        String version = getAndRemove(args, "version");
        String configuration = getAndRemove(args, "configuration");
        try {
            ExternalDependency dependency = dependencyType.getConstructor(String.class, String.class, String.class, String.class).
                    newInstance(group, name, version, configuration);
            ModuleFactoryHelper.addExplicitArtifactsIfDefined(dependency, getAndRemove(args, "ext"), getAndRemove(args, "classifier"));
            ReflectionUtil.setFromMap(dependency, args);
            return dependency;
        } catch (InstantiationException e) {
            throw new GradleException(e);
        } catch (IllegalAccessException e) {
            throw new GradleException(e);
        } catch (InvocationTargetException e) {
            throw new GradleException(e);
        } catch (NoSuchMethodException e) {
            throw new GradleException(e);
        }
    }

    private String getAndRemove(Map<String, Object> args, String key) {
        Object value = args.get(key);
        args.remove(key);
        return value != null ? value.toString() : null;
    }
}