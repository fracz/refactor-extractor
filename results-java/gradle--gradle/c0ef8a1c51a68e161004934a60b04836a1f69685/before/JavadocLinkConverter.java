/*
 * Copyright 2010 the original author or authors.
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
package org.gradle.build.docs.dsl.docbook;

import org.gradle.build.docs.dsl.TypeNameResolver;
import org.gradle.build.docs.dsl.model.ClassMetaData;
import org.gradle.build.docs.model.ClassMetaDataRepository;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Arrays;

/**
 * Converts a javadoc link into docbook.
 */
public class JavadocLinkConverter {
    private final Document document;
    private final TypeNameResolver typeNameResolver;
    private final ClassMetaDataRepository<ClassMetaData> repository;

    public JavadocLinkConverter(Document document, TypeNameResolver typeNameResolver, ClassMetaDataRepository<ClassMetaData> repository) {
        this.document = document;
        this.typeNameResolver = typeNameResolver;
        this.repository = repository;
    }

    Iterable<? extends Node> resolve(String link, ClassMetaData classMetaData) {
        String className = typeNameResolver.resolve(link, classMetaData);

        if (className == null || className.contains("#")) {
            Element element = document.createElement("UNHANDLED-LINK");
            element.appendChild(document.createTextNode(link));
            return Arrays.asList(element);
        }

        if (repository.find(className) != null) {
            Element apilink = document.createElement("apilink");
            apilink.setAttribute("class", className);
            return Arrays.asList(apilink);
        }

        Element classNameElement = document.createElement("classname");
        classNameElement.appendChild(document.createTextNode(className));
        return Arrays.asList(classNameElement);
    }
}