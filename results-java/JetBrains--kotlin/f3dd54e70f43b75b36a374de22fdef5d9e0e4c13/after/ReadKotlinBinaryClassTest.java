/*
 * Copyright 2010-2012 JetBrains s.r.o.
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

package org.jetbrains.jet.jvm.compiler;

import junit.framework.Assert;
import junit.framework.Test;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.JetTestCaseBuilder;
import org.jetbrains.jet.analyzer.AnalyzeExhaust;
import org.jetbrains.jet.lang.descriptors.NamespaceDescriptor;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.jet.test.TestCaseWithTmpdir;
import org.jetbrains.jet.test.util.NamespaceComparator;

import java.io.File;

import static org.jetbrains.jet.jvm.compiler.LoadDescriptorUtil.TEST_PACKAGE_FQNAME;
import static org.jetbrains.jet.jvm.compiler.LoadDescriptorUtil.compileKotlinToDirAndGetAnalyzeExhaust;
import static org.jetbrains.jet.test.util.NamespaceComparator.compareNamespaces;

/**
 * Compile Kotlin and then parse model from .class files.
 *
 * @author Stepan Koltsov
 */
@SuppressWarnings("JUnitTestCaseWithNoTests")
public final class ReadKotlinBinaryClassTest extends TestCaseWithTmpdir {

    @NotNull
    private final File testFile;
    @NotNull
    private final File txtFile;

    @SuppressWarnings("JUnitTestCaseWithNonTrivialConstructors")
    public ReadKotlinBinaryClassTest(@NotNull File testFile) {
        this.testFile = testFile;
        this.txtFile = new File(testFile.getPath().replaceFirst("\\.kt$", ".txt"));
        setName(testFile.getName());
    }

    @Override
    public void runTest() throws Exception {
        AnalyzeExhaust exhaust = compileKotlinToDirAndGetAnalyzeExhaust(testFile, tmpdir, getTestRootDisposable());

        NamespaceDescriptor namespaceFromSource = exhaust.getBindingContext().get(BindingContext.FQNAME_TO_NAMESPACE_DESCRIPTOR,
                                                                                  TEST_PACKAGE_FQNAME);
        assert namespaceFromSource != null;
        Assert.assertEquals("test", namespaceFromSource.getName().getName());
        NamespaceDescriptor namespaceFromClass = LoadDescriptorUtil.extractTestNamespaceFromBinaries(tmpdir, getTestRootDisposable());
        compareNamespaces(namespaceFromSource, namespaceFromClass, NamespaceComparator.DONT_INCLUDE_METHODS_OF_OBJECT, txtFile);
    }

    public static Test suite() {
        return JetTestCaseBuilder.suiteForDirectory(JetTestCaseBuilder.getTestDataPathBase(), "/readKotlinBinaryClass", true, new JetTestCaseBuilder.NamedTestFactory() {
            @NotNull
            @Override
            public Test createTest(@NotNull String dataPath, @NotNull String name, @NotNull File file) {
                return new ReadKotlinBinaryClassTest(file);
            }
        });
    }

}