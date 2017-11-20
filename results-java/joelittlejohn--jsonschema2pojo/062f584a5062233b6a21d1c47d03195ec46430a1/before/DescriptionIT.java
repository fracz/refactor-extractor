/**
 * Copyright © 2010-2011 Nokia
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

package com.googlecode.jsonschema2pojo.integration;

import static com.googlecode.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.Type;

public class DescriptionIT {

    private static JavaClass classWithDescription;

    @BeforeClass
    public static void generateClasses() throws ClassNotFoundException, IOException {

        File outputDirectory = generate("/schema/description/description.json", "com.example", true, false, false);
        File generatedJavaFile = new File(outputDirectory, "com/example/Description.java");

        compile(outputDirectory);

        JavaDocBuilder javaDocBuilder = new JavaDocBuilder();
        javaDocBuilder.addSource(generatedJavaFile);

        classWithDescription = javaDocBuilder.getClassByName("com.example.Description");
    }

    @Test
    public void descriptionAppearsInClassJavadoc() throws IOException {

        String javaDocComment = classWithDescription.getComment();

        assertThat(javaDocComment, containsString("A description for this type"));

    }

    @Test
    public void descriptionAppearsInFieldJavadoc() throws IOException {

        JavaField javaField = classWithDescription.getFieldByName("description");
        String javaDocComment = javaField.getComment();

        assertThat(javaDocComment, containsString("A description for this property"));

    }

    @Test
    public void descriptionAppearsInGetterJavadoc() throws IOException {

        JavaMethod javaMethod = classWithDescription.getMethodBySignature("getDescription", new Type[] {});
        String javaDocComment = javaMethod.getComment();

        assertThat(javaDocComment, containsString("A description for this property"));

    }

    @Test
    public void descriptionAppearsInSetterJavadoc() throws IOException {

        JavaMethod javaMethod = classWithDescription.getMethodBySignature("setDescription", new Type[] {new Type("java.lang.String")});
        String javaDocComment = javaMethod.getComment();

        assertThat(javaDocComment, containsString("A description for this property"));

    }

    @Test
    public void descriptionAppearsAfterTitleInJavadoc() throws IOException {

        JavaField javaField = classWithDescription.getFieldByName("descriptionAndTitle");
        String javaDocComment = javaField.getComment();

        assertThat(javaDocComment, containsString("A title for this property"));
        assertThat(javaDocComment.indexOf("A description for this property"), is(greaterThan(javaDocComment.indexOf("A title for this property"))));

    }
}