/*
 * Copyright 2007-2008 the original author or authors.
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
package org.gradle.api.internal.project;

import org.gradle.api.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gradle.api.Project;
import org.gradle.util.GUtil;

import java.util.Properties;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author Hans Dockter
 */

public class PluginRegistry {
    private static Logger logger = LoggerFactory.getLogger(PluginRegistry.class);

    private Properties properties = new Properties();

    private Map plugins = new HashMap();

    public PluginRegistry() {
    }

    public PluginRegistry(File pluginProperties) {
        if (!pluginProperties.isFile()) {
            return;
        }
        logger.debug("Checking file= {}", pluginProperties);
        properties = new Properties();
        try {
            properties.load(new FileInputStream(pluginProperties));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    Plugin getPlugin(String pluginId) {
        if (!GUtil.isTrue(properties.get(pluginId))) {
        return null;
    }
        try {
            return getPlugin(Class.forName((String) properties.get(pluginId)));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    Plugin getPlugin(Class pluginClass) {
        if (plugins.get(pluginClass) == null) {
            try {
                plugins.put(pluginClass, pluginClass.newInstance());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return (Plugin) plugins.get(pluginClass);
    }

    void apply(Class pluginClass, Project project, PluginRegistry pluginRegistry, Map customValues) {
        if (project.getPluginApplyRegistry().get(pluginClass) == null) {
            getPlugin(pluginClass).apply(project, pluginRegistry, customValues);
            project.getPluginApplyRegistry().put(pluginClass, new Object());
        }
    }

}