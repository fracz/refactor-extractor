/*
 * Copyright 2012 the original author or authors.
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

package org.gradle.api.publish.ivy;

import org.gradle.api.Action;
import org.gradle.api.Incubating;
import org.gradle.api.XmlProvider;

import java.io.File;

/**
 * The descriptor of any Ivy publication.
 *
 * Corresponds to the {@see <a href="http://ant.apache.org/ivy/history/latest-milestone/ivyfile.html">XML version of the Ivy Module Descriptor</a>}.
 *
 * The {@link #withXml(org.gradle.api.Action)} method can be used to modify the descriptor after it has been generated according the the publication data.
 *
 * @since 1.3
 */
@Incubating
public interface IvyModuleDescriptor {

    /**
     * Allow configuration of the descriptor file, after it has been generated.
     *
     * @param action The configuration action.
     * @see IvyPublication
     * @see XmlProvider
     */
    void withXml(Action<XmlProvider> action);

    /**
     * The generated descriptor file.
     *
     * This file will only exist <b>after</b> the publishing task that publishing the publication this descriptor is part of.
     *
     * Defaults to {@code $buildDir/publications/«publication name»/ivy.xml} with {@code “ivy-publish”} plugin.
     *
     * @return The generated descriptor file
     */
    File getFile();

    /**
     * Sets where the descriptor file should be generated.
     *
     * @param descriptorFile The new location to generate the descriptor to
     */
    void setFile(File descriptorFile);

}