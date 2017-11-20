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

package com.googlecode.jsonschema2pojo.integration.ref;

import static com.googlecode.jsonschema2pojo.integration.util.CodeGenerationHelper.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class CyclicalRefIT {

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void cyclicalRefsAreReadSuccessfully() throws ClassNotFoundException, NoSuchMethodException {

        ClassLoader resultsClassLoader = generateAndCompile("/schema/ref/subdirectory1/refToSubdirectory2.json", "com.example");

        Class class1 = resultsClassLoader.loadClass("com.example.RefToSubdirectory2");
        Class class2 = resultsClassLoader.loadClass("com.example.RefToSubdirectory1");

        Class refToClass2 = class1.getMethod("getRefToOther").getReturnType();
        Class refToClass1 = class2.getMethod("getRefToOther").getReturnType();

        assertThat(refToClass2, is(equalTo(class2)));
        assertThat(refToClass1, is(equalTo(class1)));

    }

}