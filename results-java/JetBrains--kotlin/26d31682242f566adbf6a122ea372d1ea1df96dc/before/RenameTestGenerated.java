/*
 * Copyright 2010-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.jet.plugin.refactoring.rename;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.File;
import java.util.regex.Pattern;
import org.jetbrains.jet.JetTestUtils;
import org.jetbrains.jet.test.InnerTestClasses;
import org.jetbrains.jet.test.TestMetadata;

import org.jetbrains.jet.plugin.refactoring.rename.AbstractRenameTest;

/** This class is generated by {@link org.jetbrains.jet.generators.tests.GenerateTests}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("idea/testData/refactoring/rename")
public class RenameTestGenerated extends AbstractRenameTest {
    public void testAllFilesPresentInRename() throws Exception {
        JetTestUtils.assertAllTestsPresentInSingleGeneratedClass(this.getClass(), "org.jetbrains.jet.generators.tests.GenerateTests",
                                                                 new File("idea/testData/refactoring/rename"),
                                                                 Pattern.compile("^(.+)\\.test$"));
    }

    @TestMetadata("renameJavaClass/renameJavaClass.test")
    public void testRenameJavaClass_RenameJavaClass() throws Exception {
        doTest("idea/testData/refactoring/rename/renameJavaClass/renameJavaClass.test");
    }

    @TestMetadata("renameKotlinBaseMethod/javaWrapperForBaseFunction.test")
    public void testRenameKotlinBaseMethod_JavaWrapperForBaseFunction() throws Exception {
        doTest("idea/testData/refactoring/rename/renameKotlinBaseMethod/javaWrapperForBaseFunction.test");
    }

    @TestMetadata("renameKotlinBaseMethod/kotlinBaseFunction.test")
    public void testRenameKotlinBaseMethod_KotlinBaseFunction() throws Exception {
        doTest("idea/testData/refactoring/rename/renameKotlinBaseMethod/kotlinBaseFunction.test");
    }

    @TestMetadata("renameKotlinClass/javaWrapperForKotlinClass.test")
    public void testRenameKotlinClass_JavaWrapperForKotlinClass() throws Exception {
        doTest("idea/testData/refactoring/rename/renameKotlinClass/javaWrapperForKotlinClass.test");
    }

    @TestMetadata("renameKotlinClass/kotlinClass.test")
    public void testRenameKotlinClass_KotlinClass() throws Exception {
        doTest("idea/testData/refactoring/rename/renameKotlinClass/kotlinClass.test");
    }

    @TestMetadata("renameKotlinClassConstructor/renameKotlinConstructor.test")
    public void testRenameKotlinClassConstructor_RenameKotlinConstructor() throws Exception {
        doTest("idea/testData/refactoring/rename/renameKotlinClassConstructor/renameKotlinConstructor.test");
    }

    @TestMetadata("renameKotlinMethod/javaWrapperForKotlinMethod.test")
    public void testRenameKotlinMethod_JavaWrapperForKotlinMethod() throws Exception {
        doTest("idea/testData/refactoring/rename/renameKotlinMethod/javaWrapperForKotlinMethod.test");
    }

    @TestMetadata("renameKotlinMethod/renameKotlinMethod.test")
    public void testRenameKotlinMethod_RenameKotlinMethod() throws Exception {
        doTest("idea/testData/refactoring/rename/renameKotlinMethod/renameKotlinMethod.test");
    }

    @TestMetadata("renameKotlinPackageClass/javaWrapperForKotlinPackageClass.test")
    public void testRenameKotlinPackageClass_JavaWrapperForKotlinPackageClass() throws Exception {
        doTest("idea/testData/refactoring/rename/renameKotlinPackageClass/javaWrapperForKotlinPackageClass.test");
    }

    @TestMetadata("renameKotlinPackageFunctionFromJava/renameKotlinPackageFunctionFromJava.test")
    public void testRenameKotlinPackageFunctionFromJava_RenameKotlinPackageFunctionFromJava() throws Exception {
        doTest("idea/testData/refactoring/rename/renameKotlinPackageFunctionFromJava/renameKotlinPackageFunctionFromJava.test");
    }

}